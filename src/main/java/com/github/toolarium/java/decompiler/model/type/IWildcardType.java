/*
 * IWildcardType.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type;


/**
 * Represents a wildcard type argument used in parameterized types
 * (e.g., {@code ?}, {@code ? extends Number}, {@code ? super String}).
 *
 * @author patrick
 * @see IType
 * @see IParameterizedType
 */
public interface IWildcardType extends IType {

    /**
     * Get the bound of this wildcard type.
     *
     * @return the bound type, or null for an unbounded wildcard ({@code ?})
     */
    IType getBound();


    /**
     * Check whether this is an upper-bounded wildcard ({@code ? extends X}).
     * For unbounded wildcards, this method returns true by convention.
     *
     * @return true for unbounded or upper-bounded wildcards, false for lower-bounded ({@code ? super X})
     */
    boolean isUpperBound();
}
