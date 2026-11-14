/*
 * ControlFlowBuilderImplTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.controlflow.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.controlflow.ControlFlowBuilderFactory;
import com.github.toolarium.java.decompiler.controlflow.IControlFlowBuilder;
import com.github.toolarium.java.decompiler.model.controlflow.EdgeType;
import com.github.toolarium.java.decompiler.model.controlflow.IBasicBlock;
import com.github.toolarium.java.decompiler.model.controlflow.IControlFlowGraph;
import com.github.toolarium.java.decompiler.model.controlflow.IEdge;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;


/**
 * Tests for {@link ControlFlowBuilderImpl}.
 *
 * @author patrick
 */
public class ControlFlowBuilderImplTest {
    private final IControlFlowBuilder builder = ControlFlowBuilderFactory.getInstance().getCfgBuilder();


    /**
     * Test CFG for a linear method (no branches).
     */
    @Test
    void testLinearMethod() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "linear", "()V", null, null);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 1);

        IControlFlowGraph cfg = builder.buildCfg(method);

        assertNotNull(cfg);
        assertNotNull(cfg.getEntryBlock());
        assertEquals(1, cfg.getBlockCount());
        assertTrue(cfg.getEdges().isEmpty());
        assertFalse(cfg.getEntryBlock().getInstructions().isEmpty());
    }


    /**
     * Test CFG for a method with a conditional branch (if-else).
     */
    @Test
    void testConditionalBranch() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "conditional", "(I)V", null, null);
        Label elseLabel = new Label();
        Label endLabel = new Label();

        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitJumpInsn(Opcodes.IFEQ, elseLabel);
        // then branch
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);
        // else branch
        method.visitLabel(elseLabel);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        // end
        method.visitLabel(endLabel);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 2);

        IControlFlowGraph cfg = builder.buildCfg(method);

        assertNotNull(cfg);
        assertTrue(cfg.getBlockCount() >= 3, "Expected at least 3 blocks for if-else, got: " + cfg.getBlockCount());
        assertFalse(cfg.getEdges().isEmpty());

        // Should have JUMP and FALL_THROUGH edges
        boolean hasJump = false;
        boolean hasFallThrough = false;
        for (IEdge edge : cfg.getEdges()) {
            if (edge.getType() == EdgeType.JUMP) {
                hasJump = true;
            }
            if (edge.getType() == EdgeType.FALL_THROUGH) {
                hasFallThrough = true;
            }
        }
        assertTrue(hasJump, "Expected JUMP edge");
        assertTrue(hasFallThrough, "Expected FALL_THROUGH edge");
    }


    /**
     * Test CFG for a simple loop.
     */
    @Test
    void testLoop() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "loop", "()V", null, null);
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

        IControlFlowGraph cfg = builder.buildCfg(method);

        assertNotNull(cfg);
        assertTrue(cfg.getBlockCount() >= 3, "Expected at least 3 blocks for loop");

        // Verify back edge exists (a block that jumps back to an earlier block)
        boolean hasBackEdge = false;
        for (IEdge edge : cfg.getEdges()) {
            if (edge.getType() == EdgeType.JUMP) {
                if (edge.getTarget().getStartOffset() <= edge.getSource().getStartOffset()) {
                    hasBackEdge = true;
                }
            }
        }
        assertTrue(hasBackEdge, "Expected a back edge for the loop");
    }


    /**
     * Test CFG for a method with try-catch.
     */
    @Test
    void testTryCatch() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "tryCatch", "()V", null, null);
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

        IControlFlowGraph cfg = builder.buildCfg(method);

        assertNotNull(cfg);

        // Should have EXCEPTION edges
        boolean hasExceptionEdge = false;
        boolean hasHandlerBlock = false;
        for (IEdge edge : cfg.getEdges()) {
            if (edge.getType() == EdgeType.EXCEPTION) {
                hasExceptionEdge = true;
            }
        }
        for (IBasicBlock block : cfg.getBlocks()) {
            if (block.isExceptionHandler()) {
                hasHandlerBlock = true;
                assertEquals("java/lang/Exception", block.getCatchType());
            }
        }
        assertTrue(hasExceptionEdge, "Expected EXCEPTION edge");
        assertTrue(hasHandlerBlock, "Expected an exception handler block");
    }


    /**
     * Test CFG for an empty method.
     */
    @Test
    void testEmptyMethod() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "empty", "()V", null, null);
        method.visitMaxs(0, 1);

        IControlFlowGraph cfg = builder.buildCfg(method);

        assertNotNull(cfg);
        assertNotNull(cfg.getEntryBlock());
    }


    /**
     * Test CFG built from a real JDK class method.
     *
     * @throws IOException if loading fails
     */
    @Test
    void testRealClassMethod() throws IOException {
        ClassNode classNode = loadClassNode("java/lang/Integer");
        MethodNode intValueMethod = null;
        for (MethodNode mn : classNode.methods) {
            if ("intValue".equals(mn.name)) {
                intValueMethod = mn;
                break;
            }
        }
        assertNotNull(intValueMethod, "Could not find intValue method");

        IControlFlowGraph cfg = builder.buildCfg(intValueMethod);
        assertNotNull(cfg);
        assertTrue(cfg.getBlockCount() >= 1);
        assertNotNull(cfg.getEntryBlock());
    }


    /**
     * Test CFG for a tableswitch instruction.
     */
    @Test
    void testTableSwitch() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "tableSwitch", "(I)V", null, null);
        Label case0 = new Label();
        Label case1 = new Label();
        Label case2 = new Label();
        Label dflt = new Label();
        Label endLabel = new Label();

        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitTableSwitchInsn(0, 2, dflt, case0, case1, case2);

        method.visitLabel(case0);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);
        method.visitLabel(case1);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);
        method.visitLabel(case2);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);
        method.visitLabel(dflt);
        method.visitLabel(endLabel);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 2);

        IControlFlowGraph cfg = builder.buildCfg(method);

        assertNotNull(cfg);
        assertTrue(cfg.getBlockCount() >= 4, "Expected at least 4 blocks for tableswitch");

        boolean hasSwitchCase = false;
        boolean hasSwitchDefault = false;
        for (IEdge edge : cfg.getEdges()) {
            if (edge.getType() == EdgeType.SWITCH_CASE) {
                hasSwitchCase = true;
            }
            if (edge.getType() == EdgeType.SWITCH_DEFAULT) {
                hasSwitchDefault = true;
            }
        }
        assertTrue(hasSwitchCase, "Expected SWITCH_CASE edges");
        assertTrue(hasSwitchDefault, "Expected SWITCH_DEFAULT edge");
    }


    /**
     * Test CFG for nested try-catch blocks.
     */
    @Test
    void testNestedTryCatch() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "nestedTryCatch", "()V", null, null);
        Label outerTryStart = new Label();
        Label outerTryEnd = new Label();
        Label outerCatch = new Label();
        Label innerTryStart = new Label();
        Label innerTryEnd = new Label();
        Label innerCatch = new Label();
        Label endLabel = new Label();

        method.visitTryCatchBlock(outerTryStart, outerTryEnd, outerCatch, "java/lang/Exception");
        method.visitTryCatchBlock(innerTryStart, innerTryEnd, innerCatch, "java/lang/RuntimeException");

        method.visitLabel(outerTryStart);
        method.visitLabel(innerTryStart);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitLabel(innerTryEnd);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);

        method.visitLabel(innerCatch);
        method.visitVarInsn(Opcodes.ASTORE, 1);
        method.visitLabel(outerTryEnd);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);

        method.visitLabel(outerCatch);
        method.visitVarInsn(Opcodes.ASTORE, 2);

        method.visitLabel(endLabel);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 3);

        IControlFlowGraph cfg = builder.buildCfg(method);

        assertNotNull(cfg);

        int exceptionEdgeCount = 0;
        int handlerBlockCount = 0;
        for (IEdge edge : cfg.getEdges()) {
            if (edge.getType() == EdgeType.EXCEPTION) {
                exceptionEdgeCount++;
            }
        }
        for (IBasicBlock block : cfg.getBlocks()) {
            if (block.isExceptionHandler()) {
                handlerBlockCount++;
            }
        }
        assertTrue(exceptionEdgeCount >= 2, "Expected at least 2 exception edges for nested try-catch");
        assertTrue(handlerBlockCount >= 2, "Expected at least 2 handler blocks");
    }


    /**
     * Test CFG for a method with multiple return paths.
     */
    @Test
    void testMultipleReturnPaths() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "multiReturn", "(I)I", null, null);
        Label secondReturn = new Label();

        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitJumpInsn(Opcodes.IFEQ, secondReturn);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.IRETURN);

        method.visitLabel(secondReturn);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 2);

        IControlFlowGraph cfg = builder.buildCfg(method);

        assertNotNull(cfg);
        assertTrue(cfg.getBlockCount() >= 2, "Expected at least 2 blocks for multi-return");

        int returnBlockCount = 0;
        for (IBasicBlock block : cfg.getBlocks()) {
            if (block.getOutEdges().isEmpty() && !block.getInstructions().isEmpty()) {
                returnBlockCount++;
            }
        }
        assertTrue(returnBlockCount >= 2, "Expected at least 2 return blocks");
    }


    /**
     * Test CFG for a lookupswitch instruction.
     */
    @Test
    void testLookupSwitch() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "lookupSwitch", "(I)V", null, null);
        Label case10 = new Label();
        Label case20 = new Label();
        Label dflt = new Label();
        Label endLabel = new Label();

        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitLookupSwitchInsn(dflt, new int[]{10, 20}, new Label[]{case10, case20});

        method.visitLabel(case10);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);
        method.visitLabel(case20);
        method.visitJumpInsn(Opcodes.GOTO, endLabel);
        method.visitLabel(dflt);
        method.visitLabel(endLabel);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 2);

        IControlFlowGraph cfg = builder.buildCfg(method);

        assertNotNull(cfg);

        boolean hasSwitchCase = false;
        boolean hasSwitchDefault = false;
        for (IEdge edge : cfg.getEdges()) {
            if (edge.getType() == EdgeType.SWITCH_CASE) {
                hasSwitchCase = true;
                assertNotNull(edge.getLabel(), "Switch case edge should have a label");
            }
            if (edge.getType() == EdgeType.SWITCH_DEFAULT) {
                hasSwitchDefault = true;
            }
        }
        assertTrue(hasSwitchCase, "Expected SWITCH_CASE edges for lookupswitch");
        assertTrue(hasSwitchDefault, "Expected SWITCH_DEFAULT edge for lookupswitch");
    }


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        ControlFlowBuilderFactory factory1 = ControlFlowBuilderFactory.getInstance();
        ControlFlowBuilderFactory factory2 = ControlFlowBuilderFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Load a ClassNode from the classpath.
     *
     * @param internalName the class internal name
     * @return the class node
     * @throws IOException if loading fails
     */
    private ClassNode loadClassNode(String internalName) throws IOException {
        String resourcePath = internalName + ".class";
        try (InputStream is = ClassLoader.getSystemResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Class not found: " + internalName);
            }
            ClassReader reader = new ClassReader(is);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);
            return classNode;
        }
    }
}
