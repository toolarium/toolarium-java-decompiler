/*
 * BytecodeTranslatorImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.translator.impl;

import com.github.toolarium.java.decompiler.model.ast.expression.BinaryOperator;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IBinaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.UnaryOperator;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchCase;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import com.github.toolarium.java.decompiler.translator.IBytecodeTranslator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link IBytecodeTranslator} using JVM operand stack simulation.
 * Walks bytecode instructions, maintains a stack of {@link IExpression}, and produces
 * AST statements for assignments, method calls, returns, etc.
 *
 * @author patrick
 */
public class BytecodeTranslatorImpl implements IBytecodeTranslator {
    private static final Logger LOG = LoggerFactory.getLogger(BytecodeTranslatorImpl.class);
    private static final String THIS_VAR = "this";
    private static final int MAX_RECURSION_DEPTH = 100;
    private static final int MAX_LOCALS = 1024;
    private static final int MAX_STACK_SIZE = 1024;
    private static final char RECIPE_ARG_PLACEHOLDER = '\u0001';
    private static final char RECIPE_CONST_PLACEHOLDER = '\u0002';

    private final ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();
    private final StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
    private final TypeModelFactory typeFactory = TypeModelFactory.getInstance();
    private final IType cachedBoolType = typeFactory.getPrimitiveType('Z');
    private final IType cachedByteType = typeFactory.getPrimitiveType('B');
    private final IType cachedCharType = typeFactory.getPrimitiveType('C');
    private final IType cachedShortType = typeFactory.getPrimitiveType('S');
    private final IType cachedIntType = typeFactory.getPrimitiveType('I');
    private final IType cachedLongType = typeFactory.getPrimitiveType('J');
    private final IType cachedFloatType = typeFactory.getPrimitiveType('F');
    private final IType cachedDoubleType = typeFactory.getPrimitiveType('D');

    private Deque<IExpression> stack;
    private IExpression[] locals;
    private List<IStatement> statements;
    private String className;
    private boolean isStatic;
    private boolean returnsBooleanType;
    private int recursionDepth;
    private java.util.Map<Integer, String> localNames;
    private List<int[]> allBackEdges;
    private long maxTranslationTimeNanos;
    private long translationStartNanos;


    /**
     * Constructor for BytecodeTranslatorImpl.
     */
    public BytecodeTranslatorImpl() {
        // default constructor
    }


    /**
     * Set the maximum time in seconds allowed for translating a single method.
     * A value of zero or negative disables the limit.
     *
     * @param seconds the maximum translation time in seconds
     */
    @Override
    public void setMaxTranslationTimeSeconds(int seconds) {
        if (seconds > 0) {
            this.maxTranslationTimeNanos = seconds * 1_000_000_000L;
        } else {
            this.maxTranslationTimeNanos = 0;
        }
    }


