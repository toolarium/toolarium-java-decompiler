/*
 * IExpressionStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;


/**
 * Represents a statement that consists of a single expression (e.g., method call, assignment).
 *
 * @author patrick
 */
public interface IExpressionStatement extends IStatement {

    /**
     * Get the expression.
     *
     * @return the expression
     */
    IExpression getExpression();
}
