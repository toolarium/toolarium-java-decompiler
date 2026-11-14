/*
 * IIfStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;


/**
 * Represents an if-else statement.
 *
 * @author patrick
 */
public interface IIfStatement extends IStatement {

    /**
     * Get the condition expression.
     *
     * @return the condition
     */
    IExpression getCondition();


    /**
     * Get the then branch.
     *
     * @return the then statement
     */
    IStatement getThenBranch();


    /**
     * Get the else branch (null if no else).
     *
     * @return the else statement, or null
     */
    IStatement getElseBranch();
}
