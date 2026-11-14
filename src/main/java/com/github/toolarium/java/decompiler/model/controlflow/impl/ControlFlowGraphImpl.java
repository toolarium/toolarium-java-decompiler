/*
 * ControlFlowGraphImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.controlflow.impl;

import com.github.toolarium.java.decompiler.model.controlflow.IBasicBlock;
import com.github.toolarium.java.decompiler.model.controlflow.IControlFlowGraph;
import com.github.toolarium.java.decompiler.model.controlflow.IEdge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IControlFlowGraph}.
 *
 * @author patrick
 */
public class ControlFlowGraphImpl implements IControlFlowGraph {
    private final IBasicBlock entryBlock;
    private final List<IBasicBlock> blocks;
    private final List<IEdge> edges;


    /**
     * Constructor for ControlFlowGraphImpl.
     *
     * @param entryBlock the entry block
     * @param blocks     all basic blocks
     * @param edges      all edges
     */
    public ControlFlowGraphImpl(IBasicBlock entryBlock, List<IBasicBlock> blocks, List<IEdge> edges) {
        this.entryBlock = Objects.requireNonNull(entryBlock, "Entry block must not be null");
        this.blocks = Collections.unmodifiableList(new ArrayList<>(blocks));
        this.edges = Collections.unmodifiableList(new ArrayList<>(edges));
    }


    /**
     * Get the entry block (first block of the method).
     *
     * @return the entry block
     */
    @Override
    public IBasicBlock getEntryBlock() {
        return entryBlock;
    }


    /**
     * Get all basic blocks in the graph as an unmodifiable list.
     *
     * @return an unmodifiable list of basic blocks
     */
    @Override
    public List<IBasicBlock> getBlocks() {
        return blocks;
    }


    /**
     * Get all edges in the graph as an unmodifiable list.
     *
     * @return an unmodifiable list of edges
     */
    @Override
    public List<IEdge> getEdges() {
        return edges;
    }


    /**
     * Get the basic block that contains the instruction at the given byte offset.
     *
     * @param offset the byte offset
     * @return the basic block, or null if not found
     */
    @Override
    public IBasicBlock getBlockAtOffset(int offset) {
        for (IBasicBlock block : blocks) {
            if (block.getStartOffset() == offset) {
                return block;
            }
        }
        return null;
    }


    /**
     * Get the number of basic blocks in this graph.
     *
     * @return the block count
     */
    @Override
    public int getBlockCount() {
        return blocks.size();
    }


    /**
     * Compute the hash code based on the entry block, block count, and edge count.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(entryBlock, blocks.size(), edges.size());
    }


    /**
     * Check equality based on blocks and edges.
     *
     * @param obj the object to compare with
     * @return true if the other object is a ControlFlowGraphImpl with equal blocks and edges
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ControlFlowGraphImpl other = (ControlFlowGraphImpl) obj;
        return Objects.equals(blocks, other.blocks) && Objects.equals(edges, other.edges);
    }


    /**
     * Return a string representation showing block and edge counts.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ControlFlowGraph[" + blocks.size() + " blocks, " + edges.size() + " edges]";
    }
}
