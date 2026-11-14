/*
 * AssertStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IAssertStatement;
import java.util.Objects;


/**
 * Implementation of {@link IAssertStatement}.
 *
 * @author patrick
 */
public class AssertStatementImpl implements IAssertStatement {
    private final String label;
    private final IExpression condition;
    private final IExpression message;


    /**
     * Constructor for AssertStatementImpl.
     *
     * @param label     the optional label, or null
     * @param condition the condition expression
     * @param message   the optional message expression, or null
     */
    public AssertStatementImpl(String label, IExpression condition, IExpression message) {
        this.label = label;
        this.condition = condition;
        this.message = message;
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
     * Get the assert condition.
     *
     * @return the condition expression
     */
    @Override
    public IExpression getCondition() {
        return condition;
    }


    /**
     * Get the optional message expression for the assertion error.
     *
     * @return the message expression, or null if no message
     */
    @Override
    public IExpression getMessage() {
        return message;
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitAssertStatement(this);
    }


    /**
     * Return a hash code value for this AssertStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, condition, message);
    }


    /**
     * Check whether this AssertStatementImpl is equal to another object.
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
        AssertStatementImpl other = (AssertStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(condition, other.condition)
                && Objects.equals(message, other.message);
    }


    /**
     * Return a string representation of this AssertStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "AssertStatement[label=" + label + ", condition=" + condition + ", message=" + message + "]";
    }
}
