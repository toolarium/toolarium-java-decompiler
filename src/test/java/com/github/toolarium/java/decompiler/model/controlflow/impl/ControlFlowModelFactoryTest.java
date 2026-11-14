/*
 * ControlFlowModelFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.controlflow.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.bytecode.BytecodeModelFactory;
import com.github.toolarium.java.decompiler.model.bytecode.IInstruction;
import com.github.toolarium.java.decompiler.model.controlflow.ControlFlowModelFactory;
import com.github.toolarium.java.decompiler.model.controlflow.EdgeType;
import com.github.toolarium.java.decompiler.model.controlflow.IBasicBlock;
import com.github.toolarium.java.decompiler.model.controlflow.IControlFlowGraph;
import com.github.toolarium.java.decompiler.model.controlflow.IEdge;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link ControlFlowModelFactory} and its model implementations.
 *
 * @author patrick
 */
public class ControlFlowModelFactoryTest {

    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        ControlFlowModelFactory factory1 = ControlFlowModelFactory.getInstance();
        ControlFlowModelFactory factory2 = ControlFlowModelFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Test basic block creation with id and empty state.
     */
    @Test
    void testBasicBlockCreation() {
        BasicBlockImpl block = ControlFlowModelFactory.getInstance().getBasicBlock(0);
        assertEquals(0, block.getId());
        assertTrue(block.getInstructions().isEmpty());
        assertTrue(block.getSuccessors().isEmpty());
        assertTrue(block.getPredecessors().isEmpty());
        assertTrue(block.getOutEdges().isEmpty());
        assertTrue(block.getInEdges().isEmpty());
        assertFalse(block.isExceptionHandler());
        assertNull(block.getCatchType());
    }


    /**
     * Test basic block start offset is -1 when no instructions are present.
     */
    @Test
    void testBasicBlockEmptyStartOffset() {
        BasicBlockImpl block = ControlFlowModelFactory.getInstance().getBasicBlock(1);
        assertEquals(-1, block.getStartOffset());
    }


    /**
     * Test adding an instruction to a basic block.
     */
    @Test
    void testBasicBlockAddInstruction() {
        BasicBlockImpl block = ControlFlowModelFactory.getInstance().getBasicBlock(0);
        IInstruction instruction = BytecodeModelFactory.getInstance().getInstruction(10, 42, "aload_0", new int[0]);
        block.addInstruction(instruction);

        assertEquals(1, block.getInstructions().size());
        assertEquals(10, block.getStartOffset());
        assertEquals(instruction, block.getInstructions().get(0));
    }


    /**
     * Test that getInstructions returns an unmodifiable list.
     */
    @Test
    void testBasicBlockInstructionsUnmodifiable() {
        BasicBlockImpl block = ControlFlowModelFactory.getInstance().getBasicBlock(0);
        IInstruction instruction = BytecodeModelFactory.getInstance().getInstruction(0, 42, "aload_0", new int[0]);
        block.addInstruction(instruction);

        assertThrows(UnsupportedOperationException.class, () ->
                block.getInstructions().add(instruction));
    }


    /**
     * Test setting a block as an exception handler with a catch type.
     */
    @Test
    void testBasicBlockExceptionHandler() {
        BasicBlockImpl block = ControlFlowModelFactory.getInstance().getBasicBlock(5);
        block.setExceptionHandler("java/lang/IOException");

        assertTrue(block.isExceptionHandler());
        assertEquals("java/lang/IOException", block.getCatchType());
    }


    /**
     * Test setting a block as a finally handler (null catch type).
     */
    @Test
    void testBasicBlockFinallyHandler() {
        BasicBlockImpl block = ControlFlowModelFactory.getInstance().getBasicBlock(6);
        block.setExceptionHandler(null);

        assertTrue(block.isExceptionHandler());
        assertNull(block.getCatchType());
    }


    /**
     * Test basic block equals and hashCode.
     */
    @Test
    void testBasicBlockEqualsHashCode() {
        ControlFlowModelFactory factory = ControlFlowModelFactory.getInstance();
        BasicBlockImpl block1 = factory.getBasicBlock(0);
        BasicBlockImpl block2 = factory.getBasicBlock(0);
        BasicBlockImpl block3 = factory.getBasicBlock(1);

        assertEquals(block1, block2);
        assertEquals(block1.hashCode(), block2.hashCode());
        assertNotEquals(block1, block3);
        assertNotEquals(block1, null);
        assertNotEquals(block1, "string");
        assertEquals(block1, block1);
    }


    /**
     * Test basic block toString.
     */
    @Test
    void testBasicBlockToString() {
        BasicBlockImpl block = ControlFlowModelFactory.getInstance().getBasicBlock(3);
        String str = block.toString();
        assertNotNull(str);
        assertTrue(str.contains("3"));
        assertTrue(str.contains("BasicBlock"));
    }


