/*
 * IReturnStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;


/**
 * Represents a return statement.
 *
 * @author patrick
 */
public interface IReturnStatement extends IStatement {

    /**
     * Get the return value expression (null for void return).
     *
     * @return the return expression, or null
     */
    IExpression getValue();
}
