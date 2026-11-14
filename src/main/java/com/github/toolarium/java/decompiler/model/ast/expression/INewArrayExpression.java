/*
 * INewArrayExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;

import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.List;


/**
 * Represents an array creation expression (e.g., new int[10] or new String[]{"a", "b"}).
 *
 * @author patrick
 */
public interface INewArrayExpression extends IExpression {

    /**
     * Get the element type.
     *
     * @return the array element type
     */
    IType getElementType();


    /**
     * Get the dimension size expressions.
     *
     * @return an unmodifiable list of dimension size expressions
     */
    List<IExpression> getDimensionSizes();


    /**
     * Get the initializer expressions (for array initializers).
     *
     * @return an unmodifiable list of initializer expressions, empty if not an initializer
     */
    List<IExpression> getInitializers();
}
