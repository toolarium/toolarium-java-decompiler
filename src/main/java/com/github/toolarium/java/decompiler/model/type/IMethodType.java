/*
 * IMethodType.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type;

import java.util.List;


/**
 * Represents a method descriptor type.
 *
 * @author patrick
 * @see IType
 */
public interface IMethodType extends IType {

    /**
     * Get the return type.
     *
     * @return the return type
     */
    IType getReturnType();


    /**
     * Get the parameter types.
     *
     * @return an unmodifiable list of parameter types
     */
    List<IType> getParameterTypes();
}
