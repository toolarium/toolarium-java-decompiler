/*
 * MethodCodeImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile.impl;

import com.github.toolarium.java.decompiler.model.classfile.IMethodCode;
import java.util.Arrays;
import java.util.Objects;


/**
 * Implementation of {@link IMethodCode}.
 *
 * @author patrick
 */
public class MethodCodeImpl implements IMethodCode {
    private final byte[] code;
    private final int maxStack;
    private final int maxLocals;


    /**
     * Constructor for MethodCodeImpl.
     *
     * @param code      the raw bytecode (may be null)
     * @param maxStack  the max operand stack depth
     * @param maxLocals the max local variable count
     */
    public MethodCodeImpl(byte[] code, int maxStack, int maxLocals) {
        if (code != null) {
            this.code = Arrays.copyOf(code, code.length);
        } else {
            this.code = null;
        }
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
    }


    /**
     * Get a defensive copy of the raw bytecode.
     *
     * @return the bytecode array, or null for abstract or native methods
     */
    @Override
    public byte[] getCode() {
        if (code != null) {
            return Arrays.copyOf(code, code.length);
        }
        return null;
    }


    /**
     * Get the maximum operand stack depth.
     *
     * @return the max stack depth
     */
    @Override
    public int getMaxStack() {
        return maxStack;
    }


    /**
     * Get the maximum number of local variables.
     *
     * @return the max local variable count
     */
    @Override
    public int getMaxLocals() {
        return maxLocals;
    }


    /**
     * Compute the hash code based on code, maxStack, and maxLocals.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(maxStack, maxLocals);
        result = 31 * result + Arrays.hashCode(code);
        return result;
    }


    /**
     * Check equality based on code, maxStack, and maxLocals.
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
        MethodCodeImpl other = (MethodCodeImpl) obj;
        return maxStack == other.maxStack
                && maxLocals == other.maxLocals
                && Arrays.equals(code, other.code);
    }


    /**
     * Return a string representation of this method code.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        int codeLength = 0;
        if (code != null) {
            codeLength = code.length;
        }
        return "MethodCode[codeLength=" + codeLength + ", maxStack=" + maxStack + ", maxLocals=" + maxLocals + "]";
    }
}
