/*
 * InstructionSequenceImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode.impl;

import com.github.toolarium.java.decompiler.model.bytecode.IInstruction;
import com.github.toolarium.java.decompiler.model.bytecode.IInstructionSequence;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IInstructionSequence}.
 *
 * @author patrick
 */
public class InstructionSequenceImpl implements IInstructionSequence {
    private final List<IInstruction> instructions;


    /**
     * Constructor for InstructionSequenceImpl.
     *
     * @param instructions the instructions (defensively copied)
     */
    public InstructionSequenceImpl(List<IInstruction> instructions) {
        if (instructions != null) {
            this.instructions = Collections.unmodifiableList(new ArrayList<>(instructions));
        } else {
            this.instructions = Collections.emptyList();
        }
    }


    /**
     * Get all instructions in order.
     *
     * @return an unmodifiable list of instructions
     */
    @Override
    public List<IInstruction> getInstructions() {
        return instructions;
    }


    /**
     * Get the number of instructions in this sequence.
     *
     * @return the number of instructions
     */
    @Override
    public int getLength() {
        return instructions.size();
    }


    /**
     * Get the instruction at the given index.
     *
     * @param index the zero-based index
     * @return the instruction
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public IInstruction getInstruction(int index) {
        return instructions.get(index);
    }


    /**
     * Find the instruction at the given byte offset.
     *
     * @param offset the byte offset in the code array
     * @return the instruction at that offset, or null if not found
     */
    @Override
    public IInstruction getInstructionAtOffset(int offset) {
        for (IInstruction instruction : instructions) {
            if (instruction.getOffset() == offset) {
                return instruction;
            }
        }
        return null;
    }


    /**
     * Compute the hash code based on the instruction list.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(instructions);
    }


    /**
     * Check equality based on the instruction list.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        InstructionSequenceImpl other = (InstructionSequenceImpl) obj;
        return Objects.equals(instructions, other.instructions);
    }


    /**
     * Return a string representation including the instruction count.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "InstructionSequence[" + instructions.size() + " instructions]";
    }
}
