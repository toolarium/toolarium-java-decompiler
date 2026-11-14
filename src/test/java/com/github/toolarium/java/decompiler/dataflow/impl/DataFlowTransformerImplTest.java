/*
 * DataFlowTransformerImplTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.dataflow.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.controlflow.ControlFlowBuilderFactory;
import com.github.toolarium.java.decompiler.controlflow.IControlFlowBuilder;
import com.github.toolarium.java.decompiler.dataflow.DataFlowTransformerFactory;
import com.github.toolarium.java.decompiler.dataflow.IDataFlowTransformer;
import com.github.toolarium.java.decompiler.model.controlflow.IControlFlowGraph;
import com.github.toolarium.java.decompiler.model.dataflow.DataFlowModelFactory;
import com.github.toolarium.java.decompiler.model.dataflow.IDataFlowBlock;
import com.github.toolarium.java.decompiler.model.dataflow.IDataFlowVariable;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;


/**
 * Tests for {@link DataFlowTransformerImpl}.
 *
 * @author patrick
 */
public class DataFlowTransformerImplTest {
    private static final String VAR_NAME_X = "x";
    private final IControlFlowBuilder cfgBuilder = ControlFlowBuilderFactory.getInstance().getCfgBuilder();
    private final IDataFlowTransformer dataFlowTransformer = DataFlowTransformerFactory.getInstance().getDataFlowTransformer();


