/*
 * ArrayTypeImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type.impl;

import com.github.toolarium.java.decompiler.model.type.IArrayType;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IArrayType}.
 *
 * @author patrick
 */
public class ArrayTypeImpl implements IArrayType {
    private final IType elementType;
    private final int dimensions;


    /**
     * Constructor for ArrayTypeImpl.
     *
     * @param elementType the element type
     * @param dimensions  the number of array dimensions
     * @throws IllegalArgumentException if dimensions is less than 1
     */
    public ArrayTypeImpl(IType elementType, int dimensions) {
        this.elementType = Objects.requireNonNull(elementType, "Element type must not be null");
        if (dimensions < 1) {
            throw new IllegalArgumentException("Array dimensions must be at least 1");
        }
        this.dimensions = dimensions;
    }


    /**
     * Get the element type. For multi-dimensional arrays this may itself be an array type.
     *
     * @return the element type
     */
    @Override
    public IType getElementType() {
        return elementType;
    }


    /**
     * Get the innermost non-array component type by unwrapping nested array types.
     *
     * @return the base type
     */
    @Override
    public IType getBaseType() {
        IType current = elementType;
        while (current instanceof IArrayType) {
            current = ((IArrayType) current).getElementType();
        }
        return current;
    }


    /**
     * Get the JVM type descriptor with leading '[' characters for each dimension.
     *
     * @return the type descriptor (e.g., "[[I" for int[][])
     */
    @Override
    public String getDescriptor() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dimensions; i++) {
            sb.append('[');
        }
        sb.append(elementType.getDescriptor());
        return sb.toString();
    }


    /**
     * Get the human-readable display name with trailing "[]" for each dimension.
     *
     * @return the display name (e.g., "int[][]")
     */
    @Override
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder(elementType.getDisplayName());
        for (int i = 0; i < dimensions; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }


    /**
     * Check whether this is a primitive type. Always returns false.
     *
     * @return false
     */
    @Override
    public boolean isPrimitive() {
        return false;
    }


    /**
     * Check whether this is an array type. Always returns true.
     *
     * @return true
     */
    @Override
    public boolean isArray() {
        return true;
    }


    /**
     * Check whether this is the void type. Always returns false.
     *
     * @return false
     */
    @Override
    public boolean isVoid() {
        return false;
    }


    /**
     * Get the number of array dimensions.
     *
     * @return the number of dimensions (at least 1)
     */
    @Override
    public int getDimensions() {
        return dimensions;
    }


    /**
     * Compute the hash code based on the element type and dimensions.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(elementType, dimensions);
    }


    /**
     * Check equality based on the element type and number of dimensions.
     *
     * @param obj the object to compare with
     * @return true if the other object is an ArrayTypeImpl with equal element type and dimensions
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ArrayTypeImpl other = (ArrayTypeImpl) obj;
        return dimensions == other.dimensions && Objects.equals(elementType, other.elementType);
    }


    /**
     * Return a string representation including the array display name.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ArrayType[" + getDisplayName() + "]";
    }
}
