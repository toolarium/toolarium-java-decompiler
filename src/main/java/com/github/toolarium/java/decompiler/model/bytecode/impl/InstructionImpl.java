/*
 * InstructionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode.impl;

import com.github.toolarium.java.decompiler.model.bytecode.IInstruction;
import java.util.Arrays;
import java.util.Objects;


/**
 * Implementation of {@link IInstruction}.
 *
 * @author patrick
 */
public class InstructionImpl implements IInstruction {
    private final int offset;
    private final int opcode;
    private final String mnemonic;
    private final int[] operands;


    /**
     * Constructor for InstructionImpl.
     *
     * @param offset   the byte offset in the code array
     * @param opcode   the opcode (0-255)
     * @param mnemonic the human-readable mnemonic
     * @param operands the raw operand values (defensively copied)
     */
    public InstructionImpl(int offset, int opcode, String mnemonic, int[] operands) {
        this.offset = offset;
        this.opcode = opcode;
        this.mnemonic = Objects.requireNonNull(mnemonic, "Mnemonic must not be null");
        if (operands != null) {
            this.operands = Arrays.copyOf(operands, operands.length);
        } else {
            this.operands = new int[0];
        }
    }


    /**
     * Get the byte offset of this instruction in the method's code array.
     *
     * @return the byte offset
     */
    @Override
    public int getOffset() {
        return offset;
    }


    /**
     * Get the opcode of this instruction (0-255).
     *
     * @return the opcode
     */
    @Override
    public int getOpcode() {
        return opcode;
    }


    /**
     * Get the human-readable mnemonic (e.g., "aload_0", "invokevirtual").
     *
     * @return the mnemonic
     */
    @Override
    public String getMnemonic() {
        return mnemonic;
    }


    /**
     * Get a defensive copy of the raw operand bytes of this instruction.
     *
     * @return the operands as an int array, empty if none
     */
    @Override
    public int[] getOperands() {
        return Arrays.copyOf(operands, operands.length);
    }


    /**
     * Get the total length of this instruction in bytes (opcode + operands).
     *
     * @return the instruction length
     */
    @Override
    public int getLength() {
        return 1 + operands.length;
    }


    /**
     * Compute the hash code based on offset, opcode, mnemonic, and operands.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(offset, opcode, mnemonic);
        result = 31 * result + Arrays.hashCode(operands);
        return result;
    }


    /**
     * Check equality based on offset, opcode, mnemonic, and operands.
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
        InstructionImpl other = (InstructionImpl) obj;
        return offset == other.offset
                && opcode == other.opcode
                && Objects.equals(mnemonic, other.mnemonic)
                && Arrays.equals(operands, other.operands);
    }


    /**
     * Return a string representation including offset and mnemonic.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "Instruction[" + offset + ": " + mnemonic + "]";
    }
}
