/*
 * SynchronizedStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ISynchronizedStatement;
import java.util.Objects;


/**
 * Implementation of {@link ISynchronizedStatement}.
 *
 * @author patrick
 */
public class SynchronizedStatementImpl implements ISynchronizedStatement {
    private final String label;
    private final IExpression lock;
    private final IStatement body;


    /**
     * Constructor for SynchronizedStatementImpl.
     *
     * @param label the optional label, or null
     * @param lock  the lock expression
     * @param body  the synchronized body statement
     */
    public SynchronizedStatementImpl(String label, IExpression lock, IStatement body) {
        this.label = label;
        this.lock = lock;
        this.body = body;
    }


    /**
     * Get an optional label for this statement.
     *
     * @return the label, or null if unlabeled
     */
    @Override
    public String getLabel() {
        return label;
    }


    /**
     * Get the lock expression.
     *
     * @return the lock expression
     */
    @Override
    public IExpression getLock() {
        return lock;
    }


    /**
     * Get the synchronized block body.
     *
     * @return the body statement
     */
    @Override
    public IStatement getBody() {
        return body;
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitSynchronizedStatement(this);
    }


    /**
     * Return a hash code value for this SynchronizedStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, lock, body);
    }


    /**
     * Check whether this SynchronizedStatementImpl is equal to another object.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SynchronizedStatementImpl other = (SynchronizedStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(lock, other.lock)
                && Objects.equals(body, other.body);
    }


    /**
     * Return a string representation of this SynchronizedStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "SynchronizedStatement[label=" + label + ", lock=" + lock + ", body=" + body + "]";
    }
}
