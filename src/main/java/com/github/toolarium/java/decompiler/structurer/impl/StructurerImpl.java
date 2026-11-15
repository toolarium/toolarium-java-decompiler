/*
 * StructurerImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.structurer.impl;

import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.bytecode.IInstruction;
import com.github.toolarium.java.decompiler.model.controlflow.EdgeType;
import com.github.toolarium.java.decompiler.model.controlflow.IBasicBlock;
import com.github.toolarium.java.decompiler.model.controlflow.IControlFlowGraph;
import com.github.toolarium.java.decompiler.model.controlflow.IEdge;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import com.github.toolarium.java.decompiler.structurer.IStructurer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link IStructurer} that converts a control flow graph
 * into structured AST statements. Handles if/else, while loops, do-while loops,
 * try-catch, and switch statements.
 *
 * @author patrick
 */
public class StructurerImpl implements IStructurer {
    private static final Logger LOG = LoggerFactory.getLogger(StructurerImpl.class);
    private static final String CONDITION_PREFIX = "cond_";
    private static final int MAX_STRUCTURE_DEPTH = 500;
    private static final int MAX_CONVERGENCE_ITERATIONS = 10_000;


    /**
     * Constructor for StructurerImpl.
     */
    public StructurerImpl() {
        // default constructor
    }


    /**
     * Convert a control flow graph into structured AST statements.
     * Handles if/else, while loops, do-while loops, try-catch, and switch statements.
     *
     * @param cfg the control flow graph to structure
     * @return a block statement containing the structured AST
     */
    @Override
    public IBlockStatement structure(IControlFlowGraph cfg) {
        if (cfg.getBlockCount() == 0) {
            return StatementModelFactory.getInstance().getBlockStatement(null, Collections.emptyList());
        }

        // Detect natural loops (back edges)
        Map<Integer, Integer> loopHeaders = detectLoopHeaders(cfg);

        // Pre-build set of loop header block ids for O(1) lookup
        Set<Integer> loopHeaderSet = new HashSet<>(loopHeaders.values());

        // Structure blocks starting from entry
        Set<Integer> visited = new LinkedHashSet<>();
        List<IStatement> statements = structureRegion(cfg, cfg.getEntryBlock(), visited,
                loopHeaders, loopHeaderSet, null, 0);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Structured " + cfg.getBlockCount() + " blocks into " + statements.size() + " statements.");
        }

