/*
 * PrimitiveTypeImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type.impl;

import com.github.toolarium.java.decompiler.model.type.IPrimitiveType;
import java.util.Objects;


/**
 * Implementation of {@link IPrimitiveType}.
 *
 * @author patrick
 */
public final class PrimitiveTypeImpl implements IPrimitiveType {
    /** The boolean type. */
    public static final PrimitiveTypeImpl BOOLEAN = new PrimitiveTypeImpl('Z');

    /** The byte type. */
    public static final PrimitiveTypeImpl BYTE = new PrimitiveTypeImpl('B');

    /** The char type. */
    public static final PrimitiveTypeImpl CHAR = new PrimitiveTypeImpl('C');

    /** The short type. */
    public static final PrimitiveTypeImpl SHORT = new PrimitiveTypeImpl('S');

    /** The int type. */
    public static final PrimitiveTypeImpl INT = new PrimitiveTypeImpl('I');

    /** The long type. */
    public static final PrimitiveTypeImpl LONG = new PrimitiveTypeImpl('J');

    /** The float type. */
    public static final PrimitiveTypeImpl FLOAT = new PrimitiveTypeImpl('F');

    /** The double type. */
    public static final PrimitiveTypeImpl DOUBLE = new PrimitiveTypeImpl('D');

    /** The void type. */
    public static final PrimitiveTypeImpl VOID = new PrimitiveTypeImpl('V');

    private final char typeChar;


    /**
     * Constructor for PrimitiveTypeImpl.
     *
     * @param typeChar the JVM type character
     */
    private PrimitiveTypeImpl(char typeChar) {
        this.typeChar = typeChar;
    }


    /**
     * Get the JVM type character ('I', 'J', 'Z', 'B', 'C', 'S', 'F', 'D', 'V').
     *
     * @return the type character
     */
    @Override
    public char getTypeChar() {
        return typeChar;
    }


    /**
     * Get the operand stack size: 1 for most types, 2 for long and double.
     *
     * @return the stack size
     */
    @Override
    public int getSize() {
        if (typeChar == 'J' || typeChar == 'D') {
            return 2;
        }
        return 1;
    }


    /**
     * Get the JVM type descriptor for this primitive type.
     *
     * @return the single-character type descriptor
     */
    @Override
    public String getDescriptor() {
        return String.valueOf(typeChar);
    }


    /**
     * Get the human-readable display name for this primitive type.
     *
     * @return the display name (e.g., "int", "boolean", "void")
     */
    @Override
    public String getDisplayName() {
        switch (typeChar) {
            case 'Z': return "boolean";
            case 'B': return "byte";
            case 'C': return "char";
            case 'S': return "short";
            case 'I': return "int";
            case 'J': return "long";
            case 'F': return "float";
            case 'D': return "double";
            case 'V': return "void";
            default: return String.valueOf(typeChar);
        }
    }


    /**
     * Check whether this is a primitive type. Always returns true.
     *
     * @return true
     */
    @Override
    public boolean isPrimitive() {
        return true;
    }


    /**
     * Check whether this is an array type. Always returns false.
     *
     * @return false
     */
    @Override
    public boolean isArray() {
        return false;
    }


    /**
     * Check whether this is the void type.
     *
     * @return true if the type character is 'V'
     */
    @Override
    public boolean isVoid() {
        return typeChar == 'V';
    }


    /**
     * Get the number of array dimensions. Always returns 0.
     *
     * @return 0
     */
    @Override
    public int getDimensions() {
        return 0;
    }


    /**
     * Compute the hash code based on the type character.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(typeChar);
    }


    /**
     * Check equality based on the type character.
     *
     * @param obj the object to compare with
     * @return true if the other object is a PrimitiveTypeImpl with the same type character
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PrimitiveTypeImpl other = (PrimitiveTypeImpl) obj;
        return typeChar == other.typeChar;
    }


    /**
     * Return a string representation including the display name.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "PrimitiveType[" + getDisplayName() + "]";
    }
}
