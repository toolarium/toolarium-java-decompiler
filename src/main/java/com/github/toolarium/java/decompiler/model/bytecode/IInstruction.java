/*
 * IInstruction.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode;


/**
 * Represents a single bytecode instruction.
 *
 * @author patrick
 */
public interface IInstruction {

    /**
     * Get the byte offset of this instruction in the method's code array.
     *
     * @return the byte offset
     */
    int getOffset();


    /**
     * Get the opcode of this instruction (0-255).
     *
     * @return the opcode
     */
    int getOpcode();


    /**
     * Get the human-readable mnemonic (e.g., "aload_0", "invokevirtual").
     *
     * @return the mnemonic
     */
    String getMnemonic();


    /**
     * Get the raw operand bytes of this instruction.
     *
     * @return the operands as an int array, empty if none
     */
    int[] getOperands();


    /**
     * Get the total length of this instruction in bytes (opcode + operands).
     *
     * @return the instruction length
     */
    int getLength();
}
