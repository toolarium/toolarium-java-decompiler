/*
 * IGenericType.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type;


/**
 * Represents a generic type variable (e.g., "T", "E").
 *
 * @author patrick
 * @see IType
 */
public interface IGenericType extends IType {

    /**
     * Get the type variable name.
     *
     * @return the type variable name
     */
    String getTypeVariable();


    /**
     * Get the upper bound of this type variable.
     *
     * @return the upper bound, or null if unbounded (defaults to Object)
     */
    IType getBound();
}