        return StatementModelFactory.getInstance().getBlockStatement(null, statements);
    }


    /**
     * Structure a region of the CFG starting from a given block.
     *
     * @param cfg           the control flow graph
     * @param startBlock    the block to start from
     * @param visited       set of already visited block ids
     * @param loopHeaders   map from back-edge source to loop header block id
     * @param loopHeaderSet pre-built set of loop header block ids for O(1) lookup
     * @param stopBlock     block id to stop at (exclusive), or null to process all reachable
     * @param depth         current recursion depth
     * @return list of structured statements
     */
    private List<IStatement> structureRegion(IControlFlowGraph cfg, IBasicBlock startBlock,
                                              Set<Integer> visited, Map<Integer, Integer> loopHeaders,
                                              Set<Integer> loopHeaderSet, Integer stopBlock, int depth) {
        List<IStatement> statements = new ArrayList<>();

        if (depth >= MAX_STRUCTURE_DEPTH) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Structuring depth limit reached at " + MAX_STRUCTURE_DEPTH);
            }
            return statements;
        }

        IBasicBlock current = startBlock;

        while (current != null) {
            int blockId = current.getId();

            if (visited.contains(blockId)) {
                break;
            }
            if (stopBlock != null && blockId == stopBlock) {
                break;
            }

            visited.add(blockId);

            // Check if this block is a loop header
            if (loopHeaderSet.contains(blockId)) {
                IStatement loopStmt = structureLoop(cfg, current, visited, loopHeaders,
                        loopHeaderSet, depth + 1);
                statements.add(loopStmt);
                current = findLoopFollowBlock(current, visited, cfg);
                continue;
            }

            // Check if this block is a try-catch handler entry
            if (current.isExceptionHandler()) {
                // Exception handler blocks are handled when structuring the try region
                break;
            }

            // Check for conditional branch (if/else)
            IEdge jumpEdge = findEdgeOfType(current, EdgeType.JUMP);
            IEdge fallEdge = findEdgeOfType(current, EdgeType.FALL_THROUGH);

            if (jumpEdge != null && fallEdge != null) {
                // Conditional branch: if/else structure
                IStatement ifStmt = structureConditional(cfg, current, jumpEdge, fallEdge,
                        visited, loopHeaders, loopHeaderSet, depth + 1);
                statements.add(ifStmt);
                current = findConvergencePoint(jumpEdge.getTarget(), fallEdge.getTarget(), cfg, visited);
                continue;
            }

            // Check for switch
            List<IEdge> switchEdges = findEdgesOfType(current, EdgeType.SWITCH_CASE);
            if (!switchEdges.isEmpty()) {
                IStatement switchStmt = structureSwitch(current, switchEdges, cfg, visited,
                        loopHeaders, loopHeaderSet, depth + 1);
                statements.add(switchStmt);
                current = null;
                continue;
            }

            // Check for exception edges (try block)
            List<IEdge> exceptionEdges = findEdgesOfType(current, EdgeType.EXCEPTION);
            if (!exceptionEdges.isEmpty()) {
                IStatement tryStmt = structureTryCatch(current, exceptionEdges, cfg, visited,
                        loopHeaders, loopHeaderSet, depth + 1);
                statements.add(tryStmt);
                current = findNextUnvisited(current, cfg, visited);
                continue;
            }

            // Linear block: emit instructions as expression statements
            emitBlockInstructions(current, statements);

            // Follow fall-through or unconditional jump
            if (fallEdge != null) {
                current = fallEdge.getTarget();
            } else if (jumpEdge != null) {
                current = jumpEdge.getTarget();
            } else {
                // Return or end of method
                emitReturnIfNeeded(current, statements);
                current = null;
            }
        }

        return statements;
    }


    /**
     * Detect loop headers by finding back edges in the CFG.
     * A back edge is an edge from a block to a dominating block.
     *
     * @param cfg the control flow graph
     * @return map from back-edge source block id to loop header block id
     */
    private Map<Integer, Integer> detectLoopHeaders(IControlFlowGraph cfg) {
        Map<Integer, Integer> loopHeaders = new HashMap<>();
        Set<Integer> visiting = new HashSet<>();
        Set<Integer> visited = new HashSet<>();

        detectLoopHeadersDfs(cfg.getEntryBlock(), visiting, visited, loopHeaders, 0);
        return loopHeaders;
    }


    /**
     * DFS to find back edges (loops), with depth guard.
     *
     * @param block       the current block
     * @param visiting    blocks currently on the DFS stack
     * @param visited     fully visited blocks
     * @param loopHeaders result map
     * @param depth       current recursion depth
     */
    private void detectLoopHeadersDfs(IBasicBlock block, Set<Integer> visiting,
                                      Set<Integer> visited, Map<Integer, Integer> loopHeaders,
                                      int depth) {
        if (depth >= MAX_STRUCTURE_DEPTH) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Loop detection DFS depth limit reached at " + MAX_STRUCTURE_DEPTH);
            }
            return;
        }

        int blockId = block.getId();
        if (visited.contains(blockId)) {
            return;
        }
        visiting.add(blockId);

        for (IEdge edge : block.getOutEdges()) {
            if (edge.getType() == EdgeType.EXCEPTION) {
                continue;
            }
            int targetId = edge.getTarget().getId();
            if (visiting.contains(targetId)) {
                loopHeaders.put(blockId, targetId);
            } else if (!visited.contains(targetId)) {
                detectLoopHeadersDfs(edge.getTarget(), visiting, visited, loopHeaders, depth + 1);
            }
        }

        visiting.remove(blockId);
        visited.add(blockId);
    }


    /**
     * Structure a loop starting at the given header block.
     *
     * @param cfg           the control flow graph
     * @param header        the loop header block
     * @param visited       visited blocks
     * @param loopHeaders   the loop header map
     * @param loopHeaderSet pre-built set of loop header block ids
     * @param depth         current recursion depth
     * @return the loop statement (while or do-while)
     */
    private IStatement structureLoop(IControlFlowGraph cfg, IBasicBlock header,
                                     Set<Integer> visited, Map<Integer, Integer> loopHeaders,
                                     Set<Integer> loopHeaderSet, int depth) {
        // Create a condition expression from the header block
        IExpression condition = createConditionExpression(header);

        // Mark header as visited
        visited.add(header.getId());

        // Find the body: follow the fall-through edge from header
        IEdge fallEdge = findEdgeOfType(header, EdgeType.FALL_THROUGH);
        IEdge jumpEdge = findEdgeOfType(header, EdgeType.JUMP);

        List<IStatement> bodyStatements = new ArrayList<>();
        if (fallEdge != null && jumpEdge != null) {
            IBasicBlock bodyStart = fallEdge.getTarget();
            bodyStatements = structureRegion(cfg, bodyStart, visited, loopHeaders,
                    loopHeaderSet, null, depth);
        } else if (fallEdge != null) {
            IBasicBlock bodyStart = fallEdge.getTarget();
            bodyStatements = structureRegion(cfg, bodyStart, visited, loopHeaders,
                    loopHeaderSet, null, depth);
        }

        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        IBlockStatement body = stmtFactory.getBlockStatement(null, bodyStatements);
        return stmtFactory.getWhileStatement(null, condition, body);
    }


    /**
     * Structure a conditional (if/else) from a block with both jump and fall-through edges.
     *
     * @param cfg           the control flow graph
     * @param block         the conditional block
     * @param jumpEdge      the jump edge (condition true target)
     * @param fallEdge      the fall-through edge (condition false target)
     * @param visited       visited blocks
     * @param loopHeaders   loop header map
     * @param loopHeaderSet pre-built set of loop header block ids
     * @param depth         current recursion depth
     * @return the if statement
     */
    private IStatement structureConditional(IControlFlowGraph cfg, IBasicBlock block,
                                            IEdge jumpEdge, IEdge fallEdge,
                                            Set<Integer> visited, Map<Integer, Integer> loopHeaders,
                                            Set<Integer> loopHeaderSet, int depth) {
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();

        IExpression condition = createConditionExpression(block);

        // Structure then and else branches
        IBasicBlock thenStart = jumpEdge.getTarget();
        IBasicBlock elseStart = fallEdge.getTarget();

        List<IStatement> thenStatements = structureRegion(cfg, thenStart, visited, loopHeaders,
                loopHeaderSet, null, depth);
        List<IStatement> elseStatements = structureRegion(cfg, elseStart, visited, loopHeaders,
                loopHeaderSet, null, depth);

        IBlockStatement thenBlock = stmtFactory.getBlockStatement(null, thenStatements);

        IStatement elseBlock = null;
        if (!elseStatements.isEmpty()) {
            elseBlock = stmtFactory.getBlockStatement(null, elseStatements);
        }

        return stmtFactory.getIfStatement(null, condition, thenBlock, elseBlock);
    }


    /**
     * Structure a switch statement from switch case edges.
     *
     * @param block         the switch block
     * @param switchEdges   the switch case edges
     * @param cfg           the control flow graph
     * @param visited       visited blocks
     * @param loopHeaders   loop header map
     * @param loopHeaderSet pre-built set of loop header block ids
     * @param depth         current recursion depth
     * @return the switch statement
     */
    private IStatement structureSwitch(IBasicBlock block, List<IEdge> switchEdges,
                                       IControlFlowGraph cfg, Set<Integer> visited,
                                       Map<Integer, Integer> loopHeaders,
                                       Set<Integer> loopHeaderSet, int depth) {
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();

        IExpression selector = createConditionExpression(block);
        List<com.github.toolarium.java.decompiler.model.ast.statement.ISwitchCase> cases = new ArrayList<>();

        // Process each case edge
        IEdge defaultEdge = findEdgeOfType(block, EdgeType.SWITCH_DEFAULT);
        for (IEdge edge : switchEdges) {
            List<IExpression> caseValues = new ArrayList<>();
            if (edge.getLabel() != null) {
                IExpression caseValue = exprFactory.getConstantExpression(
                        TypeModelFactory.getInstance().getPrimitiveType('I'),
                        Integer.parseInt(edge.getLabel()));
                caseValues.add(caseValue);
            }

            List<IStatement> caseBody = structureRegion(cfg, edge.getTarget(), visited,
                    loopHeaders, loopHeaderSet, null, depth);
            cases.add(stmtFactory.getSwitchCase(caseValues, false, caseBody));
        }

        // Default case
        if (defaultEdge != null) {
            List<IStatement> defaultBody = structureRegion(cfg, defaultEdge.getTarget(),
                    visited, loopHeaders, loopHeaderSet, null, depth);
            cases.add(stmtFactory.getSwitchCase(Collections.emptyList(), true, defaultBody));
        }

        return stmtFactory.getSwitchStatement(null, selector, cases);
    }


    /**
     * Structure a try-catch from exception edges.
     *
     * @param block          the try block
     * @param exceptionEdges the exception edges
     * @param cfg            the control flow graph
     * @param visited        visited blocks
     * @param loopHeaders    loop header map
     * @param loopHeaderSet  pre-built set of loop header block ids
     * @param depth          current recursion depth
     * @return the try-catch statement
     */
    private IStatement structureTryCatch(IBasicBlock block, List<IEdge> exceptionEdges,
                                         IControlFlowGraph cfg, Set<Integer> visited,
                                         Map<Integer, Integer> loopHeaders,
                                         Set<Integer> loopHeaderSet, int depth) {
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        TypeModelFactory typeFactory = TypeModelFactory.getInstance();

        // Try body: the current block's instructions
        List<IStatement> tryStatements = new ArrayList<>();
        emitBlockInstructions(block, tryStatements);
        IBlockStatement tryBody = stmtFactory.getBlockStatement(null, tryStatements);

        // Catch clauses
        List<com.github.toolarium.java.decompiler.model.ast.statement.ICatchClause> catchClauses = new ArrayList<>();
        IStatement finallyBody = null;

        for (IEdge edge : exceptionEdges) {
            IBasicBlock handlerBlock = edge.getTarget();
            if (visited.contains(handlerBlock.getId())) {
                continue;
            }

            List<IStatement> handlerStatements = structureRegion(cfg, handlerBlock, visited,
                    loopHeaders, loopHeaderSet, null, depth);
            IBlockStatement handlerBody = stmtFactory.getBlockStatement(null, handlerStatements);

            String catchType = handlerBlock.getCatchType();
            if (catchType == null) {
                finallyBody = handlerBody;
            } else {
                List<com.github.toolarium.java.decompiler.model.type.IType> exceptionTypes = new ArrayList<>();
                exceptionTypes.add(typeFactory.getObjectType(catchType));
                catchClauses.add(stmtFactory.getCatchClause(exceptionTypes, "ex", handlerBody));
            }
        }

        return stmtFactory.getTryCatchStatement(null, tryBody, catchClauses, finallyBody);
    }


    /**
     * Emit the instructions of a basic block as expression statements.
     *
     * @param block      the basic block
     * @param statements the statement list to add to
     */
    private void emitBlockInstructions(IBasicBlock block, List<IStatement> statements) {
        ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();

        for (IInstruction instruction : block.getInstructions()) {
            // Create a placeholder expression for the instruction
            IExpression expr = exprFactory.getConstantExpression(null,
                    instruction.getMnemonic());
            statements.add(stmtFactory.getExpressionStatement(null, expr));
        }
    }


    /**
     * Emit a return statement if the block ends with a return instruction.
     *
     * @param block      the basic block
     * @param statements the statement list to add to
     */
    private void emitReturnIfNeeded(IBasicBlock block, List<IStatement> statements) {
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();

        if (!block.getInstructions().isEmpty()) {
            IInstruction lastInsn = block.getInstructions().get(block.getInstructions().size() - 1);
            String mnemonic = lastInsn.getMnemonic();
            if ("return".equals(mnemonic) || "ireturn".equals(mnemonic)
                    || "lreturn".equals(mnemonic) || "freturn".equals(mnemonic)
                    || "dreturn".equals(mnemonic) || "areturn".equals(mnemonic)) {
                statements.add(stmtFactory.getReturnStatement(null, null));
            }
        }
    }


    /**
     * Create a condition expression for a block (based on its last instruction).
     *
     * @param block the block
     * @return a condition expression
     */
    private IExpression createConditionExpression(IBasicBlock block) {
        ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();

        if (!block.getInstructions().isEmpty()) {
            IInstruction lastInsn = block.getInstructions().get(block.getInstructions().size() - 1);
            // Create a symbolic condition placeholder
            return exprFactory.getVariableExpression(
                    TypeModelFactory.getInstance().getPrimitiveType('Z'),
                    CONDITION_PREFIX + block.getId(), 0);
        }

        // Default true condition
        return exprFactory.getConstantExpression(
                TypeModelFactory.getInstance().getPrimitiveType('Z'), Boolean.TRUE);
    }


    /**
     * Find the first edge of a given type from a block.
     *
     * @param block the block
     * @param type  the edge type
     * @return the edge, or null
     */
    private IEdge findEdgeOfType(IBasicBlock block, EdgeType type) {
        for (IEdge edge : block.getOutEdges()) {
            if (edge.getType() == type) {
                return edge;
            }
        }
        return null;
    }


    /**
     * Find all edges of a given type from a block.
     *
     * @param block the block
     * @param type  the edge type
     * @return list of matching edges
     */
    private List<IEdge> findEdgesOfType(IBasicBlock block, EdgeType type) {
        List<IEdge> result = new ArrayList<>();
        for (IEdge edge : block.getOutEdges()) {
            if (edge.getType() == type) {
                result.add(edge);
            }
        }
        return result;
    }


    /**
     * Find a convergence point where two branches meet.
     * Uses alternating BFS from both branches: the first unvisited block
     * reachable from both sides is the convergence point.
     *
     * @param thenTarget the then branch target
     * @param elseTarget the else branch target
     * @param cfg        the control flow graph
     * @param visited    visited blocks
     * @return the convergence block, or null
     */
    private IBasicBlock findConvergencePoint(IBasicBlock thenTarget, IBasicBlock elseTarget,
                                              IControlFlowGraph cfg, Set<Integer> visited) {
        Set<Integer> thenReachable = new HashSet<>();
        Set<Integer> elseReachable = new HashSet<>();
        java.util.Queue<IBasicBlock> thenQueue = new java.util.LinkedList<>();
        java.util.Queue<IBasicBlock> elseQueue = new java.util.LinkedList<>();

        if (!visited.contains(thenTarget.getId())) {
            thenQueue.add(thenTarget);
            thenReachable.add(thenTarget.getId());
        }
        if (!visited.contains(elseTarget.getId())) {
            elseQueue.add(elseTarget);
            elseReachable.add(elseTarget.getId());
        }

        if (thenReachable.contains(elseTarget.getId())) {
            return elseTarget;
        }
        if (elseReachable.contains(thenTarget.getId())) {
            return thenTarget;
        }

        int iterations = 0;
        while (!thenQueue.isEmpty() || !elseQueue.isEmpty()) {
            iterations++;
            if (iterations > MAX_CONVERGENCE_ITERATIONS) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Convergence point search exceeded maximum iterations.");
                }
                return null;
            }
            if (!thenQueue.isEmpty()) {
                IBasicBlock current = thenQueue.poll();
                for (IEdge edge : current.getOutEdges()) {
                    int succId = edge.getTarget().getId();
                    if (!visited.contains(succId) && thenReachable.add(succId)) {
                        if (elseReachable.contains(succId)) {
                            return edge.getTarget();
                        }
                        thenQueue.add(edge.getTarget());
                    }
                }
            }
            if (!elseQueue.isEmpty()) {
                IBasicBlock current = elseQueue.poll();
                for (IEdge edge : current.getOutEdges()) {
                    int succId = edge.getTarget().getId();
                    if (!visited.contains(succId) && elseReachable.add(succId)) {
                        if (thenReachable.contains(succId)) {
                            return edge.getTarget();
                        }
                        elseQueue.add(edge.getTarget());
                    }
                }
            }
        }

        // Fallback: first unvisited block with multiple predecessors
        for (IBasicBlock block : cfg.getBlocks()) {
            if (!visited.contains(block.getId()) && block.getPredecessors().size() >= 2) {
                return block;
            }
        }
        return null;
    }


    /**
     * Find the next unvisited block reachable from the given block.
     *
     * @param block   the current block
     * @param cfg     the control flow graph
     * @param visited visited blocks
     * @return the next unvisited block, or null
     */
    private IBasicBlock findNextUnvisited(IBasicBlock block, IControlFlowGraph cfg, Set<Integer> visited) {
        for (IEdge edge : block.getOutEdges()) {
            if (edge.getType() != EdgeType.EXCEPTION && !visited.contains(edge.getTarget().getId())) {
                return edge.getTarget();
            }
        }
        return null;
    }


    /**
     * Find the block after a loop (first unvisited successor).
     *
     * @param header  the loop header
     * @param visited visited blocks
     * @param cfg     the control flow graph
     * @return the follow block, or null
     */
    private IBasicBlock findLoopFollowBlock(IBasicBlock header, Set<Integer> visited, IControlFlowGraph cfg) {
        // The loop exit is the jump target from the header that's not the body
        IEdge jumpEdge = findEdgeOfType(header, EdgeType.JUMP);
        if (jumpEdge != null && !visited.contains(jumpEdge.getTarget().getId())) {
            return jumpEdge.getTarget();
        }
        IEdge fallEdge = findEdgeOfType(header, EdgeType.FALL_THROUGH);
        if (fallEdge != null && !visited.contains(fallEdge.getTarget().getId())) {
            return fallEdge.getTarget();
        }
        return null;
    }
}