    /**
     * Test edge creation with all fields.
     */
    @Test
    void testEdgeCreation() {
        ControlFlowModelFactory factory = ControlFlowModelFactory.getInstance();
        IBasicBlock source = factory.getBasicBlock(0);
        IBasicBlock target = factory.getBasicBlock(1);

        IEdge edge = factory.getEdge(source, target, EdgeType.FALL_THROUGH, null);
        assertEquals(source, edge.getSource());
        assertEquals(target, edge.getTarget());
        assertEquals(EdgeType.FALL_THROUGH, edge.getType());
        assertNull(edge.getLabel());
    }


    /**
     * Test edge creation with a label.
     */
    @Test
    void testEdgeWithLabel() {
        ControlFlowModelFactory factory = ControlFlowModelFactory.getInstance();
        IBasicBlock source = factory.getBasicBlock(0);
        IBasicBlock target = factory.getBasicBlock(1);

        IEdge edge = factory.getEdge(source, target, EdgeType.SWITCH_CASE, "42");
        assertEquals(EdgeType.SWITCH_CASE, edge.getType());
        assertEquals("42", edge.getLabel());
    }


    /**
     * Test edge equals and hashCode.
     */
    @Test
    void testEdgeEqualsHashCode() {
        ControlFlowModelFactory factory = ControlFlowModelFactory.getInstance();
        IBasicBlock block0 = factory.getBasicBlock(0);
        IBasicBlock block1 = factory.getBasicBlock(1);
        IBasicBlock block2 = factory.getBasicBlock(2);

        IEdge edge1 = factory.getEdge(block0, block1, EdgeType.JUMP, null);
        IEdge edge2 = factory.getEdge(block0, block1, EdgeType.JUMP, null);
        IEdge edge3 = factory.getEdge(block0, block2, EdgeType.JUMP, null);

        assertEquals(edge1, edge2);
        assertEquals(edge1.hashCode(), edge2.hashCode());
        assertNotEquals(edge1, edge3);
        IEdge edge4 = factory.getEdge(block0, block1, EdgeType.EXCEPTION, null);
        assertNotEquals(edge1, edge4);
        assertNotEquals(edge1, null);
        assertEquals(edge1, edge1);
    }


    /**
     * Test edge toString.
     */
    @Test
    void testEdgeToString() {
        ControlFlowModelFactory factory = ControlFlowModelFactory.getInstance();
        IEdge edge = factory.getEdge(factory.getBasicBlock(0), factory.getBasicBlock(1),
                EdgeType.JUMP, null);
        String str = edge.toString();
        assertNotNull(str);
        assertTrue(str.contains("0"));
        assertTrue(str.contains("1"));
        assertTrue(str.contains("JUMP"));
    }


    /**
     * Test that addOutEdge wires up successor, predecessor, and in-edge links.
     */
    @Test
    void testAddOutEdgeWiresLinks() {
        ControlFlowModelFactory factory = ControlFlowModelFactory.getInstance();
        BasicBlockImpl block0 = factory.getBasicBlock(0);
        BasicBlockImpl block1 = factory.getBasicBlock(1);

        IEdge edge = factory.getEdge(block0, block1, EdgeType.FALL_THROUGH, null);
        block0.addOutEdge(edge);

        assertEquals(1, block0.getOutEdges().size());
        assertEquals(1, block0.getSuccessors().size());
        assertEquals(block1, block0.getSuccessors().get(0));

        assertEquals(1, block1.getInEdges().size());
        assertEquals(1, block1.getPredecessors().size());
        assertEquals(block0, block1.getPredecessors().get(0));
    }


    /**
     * Test that adding a duplicate successor does not create duplicates.
     */
    @Test
    void testAddOutEdgeDuplicateSuccessor() {
        ControlFlowModelFactory factory = ControlFlowModelFactory.getInstance();
        BasicBlockImpl block0 = factory.getBasicBlock(0);
        BasicBlockImpl block1 = factory.getBasicBlock(1);

        IEdge edge1 = factory.getEdge(block0, block1, EdgeType.FALL_THROUGH, null);
        IEdge edge2 = factory.getEdge(block0, block1, EdgeType.JUMP, null);
        block0.addOutEdge(edge1);
        block0.addOutEdge(edge2);

        assertEquals(2, block0.getOutEdges().size());
        assertEquals(1, block0.getSuccessors().size());
    }


    /**
     * Test EdgeType enum has all expected values.
     */
    @Test
    void testEdgeTypeValues() {
        EdgeType[] values = EdgeType.values();
        assertEquals(5, values.length);
        assertNotNull(EdgeType.valueOf("FALL_THROUGH"));
        assertNotNull(EdgeType.valueOf("JUMP"));
        assertNotNull(EdgeType.valueOf("EXCEPTION"));
        assertNotNull(EdgeType.valueOf("SWITCH_CASE"));
        assertNotNull(EdgeType.valueOf("SWITCH_DEFAULT"));
    }


