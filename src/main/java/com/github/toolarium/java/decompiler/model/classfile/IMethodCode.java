/*
 * IMethodCode.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile;


/**
 * Represents the code attribute of a method, including the raw bytecode
 * and the maximum stack depth and local variable count.
 *
 * @author patrick
 */
public interface IMethodCode {

    /**
     * Get a defensive copy of the raw bytecode.
     *
     * @return the bytecode array, or null for abstract or native methods
     */
    byte[] getCode();


    /**
     * Get the maximum operand stack depth.
     *
     * @return the max stack depth
     */
    int getMaxStack();


    /**
     * Get the maximum number of local variables.
     *
     * @return the max local variable count
     */
    int getMaxLocals();
}
