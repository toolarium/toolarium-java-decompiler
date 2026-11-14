/*
 * ISynchronizedStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;


/**
 * Represents a synchronized block.
 *
 * @author patrick
 */
public interface ISynchronizedStatement extends IStatement {

    /**
     * Get the lock expression.
     *
     * @return the lock expression
     */
    IExpression getLock();


    /**
     * Get the synchronized body.
     *
     * @return the body statement
     */
    IStatement getBody();
}