    /**
     * Translate the bytecode of a method into a list of AST statements.
     *
     * @param methodNode the ASM method node
     * @param className  the owning class internal name
     * @return the translated statements
     */
    @Override
    public List<IStatement> translate(MethodNode methodNode, String className) {
        this.stack = new ArrayDeque<>();
        int effectiveMaxLocals = Math.min(methodNode.maxLocals, MAX_LOCALS);
        if (methodNode.maxLocals > MAX_LOCALS) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("maxLocals " + methodNode.maxLocals + " exceeds limit, capping at " + MAX_LOCALS);
            }
        }
        this.locals = new IExpression[effectiveMaxLocals + 1];
        this.statements = new ArrayList<>();
        this.className = className;
        this.isStatic = (methodNode.access & Opcodes.ACC_STATIC) != 0;
        Type returnType = Type.getReturnType(methodNode.desc);
        this.returnsBooleanType = (returnType.getSort() == Type.BOOLEAN);
        this.localNames = new java.util.HashMap<>();
        this.recursionDepth = 0;
        this.translationStartNanos = System.nanoTime();

        initLocals(methodNode);

        // Build label-to-index map for jump resolution
        int insnCount = methodNode.instructions.size();
        java.util.Map<LabelNode, Integer> labelIndices = new java.util.HashMap<>(insnCount / 3 + 1);
        for (int i = 0; i < insnCount; i++) {
            AbstractInsnNode insn = methodNode.instructions.get(i);
            if (insn instanceof LabelNode) {
                labelIndices.put((LabelNode) insn, i);
            }
        }

        // Precompute all backward GOTO edges for efficient lookup during loop structuring
        this.allBackEdges = new ArrayList<>();
        for (int i = 0; i < insnCount; i++) {
            AbstractInsnNode insn = methodNode.instructions.get(i);
            if (insn instanceof JumpInsnNode && insn.getOpcode() == Opcodes.GOTO) {
                Integer target = labelIndices.get(((JumpInsnNode) insn).label);
                if (target != null && target <= i) {
                    allBackEdges.add(new int[]{i, target});
                }
            }
        }

        translateRegion(methodNode, 0, methodNode.instructions.size(), labelIndices, new java.util.HashSet<>());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Translated " + methodNode.name + " into " + statements.size() + " statements.");
        }

        return statements;
    }


    /**
     * Translate a region of bytecode instructions into statements.
     * Detects while loops, switch statements, conditional branches, and emits
     * the appropriate AST structures.
     *
     * @param methodNode   the method node
     * @param startIdx     start instruction index (inclusive)
     * @param endIdx       end instruction index (exclusive)
     * @param labelIndices map from label to instruction index
     * @param visited      set of already-visited instruction indices (for loop detection)
     */
    private void translateRegion(MethodNode methodNode, int startIdx, int endIdx,
                                 java.util.Map<LabelNode, Integer> labelIndices,
                                 java.util.Set<Integer> visited) {
        recursionDepth++;
        if (recursionDepth > MAX_RECURSION_DEPTH) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Recursion depth exceeded during bytecode translation, region may be incomplete.");
            }
            recursionDepth--;
            return;
        }
        if (maxTranslationTimeNanos > 0
                && (System.nanoTime() - translationStartNanos) > maxTranslationTimeNanos) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Translation time limit exceeded, returning partial result.");
            }
            recursionDepth--;
            return;
        }
        try {
            for (int i = startIdx; i < endIdx; i++) {
                if (visited.contains(i)) {
                    break;
                }

                AbstractInsnNode insn = methodNode.instructions.get(i);

                // Handle tableswitch and lookupswitch instructions
                if (insn instanceof TableSwitchInsnNode || insn instanceof LookupSwitchInsnNode) {
                    visited.add(i);
                    emitSwitch(methodNode, insn, i, endIdx, labelIndices, visited);
                    return;
                }

                // Handle conditional jumps: detect while loops first, then if/else
                if (insn instanceof JumpInsnNode && insn.getOpcode() != Opcodes.GOTO) {
                    JumpInsnNode jumpInsn = (JumpInsnNode) insn;
                    Integer targetIdx = labelIndices.get(jumpInsn.label);
                    if (targetIdx != null) {
                        // Check for while loop: conditional forward jump with back-edge in body
                        if (targetIdx > i) {
                            int backEdgeIdx = findBackEdgeInRegion(methodNode, i + 1, targetIdx, i, labelIndices);
                            if (backEdgeIdx >= 0) {
                                visited.add(i);
                                emitWhileFromCondition(methodNode, i, targetIdx, backEdgeIdx, endIdx,
                                        labelIndices, visited);
                                return;
                            }
                        }

                        if (tryEmitConditionalReturn(methodNode, i, targetIdx, labelIndices, visited)) {
                            return;
                        }
                        visited.add(i);
                        emitConditionalBlock(methodNode, i, targetIdx, endIdx, labelIndices, visited);
                        return;
                    }
                }

                // Handle GOTO (unconditional jump)
                if (insn instanceof JumpInsnNode && insn.getOpcode() == Opcodes.GOTO) {
                    JumpInsnNode jumpInsn = (JumpInsnNode) insn;
                    Integer targetIdx = labelIndices.get(jumpInsn.label);
                    if (targetIdx != null && targetIdx <= i) {
                        visited.add(i);
                        if (targetIdx < startIdx) {
                            // Back-edge exits the current region (continue to outer loop)
                            return;
                        }
                        emitLoopFromBackEdge(methodNode, targetIdx, i, endIdx, labelIndices, visited);
                        return;
                    }
                    visited.add(i);
                    translateRegion(methodNode, targetIdx, endIdx, labelIndices, visited);
                    return;
                }

                visited.add(i);
                processInstruction(insn);
            }
        } finally {
            recursionDepth--;
        }
    }


    /**
     * Emit an if/else block from a conditional jump. The bytecode condition represents
     * "when to jump" (to the else/exit), so it is negated to get the "then" condition.
     *
     * @param methodNode   the method node
     * @param jumpIdx      the index of the jump instruction
     * @param targetIdx    the jump target index
     * @param regionEnd    the end of the current region
     * @param labelIndices label-to-index map
     * @param visited      visited indices
     */
    private void emitConditionalBlock(MethodNode methodNode, int jumpIdx, int targetIdx,
                                      int regionEnd, java.util.Map<LabelNode, Integer> labelIndices,
                                      java.util.Set<Integer> visited) {

        // Process the jump to get condition on stack, then pop it
        processInstruction(methodNode.instructions.get(jumpIdx));

        int gotoIdx = findGotoBefore(methodNode, targetIdx);

        // Translate "then" branch in isolated context
        List<IStatement> thenStatements = translateBranch(methodNode, jumpIdx + 1,
                targetIdx, labelIndices, visited);

        // Check for else branch
        List<IStatement> elseStatements = new ArrayList<>();
        if (gotoIdx >= 0) {
            AbstractInsnNode gotoInsn = methodNode.instructions.get(gotoIdx);
            if (gotoInsn instanceof JumpInsnNode) {
                Integer elseEnd = labelIndices.get(((JumpInsnNode) gotoInsn).label);
                if (elseEnd != null && elseEnd > targetIdx) {
                    elseStatements = translateBranch(methodNode, targetIdx,
                            elseEnd, labelIndices, visited);
                }
            }
        }

        // Negate the condition: bytecode jumps when condition is true (to else/exit),
        // but we want the condition under which the fall-through (then) branch executes
        IExpression condition = negateCondition(safePop());
        IStatement thenBlock = stmtFactory.getBlockStatement(null, thenStatements);
        IStatement elseBlock = null;
        if (!elseStatements.isEmpty()) {
            elseBlock = stmtFactory.getBlockStatement(null, elseStatements);
        }
        statements.add(stmtFactory.getIfStatement(null, condition, thenBlock, elseBlock));

        // Continue after the if/else
        int continueIdx = targetIdx;
        if (gotoIdx >= 0) {
            AbstractInsnNode gotoInsn = methodNode.instructions.get(gotoIdx);
            if (gotoInsn instanceof JumpInsnNode) {
                Integer afterElse = labelIndices.get(((JumpInsnNode) gotoInsn).label);
                if (afterElse != null) {
                    continueIdx = afterElse;
                }
            }
        }
        translateRegion(methodNode, continueIdx, regionEnd, labelIndices, visited);
    }


    /**
     * Emit a while loop detected from a conditional forward jump whose body contains
     * a backward GOTO (back-edge). The bytecode condition represents "when to exit",
     * so it is negated to get the "while continuing" condition.
     *
     * @param methodNode   the method node
     * @param condJumpIdx  the index of the conditional jump at the loop header
     * @param exitIdx      the jump target (loop exit point)
     * @param backEdgeIdx  the index of the backward GOTO in the loop body
     * @param regionEnd    the end of the enclosing region
     * @param labelIndices label-to-index map
     * @param visited      visited indices
     */
    private void emitWhileFromCondition(MethodNode methodNode, int condJumpIdx, int exitIdx,
                                         int backEdgeIdx, int regionEnd,
                                         java.util.Map<LabelNode, Integer> labelIndices,
                                         java.util.Set<Integer> visited) {

        // Process the condition instruction to get the bytecode condition on stack
        processInstruction(methodNode.instructions.get(condJumpIdx));

        // Negate: bytecode jumps to exit when false, we want "while true" condition
        IExpression condition = negateCondition(safePop());

        // Translate loop body (from after the condition to the back-edge GOTO).
        // Pre-mark backward GOTOs targeting at or before the loop header to prevent
        // recursive re-translation when catch/finally handlers jump back to the loop start.
        java.util.Set<Integer> bodyVisited = collectOuterBackEdges(condJumpIdx + 1, backEdgeIdx, condJumpIdx);
        List<IStatement> bodyStatements = translateBranch(methodNode, condJumpIdx + 1, backEdgeIdx, labelIndices, bodyVisited);

        IStatement bodyBlock = stmtFactory.getBlockStatement(null, bodyStatements);
        statements.add(stmtFactory.getWhileStatement(null, condition, bodyBlock));

        // Continue after the loop
        translateRegion(methodNode, exitIdx, regionEnd, labelIndices, visited);
    }


    /**
     * Emit a while loop from a backward GOTO (back-edge).
     * Detects whether the loop header has a condition (while) or not (do-while/infinite).
     *
     * @param methodNode   the method node
     * @param loopStart    the target of the back-edge (loop header)
     * @param gotoIdx      the index of the GOTO instruction
     * @param regionEnd    the end of the enclosing region
     * @param labelIndices label-to-index map
     * @param visited      visited indices
     */
    private void emitLoopFromBackEdge(MethodNode methodNode, int loopStart, int gotoIdx,
                                      int regionEnd, java.util.Map<LabelNode, Integer> labelIndices,
                                      java.util.Set<Integer> visited) {

        // Check if the loop header starts with a conditional jump (while-loop pattern)
        IExpression condition = exprFactory.getConstantExpression(
                typeFactory.getPrimitiveType('Z'), Boolean.TRUE);
        int bodyStart = loopStart;
        int afterLoop = gotoIdx + 1;

        // Look for: L1: [body...] condition_insn ifeq/ifne L_exit -> goto L1
        int condJumpIdx = findConditionalJumpBefore(methodNode, gotoIdx);
        if (condJumpIdx >= 0) {
            AbstractInsnNode condJump = methodNode.instructions.get(condJumpIdx);
            if (condJump instanceof JumpInsnNode) {
                Integer exitIdx = labelIndices.get(((JumpInsnNode) condJump).label);
                if (exitIdx != null && exitIdx > gotoIdx) {
                    afterLoop = exitIdx;
                }
            }
        }

        // Translate the loop body.
        // Pre-mark backward GOTOs targeting at or before the loop header to prevent
        // recursive re-translation when catch/finally handlers jump back to the loop start.
        java.util.Set<Integer> bodyVisited = collectOuterBackEdges(bodyStart, gotoIdx, bodyStart);
        List<IStatement> bodyStatements = translateBranch(methodNode, bodyStart, gotoIdx, labelIndices, bodyVisited);

        // If body ends with a condition expression on stack, use it (negated)
        if (!stack.isEmpty()) {
            condition = negateCondition(safePop());
        }

        IStatement bodyBlock = stmtFactory.getBlockStatement(null, bodyStatements);
        statements.add(stmtFactory.getWhileStatement(null, condition, bodyBlock));

        // Continue after the loop
        translateRegion(methodNode, afterLoop, regionEnd, labelIndices, visited);
    }


    /**
     * Emit a switch statement from a tableswitch or lookupswitch instruction.
     * Groups case values that share the same target, translates each case body,
     * and emits the complete switch AST node.
     *
     * @param methodNode   the method node
     * @param switchInsn   the switch instruction (TableSwitchInsnNode or LookupSwitchInsnNode)
     * @param switchIdx    the instruction index
     * @param regionEnd    the end of the enclosing region
     * @param labelIndices label-to-index map
     * @param visited      visited indices
     */
    private void emitSwitch(MethodNode methodNode, AbstractInsnNode switchInsn, int switchIdx,
                             int regionEnd, java.util.Map<LabelNode, Integer> labelIndices,
                             java.util.Set<Integer> visited) {
        final IExpression selector = safePop();
        IType intType = typeFactory.getPrimitiveType('I');

        // Collect case value -> target index mappings, grouped by target
        java.util.Map<Integer, List<Integer>> targetToCaseValues = new java.util.LinkedHashMap<>();
        Integer defaultTargetIdx = null;

        if (switchInsn instanceof TableSwitchInsnNode) {
            TableSwitchInsnNode tableSwitch = (TableSwitchInsnNode) switchInsn;
            defaultTargetIdx = labelIndices.get(tableSwitch.dflt);
            for (int val = tableSwitch.min; val <= tableSwitch.max; val++) {
                Integer targetIdx = labelIndices.get(tableSwitch.labels.get(val - tableSwitch.min));
                if (targetIdx != null) {
                    targetToCaseValues.computeIfAbsent(targetIdx, k -> new ArrayList<>()).add(val);
                }
            }
        } else if (switchInsn instanceof LookupSwitchInsnNode) {
            LookupSwitchInsnNode lookupSwitch = (LookupSwitchInsnNode) switchInsn;
            defaultTargetIdx = labelIndices.get(lookupSwitch.dflt);
            for (int ki = 0; ki < lookupSwitch.keys.size(); ki++) {
                Integer targetIdx = labelIndices.get(lookupSwitch.labels.get(ki));
                if (targetIdx != null) {
                    targetToCaseValues.computeIfAbsent(targetIdx, k -> new ArrayList<>())
                            .add(lookupSwitch.keys.get(ki));
                }
            }
        }

        // Collect all unique targets and sort by instruction index
        java.util.Set<Integer> allTargets = new java.util.TreeSet<>(targetToCaseValues.keySet());
        if (defaultTargetIdx != null) {
            allTargets.add(defaultTargetIdx);
        }
        List<Integer> sortedTargets = new ArrayList<>(allTargets);

        // Build switch cases
        List<ISwitchCase> cases = new ArrayList<>();
        for (int ti = 0; ti < sortedTargets.size(); ti++) {
            int targetIdx = sortedTargets.get(ti);
            int bodyEnd = regionEnd;
            if (ti + 1 < sortedTargets.size()) {
                bodyEnd = sortedTargets.get(ti + 1);
            }

            List<IExpression> caseValues = new ArrayList<>();
            if (targetToCaseValues.containsKey(targetIdx)) {
                for (int val : targetToCaseValues.get(targetIdx)) {
                    caseValues.add(exprFactory.getConstantExpression(intType, val));
                }
            }

            boolean isDefault = (defaultTargetIdx != null && targetIdx == defaultTargetIdx.intValue());

            List<IStatement> caseBody = translateBranch(methodNode, targetIdx, bodyEnd, labelIndices, collectOuterBackEdges(targetIdx, bodyEnd, targetIdx));
            cases.add(stmtFactory.getSwitchCase(caseValues, isDefault, caseBody));
        }

        statements.add(stmtFactory.getSwitchStatement(null, selector, cases));
    }


    /**
     * Find a backward GOTO (back-edge) within a region that jumps to or before
     * the given loop header index.
     *
     * @param methodNode     the method node
     * @param startIdx       region start (inclusive)
     * @param endIdx         region end (exclusive)
     * @param loopHeaderIdx  the loop header instruction index
     * @param labelIndices   label-to-index map
     * @return the index of the backward GOTO, or -1 if not found
     */
    private int findBackEdgeInRegion(MethodNode methodNode, int startIdx, int endIdx,
                                      int loopHeaderIdx,
                                      java.util.Map<LabelNode, Integer> labelIndices) {
        for (int idx = endIdx - 1; idx >= startIdx; idx--) {
            AbstractInsnNode insn = methodNode.instructions.get(idx);
            if (insn instanceof JumpInsnNode && insn.getOpcode() == Opcodes.GOTO) {
                Integer target = labelIndices.get(((JumpInsnNode) insn).label);
                if (target != null && target <= loopHeaderIdx) {
                    return idx;
                }
            }
        }
        return -1;
    }


    /**
     * Collect instruction indices of backward GOTOs within a region that target at or
     * before the given loop header. Uses the precomputed back-edge list for efficient
     * lookup. These are marked as visited before translating the loop body so that
     * catch/finally handlers jumping back to the enclosing loop do not trigger recursive
     * loop creation.
     *
     * @param startIdx      region start (inclusive)
     * @param endIdx        region end (exclusive)
     * @param loopHeaderIdx the loop header instruction index
     * @return set of instruction indices to pre-mark as visited
     */
    private java.util.Set<Integer> collectOuterBackEdges(int startIdx, int endIdx, int loopHeaderIdx) {
        java.util.Set<Integer> result = new java.util.HashSet<>();
        for (int[] edge : allBackEdges) {
            if (edge[0] >= startIdx && edge[0] < endIdx && edge[1] <= loopHeaderIdx) {
                result.add(edge[0]);
            }
        }
        return result;
    }


    /**
     * Negate a boolean condition expression. For binary comparisons, flips the
     * operator (e.g., EQ to NE, LT to GE). For other expressions, wraps in logical NOT.
     *
     * @param condition the condition to negate
     * @return the negated condition
     */
    private IExpression negateCondition(IExpression condition) {
        if (condition instanceof IBinaryExpression) {
            IBinaryExpression binExpr = (IBinaryExpression) condition;
            BinaryOperator negated = negateOperator(binExpr.getOperator());
            if (negated != null) {
                return exprFactory.getBinaryExpression(condition.getType(), binExpr.getLeft(), binExpr.getRight(), negated);
            }
        }
        return exprFactory.getUnaryExpression(typeFactory.getPrimitiveType('Z'), condition, UnaryOperator.LOGICAL_NOT);
    }


    /**
     * Get the negated comparison operator for a binary operator.
     *
     * @param op the operator
     * @return the negated operator, or null if not a comparison
     */
    private BinaryOperator negateOperator(BinaryOperator op) {
        switch (op) {
            case EQ: return BinaryOperator.NE;
            case NE: return BinaryOperator.EQ;
            case LT: return BinaryOperator.GE;
            case GE: return BinaryOperator.LT;
            case GT: return BinaryOperator.LE;
            case LE: return BinaryOperator.GT;
            default: return null;
        }
    }


    /**
     * Find a conditional jump instruction before the given index (skipping labels/frames).
     *
     * @param methodNode the method node
     * @param beforeIdx  the index to search before
     * @return the conditional jump index, or -1
     */
    private int findConditionalJumpBefore(MethodNode methodNode, int beforeIdx) {
        for (int ii = beforeIdx - 1; ii >= 0; ii--) {
            AbstractInsnNode insn = methodNode.instructions.get(ii);
            if (insn instanceof JumpInsnNode && insn.getOpcode() != Opcodes.GOTO) {
                return ii;
            }
            int type = insn.getType();
            if (type != AbstractInsnNode.LABEL && type != AbstractInsnNode.FRAME
                    && type != AbstractInsnNode.LINE) {
                break;
            }
        }
        return -1;
    }


    /**
     * Try to detect and emit a conditional return pattern: cmp, const_a, goto, const_b, return.
     * The bytecode condition is negated so that the fall-through value (thenValue) is returned
     * when the negated condition is true.
     *
     * @param methodNode   the method node
     * @param jumpIdx      the conditional jump index
     * @param targetIdx    the jump target index
     * @param labelIndices label-to-index map
     * @param visited      visited indices
     * @return true if the pattern was detected and emitted
     */
    private boolean tryEmitConditionalReturn(MethodNode methodNode, int jumpIdx, int targetIdx,
                                             java.util.Map<LabelNode, Integer> labelIndices,
                                             java.util.Set<Integer> visited) {
        // Look for: [jumpIdx] ifeq -> [jumpIdx+1..] iconst_X, goto L2 -> [targetIdx..] iconst_Y -> [L2] xreturn
        int fallIdx = jumpIdx + 1;

        // Find the first real instruction after the jump (skip labels/frames)
        int thenConstIdx = findNextRealInstruction(methodNode, fallIdx);
        if (thenConstIdx < 0) {
            return false;
        }

        // Check if it's a constant push
        AbstractInsnNode thenInsn = methodNode.instructions.get(thenConstIdx);
        if (!isConstantPush(thenInsn)) {
            return false;
        }

        // Next should be GOTO
        int afterThenIdx = findNextRealInstruction(methodNode, thenConstIdx + 1);
        if (afterThenIdx < 0) {
            return false;
        }
        AbstractInsnNode gotoInsn = methodNode.instructions.get(afterThenIdx);
        if (gotoInsn.getOpcode() != Opcodes.GOTO) {
            return false;
        }

        // At target, should be a constant push
        int elseConstIdx = findNextRealInstruction(methodNode, targetIdx);
        if (elseConstIdx < 0) {
            return false;
        }
        AbstractInsnNode elseInsn = methodNode.instructions.get(elseConstIdx);
        if (!isConstantPush(elseInsn)) {
            return false;
        }

        // After else const, should be a return
        int returnIdx = findNextRealInstruction(methodNode, elseConstIdx + 1);
        if (returnIdx < 0) {
            return false;
        }
        AbstractInsnNode retInsn = methodNode.instructions.get(returnIdx);
        if (!isReturnInstruction(retInsn)) {
            return false;
        }

        // Pattern matched! Process the jump to get condition, then negate
        processInstruction(methodNode.instructions.get(jumpIdx));
        IExpression condition = negateCondition(safePop());

        // Get the constant values, converting to boolean if method returns boolean.
        // thenValue = fall-through (returned when negated condition is true)
        // elseValue = jump target (returned when negated condition is false)
        processInstruction(thenInsn);
        IExpression thenValue = toBooleanIfNeeded(safePop());
        processInstruction(elseInsn);
        IExpression elseValue = toBooleanIfNeeded(safePop());

        // Emit: if (negatedCondition) { return thenValue; } else { return elseValue; }
        IStatement thenReturn = stmtFactory.getReturnStatement(null, thenValue);
        IStatement elseReturn = stmtFactory.getReturnStatement(null, elseValue);
        IStatement thenBlock = stmtFactory.getBlockStatement(null, java.util.Collections.singletonList(thenReturn));
        IStatement elseBlock = stmtFactory.getBlockStatement(null, java.util.Collections.singletonList(elseReturn));
        statements.add(stmtFactory.getIfStatement(null, condition, thenBlock, elseBlock));

        // Mark all as visited
        for (int vi = jumpIdx; vi <= returnIdx; vi++) {
            visited.add(vi);
        }
        return true;
    }


    /**
     * Find the next real instruction index (skip labels, frames, line numbers).
     *
     * @param methodNode the method node
     * @param fromIdx    starting index
     * @return the index, or -1
     */
    private int findNextRealInstruction(MethodNode methodNode, int fromIdx) {
        for (int ii = fromIdx; ii < methodNode.instructions.size(); ii++) {
            AbstractInsnNode insn = methodNode.instructions.get(ii);
            int type = insn.getType();
            if (type != AbstractInsnNode.LABEL && type != AbstractInsnNode.FRAME
                    && type != AbstractInsnNode.LINE) {
                return ii;
            }
        }
        return -1;
    }


    /**
     * Check if an instruction pushes a constant value.
     *
     * @param insn the instruction
     * @return true if it's a constant push
     */
    private boolean isConstantPush(AbstractInsnNode insn) {
        int opcode = insn.getOpcode();
        return (opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5)
                || opcode == Opcodes.LCONST_0 || opcode == Opcodes.LCONST_1
                || opcode == Opcodes.FCONST_0 || opcode == Opcodes.FCONST_1
                || opcode == Opcodes.DCONST_0 || opcode == Opcodes.DCONST_1
                || opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH
                || opcode == Opcodes.LDC
                || opcode == Opcodes.ACONST_NULL;
    }


    /**
     * Check if an instruction is a return instruction.
     *
     * @param insn the instruction
     * @return true if it's a return
     */
    private boolean isReturnInstruction(AbstractInsnNode insn) {
        int opcode = insn.getOpcode();
        return opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN
                || opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN
                || opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN;
    }


    /**
     * Translate a branch region in an isolated statement context.
     * Saves and restores the current statements list and stack.
     *
     * @param methodNode   the method node
     * @param startIdx     branch start index
     * @param endIdx       branch end index
     * @param labelIndices label-to-index map
     * @param visited      visited indices
     * @return the translated statements for this branch
     */
    private List<IStatement> translateBranch(MethodNode methodNode, int startIdx, int endIdx,
                                             java.util.Map<LabelNode, Integer> labelIndices,
                                             java.util.Set<Integer> visited) {
        Deque<IExpression> stackBackup = this.stack;
        final List<IStatement> statementsBackup = this.statements;
        IExpression[] localsBackup = this.locals;
        this.stack = new ArrayDeque<>(stackBackup);
        this.locals = localsBackup.clone();
        this.statements = new ArrayList<>();
        translateRegion(methodNode, startIdx, endIdx, labelIndices, visited);
        final List<IStatement> branchResult = this.statements;
        this.locals = localsBackup;
        this.stack = stackBackup;
        this.statements = statementsBackup;
        return branchResult;
    }


    /**
     * Find a GOTO instruction just before the given index (skipping labels/frames).
     *
     * @param methodNode the method node
     * @param beforeIdx  the index to search before
     * @return the GOTO index, or -1 if not found
     */
    private int findGotoBefore(MethodNode methodNode, int beforeIdx) {
        for (int i = beforeIdx - 1; i >= 0; i--) {
            AbstractInsnNode insn = methodNode.instructions.get(i);
            if (insn.getOpcode() == Opcodes.GOTO) {
                return i;
            }
            // Skip labels, frames, line numbers
            int type = insn.getType();
            if (type != AbstractInsnNode.LABEL && type != AbstractInsnNode.FRAME
                    && type != AbstractInsnNode.LINE) {
                break;
            }
        }
        return -1;
    }


    /**
     * Initialize local variables: slot 0 = this (for instance methods), parameters follow.
     * Reads the LocalVariableTable (LVT) from debug info to recover original variable names.
     *
     * @param methodNode the method node
     */
    private void initLocals(MethodNode methodNode) {
        // Build a local variable name lookup from debug info (LVT)
        java.util.Map<Integer, String> localDescs = new java.util.HashMap<>();
        if (methodNode.localVariables != null) {
            for (org.objectweb.asm.tree.LocalVariableNode lvn : methodNode.localVariables) {
                localNames.put(lvn.index, lvn.name);
                localDescs.put(lvn.index, lvn.desc);
            }
        }

        int slot = 0;
        if (!isStatic) {
            locals[0] = exprFactory.getVariableExpression(
                    typeFactory.getObjectType(className), THIS_VAR, 0);
            slot = 1;
        }

        Type[] argTypes = Type.getArgumentTypes(methodNode.desc);
        for (int i = 0; i < argTypes.length; i++) {
            String paramName = localNames.getOrDefault(slot, "arg" + i);
            if (THIS_VAR.equals(paramName)) {
                paramName = "arg" + i;
            }
            if (methodNode.parameters != null && i < methodNode.parameters.size()) {
                String explicitName = methodNode.parameters.get(i).name;
                if (explicitName != null && !explicitName.startsWith("arg")) {
                    paramName = explicitName;
                }
            }
            IType paramType = asmTypeToIType(argTypes[i]);
            locals[slot] = exprFactory.getVariableExpression(paramType, paramName, slot);
            slot++;
            if (argTypes[i].getSize() == 2) {
                slot++;
            }
        }

        // Pre-populate local variable names from debug info for non-parameter slots
        for (java.util.Map.Entry<Integer, String> entry : localNames.entrySet()) {
            int lvSlot = entry.getKey();
            if (lvSlot >= slot && lvSlot < locals.length && locals[lvSlot] == null) {
                String lvDesc = localDescs.get(lvSlot);
                IType lvType = typeFactory.getObjectType("java/lang/Object");
                if (lvDesc != null) {
                    lvType = asmTypeToIType(Type.getType(lvDesc));
                }
                locals[lvSlot] = exprFactory.getVariableExpression(lvType, entry.getValue(), lvSlot);
            }
        }
    }


    /**
     * Process a single bytecode instruction.
     *
     * @param insn the instruction
     */
    private void processInstruction(AbstractInsnNode insn) {
        int opcode = insn.getOpcode();
        if (opcode == -1) {
            return; // label, frame, or line number
        }

        switch (insn.getType()) {
            case AbstractInsnNode.INSN:
                processInsn(opcode);
                break;
            case AbstractInsnNode.INT_INSN:
                processIntInsn((IntInsnNode) insn);
                break;
            case AbstractInsnNode.VAR_INSN:
                processVarInsn((VarInsnNode) insn);
                break;
            case AbstractInsnNode.FIELD_INSN:
                processFieldInsn((FieldInsnNode) insn);
                break;
            case AbstractInsnNode.METHOD_INSN:
                processMethodInsn((MethodInsnNode) insn);
                break;
            case AbstractInsnNode.TYPE_INSN:
                processTypeInsn((TypeInsnNode) insn);
                break;
            case AbstractInsnNode.JUMP_INSN:
                processJumpInsn((JumpInsnNode) insn);
                break;
            case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
                processInvokeDynamic((InvokeDynamicInsnNode) insn);
                break;
            case AbstractInsnNode.LDC_INSN:
                processLdcInsn((LdcInsnNode) insn);
                break;
            case AbstractInsnNode.IINC_INSN:
                processIincInsn((IincInsnNode) insn);
                break;
            default:
                break;
        }
    }


    /**
     * Process zero-operand instructions (constants, arithmetic, returns, stack ops).
     *
     * @param opcode the opcode
     */
    private void processInsn(int opcode) {
        if (processConstantInsn(opcode)) {
            return;
        }
        if (processArithmeticInsn(opcode)) {
            return;
        }
        processOtherInsn(opcode);
    }


    /**
     * Process constant push instructions.
     *
     * @param opcode the opcode
     * @return true if handled
     */
    private boolean processConstantInsn(int opcode) {
        IType intType = typeFactory.getPrimitiveType('I');
        IType longType = typeFactory.getPrimitiveType('J');

        switch (opcode) {
            case Opcodes.ACONST_NULL:
                push(exprFactory.getConstantExpression(typeFactory.getObjectType("java/lang/Object"), null));
                break;
            case Opcodes.ICONST_M1:
                push(exprFactory.getConstantExpression(intType, -1));
                break;
            case Opcodes.ICONST_0:
                push(exprFactory.getConstantExpression(intType, 0));
                break;
            case Opcodes.ICONST_1:
                push(exprFactory.getConstantExpression(intType, 1));
                break;
            case Opcodes.ICONST_2:
                push(exprFactory.getConstantExpression(intType, 2));
                break;
            case Opcodes.ICONST_3:
                push(exprFactory.getConstantExpression(intType, 3));
                break;
            case Opcodes.ICONST_4:
                push(exprFactory.getConstantExpression(intType, 4));
                break;
            case Opcodes.ICONST_5:
                push(exprFactory.getConstantExpression(intType, 5));
                break;
            case Opcodes.LCONST_0:
                push(exprFactory.getConstantExpression(longType, 0L));
                break;
            case Opcodes.LCONST_1:
                push(exprFactory.getConstantExpression(longType, 1L));
                break;
            case Opcodes.FCONST_0:
                push(exprFactory.getConstantExpression(typeFactory.getPrimitiveType('F'), 0.0f));
                break;
            case Opcodes.FCONST_1:
                push(exprFactory.getConstantExpression(typeFactory.getPrimitiveType('F'), 1.0f));
                break;
            case Opcodes.DCONST_0:
                push(exprFactory.getConstantExpression(typeFactory.getPrimitiveType('D'), 0.0d));
                break;
            case Opcodes.DCONST_1:
                push(exprFactory.getConstantExpression(typeFactory.getPrimitiveType('D'), 1.0d));
                break;

            default:
                return false;
        }
        return true;
    }


    /**
     * Process arithmetic and logic instructions.
     *
     * @param opcode the opcode
     * @return true if handled
     */
    private boolean processArithmeticInsn(int opcode) {
        switch (opcode) {
            case Opcodes.IADD: case Opcodes.LADD: case Opcodes.FADD: case Opcodes.DADD:
                processBinaryOp(BinaryOperator.ADD);
                break;
            case Opcodes.ISUB: case Opcodes.LSUB: case Opcodes.FSUB: case Opcodes.DSUB:
                processBinaryOp(BinaryOperator.SUB);
                break;
            case Opcodes.IMUL: case Opcodes.LMUL: case Opcodes.FMUL: case Opcodes.DMUL:
                processBinaryOp(BinaryOperator.MUL);
                break;
            case Opcodes.IDIV: case Opcodes.LDIV: case Opcodes.FDIV: case Opcodes.DDIV:
                processBinaryOp(BinaryOperator.DIV);
                break;
            case Opcodes.IREM: case Opcodes.LREM: case Opcodes.FREM: case Opcodes.DREM:
                processBinaryOp(BinaryOperator.REM);
                break;
            case Opcodes.IAND: case Opcodes.LAND:
                processBinaryOp(BinaryOperator.AND);
                break;
            case Opcodes.IOR: case Opcodes.LOR:
                processBinaryOp(BinaryOperator.OR);
                break;
            case Opcodes.IXOR: case Opcodes.LXOR:
                processBinaryOp(BinaryOperator.XOR);
                break;
            case Opcodes.ISHL: case Opcodes.LSHL:
                processBinaryOp(BinaryOperator.SHL);
                break;
            case Opcodes.ISHR: case Opcodes.LSHR:
                processBinaryOp(BinaryOperator.SHR);
                break;
            case Opcodes.IUSHR: case Opcodes.LUSHR:
                processBinaryOp(BinaryOperator.USHR);
                break;
            case Opcodes.INEG: case Opcodes.LNEG: case Opcodes.FNEG: case Opcodes.DNEG:
                processUnaryOp(UnaryOperator.NEG);
                break;

            default:
                return false;
        }
        return true;
    }


    /**
     * Process return, throw, comparison, stack, and array instructions.
     *
     * @param opcode the opcode
     */
    private void processOtherInsn(int opcode) {
        switch (opcode) {
            // Returns
            case Opcodes.IRETURN: case Opcodes.LRETURN: case Opcodes.FRETURN:
            case Opcodes.DRETURN: case Opcodes.ARETURN:
                emitReturnWithValue();
                break;
            case Opcodes.RETURN:
                statements.add(stmtFactory.getReturnStatement(null, null));
                break;

            // Throw
            case Opcodes.ATHROW:
                emitThrow();
                break;

            // Comparison instructions (produce int result from two operands)
            case Opcodes.LCMP:
            case Opcodes.FCMPL: case Opcodes.FCMPG:
            case Opcodes.DCMPL: case Opcodes.DCMPG:
                processComparison();
                break;

            // Type conversions (pop one, push converted)
            case Opcodes.I2L: case Opcodes.I2F: case Opcodes.I2D:
            case Opcodes.L2I: case Opcodes.L2F: case Opcodes.L2D:
            case Opcodes.F2I: case Opcodes.F2L: case Opcodes.F2D:
            case Opcodes.D2I: case Opcodes.D2L: case Opcodes.D2F:
            case Opcodes.I2B: case Opcodes.I2C: case Opcodes.I2S:
                // Keep the value on stack (implicit widening/narrowing)
                break;

            // Stack operations
            case Opcodes.POP:
                emitPop();
                break;
            case Opcodes.POP2:
                safePop();
                safePop();
                break;
            case Opcodes.DUP:
                if (!stack.isEmpty()) {
                    push(stack.peek());
                }
                break;
            case Opcodes.DUP2:
                if (!stack.isEmpty()) {
                    IExpression top = stack.peek();
                    push(top);
                }
                break;
            case Opcodes.SWAP:
                if (stack.size() >= 2) {
                    IExpression swapTop = stack.pop();
                    IExpression swapSecond = stack.pop();
                    push(swapTop);
                    push(swapSecond);
                }
                break;

            // Array load
            case Opcodes.IALOAD: case Opcodes.LALOAD: case Opcodes.FALOAD:
            case Opcodes.DALOAD: case Opcodes.AALOAD: case Opcodes.BALOAD:
            case Opcodes.CALOAD: case Opcodes.SALOAD:
                processArrayLoad();
                break;

            // Array store
            case Opcodes.IASTORE: case Opcodes.LASTORE: case Opcodes.FASTORE:
            case Opcodes.DASTORE: case Opcodes.AASTORE: case Opcodes.BASTORE:
            case Opcodes.CASTORE: case Opcodes.SASTORE:
                processArrayStore();
                break;

            // Array length
            case Opcodes.ARRAYLENGTH:
                processArrayLength();
                break;

            default:
                break;
        }
    }


    /**
     * Process integer push instructions (bipush, sipush, newarray).
     *
     * @param insn the instruction
     */
    private void processIntInsn(IntInsnNode insn) {
        if (insn.getOpcode() == Opcodes.BIPUSH || insn.getOpcode() == Opcodes.SIPUSH) {
            push(exprFactory.getConstantExpression(typeFactory.getPrimitiveType('I'), insn.operand));
        }
    }


    /**
     * Process jump/conditional instructions. Translates bytecode comparisons into
     * binary expression conditions and pushes them onto the stack.
     *
     * @param insn the jump instruction
     */
    private void processJumpInsn(JumpInsnNode insn) {
        int opcode = insn.getOpcode();
        IType boolType = typeFactory.getPrimitiveType('Z');
        IExpression zero = exprFactory.getConstantExpression(typeFactory.getPrimitiveType('I'), 0);
        IExpression nullConst = exprFactory.getConstantExpression(
                typeFactory.getObjectType("java/lang/Object"), null);

        switch (opcode) {
            // Compare one value against zero
            case Opcodes.IFEQ:
                push(exprFactory.getBinaryExpression(boolType, safePop(), zero, BinaryOperator.EQ));
                break;
            case Opcodes.IFNE:
                push(exprFactory.getBinaryExpression(boolType, safePop(), zero, BinaryOperator.NE));
                break;
            case Opcodes.IFLT:
                push(exprFactory.getBinaryExpression(boolType, safePop(), zero, BinaryOperator.LT));
                break;
            case Opcodes.IFGE:
                push(exprFactory.getBinaryExpression(boolType, safePop(), zero, BinaryOperator.GE));
                break;
            case Opcodes.IFGT:
                push(exprFactory.getBinaryExpression(boolType, safePop(), zero, BinaryOperator.GT));
                break;
            case Opcodes.IFLE:
                push(exprFactory.getBinaryExpression(boolType, safePop(), zero, BinaryOperator.LE));
                break;

            // Compare two int values
            case Opcodes.IF_ICMPEQ:
                pushBinaryComparison(BinaryOperator.EQ);
                break;
            case Opcodes.IF_ICMPNE:
                pushBinaryComparison(BinaryOperator.NE);
                break;
            case Opcodes.IF_ICMPLT:
                pushBinaryComparison(BinaryOperator.LT);
                break;
            case Opcodes.IF_ICMPGE:
                pushBinaryComparison(BinaryOperator.GE);
                break;
            case Opcodes.IF_ICMPGT:
                pushBinaryComparison(BinaryOperator.GT);
                break;
            case Opcodes.IF_ICMPLE:
                pushBinaryComparison(BinaryOperator.LE);
                break;

            // Compare two object references
            case Opcodes.IF_ACMPEQ:
                pushBinaryComparison(BinaryOperator.EQ);
                break;
            case Opcodes.IF_ACMPNE:
                pushBinaryComparison(BinaryOperator.NE);
                break;

            // Null checks
            case Opcodes.IFNULL:
                push(exprFactory.getBinaryExpression(boolType, safePop(), nullConst, BinaryOperator.EQ));
                break;
            case Opcodes.IFNONNULL:
                push(exprFactory.getBinaryExpression(boolType, safePop(), nullConst, BinaryOperator.NE));
                break;

            // GOTO: no stack effect, just control flow
            case Opcodes.GOTO:
                break;

            default:
                break;
        }
    }


    /**
     * Pop two values and push a binary comparison.
     *
     * @param op the comparison operator
     */
    private void pushBinaryComparison(BinaryOperator op) {
        IExpression right = safePop();
        IExpression left = safePop();
        push(exprFactory.getBinaryExpression(typeFactory.getPrimitiveType('Z'), left, right, op));
    }


    /**
     * Process variable load/store instructions. Uses the LocalVariableTable (LVT) names
     * when available to recover original variable names.
     *
     * @param insn the instruction
     */
    private void processVarInsn(VarInsnNode insn) {
        int slot = insn.var;
        switch (insn.getOpcode()) {
            case Opcodes.ALOAD: case Opcodes.ILOAD: case Opcodes.LLOAD:
            case Opcodes.FLOAD: case Opcodes.DLOAD:
                if (slot < locals.length && locals[slot] != null) {
                    push(locals[slot]);
                } else {
                    String loadName = localNames.getOrDefault(slot, "var" + slot);
                    push(exprFactory.getVariableExpression(null, loadName, slot));
                }
                break;
            case Opcodes.ASTORE: case Opcodes.ISTORE: case Opcodes.LSTORE:
            case Opcodes.FSTORE: case Opcodes.DSTORE:
                IExpression value = safePop();
                if (slot < locals.length) {
                    // If storing to a new variable, emit declaration; otherwise assignment
                    if (locals[slot] == null) {
                        String varName = localNames.getOrDefault(slot, "var" + slot);
                        IType varType = value.getType();
                        if (varType == null) {
                            varType = typeFactory.getObjectType("java/lang/Object");
                        }
                        locals[slot] = exprFactory.getVariableExpression(varType, varName, slot);
                        statements.add(stmtFactory.getVariableDeclarationStatement(
                                null, varType, varName, value, false));
                    } else {
                        IExpression assignment = exprFactory.getAssignmentExpression(
                                value.getType(), locals[slot], value);
                        statements.add(stmtFactory.getExpressionStatement(null, assignment));
                    }
                }
                break;
            default:
                break;
        }
    }


    /**
     * Process field access instructions.
     *
     * @param insn the instruction
     */
    private void processFieldInsn(FieldInsnNode insn) {
        IType fieldType = asmTypeToIType(Type.getType(insn.desc));

        switch (insn.getOpcode()) {
            case Opcodes.GETFIELD:
                IExpression obj = safePop();
                push(exprFactory.getFieldAccessExpression(fieldType, obj, insn.name, insn.owner));
                break;
            case Opcodes.PUTFIELD:
                IExpression val = safePop();
                IExpression target = safePop();
                IExpression fieldAccess = exprFactory.getFieldAccessExpression(
                        fieldType, target, insn.name, insn.owner);
                IExpression assign = exprFactory.getAssignmentExpression(fieldType, fieldAccess, val);
                statements.add(stmtFactory.getExpressionStatement(null, assign));
                break;
            case Opcodes.GETSTATIC:
                push(exprFactory.getFieldAccessExpression(fieldType, null, insn.name, insn.owner));
                break;
            case Opcodes.PUTSTATIC:
                IExpression staticVal = safePop();
                IExpression staticField = exprFactory.getFieldAccessExpression(
                        fieldType, null, insn.name, insn.owner);
                IExpression staticAssign = exprFactory.getAssignmentExpression(fieldType, staticField, staticVal);
                statements.add(stmtFactory.getExpressionStatement(null, staticAssign));
                break;
            default:
                break;
        }
    }


    /**
     * Process method invocation instructions.
     *
     * @param insn the instruction
     */
    private void processMethodInsn(MethodInsnNode insn) {
        Type methodType = Type.getMethodType(insn.desc);
        Type[] argTypes = methodType.getArgumentTypes();

        // Pop arguments (right to left) into indexed array to preserve correct order
        IExpression[] argArray = new IExpression[argTypes.length];
        for (int i = argTypes.length - 1; i >= 0; i--) {
            argArray[i] = safePop();
        }
        List<IExpression> args = new ArrayList<>(argTypes.length);
        for (IExpression arg : argArray) {
            args.add(arg);
        }

        IExpression targetExpr = null;
        boolean isConstructor = "<init>".equals(insn.name);

        if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
            targetExpr = safePop();
        }

        Type returnType = methodType.getReturnType();
        IType retIType = asmTypeToIType(returnType);
        if (isConstructor) {
            if (targetExpr != null && THIS_VAR.equals(targetExpr.toString())) {
                // super() or this() call in constructor
                IExpression superCall = exprFactory.getMethodCallExpression(
                        retIType, null, "super", insn.owner, args);
                statements.add(stmtFactory.getExpressionStatement(null, superCall));
            } else if (targetExpr instanceof com.github.toolarium.java.decompiler.model.ast.expression.INewExpression) {
                // new Foo(args): replace the placeholder new expression with one that has args
                com.github.toolarium.java.decompiler.model.ast.expression.INewExpression newExpr =
                        (com.github.toolarium.java.decompiler.model.ast.expression.INewExpression) targetExpr;
                IExpression withArgs = exprFactory.getNewExpression(
                        newExpr.getType(), newExpr.getInstantiatedType(), args);
                push(withArgs);
            }
        } else {
            IExpression call = exprFactory.getMethodCallExpression(
                    retIType, targetExpr, insn.name, insn.owner, args);

            if (returnType.getSort() == Type.VOID) {
                statements.add(stmtFactory.getExpressionStatement(null, call));
            } else {
                push(call);
            }
        }
    }


    /**
     * Process invokedynamic instructions (lambdas, method references, string concat).
     * Detects LambdaMetafactory bootstrap methods and translates them into
     * lambda expressions or method references. Detects StringConcatFactory and
     * reconstructs the concatenation from the recipe template.
     *
     * @param insn the invokedynamic instruction
     */
    private void processInvokeDynamic(InvokeDynamicInsnNode insn) {
        Type methodType = Type.getMethodType(insn.desc);
        Type returnType = methodType.getReturnType();
        Type[] argTypes = methodType.getArgumentTypes();
        IType retIType = asmTypeToIType(returnType);

        // Pop captured arguments from stack (right to left)
        List<IExpression> capturedArgs = new ArrayList<>();
        for (int i = argTypes.length - 1; i >= 0; i--) {
            capturedArgs.add(0, safePop());
        }

        // Check if this is a LambdaMetafactory call
        String bootstrapName = "";
        String targetMethod = "";
        if (insn.bsmArgs != null && insn.bsmArgs.length >= 2) {
            Object bsmArg = insn.bsmArgs[1];
            if (bsmArg instanceof org.objectweb.asm.Handle) {
                org.objectweb.asm.Handle handle = (org.objectweb.asm.Handle) bsmArg;
                bootstrapName = handle.getOwner();
                targetMethod = handle.getName();
            }
        }

        if (insn.bsm != null && "java/lang/invoke/LambdaMetafactory".equals(insn.bsm.getOwner())) {
            emitLambdaOrMethodRef(insn, retIType, capturedArgs, bootstrapName, targetMethod);
        } else if (insn.bsm != null && "java/lang/invoke/StringConcatFactory".equals(insn.bsm.getOwner())) {
            emitStringConcat(insn, capturedArgs);
        } else {
            // Unknown invokedynamic, push as placeholder
            push(exprFactory.getConstantExpression(retIType, "/* invokedynamic " + insn.name + " */"));
        }
    }


    /**
     * Emit a lambda expression or method reference from a LambdaMetafactory invokedynamic.
     *
     * @param insn          the invokedynamic instruction
     * @param retIType      the return type
     * @param capturedArgs  the captured arguments
     * @param bootstrapName the bootstrap method owner
     * @param targetMethod  the target method name
     */
    private void emitLambdaOrMethodRef(InvokeDynamicInsnNode insn, IType retIType,
                                       List<IExpression> capturedArgs,
                                       String bootstrapName, String targetMethod) {
        if (!targetMethod.isEmpty() && !targetMethod.startsWith("lambda$")) {
            // Method reference: Class::method or instance::method
            boolean isConstructorRef = "<init>".equals(targetMethod);
            IExpression qualifier = null;

            // Determine the handle tag to distinguish static vs. instance references
            int handleTag = Opcodes.H_INVOKESTATIC;
            if (insn.bsmArgs != null && insn.bsmArgs.length >= 2
                    && insn.bsmArgs[1] instanceof org.objectweb.asm.Handle) {
                handleTag = ((org.objectweb.asm.Handle) insn.bsmArgs[1]).getTag();
            }

            // Instance method reference on a captured receiver (e.g., list::add)
            if (!capturedArgs.isEmpty() && handleTag != Opcodes.H_INVOKESTATIC) {
                qualifier = capturedArgs.get(0);
            }

            push(exprFactory.getMethodReferenceExpression(
                    retIType, qualifier, bootstrapName, targetMethod, isConstructorRef));
        } else {
            // Lambda expression: (args) -> body
            // Extract the functional interface method descriptor for parameter types
            List<String> paramNames = new ArrayList<>();
            List<String> paramTypeDescs = new ArrayList<>();
            if (insn.bsmArgs != null && insn.bsmArgs.length >= 1 && insn.bsmArgs[0] instanceof Type) {
                Type samType = (Type) insn.bsmArgs[0];
                Type[] samArgTypes = samType.getArgumentTypes();
                for (int i = 0; i < samArgTypes.length; i++) {
                    paramNames.add("arg" + i);
                    paramTypeDescs.add(samArgTypes[i].getDescriptor());
                }
            }

            push(exprFactory.getLambdaExpression(retIType, insn.name,
                    paramNames, paramTypeDescs, capturedArgs, null,
                    targetMethod, bootstrapName));
        }
    }


    /**
     * Emit a string concatenation expression from a StringConcatFactory invokedynamic.
     * Parses the recipe template to interleave string literals with dynamic arguments.
     *
     * @param insn         the invokedynamic instruction
     * @param capturedArgs the captured arguments (values to concatenate)
     */
    private void emitStringConcat(InvokeDynamicInsnNode insn, List<IExpression> capturedArgs) {
        IType stringType = typeFactory.getObjectType("java/lang/String");

        // Extract the recipe template from bootstrap arguments
        String recipe = "";
        if (insn.bsmArgs != null && insn.bsmArgs.length >= 1 && insn.bsmArgs[0] instanceof String) {
            recipe = (String) insn.bsmArgs[0];
        }

        // If no recipe, fall back to simple concatenation of arguments
        if (recipe.isEmpty()) {
            IExpression result = null;
            for (IExpression arg : capturedArgs) {
                result = appendToConcat(result, arg, stringType);
            }
            if (result == null) {
                result = exprFactory.getConstantExpression(stringType, "");
            }
            push(result);
            return;
        }

        // Parse the recipe: \u0001 = next dynamic arg, \u0002 = next bsmArgs constant
        IExpression result = null;
        int argIdx = 0;
        int constIdx = 0;
        StringBuilder literal = new StringBuilder();

        for (int ci = 0; ci < recipe.length(); ci++) {
            char ch = recipe.charAt(ci);
            if (ch == RECIPE_ARG_PLACEHOLDER) {
                // Flush accumulated literal text
                if (literal.length() > 0) {
                    IExpression litExpr = exprFactory.getConstantExpression(stringType, literal.toString());
                    result = appendToConcat(result, litExpr, stringType);
                    literal.setLength(0);
                }
                // Insert the next captured argument
                if (argIdx < capturedArgs.size()) {
                    result = appendToConcat(result, capturedArgs.get(argIdx), stringType);
                    argIdx++;
                }
            } else if (ch == RECIPE_CONST_PLACEHOLDER) {
                // Flush accumulated literal text
                if (literal.length() > 0) {
                    IExpression litExpr = exprFactory.getConstantExpression(stringType, literal.toString());
                    result = appendToConcat(result, litExpr, stringType);
                    literal.setLength(0);
                }
                // Insert a constant from bsmArgs (index 1+constIdx, since bsmArgs[0] is the recipe)
                constIdx++;
                if (insn.bsmArgs != null && constIdx < insn.bsmArgs.length) {
                    Object constVal = insn.bsmArgs[constIdx];
                    IExpression constExpr = exprFactory.getConstantExpression(stringType,
                            String.valueOf(constVal));
                    result = appendToConcat(result, constExpr, stringType);
                }
            } else {
                literal.append(ch);
            }
        }

        // Flush remaining literal text
        if (literal.length() > 0) {
            IExpression litExpr = exprFactory.getConstantExpression(stringType, literal.toString());
            result = appendToConcat(result, litExpr, stringType);
        }

        if (result == null) {
            result = exprFactory.getConstantExpression(stringType, "");
        }
        push(result);
    }


    /**
     * Append an expression to a string concatenation chain using the + operator.
     *
     * @param current  the current concatenation expression, or null if this is the first part
     * @param addition the expression to append
     * @param stringType the String type
     * @return the concatenation expression
     */
    private IExpression appendToConcat(IExpression current, IExpression addition, IType stringType) {
        if (current == null) {
            return addition;
        }
        return exprFactory.getBinaryExpression(stringType, current, addition, BinaryOperator.ADD);
    }


    /**
     * Process type instructions (new, checkcast, instanceof, anewarray).
     *
     * @param insn the instruction
     */
    private void processTypeInsn(TypeInsnNode insn) {
        IType targetType = typeFactory.getObjectType(insn.desc);

        switch (insn.getOpcode()) {
            case Opcodes.NEW:
                // Push a placeholder; actual construction happens at invokespecial <init>
                push(exprFactory.getNewExpression(targetType, targetType, new ArrayList<>()));
                break;
            case Opcodes.CHECKCAST:
                IExpression castObj = safePop();
                push(exprFactory.getCastExpression(targetType, castObj, targetType));
                break;
            case Opcodes.INSTANCEOF:
                IExpression checkObj = safePop();
                push(exprFactory.getInstanceOfExpression(
                        typeFactory.getPrimitiveType('Z'), checkObj, targetType, null));
                break;
            case Opcodes.ANEWARRAY:
                IExpression size = safePop();
                push(exprFactory.getNewArrayExpression(
                        typeFactory.getArrayType(targetType, 1), targetType,
                        java.util.Collections.singletonList(size), null));
                break;
            default:
                break;
        }
    }


    /**
     * Process LDC (load constant) instruction.
     *
     * @param insn the instruction
     */
    private void processLdcInsn(LdcInsnNode insn) {
        Object value = insn.cst;
        IType constType;
        if (value instanceof Integer) {
            constType = typeFactory.getPrimitiveType('I');
        } else if (value instanceof Long) {
            constType = typeFactory.getPrimitiveType('J');
        } else if (value instanceof Float) {
            constType = typeFactory.getPrimitiveType('F');
        } else if (value instanceof Double) {
            constType = typeFactory.getPrimitiveType('D');
        } else if (value instanceof String) {
            constType = typeFactory.getObjectType("java/lang/String");
        } else if (value instanceof Type) {
            constType = typeFactory.getObjectType("java/lang/Class");
            value = ((Type) value).getClassName();
        } else {
            constType = typeFactory.getObjectType("java/lang/Object");
        }
        push(exprFactory.getConstantExpression(constType, value));
    }


    /**
     * Process iinc (increment local variable) instruction.
     *
     * @param insn the instruction
     */
    private void processIincInsn(IincInsnNode insn) {
        int slot = insn.var;
        if (slot < locals.length && locals[slot] != null) {
            if (insn.incr == 1) {
                IExpression inc = exprFactory.getUnaryExpression(
                        typeFactory.getPrimitiveType('I'), locals[slot], UnaryOperator.POST_INC);
                statements.add(stmtFactory.getExpressionStatement(null, inc));
            } else if (insn.incr == -1) {
                IExpression dec = exprFactory.getUnaryExpression(
                        typeFactory.getPrimitiveType('I'), locals[slot], UnaryOperator.POST_DEC);
                statements.add(stmtFactory.getExpressionStatement(null, dec));
            } else if (insn.incr > 0) {
                IExpression addExpr = exprFactory.getBinaryExpression(
                        typeFactory.getPrimitiveType('I'), locals[slot],
                        exprFactory.getConstantExpression(typeFactory.getPrimitiveType('I'), insn.incr),
                        BinaryOperator.ADD);
                IExpression assign = exprFactory.getAssignmentExpression(
                        typeFactory.getPrimitiveType('I'), locals[slot], addExpr);
                statements.add(stmtFactory.getExpressionStatement(null, assign));
            } else {
                IExpression subExpr = exprFactory.getBinaryExpression(
                        typeFactory.getPrimitiveType('I'), locals[slot],
                        exprFactory.getConstantExpression(typeFactory.getPrimitiveType('I'), -insn.incr),
                        BinaryOperator.SUB);
                IExpression assign = exprFactory.getAssignmentExpression(
                        typeFactory.getPrimitiveType('I'), locals[slot], subExpr);
                statements.add(stmtFactory.getExpressionStatement(null, assign));
            }
        }
    }


    /**
     * Process a binary operation: pop two values, push result.
     *
     * @param op the operator
     */
    private void processBinaryOp(BinaryOperator op) {
        IExpression right = safePop();
        IExpression left = safePop();
        IType resultType = left.getType();
        if (resultType == null) {
            resultType = typeFactory.getPrimitiveType('I');
        }
        push(exprFactory.getBinaryExpression(resultType, left, right, op));
    }


    /**
     * Process a unary operation.
     *
     * @param op the operator
     */
    private void processUnaryOp(UnaryOperator op) {
        IExpression operand = safePop();
        IType resultType = operand.getType();
        if (resultType == null) {
            resultType = typeFactory.getPrimitiveType('I');
        }
        push(exprFactory.getUnaryExpression(resultType, operand, op));
    }


    /**
     * Emit a return statement with the top-of-stack value.
     */
    private void emitReturnWithValue() {
        IExpression value = toBooleanIfNeeded(safePop());
        statements.add(stmtFactory.getReturnStatement(null, value));
    }


    /**
     * Process a comparison instruction (lcmp, fcmpl, dcmpl, etc.).
     * Pops two values, pushes a comparison expression.
     */
    private void processComparison() {
        IExpression right = safePop();
        IExpression left = safePop();
        // Comparison result is used by subsequent ifeq/ifne etc.
        // Push a subtraction-like expression as a placeholder for the comparison
        push(exprFactory.getBinaryExpression(
                typeFactory.getPrimitiveType('I'), left, right, BinaryOperator.SUB));
    }


    /**
     * Emit a throw statement.
     */
    private void emitThrow() {
        IExpression value = safePop();
        statements.add(stmtFactory.getThrowStatement(null, value));
    }


    /**
     * Emit a pop (expression statement if top of stack is a method call).
     */
    private void emitPop() {
        if (!stack.isEmpty()) {
            IExpression top = stack.pop();
            statements.add(stmtFactory.getExpressionStatement(null, top));
        }
    }


    /**
     * Process array load: pop index and array, push element access.
     */
    private void processArrayLoad() {
        IExpression index = safePop();
        IExpression array = safePop();
        push(exprFactory.getArrayAccessExpression(null, array, index));
    }


    /**
     * Process array store: pop value, index, array, emit assignment.
     */
    private void processArrayStore() {
        IExpression value = safePop();
        IExpression index = safePop();
        IExpression array = safePop();
        IExpression arrayAccess = exprFactory.getArrayAccessExpression(null, array, index);
        IExpression assign = exprFactory.getAssignmentExpression(null, arrayAccess, value);
        statements.add(stmtFactory.getExpressionStatement(null, assign));
    }


    /**
     * Process arraylength: pop array, push length field access.
     */
    private void processArrayLength() {
        IExpression array = safePop();
        push(exprFactory.getFieldAccessExpression(
                typeFactory.getPrimitiveType('I'), array, "length", ""));
    }


    /**
     * Convert an ASM Type to our IType.
     *
     * @param asmType the ASM type
     * @return the IType
     */
    private IType asmTypeToIType(Type asmType) {
        switch (asmType.getSort()) {
            case Type.VOID: return typeFactory.getPrimitiveType('V');
            case Type.BOOLEAN: return cachedBoolType;
            case Type.BYTE: return cachedByteType;
            case Type.CHAR: return cachedCharType;
            case Type.SHORT: return cachedShortType;
            case Type.INT: return cachedIntType;
            case Type.LONG: return cachedLongType;
            case Type.FLOAT: return cachedFloatType;
            case Type.DOUBLE: return cachedDoubleType;
            case Type.ARRAY:
                IType elementType = asmTypeToIType(asmType.getElementType());
                return typeFactory.getArrayType(elementType, asmType.getDimensions());
            default:
                return typeFactory.getObjectType(asmType.getInternalName());
        }
    }


    /**
     * Push an expression onto the stack, enforcing a maximum stack depth
     * to guard against unbounded growth from malformed bytecode.
     *
     * @param expr the expression
     */
    private void push(IExpression expr) {
        if (stack.size() >= MAX_STACK_SIZE) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stack size reached limit of " + MAX_STACK_SIZE + ", discarding oldest entry.");
            }
            LOG.warn("Operand stack reached limit of " + MAX_STACK_SIZE + ", discarding oldest entry. The class may contain pathological bytecode.");
            stack.removeLast();
        }
        stack.push(expr);
    }


    /**
     * Safely pop from the stack, returning a placeholder if empty.
     *
     * @return the popped expression
     */
    private IExpression safePop() {
        if (stack.isEmpty()) {
            return exprFactory.getConstantExpression(null, "/* stack underflow */");
        }
        return stack.pop();
    }


    /**
     * Convert an integer constant to a boolean constant if the method returns boolean.
     * Converts 0 to false, non-zero to true.
     *
     * @param expr the expression
     * @return the expression, possibly converted to boolean
     */
    private IExpression toBooleanIfNeeded(IExpression expr) {
        if (!returnsBooleanType) {
            return expr;
        }
        if (expr instanceof com.github.toolarium.java.decompiler.model.ast.expression.IConstantExpression) {
            Object value = ((com.github.toolarium.java.decompiler.model.ast.expression.IConstantExpression) expr).getValue();
            if (value instanceof Integer) {
                boolean boolValue = ((Integer) value) != 0;
                return exprFactory.getConstantExpression(
                        typeFactory.getPrimitiveType('Z'), boolValue);
            }
        }
        return expr;
    }
}
