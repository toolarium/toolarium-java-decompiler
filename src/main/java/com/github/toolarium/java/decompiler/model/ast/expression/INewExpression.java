/*
 * INewExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;

import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.List;


/**
 * Represents an object creation expression (e.g., new ArrayList()).
 *
 * @author patrick
 */
public interface INewExpression extends IExpression {

    /**
     * Get the type being instantiated.
     *
     * @return the instantiated type
     */
    IType getInstantiatedType();


    /**
     * Get the constructor arguments.
     *
     * @return an unmodifiable list of argument expressions
     */
    List<IExpression> getArguments();
}
