/*
 * WhileStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IWhileStatement;
import java.util.Objects;


/**
 * Implementation of {@link IWhileStatement}.
 *
 * @author patrick
 */
public class WhileStatementImpl implements IWhileStatement {
    private final String label;
    private final IExpression condition;
    private final IStatement body;


    /**
     * Constructor for WhileStatementImpl.
     *
     * @param label     the optional label, or null
     * @param condition the loop condition expression
     * @param body      the loop body statement
     */
    public WhileStatementImpl(String label, IExpression condition, IStatement body) {
        this.label = label;
        this.condition = condition;
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
     * Get the while loop condition.
     *
     * @return the condition expression
     */
    @Override
    public IExpression getCondition() {
        return condition;
    }


    /**
     * Get the while loop body.
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
        visitor.visitWhileStatement(this);
    }


    /**
     * Return a hash code value for this WhileStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, condition, body);
    }


    /**
     * Check whether this WhileStatementImpl is equal to another object.
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
        WhileStatementImpl other = (WhileStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(condition, other.condition)
                && Objects.equals(body, other.body);
    }


    /**
     * Return a string representation of this WhileStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "WhileStatement[label=" + label + ", condition=" + condition + ", body=" + body + "]";
    }
}
