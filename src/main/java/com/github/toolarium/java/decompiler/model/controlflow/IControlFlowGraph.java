/*
 * IControlFlowGraph.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.controlflow;

import java.util.List;


/**
 * Represents a control flow graph for a single method.
 *
 * @author patrick
 */
public interface IControlFlowGraph {

    /**
     * Get the entry block (first block of the method).
     *
     * @return the entry block
     */
    IBasicBlock getEntryBlock();


    /**
     * Get all basic blocks in the graph.
     *
     * @return an unmodifiable list of basic blocks
     */
    List<IBasicBlock> getBlocks();


    /**
     * Get all edges in the graph.
     *
     * @return an unmodifiable list of edges
     */
    List<IEdge> getEdges();


    /**
     * Get the basic block that contains the instruction at the given byte offset.
     *
     * @param offset the byte offset
     * @return the basic block, or null if not found
     */
    IBasicBlock getBlockAtOffset(int offset);


    /**
     * Get the number of basic blocks.
     *
     * @return the block count
     */
    int getBlockCount();
}
