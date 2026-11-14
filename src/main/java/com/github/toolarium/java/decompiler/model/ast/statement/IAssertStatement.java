/*
 * IAssertStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;


/**
 * Represents a Java assert statement, with a condition and optional message expression.
 *
 * @author patrick
 */
public interface IAssertStatement extends IStatement {

    /**
     * Get the assert condition. When this expression evaluates to false, the assertion error is thrown.
     *
     * @return the condition expression
     */
    IExpression getCondition();


    /**
     * Get the optional message expression for the assertion error.
     *
     * @return the message expression, or null if no message
     */
    IExpression getMessage();
}
