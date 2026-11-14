/*
 * IVariableExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;


/**
 * Represents a reference to a local variable or parameter.
 *
 * @author patrick
 */
public interface IVariableExpression extends IExpression {

    /**
     * Get the variable name.
     *
     * @return the variable name
     */
    String getName();


    /**
     * Get the local variable index.
     *
     * @return the variable index
     */
    int getIndex();
}