    /**
     * Test control flow graph creation.
     */
    @Test
    void testControlFlowGraphCreation() {
        ControlFlowModelFactory factory = ControlFlowModelFactory.getInstance();
        BasicBlockImpl block0 = factory.getBasicBlock(0);
        BasicBlockImpl block1 = factory.getBasicBlock(1);
        IEdge edge = factory.getEdge(block0, block1, EdgeType.FALL_THROUGH, null);

        List<IBasicBlock> blocks = Arrays.asList(block0, block1);
        List<IEdge> edges = Collections.singletonList(edge);
        IControlFlowGraph graph = factory.getControlFlowGraph(block0, blocks, edges);

        assertEquals(block0, graph.getEntryBlock());
        assertEquals(2, graph.getBlockCount());
        assertEquals(2, graph.getBlocks().size());
        assertEquals(1, graph.getEdges().size());
    }


    /**
     * Test control flow graph getBlockAtOffset.
     */
    @Test
    void testControlFlowGraphGetBlockAtOffset() {
        ControlFlowModelFactory cfgFactory = ControlFlowModelFactory.getInstance();
        BasicBlockImpl block0 = cfgFactory.getBasicBlock(0);
        IInstruction instruction = BytecodeModelFactory.getInstance().getInstruction(10, 42, "aload_0", new int[0]);
        block0.addInstruction(instruction);

        BasicBlockImpl block1 = cfgFactory.getBasicBlock(1);
        IInstruction instruction2 = BytecodeModelFactory.getInstance().getInstruction(20, 177, "return", new int[0]);
        block1.addInstruction(instruction2);

        IControlFlowGraph graph = cfgFactory.getControlFlowGraph(block0,
                Arrays.asList(block0, block1), Collections.emptyList());

        assertEquals(block0, graph.getBlockAtOffset(10));
        assertEquals(block1, graph.getBlockAtOffset(20));
        assertNull(graph.getBlockAtOffset(99));
    }


    /**
     * Test control flow graph blocks and edges are unmodifiable.
     */
    @Test
    void testControlFlowGraphUnmodifiable() {
        ControlFlowModelFactory factory = ControlFlowModelFactory.getInstance();
        BasicBlockImpl block0 = factory.getBasicBlock(0);
        IControlFlowGraph graph = factory.getControlFlowGraph(block0,
                Collections.singletonList(block0), Collections.emptyList());

        assertThrows(UnsupportedOperationException.class, () ->
                graph.getBlocks().add(factory.getBasicBlock(99)));
        assertThrows(UnsupportedOperationException.class, () ->
                graph.getEdges().add(factory.getEdge(block0, block0, EdgeType.JUMP, null)));
    }


    /**
     * Test control flow graph equals and hashCode.
     */
    @Test
    void testControlFlowGraphEqualsHashCode() {
        ControlFlowModelFactory factory = ControlFlowModelFactory.getInstance();
        BasicBlockImpl block0 = factory.getBasicBlock(0);
        BasicBlockImpl block1 = factory.getBasicBlock(1);
        IEdge edge = factory.getEdge(block0, block1, EdgeType.JUMP, null);

        List<IBasicBlock> blocks = Arrays.asList(block0, block1);
        List<IEdge> edges = Collections.singletonList(edge);

        IControlFlowGraph graph1 = factory.getControlFlowGraph(block0, blocks, edges);
        IControlFlowGraph graph2 = factory.getControlFlowGraph(block0, blocks, edges);
        IControlFlowGraph graph3 = factory.getControlFlowGraph(block0,
                Collections.singletonList(block0), Collections.emptyList());

        assertEquals(graph1, graph2);
        assertEquals(graph1.hashCode(), graph2.hashCode());
        assertNotEquals(graph1, graph3);
        assertNotEquals(graph1, null);
        assertEquals(graph1, graph1);
    }


    /**
     * Test control flow graph toString.
     */
    @Test
    void testControlFlowGraphToString() {
        ControlFlowModelFactory factory = ControlFlowModelFactory.getInstance();
        BasicBlockImpl block0 = factory.getBasicBlock(0);
        IControlFlowGraph graph = factory.getControlFlowGraph(block0,
                Collections.singletonList(block0), Collections.emptyList());

        String str = graph.toString();
        assertNotNull(str);
        assertTrue(str.contains("1"));
        assertTrue(str.contains("0"));
    }


    /**
     * Test control flow graph defensive copy of input lists.
     */
    @Test
    void testControlFlowGraphDefensiveCopy() {
        ControlFlowModelFactory factory = ControlFlowModelFactory.getInstance();
        BasicBlockImpl block0 = factory.getBasicBlock(0);
        BasicBlockImpl block1 = factory.getBasicBlock(1);

        List<IBasicBlock> blocks = new java.util.ArrayList<>();
        blocks.add(block0);
        List<IEdge> edges = new java.util.ArrayList<>();

        IControlFlowGraph graph = factory.getControlFlowGraph(block0, blocks, edges);
        blocks.add(block1);
        edges.add(factory.getEdge(block0, block1, EdgeType.JUMP, null));

        assertEquals(1, graph.getBlockCount());
        assertEquals(0, graph.getEdges().size());
    }
}
