/*
 * ControlFlowBuilderImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.controlflow.impl;

import com.github.toolarium.java.decompiler.controlflow.IControlFlowBuilder;
import com.github.toolarium.java.decompiler.model.bytecode.BytecodeModelFactory;
import com.github.toolarium.java.decompiler.model.bytecode.IInstruction;
import com.github.toolarium.java.decompiler.model.controlflow.ControlFlowModelFactory;
import com.github.toolarium.java.decompiler.model.controlflow.EdgeType;
import com.github.toolarium.java.decompiler.model.controlflow.IBasicBlock;
import com.github.toolarium.java.decompiler.model.controlflow.IControlFlowGraph;
import com.github.toolarium.java.decompiler.model.controlflow.IEdge;
import com.github.toolarium.java.decompiler.model.controlflow.impl.BasicBlockImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.util.Printer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link IControlFlowBuilder} that builds a control flow graph from ASM bytecode.
 *
 * @author patrick
 */
public class ControlFlowBuilderImpl implements IControlFlowBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ControlFlowBuilderImpl.class);


    /**
     * Constructor for ControlFlowBuilderImpl.
     */
    public ControlFlowBuilderImpl() {
        // default constructor
    }


    /**
     * Build a control flow graph from an ASM method node.
     *
     * @param methodNode the ASM method node containing bytecode instructions
     * @return the control flow graph representing the method's control flow
     */
    @Override
    public IControlFlowGraph buildCfg(MethodNode methodNode) {
        InsnList insnList = methodNode.instructions;
        if (insnList == null || insnList.size() == 0) {
            BasicBlockImpl emptyBlock = ControlFlowModelFactory.getInstance().getBasicBlock(0);
            List<IBasicBlock> blocks = new ArrayList<>();
            blocks.add(emptyBlock);
            return ControlFlowModelFactory.getInstance().getControlFlowGraph(
                    emptyBlock, blocks, new ArrayList<>());
        }

        // Pre-build instruction-to-index map for O(1) lookups (avoids O(n) InsnList.indexOf)
        Map<AbstractInsnNode, Integer> insnIndex = buildInsnIndexMap(insnList);

        // Step 1: identify block leaders (instruction indices that start a new block)
        Set<Integer> leaders = findLeaders(methodNode, insnIndex);

        // Step 2: create basic blocks and assign instructions (TreeMap keeps leaders sorted)
        TreeMap<Integer, BasicBlockImpl> blockByIndex = createBlocks(insnList, leaders);

        // Step 3: create edges
        List<IEdge> edges = createEdges(methodNode, insnList, blockByIndex, insnIndex);

        // Step 4: add exception handler edges
        addExceptionEdges(methodNode, insnList, blockByIndex, insnIndex, edges);

        // Build final graph
        List<IBasicBlock> blockList = new ArrayList<>(blockByIndex.values());
        BasicBlockImpl entryBlock = blockByIndex.get(0);
        if (entryBlock == null && !blockList.isEmpty()) {
            entryBlock = (BasicBlockImpl) blockList.get(0);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Built CFG with " + blockList.size() + " blocks and " + edges.size() + " edges.");
        }

        return ControlFlowModelFactory.getInstance().getControlFlowGraph(entryBlock, blockList, edges);
    }


    /**
     * Build a map from each instruction node to its index in the list. O(n) construction,
     * O(1) lookup, replacing repeated O(n) InsnList.indexOf() calls.
     *
     * @param insnList the instruction list
     * @return map from instruction node to index
     */
    private Map<AbstractInsnNode, Integer> buildInsnIndexMap(InsnList insnList) {
        Map<AbstractInsnNode, Integer> map = new HashMap<>(insnList.size() * 2);
        for (int i = 0; i < insnList.size(); i++) {
            map.put(insnList.get(i), i);
        }
        return map;
    }


    /**
     * Find the set of instruction indices that are block leaders.
     * A leader is: index 0, target of a jump, instruction after a jump/return, exception handler start.
     *
     * @param methodNode the method node
     * @param insnIndex  pre-built instruction-to-index map for O(1) lookups
     * @return set of leader indices
     */
    private Set<Integer> findLeaders(MethodNode methodNode, Map<AbstractInsnNode, Integer> insnIndex) {
        InsnList insnList = methodNode.instructions;
        Set<Integer> leaders = new HashSet<>();
        leaders.add(0);

        for (int i = 0; i < insnList.size(); i++) {
            AbstractInsnNode insn = insnList.get(i);
            int type = insn.getType();

            if (insn instanceof JumpInsnNode) {
                JumpInsnNode jump = (JumpInsnNode) insn;
                Integer targetIndex = insnIndex.get(jump.label);
                if (targetIndex != null && targetIndex >= 0) {
                    leaders.add(targetIndex);
                }
                if (i + 1 < insnList.size()) {
                    leaders.add(i + 1);
                }
            } else if (insn instanceof TableSwitchInsnNode) {
                TableSwitchInsnNode sw = (TableSwitchInsnNode) insn;
                addSwitchLeaders(leaders, sw.dflt, sw.labels, i, insnList.size(), insnIndex);
            } else if (insn instanceof LookupSwitchInsnNode) {
                LookupSwitchInsnNode sw = (LookupSwitchInsnNode) insn;
                addSwitchLeaders(leaders, sw.dflt, sw.labels, i, insnList.size(), insnIndex);
            } else if (isReturnOrThrow(insn.getOpcode()) || type == AbstractInsnNode.TABLESWITCH_INSN
                    || type == AbstractInsnNode.LOOKUPSWITCH_INSN) {
                if (i + 1 < insnList.size()) {
                    leaders.add(i + 1);
                }
            }
        }

        // Exception handler entries are leaders
        if (methodNode.tryCatchBlocks != null) {
            for (TryCatchBlockNode tcb : methodNode.tryCatchBlocks) {
                Integer handlerIndex = insnIndex.get(tcb.handler);
                if (handlerIndex != null && handlerIndex >= 0) {
                    leaders.add(handlerIndex);
                }
            }
        }

        return leaders;
    }


    /**
     * Add switch target labels as leaders.
     *
     * @param leaders     the leader set
     * @param dflt        the default label
     * @param labels      the case labels
     * @param switchIndex the switch instruction index
     * @param insnCount   the total instruction count
     * @param insnIndex   pre-built instruction-to-index map
     */
    private void addSwitchLeaders(Set<Integer> leaders, LabelNode dflt,
                                  List<LabelNode> labels, int switchIndex,
                                  int insnCount, Map<AbstractInsnNode, Integer> insnIndex) {
        if (dflt != null) {
            Integer idx = insnIndex.get(dflt);
            if (idx != null && idx >= 0) {
                leaders.add(idx);
            }
        }
        if (labels != null) {
            for (LabelNode label : labels) {
                Integer idx = insnIndex.get(label);
                if (idx != null && idx >= 0) {
                    leaders.add(idx);
                }
            }
        }
        if (switchIndex + 1 < insnCount) {
            leaders.add(switchIndex + 1);
        }
    }


    /**
     * Create basic blocks from the instruction list, splitting at leaders.
     *
     * @param insnList the instruction list
     * @param leaders  the set of leader indices
     * @return a sorted map from instruction index to the block that starts there
     */
    private TreeMap<Integer, BasicBlockImpl> createBlocks(InsnList insnList, Set<Integer> leaders) {
        TreeMap<Integer, BasicBlockImpl> blockByIndex = new TreeMap<>();
        ControlFlowModelFactory cfgFactory = ControlFlowModelFactory.getInstance();
        BytecodeModelFactory bcFactory = BytecodeModelFactory.getInstance();
        int blockId = 0;
        BasicBlockImpl currentBlock = null;
        int offset = 0;

        for (int i = 0; i < insnList.size(); i++) {
            AbstractInsnNode insn = insnList.get(i);

            if (leaders.contains(i)) {
                currentBlock = cfgFactory.getBasicBlock(blockId++);
                blockByIndex.put(i, currentBlock);
            }

            // Skip pseudo-instructions (labels, frames, line numbers)
            if (isRealInstruction(insn)) {
                String mnemonic = getMnemonic(insn.getOpcode());
                IInstruction instruction = bcFactory.getInstruction(
                        offset, insn.getOpcode(), mnemonic, new int[0]);
                if (currentBlock != null) {
                    currentBlock.addInstruction(instruction);
                }
                offset++;
            }
        }

        return blockByIndex;
    }


    /**
     * Create edges between basic blocks based on control flow.
     *
     * @param methodNode   the method node
     * @param insnList     the instruction list
     * @param blockByIndex sorted map from instruction index to block
     * @param insnIndex    pre-built instruction-to-index map
     * @return list of edges
     */
    private List<IEdge> createEdges(MethodNode methodNode, InsnList insnList,
                                    TreeMap<Integer, BasicBlockImpl> blockByIndex,
                                    Map<AbstractInsnNode, Integer> insnIndex) {
        List<IEdge> edges = new ArrayList<>();
        ControlFlowModelFactory cfgFactory = ControlFlowModelFactory.getInstance();

        for (Map.Entry<Integer, BasicBlockImpl> entry : blockByIndex.entrySet()) {
            int leaderIdx = entry.getKey();
            BasicBlockImpl block = entry.getValue();
            int lastInsnIdx = findLastRealInsnIndex(insnList, leaderIdx, blockByIndex);
            if (lastInsnIdx < 0) {
                continue;
            }

            AbstractInsnNode lastInsn = insnList.get(lastInsnIdx);

            if (lastInsn instanceof JumpInsnNode) {
                JumpInsnNode jump = (JumpInsnNode) lastInsn;
                Integer targetIdx = insnIndex.get(jump.label);
                if (targetIdx != null) {
                    BasicBlockImpl targetBlock = findBlockForIndex(targetIdx, blockByIndex);
                    if (targetBlock != null) {
                        IEdge edge = cfgFactory.getEdge(block, targetBlock, EdgeType.JUMP, null);
                        block.addOutEdge(edge);
                        edges.add(edge);
                    }
                }

                // Conditional jumps also fall through
                if (lastInsn.getOpcode() != Opcodes.GOTO && lastInsn.getOpcode() != Opcodes.JSR) {
                    int nextIdx = lastInsnIdx + 1;
                    BasicBlockImpl fallBlock = findBlockForIndex(nextIdx, blockByIndex);
                    if (fallBlock != null) {
                        IEdge fallEdge = cfgFactory.getEdge(block, fallBlock, EdgeType.FALL_THROUGH, null);
                        block.addOutEdge(fallEdge);
                        edges.add(fallEdge);
                    }
                }
            } else if (lastInsn instanceof TableSwitchInsnNode) {
                addTableSwitchEdges(block, (TableSwitchInsnNode) lastInsn,
                        blockByIndex, insnIndex, edges);
            } else if (lastInsn instanceof LookupSwitchInsnNode) {
                addLookupSwitchEdges(block, (LookupSwitchInsnNode) lastInsn,
                        blockByIndex, insnIndex, edges);
            } else if (!isReturnOrThrow(lastInsn.getOpcode())) {
                // Fall through to next block
                int nextIdx = lastInsnIdx + 1;
                BasicBlockImpl fallBlock = findBlockForIndex(nextIdx, blockByIndex);
                if (fallBlock != null) {
                    IEdge fallEdge = cfgFactory.getEdge(block, fallBlock, EdgeType.FALL_THROUGH, null);
                    block.addOutEdge(fallEdge);
                    edges.add(fallEdge);
                }
            }
        }

        return edges;
    }


    /**
     * Add edges for a tableswitch instruction.
     *
     * @param block        the source block
     * @param sw           the tableswitch node
     * @param blockByIndex sorted block index map
     * @param insnIndex    pre-built instruction-to-index map
     * @param edges        edge list to add to
     */
    private void addTableSwitchEdges(BasicBlockImpl block, TableSwitchInsnNode sw,
                                     TreeMap<Integer, BasicBlockImpl> blockByIndex,
                                     Map<AbstractInsnNode, Integer> insnIndex, List<IEdge> edges) {
        ControlFlowModelFactory cfgFactory = ControlFlowModelFactory.getInstance();

        // Default edge
        Integer dfltIdx = insnIndex.get(sw.dflt);
        if (dfltIdx != null) {
            BasicBlockImpl dfltBlock = findBlockForIndex(dfltIdx, blockByIndex);
            if (dfltBlock != null) {
                IEdge edge = cfgFactory.getEdge(block, dfltBlock, EdgeType.SWITCH_DEFAULT, null);
                block.addOutEdge(edge);
                edges.add(edge);
            }
        }

        // Case edges
        for (int i = 0; i < sw.labels.size(); i++) {
            Integer caseIdx = insnIndex.get(sw.labels.get(i));
            if (caseIdx != null) {
                BasicBlockImpl caseBlock = findBlockForIndex(caseIdx, blockByIndex);
                if (caseBlock != null) {
                    String caseLabel = String.valueOf(sw.min + i);
                    IEdge edge = cfgFactory.getEdge(block, caseBlock, EdgeType.SWITCH_CASE, caseLabel);
                    block.addOutEdge(edge);
                    edges.add(edge);
                }
            }
        }
    }


    /**
     * Add edges for a lookupswitch instruction.
     *
     * @param block        the source block
     * @param sw           the lookupswitch node
     * @param blockByIndex sorted block index map
     * @param insnIndex    pre-built instruction-to-index map
     * @param edges        edge list to add to
     */
    private void addLookupSwitchEdges(BasicBlockImpl block, LookupSwitchInsnNode sw,
                                      TreeMap<Integer, BasicBlockImpl> blockByIndex,
                                      Map<AbstractInsnNode, Integer> insnIndex, List<IEdge> edges) {
        ControlFlowModelFactory cfgFactory = ControlFlowModelFactory.getInstance();

        // Default edge
        Integer dfltIdx = insnIndex.get(sw.dflt);
        if (dfltIdx != null) {
            BasicBlockImpl dfltBlock = findBlockForIndex(dfltIdx, blockByIndex);
            if (dfltBlock != null) {
                IEdge edge = cfgFactory.getEdge(block, dfltBlock, EdgeType.SWITCH_DEFAULT, null);
                block.addOutEdge(edge);
                edges.add(edge);
            }
        }

        // Case edges
        for (int i = 0; i < sw.labels.size(); i++) {
            Integer caseIdx = insnIndex.get(sw.labels.get(i));
            if (caseIdx != null) {
                BasicBlockImpl caseBlock = findBlockForIndex(caseIdx, blockByIndex);
                if (caseBlock != null) {
                    String caseLabel = String.valueOf(sw.keys.get(i));
                    IEdge edge = cfgFactory.getEdge(block, caseBlock, EdgeType.SWITCH_CASE, caseLabel);
                    block.addOutEdge(edge);
                    edges.add(edge);
                }
            }
        }
    }


    /**
     * Add exception handler edges.
     *
     * @param methodNode   the method node
     * @param insnList     the instruction list
     * @param blockByIndex sorted block index map
     * @param insnIndex    pre-built instruction-to-index map
     * @param edges        edge list to add to
     */
    private void addExceptionEdges(MethodNode methodNode, InsnList insnList,
                                   TreeMap<Integer, BasicBlockImpl> blockByIndex,
                                   Map<AbstractInsnNode, Integer> insnIndex, List<IEdge> edges) {
        if (methodNode.tryCatchBlocks == null) {
            return;
        }

        ControlFlowModelFactory cfgFactory = ControlFlowModelFactory.getInstance();

        for (TryCatchBlockNode tcb : methodNode.tryCatchBlocks) {
            Integer handlerIdx = insnIndex.get(tcb.handler);
            if (handlerIdx == null) {
                continue;
            }
            BasicBlockImpl handlerBlock = findBlockForIndex(handlerIdx, blockByIndex);
            if (handlerBlock == null) {
                continue;
            }
            handlerBlock.setExceptionHandler(tcb.type);

            Integer startIdx = insnIndex.get(tcb.start);
            Integer endIdx = insnIndex.get(tcb.end);
            if (startIdx == null || endIdx == null) {
                continue;
            }

            // Use TreeMap.subMap for O(log n) range query instead of scanning all leaders
            for (Map.Entry<Integer, BasicBlockImpl> rangeEntry : blockByIndex.subMap(startIdx, endIdx).entrySet()) {
                BasicBlockImpl block = rangeEntry.getValue();
                if (block != null && block != handlerBlock) {
                    String exLabel = tcb.type;
                    IEdge edge = cfgFactory.getEdge(block, handlerBlock, EdgeType.EXCEPTION, exLabel);
                    block.addOutEdge(edge);
                    edges.add(edge);
                }
            }
        }
    }


    /**
     * Find the last real instruction index within a block's range.
     * Uses TreeMap.higherKey() for O(log n) next-leader lookup.
     *
     * @param insnList     the instruction list
     * @param leaderIdx    the block's leader index
     * @param blockByIndex sorted block index map
     * @return the last real instruction index, or -1 if none
     */
    private int findLastRealInsnIndex(InsnList insnList, int leaderIdx,
                                      TreeMap<Integer, BasicBlockImpl> blockByIndex) {
        Integer nextLeaderKey = blockByIndex.higherKey(leaderIdx);
        int nextLeaderIdx = insnList.size();
        if (nextLeaderKey != null) {
            nextLeaderIdx = nextLeaderKey;
        }

        for (int i = nextLeaderIdx - 1; i >= leaderIdx; i--) {
            if (isRealInstruction(insnList.get(i))) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Find the block that contains the given instruction index.
     * Uses TreeMap.floorEntry() for O(log n) lookup instead of linear scan.
     *
     * @param instrIdx     the instruction index
     * @param blockByIndex sorted block index map
     * @return the block, or null
     */
    private BasicBlockImpl findBlockForIndex(int instrIdx, TreeMap<Integer, BasicBlockImpl> blockByIndex) {
        Map.Entry<Integer, BasicBlockImpl> floor = blockByIndex.floorEntry(instrIdx);
        if (floor != null) {
            return floor.getValue();
        }
        return null;
    }


    /**
     * Check whether an instruction is a real bytecode instruction (not a pseudo-instruction).
     *
     * @param insn the instruction node
     * @return true if it is a real instruction
     */
    private boolean isRealInstruction(AbstractInsnNode insn) {
        int type = insn.getType();
        return type != AbstractInsnNode.LABEL
                && type != AbstractInsnNode.FRAME
                && type != AbstractInsnNode.LINE;
    }


    /**
     * Check whether an opcode is a return or throw instruction.
     *
     * @param opcode the opcode
     * @return true if it is a return or athrow
     */
    private boolean isReturnOrThrow(int opcode) {
        return opcode == Opcodes.RETURN
                || opcode == Opcodes.IRETURN
                || opcode == Opcodes.LRETURN
                || opcode == Opcodes.FRETURN
                || opcode == Opcodes.DRETURN
                || opcode == Opcodes.ARETURN
                || opcode == Opcodes.ATHROW;
    }


    /**
     * Get the mnemonic for an opcode.
     *
     * @param opcode the opcode
     * @return the mnemonic string
     */
    private String getMnemonic(int opcode) {
        if (opcode >= 0 && opcode < Printer.OPCODES.length) {
            return Printer.OPCODES[opcode].toLowerCase();
        }
        return "unknown_" + opcode;
    }
}
