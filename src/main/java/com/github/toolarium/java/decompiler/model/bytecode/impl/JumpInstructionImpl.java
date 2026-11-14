/*
 * JumpInstructionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode.impl;

import com.github.toolarium.java.decompiler.model.bytecode.IJumpInstruction;
import java.util.Objects;


/**
 * Implementation of {@link IJumpInstruction}.
 *
 * @author patrick
 */
public class JumpInstructionImpl extends InstructionImpl implements IJumpInstruction {
    private final int targetOffset;


    /**
     * Constructor for JumpInstructionImpl.
     *
     * @param offset       the byte offset in the code array
     * @param opcode       the opcode
     * @param mnemonic     the human-readable mnemonic
     * @param operands     the raw operand values
     * @param targetOffset the target byte offset to jump to
     */
    public JumpInstructionImpl(int offset, int opcode, String mnemonic, int[] operands, int targetOffset) {
        super(offset, opcode, mnemonic, operands);
        this.targetOffset = targetOffset;
    }


    /**
     * Get the target byte offset to jump to.
     *
     * @return the target byte offset
     */
    @Override
    public int getTargetOffset() {
        return targetOffset;
    }


    /**
     * Compute the hash code based on the parent instruction fields and target offset.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(targetOffset);
    }


    /**
     * Check equality based on the parent instruction fields and target offset.
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
        JumpInstructionImpl other = (JumpInstructionImpl) obj;
        return targetOffset == other.targetOffset;
    }


    /**
     * Return a string representation including offset, mnemonic, and target offset.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "JumpInstruction[" + getOffset() + ": " + getMnemonic() + " -> " + targetOffset + "]";
    }
}
