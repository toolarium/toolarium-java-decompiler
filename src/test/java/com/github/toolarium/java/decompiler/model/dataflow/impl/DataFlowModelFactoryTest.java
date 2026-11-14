/*
 * DataFlowModelFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.dataflow.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.controlflow.ControlFlowModelFactory;
import com.github.toolarium.java.decompiler.model.controlflow.impl.BasicBlockImpl;
import com.github.toolarium.java.decompiler.model.dataflow.DataFlowModelFactory;
import com.github.toolarium.java.decompiler.model.dataflow.IDataFlowBlock;
import com.github.toolarium.java.decompiler.model.dataflow.IDataFlowVariable;
import com.github.toolarium.java.decompiler.model.dataflow.IMergeFunction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link DataFlowModelFactory} and its model implementations.
 *
 * @author patrick
 */
public class DataFlowModelFactoryTest {
    private static final String VAR_X = "x";

    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        DataFlowModelFactory factory1 = DataFlowModelFactory.getInstance();
        DataFlowModelFactory factory2 = DataFlowModelFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Test data flow variable creation with a name.
     */
    @Test
    void testDataFlowVariableCreation() {
        IDataFlowVariable variable = DataFlowModelFactory.getInstance().getDataFlowVariable(0, 1, "counter");
        assertEquals(0, variable.getIndex());
        assertEquals(1, variable.getVersion());
        assertEquals("counter", variable.getName());
    }


    /**
     * Test data flow variable with null name.
     */
    @Test
    void testDataFlowVariableNullName() {
        IDataFlowVariable variable = DataFlowModelFactory.getInstance().getDataFlowVariable(3, 0, null);
        assertEquals(3, variable.getIndex());
        assertEquals(0, variable.getVersion());
        assertNull(variable.getName());
    }


    /**
     * Test versioned name with a known variable name.
     */
    @Test
    void testDataFlowVariableVersionedNameWithName() {
        IDataFlowVariable variable = DataFlowModelFactory.getInstance().getDataFlowVariable(0, 2, VAR_X);
        assertEquals("x_2", variable.getVersionedName());
    }


    /**
     * Test versioned name when name is null falls back to var plus index.
     */
    @Test
    void testDataFlowVariableVersionedNameWithoutName() {
        IDataFlowVariable variable = DataFlowModelFactory.getInstance().getDataFlowVariable(5, 0, null);
        assertEquals("var5_0", variable.getVersionedName());
    }


    /**
     * Test data flow variable equals and hashCode.
     */
    @Test
    void testDataFlowVariableEqualsHashCode() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        IDataFlowVariable var1 = factory.getDataFlowVariable(0, 1, VAR_X);
        IDataFlowVariable var2 = factory.getDataFlowVariable(0, 1, VAR_X);
        IDataFlowVariable var3 = factory.getDataFlowVariable(0, 2, VAR_X);

