/*
 * IArrayAccessExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;


/**
 * Represents an array element access (e.g., arr[i]).
 *
 * @author patrick
 */
public interface IArrayAccessExpression extends IExpression {

    /**
     * Get the array expression.
     *
     * @return the array expression
     */
    IExpression getArray();


    /**
     * Get the index expression.
     *
     * @return the index expression
     */
    IExpression getIndex();
}
