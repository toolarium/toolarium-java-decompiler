/*
 * SwitchInstructionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode.impl;

import com.github.toolarium.java.decompiler.model.bytecode.ISwitchInstruction;
import java.util.Arrays;
import java.util.Objects;


/**
 * Implementation of {@link ISwitchInstruction}.
 *
 * @author patrick
 */
public class SwitchInstructionImpl extends InstructionImpl implements ISwitchInstruction {
    private final int defaultOffset;
    private final int[] keys;
    private final int[] targetOffsets;


    /**
     * Constructor for SwitchInstructionImpl.
     *
     * @param offset        the byte offset in the code array
     * @param opcode        the opcode
     * @param mnemonic      the human-readable mnemonic
     * @param operands      the raw operand values
     * @param defaultOffset the default branch target offset
     * @param keys          the case keys (defensively copied)
     * @param targetOffsets the branch target offsets (defensively copied)
     */
    public SwitchInstructionImpl(int offset, int opcode, String mnemonic, int[] operands,
                                 int defaultOffset, int[] keys, int[] targetOffsets) {
        super(offset, opcode, mnemonic, operands);
        this.defaultOffset = defaultOffset;
        if (keys != null) {
            this.keys = Arrays.copyOf(keys, keys.length);
        } else {
            this.keys = new int[0];
        }
        if (targetOffsets != null) {
            this.targetOffsets = Arrays.copyOf(targetOffsets, targetOffsets.length);
        } else {
            this.targetOffsets = new int[0];
        }
    }


    /**
     * Get the default branch target offset.
     *
     * @return the default target offset
     */
    @Override
    public int getDefaultOffset() {
        return defaultOffset;
    }


    /**
     * Get a defensive copy of the case keys.
     *
     * @return the case keys
     */
    @Override
    public int[] getKeys() {
        return Arrays.copyOf(keys, keys.length);
    }


    /**
     * Get a defensive copy of the branch target offsets, one per key.
     *
     * @return the target offsets
     */
    @Override
    public int[] getTargetOffsets() {
        return Arrays.copyOf(targetOffsets, targetOffsets.length);
    }


    /**
     * Compute the hash code based on the parent fields, default offset, keys, and target offsets.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hash(defaultOffset);
        result = 31 * result + Arrays.hashCode(keys);
        result = 31 * result + Arrays.hashCode(targetOffsets);
        return result;
    }


    /**
     * Check equality based on the parent fields, default offset, keys, and target offsets.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SwitchInstructionImpl other = (SwitchInstructionImpl) obj;
        return defaultOffset == other.defaultOffset
                && Arrays.equals(keys, other.keys)
                && Arrays.equals(targetOffsets, other.targetOffsets);
    }


    /**
     * Return a string representation including mnemonic, case count, and default offset.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "SwitchInstruction[" + getOffset() + ": " + getMnemonic()
                + ", " + keys.length + " cases, default -> " + defaultOffset + "]";
    }
}
