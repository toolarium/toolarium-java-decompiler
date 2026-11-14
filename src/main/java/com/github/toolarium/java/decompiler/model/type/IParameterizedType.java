/*
 * IParameterizedType.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type;

import java.util.List;


/**
 * Represents a parameterized type (e.g., List&lt;String&gt;).
 *
 * @author patrick
 * @see IType
 */
public interface IParameterizedType extends IType {

    /**
     * Get the raw type (e.g., the IObjectType representing List).
     *
     * @return the raw type
     */
    IType getRawType();


    /**
     * Get the type arguments.
     *
     * @return an unmodifiable list of type arguments
     */
    List<IType> getTypeArguments();
}
