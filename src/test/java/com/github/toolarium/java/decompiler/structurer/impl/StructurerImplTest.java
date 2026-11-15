/*
 * StructurerImplTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.structurer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.controlflow.ControlFlowBuilderFactory;
import com.github.toolarium.java.decompiler.controlflow.IControlFlowBuilder;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IIfStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ITryCatchStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IWhileStatement;
import com.github.toolarium.java.decompiler.model.controlflow.IControlFlowGraph;
import com.github.toolarium.java.decompiler.structurer.IStructurer;
import com.github.toolarium.java.decompiler.structurer.StructurerFactory;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;


/**
 * Tests for {@link StructurerImpl}.
 *
 * @author patrick
 */
public class StructurerImplTest {
    private static final String DESC_VOID = "()V";
    private final IControlFlowBuilder cfgBuilder = ControlFlowBuilderFactory.getInstance().getCfgBuilder();
    private final IStructurer structurer = StructurerFactory.getInstance().getStructurer();


    /**
     * Test structuring a linear method.
     */
    @Test
    void testLinearMethod() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "linear", DESC_VOID, null, null);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 1);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        IBlockStatement result = structurer.structure(cfg);

        assertNotNull(result);
        assertFalse(result.getStatements().isEmpty());
    }


    /**
     * Test structuring a conditional method produces an if statement.
     */
    @Test
    void testConditionalProducesIfStatement() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "conditional", "(I)V", null, null);
        Label elseLabel = new Label();
        Label endLabel = new Label();

        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitJumpInsn(Opcodes.IFEQ, elseLabel);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);
        method.visitLabel(elseLabel);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitLabel(endLabel);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 2);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        IBlockStatement result = structurer.structure(cfg);

        assertNotNull(result);
        assertFalse(result.getStatements().isEmpty());

        // Should contain an if statement somewhere
        boolean hasIf = false;
        for (IStatement stmt : result.getStatements()) {
            if (stmt instanceof IIfStatement) {
                hasIf = true;
                IIfStatement ifStmt = (IIfStatement) stmt;
                assertNotNull(ifStmt.getCondition());
                assertNotNull(ifStmt.getThenBranch());
            }
        }
        assertTrue(hasIf, "Expected an if statement in the structured output");
    }


    /**
     * Test structuring a loop produces a while statement.
     */
    @Test
    void testLoopProducesWhileStatement() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "loop", DESC_VOID, null, null);
        Label loopStart = new Label();
        Label loopEnd = new Label();

        method.visitInsn(Opcodes.ICONST_0);
        method.visitVarInsn(Opcodes.ISTORE, 1);
        method.visitLabel(loopStart);
        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitIntInsn(Opcodes.BIPUSH, 10);
        method.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);
        method.visitIincInsn(1, 1);
        method.visitJumpInsn(Opcodes.GOTO, loopStart);
        method.visitLabel(loopEnd);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(2, 2);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        IBlockStatement result = structurer.structure(cfg);

        assertNotNull(result);
        assertFalse(result.getStatements().isEmpty());

        // Should contain a while statement
        boolean hasWhile = false;
        for (IStatement stmt : result.getStatements()) {
            if (stmt instanceof IWhileStatement) {
                hasWhile = true;
                IWhileStatement whileStmt = (IWhileStatement) stmt;
                assertNotNull(whileStmt.getCondition());
                assertNotNull(whileStmt.getBody());
            }
        }
        assertTrue(hasWhile, "Expected a while statement in the structured output");
    }


    /**
     * Test structuring a try-catch produces a try-catch statement.
     */
    @Test
    void testTryCatchProducesTryCatchStatement() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "tryCatch", DESC_VOID, null, null);
        Label tryStart = new Label();
        Label tryEnd = new Label();
        Label catchHandler = new Label();
        Label endLabel = new Label();

        method.visitTryCatchBlock(tryStart, tryEnd, catchHandler, "java/lang/Exception");

        method.visitLabel(tryStart);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitLabel(tryEnd);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);
        method.visitLabel(catchHandler);
        method.visitVarInsn(Opcodes.ASTORE, 1);
        method.visitLabel(endLabel);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 2);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        IBlockStatement result = structurer.structure(cfg);

        assertNotNull(result);
        assertFalse(result.getStatements().isEmpty());

        // Should contain a try-catch statement
        boolean hasTryCatch = false;
        for (IStatement stmt : result.getStatements()) {
            if (stmt instanceof ITryCatchStatement) {
                hasTryCatch = true;
                ITryCatchStatement tryCatch = (ITryCatchStatement) stmt;
                assertNotNull(tryCatch.getTryBody());
                assertFalse(tryCatch.getCatchClauses().isEmpty());
                assertEquals("java/lang/Exception",
                        tryCatch.getCatchClauses().get(0).getExceptionTypes().get(0).getDescriptor()
                                .replace("L", "").replace(";", ""));
            }
        }
        assertTrue(hasTryCatch, "Expected a try-catch statement in the structured output");
    }


    /**
     * Test structuring an empty method.
     */
    @Test
    void testEmptyMethod() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "empty", DESC_VOID, null, null);
        method.visitMaxs(0, 1);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        IBlockStatement result = structurer.structure(cfg);

        assertNotNull(result);
    }


    /**
     * Test structuring a switch statement with tableswitch bytecode.
     */
    @Test
    void testSwitchProducesSwitchStatement() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "switchTest", "(I)V", null, null);
        Label case0 = new Label();
        Label case1 = new Label();
        Label dflt = new Label();
        Label endLabel = new Label();

        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitTableSwitchInsn(0, 1, dflt, case0, case1);

        method.visitLabel(case0);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);

        method.visitLabel(case1);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);

        method.visitLabel(dflt);
        method.visitVarInsn(Opcodes.ALOAD, 0);

        method.visitLabel(endLabel);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 2);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        IBlockStatement result = structurer.structure(cfg);

        assertNotNull(result);
        assertFalse(result.getStatements().isEmpty());

        boolean hasSwitch = false;
        for (IStatement stmt : result.getStatements()) {
            if (stmt instanceof ISwitchStatement) {
                hasSwitch = true;
                ISwitchStatement switchStmt = (ISwitchStatement) stmt;
                assertNotNull(switchStmt.getSelector());
                assertFalse(switchStmt.getCases().isEmpty());
            }
        }
        assertTrue(hasSwitch, "Expected a switch statement in the structured output");
    }


    /**
     * Test structuring nested control flow (if inside a loop).
     */
    @Test
    void testNestedIfInsideLoop() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "nested", "(I)V", null, null);
        Label loopStart = new Label();
        Label loopEnd = new Label();
        Label elseLabel = new Label();
        Label endIf = new Label();

        method.visitInsn(Opcodes.ICONST_0);
        method.visitVarInsn(Opcodes.ISTORE, 2);

        method.visitLabel(loopStart);
        method.visitVarInsn(Opcodes.ILOAD, 2);
        method.visitIntInsn(Opcodes.BIPUSH, 10);
        method.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);

        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitJumpInsn(Opcodes.IFEQ, elseLabel);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitJumpInsn(Opcodes.GOTO, endIf);

        method.visitLabel(elseLabel);
        method.visitVarInsn(Opcodes.ALOAD, 0);

        method.visitLabel(endIf);
        method.visitIincInsn(2, 1);
        method.visitJumpInsn(Opcodes.GOTO, loopStart);

        method.visitLabel(loopEnd);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(2, 3);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        IBlockStatement result = structurer.structure(cfg);

        assertNotNull(result);
        assertFalse(result.getStatements().isEmpty());

        boolean hasWhile = false;
        for (IStatement stmt : result.getStatements()) {
            if (stmt instanceof IWhileStatement) {
                hasWhile = true;
            }
        }
        assertTrue(hasWhile, "Expected a while statement for the outer loop");
    }


    /**
     * Test structuring a try-catch with a finally block (null exception type).
     */
    @Test
    void testTryCatchFinally() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "tryCatchFinally", DESC_VOID, null, null);
        Label tryStart = new Label();
        Label tryEnd = new Label();
        Label catchHandler = new Label();
        Label finallyHandler = new Label();
        Label endLabel = new Label();

        method.visitTryCatchBlock(tryStart, tryEnd, catchHandler, "java/lang/Exception");
        method.visitTryCatchBlock(tryStart, tryEnd, finallyHandler, null);

        method.visitLabel(tryStart);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitLabel(tryEnd);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);

        method.visitLabel(catchHandler);
        method.visitVarInsn(Opcodes.ASTORE, 1);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);

        method.visitLabel(finallyHandler);
        method.visitVarInsn(Opcodes.ASTORE, 2);

        method.visitLabel(endLabel);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 3);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        IBlockStatement result = structurer.structure(cfg);

        assertNotNull(result);
        assertFalse(result.getStatements().isEmpty());

        boolean hasTryCatch = false;
        for (IStatement stmt : result.getStatements()) {
            if (stmt instanceof ITryCatchStatement) {
                hasTryCatch = true;
                ITryCatchStatement tryStmt = (ITryCatchStatement) stmt;
                assertNotNull(tryStmt.getTryBody());
            }
        }
        assertTrue(hasTryCatch, "Expected a try-catch statement");
    }


    /**
     * Test structuring a method with multiple sequential branches.
     */
    @Test
    void testMultipleSequentialBranches() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "multiBranch", "(II)V", null, null);
        Label else1 = new Label();
        Label end1 = new Label();
        Label else2 = new Label();
        Label end2 = new Label();

        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitJumpInsn(Opcodes.IFEQ, else1);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitJumpInsn(Opcodes.GOTO, end1);
        method.visitLabel(else1);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitLabel(end1);

        method.visitVarInsn(Opcodes.ILOAD, 2);
        method.visitJumpInsn(Opcodes.IFEQ, else2);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitJumpInsn(Opcodes.GOTO, end2);
        method.visitLabel(else2);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitLabel(end2);

        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 3);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        IBlockStatement result = structurer.structure(cfg);

        assertNotNull(result);
        assertFalse(result.getStatements().isEmpty());

        int ifCount = 0;
        for (IStatement stmt : result.getStatements()) {
            if (stmt instanceof IIfStatement) {
                ifCount++;
            }
        }
        assertTrue(ifCount >= 1, "Expected at least one if statement for sequential branches");
    }


    /**
     * Test structuring a method with only a return instruction produces a valid result.
     * This exercises the structurer on a minimal CFG.
     */
    @Test
    void testStructureMinimalReturnOnly() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "minimal", DESC_VOID, null, null);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(0, 1);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        IBlockStatement result = structurer.structure(cfg);

        assertNotNull(result);
        assertFalse(result.getStatements().isEmpty());
    }


    /**
     * Test structuring a deeply branching method does not crash.
     * Exercises the MAX_STRUCTURE_DEPTH guard at 500.
     */
    @Test
    void testStructureDeepNesting() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "deep", "(I)V", null, null);

        Label[] elseLabels = new Label[50];
        Label[] endLabels = new Label[50];
        for (int i = 0; i < 50; i++) {
            elseLabels[i] = new Label();
            endLabels[i] = new Label();
        }

        for (int i = 0; i < 50; i++) {
            method.visitVarInsn(Opcodes.ILOAD, 1);
            method.visitJumpInsn(Opcodes.IFEQ, elseLabels[i]);
        }

        method.visitInsn(Opcodes.RETURN);

        for (int i = 49; i >= 0; i--) {
            method.visitLabel(elseLabels[i]);
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitLabel(endLabels[i]);
        }

        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(2, 2);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        IBlockStatement result = structurer.structure(cfg);

        assertNotNull(result);
    }


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        StructurerFactory factory1 = StructurerFactory.getInstance();
        StructurerFactory factory2 = StructurerFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }
}
