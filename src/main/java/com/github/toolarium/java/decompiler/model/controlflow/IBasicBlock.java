/*
 * IBasicBlock.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.controlflow;

import com.github.toolarium.java.decompiler.model.bytecode.IInstruction;
import java.util.List;


/**
 * Represents a basic block in the control flow graph.
 * A basic block is a maximal sequence of instructions with no branches in and no branches out,
 * except at the entry and exit.
 *
 * @author patrick
 */
public interface IBasicBlock {

    /**
     * Get the unique identifier of this block.
     *
     * @return the block id
     */
    int getId();


    /**
     * Get the instructions in this basic block, in order.
     *
     * @return an unmodifiable list of instructions
     */
    List<IInstruction> getInstructions();


    /**
     * Get the start byte offset of this block (offset of the first instruction).
     *
     * @return the start offset, or -1 if the block has no instructions
     */
    int getStartOffset();


    /**
     * Get the successor blocks.
     *
     * @return an unmodifiable list of successor blocks
     */
    List<IBasicBlock> getSuccessors();


    /**
     * Get the predecessor blocks.
     *
     * @return an unmodifiable list of predecessor blocks
     */
    List<IBasicBlock> getPredecessors();


    /**
     * Get the outgoing edges.
     *
     * @return an unmodifiable list of outgoing edges
     */
    List<IEdge> getOutEdges();


    /**
     * Get the incoming edges.
     *
     * @return an unmodifiable list of incoming edges
     */
    List<IEdge> getInEdges();


    /**
     * Check whether this block is an exception handler entry.
     *
     * @return true if this block starts an exception handler
     */
    boolean isExceptionHandler();


    /**
     * Get the caught exception type if this is an exception handler block.
     *
     * @return the exception type internal name, or null
     */
    String getCatchType();
}
