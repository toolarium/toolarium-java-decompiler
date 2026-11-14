/*
 * ThrowStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IThrowStatement;
import java.util.Objects;


/**
 * Implementation of {@link IThrowStatement}.
 *
 * @author patrick
 */
public class ThrowStatementImpl implements IThrowStatement {
    private final String label;
    private final IExpression expression;


    /**
     * Constructor for ThrowStatementImpl.
     *
     * @param label      the optional label, or null
     * @param expression the expression being thrown
     */
    public ThrowStatementImpl(String label, IExpression expression) {
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
     * Get the thrown expression.
     *
     * @return the expression being thrown
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
        visitor.visitThrowStatement(this);
    }


    /**
     * Return a hash code value for this ThrowStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, expression);
    }


    /**
     * Check whether this ThrowStatementImpl is equal to another object.
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
        ThrowStatementImpl other = (ThrowStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(expression, other.expression);
    }


    /**
     * Return a string representation of this ThrowStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ThrowStatement[label=" + label + ", expression=" + expression + "]";
    }
}
