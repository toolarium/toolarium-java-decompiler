/*
 * BasicBlockImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.controlflow.impl;

import com.github.toolarium.java.decompiler.model.bytecode.IInstruction;
import com.github.toolarium.java.decompiler.model.controlflow.IBasicBlock;
import com.github.toolarium.java.decompiler.model.controlflow.IEdge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IBasicBlock}.
 * This is a mutable builder-style class used during CFG construction,
 * but exposes only unmodifiable views through the interface.
 *
 * @author patrick
 */
public class BasicBlockImpl implements IBasicBlock {
    private final int id;
    private final List<IInstruction> instructions = new ArrayList<>();
    private final List<IBasicBlock> successors = new ArrayList<>();
    private final List<IBasicBlock> predecessors = new ArrayList<>();
    private final List<IEdge> outEdges = new ArrayList<>();
    private final List<IEdge> inEdges = new ArrayList<>();
    private boolean exceptionHandler;
    private String catchType;


    /**
     * Constructor for BasicBlockImpl.
     *
     * @param id the unique block identifier
     */
    public BasicBlockImpl(int id) {
        this.id = id;
    }


    /**
     * Get the unique identifier of this block.
     *
     * @return the block id
     */
    @Override
    public int getId() {
        return id;
    }


    /**
     * Get the instructions in this basic block as an unmodifiable list.
     *
     * @return an unmodifiable list of instructions in order
     */
    @Override
    public List<IInstruction> getInstructions() {
        return Collections.unmodifiableList(instructions);
    }


    /**
     * Get the start byte offset of this block (offset of the first instruction).
     *
     * @return the start offset, or -1 if the block has no instructions
     */
    @Override
    public int getStartOffset() {
        if (instructions.isEmpty()) {
            return -1;
        }
        return instructions.get(0).getOffset();
    }


    /**
     * Get the successor blocks as an unmodifiable list.
     *
     * @return an unmodifiable list of successor blocks
     */
    @Override
    public List<IBasicBlock> getSuccessors() {
        return Collections.unmodifiableList(successors);
    }


    /**
     * Get the predecessor blocks as an unmodifiable list.
     *
     * @return an unmodifiable list of predecessor blocks
     */
    @Override
    public List<IBasicBlock> getPredecessors() {
        return Collections.unmodifiableList(predecessors);
    }


    /**
     * Get the outgoing edges as an unmodifiable list.
     *
     * @return an unmodifiable list of outgoing edges
     */
    @Override
    public List<IEdge> getOutEdges() {
        return Collections.unmodifiableList(outEdges);
    }


    /**
     * Get the incoming edges as an unmodifiable list.
     *
     * @return an unmodifiable list of incoming edges
     */
    @Override
    public List<IEdge> getInEdges() {
        return Collections.unmodifiableList(inEdges);
    }


    /**
     * Check whether this block is an exception handler entry.
     *
     * @return true if this block starts an exception handler
     */
    @Override
    public boolean isExceptionHandler() {
        return exceptionHandler;
    }


    /**
     * Get the caught exception type if this is an exception handler block.
     *
     * @return the exception type internal name, or null if not an exception handler or a finally block
     */
    @Override
    public String getCatchType() {
        return catchType;
    }


    /**
     * Add an instruction to this block.
     *
     * @param instruction the instruction to add
     */
    public void addInstruction(IInstruction instruction) {
        instructions.add(Objects.requireNonNull(instruction));
    }


    /**
     * Add an outgoing edge and update successor/predecessor links.
     *
     * @param edge the edge to add
     */
    public void addOutEdge(IEdge edge) {
        outEdges.add(edge);
        IBasicBlock target = edge.getTarget();
        if (!successors.contains(target)) {
            successors.add(target);
        }
        if (target instanceof BasicBlockImpl) {
            BasicBlockImpl targetImpl = (BasicBlockImpl) target;
            targetImpl.inEdges.add(edge);
            if (!targetImpl.predecessors.contains(this)) {
                targetImpl.predecessors.add(this);
            }
        }
    }


    /**
     * Mark this block as an exception handler entry.
     *
     * @param catchType the exception type (null for finally)
     */
    public void setExceptionHandler(String catchType) {
        this.exceptionHandler = true;
        this.catchType = catchType;
    }


    /**
     * Compute the hash code based on the block id.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    /**
     * Check equality based on the block id.
     *
     * @param obj the object to compare with
     * @return true if the other object is a BasicBlockImpl with the same id
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BasicBlockImpl other = (BasicBlockImpl) obj;
        return id == other.id;
    }


    /**
     * Return a string representation showing block id, offset, and instruction count.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        int startOff = getStartOffset();
        return "BasicBlock[id=" + id + ", offset=" + startOff
                + ", instructions=" + instructions.size() + "]";
    }
}
