/*
 * WildcardTypeImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type.impl;

import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.IWildcardType;
import java.util.Objects;


/**
 * Implementation of {@link IWildcardType}.
 *
 * @author patrick
 */
public class WildcardTypeImpl implements IWildcardType {
    private final IType bound;
    private final boolean upperBound;


    /**
     * Constructor for WildcardTypeImpl.
     *
     * @param bound      the bound type, or null for an unbounded wildcard
     * @param upperBound true for upper-bounded or unbounded, false for lower-bounded
     */
    public WildcardTypeImpl(IType bound, boolean upperBound) {
        this.bound = bound;
        this.upperBound = upperBound;
    }


    /**
     * Get the bound of this wildcard type.
     *
     * @return the bound type, or null for an unbounded wildcard
     */
    @Override
    public IType getBound() {
        return bound;
    }


    /**
     * Check whether this is an upper-bounded wildcard.
     *
     * @return true for unbounded or upper-bounded, false for lower-bounded
     */
    @Override
    public boolean isUpperBound() {
        return upperBound;
    }


    /**
     * Get the JVM signature character for this wildcard.
     *
     * @return "*" for unbounded, "+" plus the bound descriptor for upper-bounded,
     *         "-" plus the bound descriptor for lower-bounded
     */
    @Override
    public String getDescriptor() {
        if (bound == null) {
            return "*";
        }
        if (upperBound) {
            return "+" + bound.getDescriptor();
        }
        return "-" + bound.getDescriptor();
    }


    /**
     * Get the human-readable display name.
     *
     * @return "?" for unbounded, "? extends X" for upper-bounded, "? super X" for lower-bounded
     */
    @Override
    public String getDisplayName() {
        if (bound == null) {
            return "?";
        }
        if (upperBound) {
            return "? extends " + bound.getDisplayName();
        }
        return "? super " + bound.getDisplayName();
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
     * Compute the hash code based on the bound and bound direction.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(bound, upperBound);
    }


    /**
     * Check equality based on the bound and bound direction.
     *
     * @param obj the object to compare with
     * @return true if the other object is a WildcardTypeImpl with equal bound and direction
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        WildcardTypeImpl other = (WildcardTypeImpl) obj;
        return upperBound == other.upperBound && Objects.equals(bound, other.bound);
    }


    /**
     * Return a string representation including the wildcard display name.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "WildcardType[" + getDisplayName() + "]";
    }
}