        assertEquals(var1, var2);
        assertEquals(var1.hashCode(), var2.hashCode());
        assertNotEquals(var1, var3);
        IDataFlowVariable var4 = factory.getDataFlowVariable(1, 1, "y");
        assertNotEquals(var1, var4);
        assertNotEquals(var1, null);
        assertNotEquals(var1, "string");
        assertEquals(var1, var1);
    }


    /**
     * Test data flow variable equality uses index and version only, not name.
     */
    @Test
    void testDataFlowVariableEqualsByIndexAndVersion() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        IDataFlowVariable var1 = factory.getDataFlowVariable(0, 1, VAR_X);
        IDataFlowVariable var2 = factory.getDataFlowVariable(0, 1, "differentName");

        assertEquals(var1, var2);
        assertEquals(var1.hashCode(), var2.hashCode());
    }


    /**
     * Test data flow variable toString returns the versioned name.
     */
    @Test
    void testDataFlowVariableToString() {
        IDataFlowVariable variable = DataFlowModelFactory.getInstance().getDataFlowVariable(0, 3, "result");
        assertEquals("result_3", variable.toString());
    }


    /**
     * Test merge function creation.
     */
    @Test
    void testMergeFunctionCreation() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        IDataFlowVariable target = factory.getDataFlowVariable(0, 2, VAR_X);
        IDataFlowVariable source1 = factory.getDataFlowVariable(0, 0, VAR_X);
        IDataFlowVariable source2 = factory.getDataFlowVariable(0, 1, VAR_X);
        List<IDataFlowVariable> sources = Arrays.asList(source1, source2);

        IMergeFunction merge = factory.getMergeFunction(target, sources, 0);
        assertEquals(target, merge.getTarget());
        assertEquals(2, merge.getSources().size());
        assertEquals(source1, merge.getSources().get(0));
        assertEquals(source2, merge.getSources().get(1));
        assertEquals(0, merge.getVariableIndex());
    }


    /**
     * Test merge function with null sources defaults to empty list.
     */
    @Test
    void testMergeFunctionNullSources() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        IDataFlowVariable target = factory.getDataFlowVariable(0, 0, VAR_X);

        IMergeFunction merge = factory.getMergeFunction(target, null, 0);
        assertTrue(merge.getSources().isEmpty());
    }


    /**
     * Test merge function sources list is unmodifiable.
     */
    @Test
    void testMergeFunctionSourcesUnmodifiable() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        IDataFlowVariable target = factory.getDataFlowVariable(0, 2, VAR_X);
        IDataFlowVariable source = factory.getDataFlowVariable(0, 0, VAR_X);
        IMergeFunction merge = factory.getMergeFunction(target, Collections.singletonList(source), 0);

        assertThrows(UnsupportedOperationException.class, () ->
                merge.getSources().add(factory.getDataFlowVariable(0, 1, VAR_X)));
    }


    /**
     * Test merge function defensive copy of sources list.
     */
    @Test
    void testMergeFunctionDefensiveCopy() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        IDataFlowVariable target = factory.getDataFlowVariable(0, 2, VAR_X);
        IDataFlowVariable source = factory.getDataFlowVariable(0, 0, VAR_X);
        List<IDataFlowVariable> sources = new java.util.ArrayList<>();
        sources.add(source);

        IMergeFunction merge = factory.getMergeFunction(target, sources, 0);
        sources.add(factory.getDataFlowVariable(0, 1, VAR_X));

        assertEquals(1, merge.getSources().size());
    }


    /**
     * Test merge function equals and hashCode.
     */
    @Test
    void testMergeFunctionEqualsHashCode() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        IDataFlowVariable target = factory.getDataFlowVariable(0, 2, VAR_X);
        IDataFlowVariable source1 = factory.getDataFlowVariable(0, 0, VAR_X);
        IDataFlowVariable source2 = factory.getDataFlowVariable(0, 1, VAR_X);
        List<IDataFlowVariable> sources = Arrays.asList(source1, source2);

        IMergeFunction merge1 = factory.getMergeFunction(target, sources, 0);
        IMergeFunction merge2 = factory.getMergeFunction(target, sources, 0);
        IMergeFunction merge3 = factory.getMergeFunction(target, Collections.singletonList(source1), 0);

        assertEquals(merge1, merge2);
        assertEquals(merge1.hashCode(), merge2.hashCode());
        assertNotEquals(merge1, merge3);
        assertNotEquals(merge1, null);
        assertEquals(merge1, merge1);
    }


    /**
     * Test merge function toString.
     */
    @Test
    void testMergeFunctionToString() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        IDataFlowVariable target = factory.getDataFlowVariable(0, 2, VAR_X);
        IDataFlowVariable source1 = factory.getDataFlowVariable(0, 0, VAR_X);
        IDataFlowVariable source2 = factory.getDataFlowVariable(0, 1, VAR_X);

        IMergeFunction merge = factory.getMergeFunction(target, Arrays.asList(source1, source2), 0);
        String str = merge.toString();
        assertNotNull(str);
        assertTrue(str.contains("x_2"));
        assertTrue(str.contains("x_0"));
        assertTrue(str.contains("x_1"));
        assertTrue(str.contains("merge"));
    }


    /**
     * Test data flow block creation.
     */
    @Test
    void testDataFlowBlockCreation() {
        BasicBlockImpl cfgBlock = ControlFlowModelFactory.getInstance().getBasicBlock(0);
        DataFlowBlockImpl block = DataFlowModelFactory.getInstance().getDataFlowBlock(cfgBlock);

        assertEquals(cfgBlock, block.getCfgBlock());
        assertTrue(block.getMergeFunctions().isEmpty());
        assertTrue(block.getDefinitions().isEmpty());
        assertTrue(block.getUses().isEmpty());
    }


    /**
     * Test adding definitions and uses to a data flow block.
     */
    @Test
    void testDataFlowBlockAddDefinitionsAndUses() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        BasicBlockImpl cfgBlock = ControlFlowModelFactory.getInstance().getBasicBlock(0);
        DataFlowBlockImpl block = factory.getDataFlowBlock(cfgBlock);

        IDataFlowVariable def = factory.getDataFlowVariable(0, 0, VAR_X);
        IDataFlowVariable use = factory.getDataFlowVariable(1, 0, "y");
        block.addDefinition(def);
        block.addUse(use);

        assertEquals(1, block.getDefinitions().size());
        assertEquals(def, block.getDefinitions().get(0));
        assertEquals(1, block.getUses().size());
        assertEquals(use, block.getUses().get(0));
    }


    /**
     * Test adding a merge function to a data flow block.
     */
    @Test
    void testDataFlowBlockAddMergeFunction() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        BasicBlockImpl cfgBlock = ControlFlowModelFactory.getInstance().getBasicBlock(2);
        DataFlowBlockImpl block = factory.getDataFlowBlock(cfgBlock);

        IDataFlowVariable target = factory.getDataFlowVariable(0, 2, VAR_X);
        IDataFlowVariable source1 = factory.getDataFlowVariable(0, 0, VAR_X);
        IDataFlowVariable source2 = factory.getDataFlowVariable(0, 1, VAR_X);
        IMergeFunction merge = factory.getMergeFunction(target, Arrays.asList(source1, source2), 0);
        block.addMergeFunction(merge);

        assertEquals(1, block.getMergeFunctions().size());
        assertEquals(merge, block.getMergeFunctions().get(0));
    }


    /**
     * Test data flow block lists are unmodifiable.
     */
    @Test
    void testDataFlowBlockUnmodifiable() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        BasicBlockImpl cfgBlock = ControlFlowModelFactory.getInstance().getBasicBlock(0);
        DataFlowBlockImpl block = factory.getDataFlowBlock(cfgBlock);

        IDataFlowVariable variable = factory.getDataFlowVariable(0, 0, VAR_X);
        block.addDefinition(variable);

        assertThrows(UnsupportedOperationException.class, () ->
                block.getDefinitions().add(variable));
        assertThrows(UnsupportedOperationException.class, () ->
                block.getUses().add(variable));
        assertThrows(UnsupportedOperationException.class, () ->
                block.getMergeFunctions().add(
                        factory.getMergeFunction(variable, Collections.emptyList(), 0)));
    }


    /**
     * Test data flow block equals and hashCode.
     */
    @Test
    void testDataFlowBlockEqualsHashCode() {
        DataFlowModelFactory dfFactory = DataFlowModelFactory.getInstance();
        ControlFlowModelFactory cfFactory = ControlFlowModelFactory.getInstance();

        IDataFlowBlock block1 = dfFactory.getDataFlowBlock(cfFactory.getBasicBlock(0));
        IDataFlowBlock block2 = dfFactory.getDataFlowBlock(cfFactory.getBasicBlock(0));
        IDataFlowBlock block3 = dfFactory.getDataFlowBlock(cfFactory.getBasicBlock(1));

        assertEquals(block1, block2);
        assertEquals(block1.hashCode(), block2.hashCode());
        assertNotEquals(block1, block3);
        assertNotEquals(block1, null);
        assertEquals(block1, block1);
    }


    /**
     * Test data flow block toString.
     */
    @Test
    void testDataFlowBlockToString() {
        DataFlowModelFactory factory = DataFlowModelFactory.getInstance();
        BasicBlockImpl cfgBlock = ControlFlowModelFactory.getInstance().getBasicBlock(5);
        DataFlowBlockImpl block = factory.getDataFlowBlock(cfgBlock);

        IDataFlowVariable def = factory.getDataFlowVariable(0, 0, VAR_X);
        block.addDefinition(def);

        String str = block.toString();
        assertNotNull(str);
        assertTrue(str.contains("5"));
        assertTrue(str.contains("defs=1"));
    }
}
