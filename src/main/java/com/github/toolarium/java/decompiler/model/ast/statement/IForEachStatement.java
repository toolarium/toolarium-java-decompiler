/*
 * IForEachStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.type.IType;


/**
 * Represents an enhanced for-each loop.
 *
 * @author patrick
 */
public interface IForEachStatement extends IStatement {

    /**
     * Get the loop variable type.
     *
     * @return the variable type
     */
    IType getVariableType();


    /**
     * Get the loop variable name.
     *
     * @return the variable name
     */
    String getVariableName();


    /**
     * Get the iterable expression.
     *
     * @return the iterable expression
     */
    IExpression getIterable();


    /**
     * Get the loop body.
     *
     * @return the body statement
     */
    IStatement getBody();
}
