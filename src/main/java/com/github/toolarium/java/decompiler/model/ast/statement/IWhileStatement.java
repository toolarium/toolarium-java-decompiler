/*
 * IWhileStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;


/**
 * Represents a while loop.
 *
 * @author patrick
 */
public interface IWhileStatement extends IStatement {

    /**
     * Get the loop condition.
     *
     * @return the condition expression
     */
    IExpression getCondition();


    /**
     * Get the loop body.
     *
     * @return the body statement
     */
    IStatement getBody();
}
