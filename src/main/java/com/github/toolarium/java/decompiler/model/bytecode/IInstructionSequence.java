/*
 * IInstructionSequence.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode;

import java.util.List;


/**
 * Represents an ordered sequence of bytecode instructions.
 *
 * @author patrick
 */
public interface IInstructionSequence {

    /**
     * Get all instructions in order.
     *
     * @return an unmodifiable list of instructions
     */
    List<IInstruction> getInstructions();


    /**
     * Get the number of instructions in this sequence.
     *
     * @return the number of instructions
     */
    int getLength();


    /**
     * Get the instruction at the given index.
     *
     * @param index the zero-based index
     * @return the instruction
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    IInstruction getInstruction(int index);


    /**
     * Find the instruction at the given byte offset.
     *
     * @param offset the byte offset in the code array
     * @return the instruction at that offset, or null if not found
     */
    IInstruction getInstructionAtOffset(int offset);
}