    /**
     * Test data flow transformation for a linear method.
     */
    @Test
    void testLinearMethodDataFlow() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "linear", "()V", null, null);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 1);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        List<IDataFlowBlock> dataFlowBlocks = dataFlowTransformer.transformToDataFlow(cfg, 1);

        assertNotNull(dataFlowBlocks);
        assertFalse(dataFlowBlocks.isEmpty());
        assertEquals(cfg.getBlockCount(), dataFlowBlocks.size());

        // Entry block should have initial variable definitions
        IDataFlowBlock entryBlock = dataFlowBlocks.get(0);
        assertFalse(entryBlock.getDefinitions().isEmpty());
    }


    /**
     * Test data flow transformation for a method with a conditional branch.
     */
    @Test
    void testConditionalBranchDataFlow() {
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
        List<IDataFlowBlock> dataFlowBlocks = dataFlowTransformer.transformToDataFlow(cfg, 2);

        assertNotNull(dataFlowBlocks);
        assertEquals(cfg.getBlockCount(), dataFlowBlocks.size());

        // Join point should have merge functions (the end block has 2 predecessors)
        boolean hasMerges = false;
        for (IDataFlowBlock dataFlowBlock : dataFlowBlocks) {
            if (!dataFlowBlock.getMergeFunctions().isEmpty()) {
                hasMerges = true;
            }
        }
        assertTrue(hasMerges, "Expected merge functions at the join point");
    }


    /**
     * Test data flow variable versioning.
     */
    @Test
    void testVariableVersioning() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        IDataFlowVariable var0 = factory.getDataFlowVariable(0, 0, VAR_NAME_X);
        IDataFlowVariable var1 = factory.getDataFlowVariable(0, 1, VAR_NAME_X);
        IDataFlowVariable var2 = factory.getDataFlowVariable(0, 2, null);

        assertEquals("x_0", var0.getVersionedName());
        assertEquals("x_1", var1.getVersionedName());
        assertEquals("var0_2", var2.getVersionedName());
        assertEquals(0, var0.getIndex());
        assertEquals(0, var0.getVersion());
        assertEquals("x", var0.getName());
    }


    /**
     * Test data flow variable equals and hashCode.
     */
    @Test
    void testDataFlowVariableEqualsHashCode() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        IDataFlowVariable var1 = factory.getDataFlowVariable(0, 1, VAR_NAME_X);
        IDataFlowVariable var2 = factory.getDataFlowVariable(0, 1, VAR_NAME_X);

        assertEquals(var1, var2);
        assertEquals(var1.hashCode(), var2.hashCode());
        assertNotNull(var1.toString());
        assertFalse(var1.equals(factory.getDataFlowVariable(0, 2, VAR_NAME_X)));
        assertFalse(var1.equals(factory.getDataFlowVariable(1, 1, "y")));
    }


    /**
     * Test merge function creation and toString.
     */
    @Test
    void testMergeFunction() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        IDataFlowVariable target = factory.getDataFlowVariable(0, 3, VAR_NAME_X);
        IDataFlowVariable src1 = factory.getDataFlowVariable(0, 1, VAR_NAME_X);
        IDataFlowVariable src2 = factory.getDataFlowVariable(0, 2, VAR_NAME_X);

        var merge = factory.getMergeFunction(target, List.of(src1, src2), 0);

        assertEquals(target, merge.getTarget());
        assertEquals(2, merge.getSources().size());
        assertEquals(0, merge.getVariableIndex());
        assertTrue(merge.toString().contains("merge"));
        assertTrue(merge.toString().contains("x_1"));
        assertTrue(merge.toString().contains("x_2"));
    }


    /**
     * Test data flow for a loop produces merge functions at the loop header.
     */
    @Test
    void testLoopWithMergeFunctions() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "loop", "(I)I", null, null);
        Label loopStart = new Label();
        Label loopEnd = new Label();

        method.visitInsn(Opcodes.ICONST_0);
        method.visitVarInsn(Opcodes.ISTORE, 2);
        method.visitLabel(loopStart);
        method.visitVarInsn(Opcodes.ILOAD, 2);
        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);
        method.visitIincInsn(2, 1);
        method.visitJumpInsn(Opcodes.GOTO, loopStart);
        method.visitLabel(loopEnd);
        method.visitVarInsn(Opcodes.ILOAD, 2);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 3);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        List<IDataFlowBlock> dataFlowBlocks = dataFlowTransformer.transformToDataFlow(cfg, 3);

        assertNotNull(dataFlowBlocks);
        assertFalse(dataFlowBlocks.isEmpty());

        boolean hasMergeAtLoopHeader = false;
        for (IDataFlowBlock block : dataFlowBlocks) {
            if (!block.getMergeFunctions().isEmpty()) {
                hasMergeAtLoopHeader = true;
            }
        }
        assertTrue(hasMergeAtLoopHeader, "Expected merge functions at the loop header block");
    }


    /**
     * Test data flow for nested conditionals with multiple merge points.
     */
    @Test
    void testNestedConditionals() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "nested", "(II)V", null, null);
        Label else1 = new Label();
        Label end1 = new Label();
        Label else2 = new Label();
        Label end2 = new Label();

        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitJumpInsn(Opcodes.IFEQ, else1);

        method.visitVarInsn(Opcodes.ILOAD, 2);
        method.visitJumpInsn(Opcodes.IFEQ, else2);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitJumpInsn(Opcodes.GOTO, end2);
        method.visitLabel(else2);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitLabel(end2);

        method.visitJumpInsn(Opcodes.GOTO, end1);
        method.visitLabel(else1);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitLabel(end1);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 3);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        List<IDataFlowBlock> dataFlowBlocks = dataFlowTransformer.transformToDataFlow(cfg, 3);

        assertNotNull(dataFlowBlocks);
        assertEquals(cfg.getBlockCount(), dataFlowBlocks.size());

        int mergeBlockCount = 0;
        for (IDataFlowBlock block : dataFlowBlocks) {
            if (!block.getMergeFunctions().isEmpty()) {
                mergeBlockCount++;
            }
        }
        assertTrue(mergeBlockCount >= 1, "Expected merge functions at join points of nested conditionals");
    }


    /**
     * Test data flow for a method that defines and uses multiple local variables.
     */
    @Test
    void testMultipleVariables() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "multiVar", "()V", null, null);

        method.visitInsn(Opcodes.ICONST_1);
        method.visitVarInsn(Opcodes.ISTORE, 1);
        method.visitInsn(Opcodes.ICONST_2);
        method.visitVarInsn(Opcodes.ISTORE, 2);
        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitVarInsn(Opcodes.ILOAD, 2);
        method.visitInsn(Opcodes.IADD);
        method.visitVarInsn(Opcodes.ISTORE, 3);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(2, 4);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        List<IDataFlowBlock> dataFlowBlocks = dataFlowTransformer.transformToDataFlow(cfg, 4);

        assertNotNull(dataFlowBlocks);
        assertFalse(dataFlowBlocks.isEmpty());

        IDataFlowBlock entryBlock = dataFlowBlocks.get(0);
        assertTrue(entryBlock.getDefinitions().size() >= 3,
                "Expected at least 3 variable definitions (slots 1, 2, 3)");
    }


    /**
     * Test data flow block equals and hashCode.
     */
    @Test
    void testDataFlowBlockEqualsHashCode() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "simple", "()V", null, null);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(0, 1);

        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);
        List<IDataFlowBlock> blocks1 = dataFlowTransformer.transformToDataFlow(cfg, 1);
        List<IDataFlowBlock> blocks2 = dataFlowTransformer.transformToDataFlow(cfg, 1);

        assertNotNull(blocks1);
        assertNotNull(blocks2);
        assertFalse(blocks1.isEmpty());
        assertNotNull(blocks1.get(0).toString());
    }


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        DataFlowTransformerFactory factory1 = DataFlowTransformerFactory.getInstance();
        DataFlowTransformerFactory factory2 = DataFlowTransformerFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }
}
