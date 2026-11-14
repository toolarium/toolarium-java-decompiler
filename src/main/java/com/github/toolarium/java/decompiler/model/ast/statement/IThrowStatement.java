/*
 * IThrowStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;


/**
 * Represents a throw statement.
 *
 * @author patrick
 */
public interface IThrowStatement extends IStatement {

    /**
     * Get the thrown expression.
     *
     * @return the expression being thrown
     */
    IExpression getExpression();
}
