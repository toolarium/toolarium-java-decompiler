/*
 * ExpressionStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IExpressionStatement;
import java.util.Objects;


/**
 * Implementation of {@link IExpressionStatement}.
 *
 * @author patrick
 */
public class ExpressionStatementImpl implements IExpressionStatement {
    private final String label;
    private final IExpression expression;


    /**
     * Constructor for ExpressionStatementImpl.
     *
     * @param label      the optional label, or null
     * @param expression the expression
     */
    public ExpressionStatementImpl(String label, IExpression expression) {
        this.label = label;
        this.expression = expression;
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
     * Get the expression.
     *
     * @return the expression
     */
    @Override
    public IExpression getExpression() {
        return expression;
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitExpressionStatement(this);
    }


    /**
     * Return a hash code value for this ExpressionStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, expression);
    }


    /**
     * Check whether this ExpressionStatementImpl is equal to another object.
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
        ExpressionStatementImpl other = (ExpressionStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(expression, other.expression);
    }


    /**
     * Return a string representation of this ExpressionStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ExpressionStatement[label=" + label + ", expression=" + expression + "]";
    }
}
