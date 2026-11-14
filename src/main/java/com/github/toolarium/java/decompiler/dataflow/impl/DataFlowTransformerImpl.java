/*
 * DataFlowTransformerImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.dataflow.impl;

import com.github.toolarium.java.decompiler.dataflow.IDataFlowTransformer;
import com.github.toolarium.java.decompiler.model.controlflow.IBasicBlock;
import com.github.toolarium.java.decompiler.model.controlflow.IControlFlowGraph;
import com.github.toolarium.java.decompiler.model.dataflow.DataFlowModelFactory;
import com.github.toolarium.java.decompiler.model.dataflow.IDataFlowBlock;
import com.github.toolarium.java.decompiler.model.dataflow.IDataFlowVariable;
import com.github.toolarium.java.decompiler.model.dataflow.IMergeFunction;
import com.github.toolarium.java.decompiler.model.dataflow.impl.DataFlowBlockImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of data flow transformation using the standard algorithm:
 * compute dominance frontiers, insert merge functions, rename variables.
 *
 * @author patrick
 */
public class DataFlowTransformerImpl implements IDataFlowTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(DataFlowTransformerImpl.class);
    private static final int MAX_LOCALS = 1024;


    /**
     * Constructor for DataFlowTransformerImpl.
     */
    public DataFlowTransformerImpl() {
        // default constructor
    }


    /**
     * Transform a control flow graph into data flow form by computing dominance frontiers,
     * inserting merge functions, and renaming variables.
     *
     * @param cfg       the control flow graph to transform
     * @param maxLocals the maximum number of local variables in the method
     * @return the list of data flow blocks
     */
    @Override
    public List<IDataFlowBlock> transformToDataFlow(IControlFlowGraph cfg, int maxLocals) {
        int effectiveMaxLocals = maxLocals;
        if (effectiveMaxLocals > MAX_LOCALS) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Capping maxLocals from " + effectiveMaxLocals + " to " + MAX_LOCALS);
            }
            effectiveMaxLocals = MAX_LOCALS;
        }

        List<IBasicBlock> blocks = cfg.getBlocks();
        DataFlowModelFactory dataFlowFactory = DataFlowModelFactory.getInstance();

        // Create data flow block wrappers
        Map<Integer, DataFlowBlockImpl> blockMap = new HashMap<>();
        for (IBasicBlock block : blocks) {
            blockMap.put(block.getId(), dataFlowFactory.getDataFlowBlock(block));
        }

        // Step 1: Compute immediate dominators
        Map<Integer, Integer> idom = computeImmediateDominators(cfg);

        // Step 2: Compute dominance frontiers
        Map<Integer, Set<Integer>> domFrontiers = computeDominanceFrontiers(cfg, idom);

        // Step 3: Insert merge functions
        insertMergeFunctions(cfg, blockMap, domFrontiers, effectiveMaxLocals);

        // Step 4: Rename variables
        renameVariables(cfg, blockMap, idom, effectiveMaxLocals);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Data flow transformation completed for " + blocks.size() + " blocks, "
                    + effectiveMaxLocals + " locals.");
        }

        List<IDataFlowBlock> result = new ArrayList<>();
        for (IBasicBlock block : blocks) {
            result.add(blockMap.get(block.getId()));
        }
        return result;
    }


    /**
     * Compute immediate dominators using the Cooper-Harvey-Kennedy algorithm.
     * This runs in nearly-linear time by working on reverse postorder numbering.
     *
     * @param cfg the control flow graph
     * @return map from block id to immediate dominator block id
     */
    private Map<Integer, Integer> computeImmediateDominators(IControlFlowGraph cfg) {
        List<IBasicBlock> blocks = cfg.getBlocks();
        int entryId = cfg.getEntryBlock().getId();

        // Build block-id to block map
        Map<Integer, IBasicBlock> idToBlock = new HashMap<>();
        for (IBasicBlock block : blocks) {
            idToBlock.put(block.getId(), block);
        }

        // Compute reverse postorder
        List<Integer> rpo = computeReversePostorder(cfg);
        Map<Integer, Integer> rpoNumber = new HashMap<>();
        for (int i = 0; i < rpo.size(); i++) {
            rpoNumber.put(rpo.get(i), i);
        }

        // Initialize idom: entry dominates itself, all others undefined (-1)
        Map<Integer, Integer> idom = new HashMap<>();
        idom.put(entryId, entryId);

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int blockId : rpo) {
                if (blockId == entryId) {
                    continue;
                }

                IBasicBlock block = idToBlock.get(blockId);
                if (block == null) {
                    continue;
                }

                // Find first processed predecessor
                Integer newIdom = null;
                for (IBasicBlock pred : block.getPredecessors()) {
                    if (idom.containsKey(pred.getId())) {
                        newIdom = pred.getId();
                        break;
                    }
                }
                if (newIdom == null) {
                    continue;
                }

                // Intersect with remaining predecessors
                for (IBasicBlock pred : block.getPredecessors()) {
                    int predId = pred.getId();
                    if (predId == newIdom) {
                        continue;
                    }
                    if (idom.containsKey(predId)) {
                        newIdom = intersect(predId, newIdom, idom, rpoNumber);
                    }
                }

                Integer oldIdom = idom.get(blockId);
                if (oldIdom == null || !oldIdom.equals(newIdom)) {
                    idom.put(blockId, newIdom);
                    changed = true;
                }
            }
        }

        return idom;
    }


    /**
     * Intersect two dominator paths using RPO numbers (Cooper-Harvey-Kennedy).
     *
     * @param finger1   first block id
     * @param finger2   second block id
     * @param idom      current immediate dominator map
     * @param rpoNumber RPO numbering
     * @return the common dominator block id
     */
    private int intersect(int finger1, int finger2, Map<Integer, Integer> idom,
                          Map<Integer, Integer> rpoNumber) {
        int f1 = finger1;
        int f2 = finger2;
        while (f1 != f2) {
            while (rpoNumber.getOrDefault(f1, Integer.MAX_VALUE) > rpoNumber.getOrDefault(f2, Integer.MAX_VALUE)) {
                Integer next = idom.get(f1);
                if (next == null || next == f1) {
                    return f2;
                }
                f1 = next;
            }
            while (rpoNumber.getOrDefault(f2, Integer.MAX_VALUE) > rpoNumber.getOrDefault(f1, Integer.MAX_VALUE)) {
                Integer next = idom.get(f2);
                if (next == null || next == f2) {
                    return f1;
                }
                f2 = next;
            }
        }
        return f1;
    }


    /**
     * Compute reverse postorder of the CFG via iterative DFS.
     *
     * @param cfg the control flow graph
     * @return block ids in reverse postorder
     */
    private List<Integer> computeReversePostorder(IControlFlowGraph cfg) {
        final List<Integer> postorder = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        java.util.Deque<int[]> stack = new java.util.ArrayDeque<>();
        IBasicBlock entry = cfg.getEntryBlock();
        stack.push(new int[]{entry.getId(), 0});
        visited.add(entry.getId());

        // Pre-compute successor lists per block id to avoid rebuilding on every peek
        Map<Integer, List<IBasicBlock>> successorMap = new HashMap<>();
        for (IBasicBlock block : cfg.getBlocks()) {
            List<IBasicBlock> succs = new ArrayList<>();
            for (com.github.toolarium.java.decompiler.model.controlflow.IEdge edge : block.getOutEdges()) {
                succs.add(edge.getTarget());
            }
            successorMap.put(block.getId(), succs);
        }

        while (!stack.isEmpty()) {
            int[] frame = stack.peek();
            int blockId = frame[0];
            List<IBasicBlock> successors = successorMap.getOrDefault(blockId, java.util.Collections.emptyList());

            if (frame[1] < successors.size()) {
                IBasicBlock succ = successors.get(frame[1]);
                frame[1]++;
                if (!visited.contains(succ.getId())) {
                    visited.add(succ.getId());
                    stack.push(new int[]{succ.getId(), 0});
                }
            } else {
                stack.pop();
                postorder.add(blockId);
            }
        }

        java.util.Collections.reverse(postorder);
        return postorder;
    }


    /**
     * Compute dominance frontiers from immediate dominators.
     *
     * @param cfg  the control flow graph
     * @param idom the immediate dominator map
     * @return map from block id to set of dominance frontier block ids
     */
    private Map<Integer, Set<Integer>> computeDominanceFrontiers(IControlFlowGraph cfg,
                                                                  Map<Integer, Integer> idom) {
        Map<Integer, Set<Integer>> frontiers = new HashMap<>();
        for (IBasicBlock block : cfg.getBlocks()) {
            frontiers.put(block.getId(), new HashSet<>());
        }

        for (IBasicBlock block : cfg.getBlocks()) {
            if (block.getPredecessors().size() >= 2) {
                for (IBasicBlock pred : block.getPredecessors()) {
                    int runner = pred.getId();
                    Integer blockIdom = idom.get(block.getId());
                    if (blockIdom == null) {
                        continue;
                    }
                    while (runner != blockIdom) {
                        frontiers.get(runner).add(block.getId());
                        Integer nextRunner = idom.get(runner);
                        if (nextRunner == null || nextRunner == runner) {
                            break;
                        }
                        runner = nextRunner;
                    }
                }
            }
        }

        return frontiers;
    }


    /**
     * Insert merge functions at dominance frontier join points.
     *
     * @param cfg          the control flow graph
     * @param blockMap     map from block id to data flow block
     * @param domFrontiers the dominance frontiers
     * @param maxLocals    the number of local variable slots
     */
    private void insertMergeFunctions(IControlFlowGraph cfg,
                                      Map<Integer, DataFlowBlockImpl> blockMap,
                                      Map<Integer, Set<Integer>> domFrontiers, int maxLocals) {
        DataFlowModelFactory dataFlowFactory = DataFlowModelFactory.getInstance();

        // Pre-compute the set of non-empty block ids once (shared across all variables).
        // In a full implementation, per-variable def-site analysis would limit this further.
        Set<Integer> defBlocks = new HashSet<>();
        for (IBasicBlock block : cfg.getBlocks()) {
            if (!block.getInstructions().isEmpty()) {
                defBlocks.add(block.getId());
            }
        }

        // Skip merge insertion entirely when there are no definition sites
        if (defBlocks.isEmpty()) {
            return;
        }

        for (int varIdx = 0; varIdx < maxLocals; varIdx++) {
            Queue<Integer> worklist = new LinkedList<>(defBlocks);
            Set<Integer> mergeInserted = new HashSet<>();

            while (!worklist.isEmpty()) {
                int blockId = worklist.poll();
                Set<Integer> frontier = domFrontiers.get(blockId);
                if (frontier == null) {
                    continue;
                }

                for (int dfBlockId : frontier) {
                    if (mergeInserted.contains(dfBlockId)) {
                        continue;
                    }
                    mergeInserted.add(dfBlockId);

                    DataFlowBlockImpl dataFlowBlock = blockMap.get(dfBlockId);
                    if (dataFlowBlock != null) {
                        IDataFlowVariable target = dataFlowFactory.getDataFlowVariable(varIdx, 0, null);
                        IMergeFunction merge = dataFlowFactory.getMergeFunction(target, new ArrayList<>(), varIdx);
                        dataFlowBlock.addMergeFunction(merge);
                    }

                    if (!defBlocks.contains(dfBlockId)) {
                        worklist.add(dfBlockId);
                    }
                }
            }
        }
    }


    /**
     * Rename variables to achieve single assignment property.
     * Pre-computes dominator tree children for O(1) child lookup instead of O(B) scan.
     *
     * @param cfg      the control flow graph
     * @param blockMap map from block id to data flow block
     * @param idom     the immediate dominator map
     * @param maxLocals the number of local variable slots
     */
    private void renameVariables(IControlFlowGraph cfg,
                                 Map<Integer, DataFlowBlockImpl> blockMap,
                                 Map<Integer, Integer> idom, int maxLocals) {
        DataFlowModelFactory dataFlowFactory = DataFlowModelFactory.getInstance();
        int[] versionCounters = new int[maxLocals];
        Arrays.fill(versionCounters, 0);

        // Assign initial versions (version 0) to entry block definitions
        DataFlowBlockImpl entryBlock = blockMap.get(cfg.getEntryBlock().getId());
        if (entryBlock != null) {
            for (int i = 0; i < maxLocals; i++) {
                IDataFlowVariable initialVar = dataFlowFactory.getDataFlowVariable(i, 0, null);
                entryBlock.addDefinition(initialVar);
                versionCounters[i] = 1;
            }
        }

        // Pre-compute dominator tree children for O(1) lookup
        Map<Integer, List<Integer>> domChildren = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : idom.entrySet()) {
            int childId = entry.getKey();
            int parentId = entry.getValue();
            if (childId != parentId) {
                domChildren.computeIfAbsent(parentId, k -> new ArrayList<>()).add(childId);
            }
        }

        // Rename in dominator tree order (BFS from entry)
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(cfg.getEntryBlock().getId());

        while (!queue.isEmpty()) {
            int blockId = queue.poll();
            if (visited.contains(blockId)) {
                continue;
            }
            visited.add(blockId);

            DataFlowBlockImpl dataFlowBlock = blockMap.get(blockId);
            if (dataFlowBlock == null) {
                continue;
            }

            // Rename merge function targets
            for (IMergeFunction merge : dataFlowBlock.getMergeFunctions()) {
                int varIdx = merge.getVariableIndex();
                if (varIdx < maxLocals) {
                    int newVersion = versionCounters[varIdx]++;
                    IDataFlowVariable renamedTarget = dataFlowFactory.getDataFlowVariable(varIdx, newVersion, null);
                    dataFlowBlock.addDefinition(renamedTarget);
                }
            }

            // Add children in dominator tree to queue (O(1) lookup via pre-computed map)
            List<Integer> children = domChildren.get(blockId);
            if (children != null) {
                queue.addAll(children);
            }
        }
    }
}
