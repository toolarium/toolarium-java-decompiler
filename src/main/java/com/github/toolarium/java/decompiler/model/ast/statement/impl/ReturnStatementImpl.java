/*
 * ReturnStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IReturnStatement;
import java.util.Objects;


/**
 * Implementation of {@link IReturnStatement}.
 *
 * @author patrick
 */
public class ReturnStatementImpl implements IReturnStatement {
    private final String label;
    private final IExpression value;


    /**
     * Constructor for ReturnStatementImpl.
     *
     * @param label the optional label, or null
     * @param value the return expression, or null for void return
     */
    public ReturnStatementImpl(String label, IExpression value) {
        this.label = label;
        this.value = value;
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
     * Get the return value expression.
     *
     * @return the return expression, or null for void return
     */
    @Override
    public IExpression getValue() {
        return value;
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitReturnStatement(this);
    }


    /**
     * Return a hash code value for this ReturnStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, value);
    }


    /**
     * Check whether this ReturnStatementImpl is equal to another object.
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
        ReturnStatementImpl other = (ReturnStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(value, other.value);
    }


    /**
     * Return a string representation of this ReturnStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ReturnStatement[label=" + label + ", value=" + value + "]";
    }
}
