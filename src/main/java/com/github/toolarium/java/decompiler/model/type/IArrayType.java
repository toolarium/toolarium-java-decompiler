/*
 * IArrayType.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type;


/**
 * Represents an array type.
 *
 * @author patrick
 * @see IType
 */
public interface IArrayType extends IType {

    /**
     * Get the element type. For multi-dimensional arrays this may itself be an array type.
     *
     * @return the element type
     */
    IType getElementType();


    /**
     * Get the innermost non-array component type.
     *
     * @return the base type
     */
    IType getBaseType();
}
