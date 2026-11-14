/*
 * GenericTypeImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type.impl;

import com.github.toolarium.java.decompiler.model.type.IGenericType;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IGenericType}.
 *
 * @author patrick
 */
public class GenericTypeImpl implements IGenericType {
    private final String typeVariable;
    private final IType bound;


    /**
     * Constructor for GenericTypeImpl.
     *
     * @param typeVariable the type variable name (e.g., "T", "E")
     * @param bound        the upper bound (null if unbounded)
     */
    public GenericTypeImpl(String typeVariable, IType bound) {
        this.typeVariable = Objects.requireNonNull(typeVariable, "Type variable must not be null");
        this.bound = bound;
    }


    /**
     * Get the type variable name (e.g., "T", "E").
     *
     * @return the type variable name
     */
    @Override
    public String getTypeVariable() {
        return typeVariable;
    }


    /**
     * Get the upper bound of this type variable.
     *
     * @return the upper bound, or null if unbounded (defaults to Object)
     */
    @Override
    public IType getBound() {
        return bound;
    }


    /**
     * Get the JVM type descriptor for this generic type variable.
     *
     * @return the generic type descriptor (e.g., "TT;")
     */
    @Override
    public String getDescriptor() {
        return "T" + typeVariable + ";";
    }


    /**
     * Get the human-readable display name, which is the type variable name.
     *
     * @return the type variable name
     */
    @Override
    public String getDisplayName() {
        return typeVariable;
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
     * Check whether this is an array type. Always returns false.
     *
     * @return false
     */
    @Override
    public boolean isArray() {
        return false;
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
     * Get the number of array dimensions. Always returns 0.
     *
     * @return 0
     */
    @Override
    public int getDimensions() {
        return 0;
    }


    /**
     * Compute the hash code based on the type variable and bound.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(typeVariable, bound);
    }


    /**
     * Check equality based on the type variable name and bound.
     *
     * @param obj the object to compare with
     * @return true if the other object is a GenericTypeImpl with equal type variable and bound
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GenericTypeImpl other = (GenericTypeImpl) obj;
        return Objects.equals(typeVariable, other.typeVariable)
                && Objects.equals(bound, other.bound);
    }


    /**
     * Return a string representation including the type variable and optional bound.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        if (bound != null) {
            return "GenericType[" + typeVariable + " extends " + bound.getDisplayName() + "]";
        }
        return "GenericType[" + typeVariable + "]";
    }
}
