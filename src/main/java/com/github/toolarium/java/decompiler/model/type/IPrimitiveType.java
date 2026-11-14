/*
 * IPrimitiveType.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type;


/**
 * Represents a Java primitive type (int, long, boolean, etc.) or void.
 *
 * @author patrick
 * @see IType
 */
public interface IPrimitiveType extends IType {

    /**
     * Get the JVM type character ('I', 'J', 'Z', 'B', 'C', 'S', 'F', 'D', 'V').
     *
     * @return the type character
     */
    char getTypeChar();


    /**
     * Get the operand stack size: 1 for most types, 2 for long and double.
     *
     * @return the stack size
     */
    int getSize();
}
