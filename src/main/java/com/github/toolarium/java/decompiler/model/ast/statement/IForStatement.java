/*
 * IForStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;


/**
 * Represents a for loop.
 *
 * @author patrick
 */
public interface IForStatement extends IStatement {

    /**
     * Get the initialization statement (may be null).
     *
     * @return the init statement, or null
     */
    IStatement getInit();


    /**
     * Get the loop condition (may be null for infinite loop).
     *
     * @return the condition expression, or null
     */
    IExpression getCondition();


    /**
     * Get the update expression (may be null).
     *
     * @return the update expression, or null
     */
    IExpression getUpdate();


    /**
     * Get the loop body.
     *
     * @return the body statement
     */
    IStatement getBody();
}
