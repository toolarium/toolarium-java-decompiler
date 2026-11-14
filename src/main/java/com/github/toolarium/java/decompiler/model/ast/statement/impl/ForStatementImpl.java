/*
 * ForStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IForStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import java.util.Objects;


/**
 * Implementation of {@link IForStatement}.
 *
 * @author patrick
 */
public class ForStatementImpl implements IForStatement {
    private final String label;
    private final IStatement init;
    private final IExpression condition;
    private final IExpression update;
    private final IStatement body;


    /**
     * Constructor for ForStatementImpl.
     *
     * @param label     the optional label, or null
     * @param init      the initialization statement, or null
     * @param condition the loop condition expression, or null
     * @param update    the update expression, or null
     * @param body      the loop body statement
     */
    public ForStatementImpl(String label, IStatement init, IExpression condition, IExpression update, IStatement body) {
        this.label = label;
        this.init = init;
        this.condition = condition;
        this.update = update;
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
     * Get the initialization statement.
     *
     * @return the init statement, or null
     */
    @Override
    public IStatement getInit() {
        return init;
    }


    /**
     * Get the for loop condition.
     *
     * @return the condition expression, or null for infinite loop
     */
    @Override
    public IExpression getCondition() {
        return condition;
    }


    /**
     * Get the update expression.
     *
     * @return the update expression, or null
     */
    @Override
    public IExpression getUpdate() {
        return update;
    }


    /**
     * Get the for loop body.
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
        visitor.visitForStatement(this);
    }


    /**
     * Return a hash code value for this ForStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, init, condition, update, body);
    }


    /**
     * Check whether this ForStatementImpl is equal to another object.
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
        ForStatementImpl other = (ForStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(init, other.init)
                && Objects.equals(condition, other.condition)
                && Objects.equals(update, other.update)
                && Objects.equals(body, other.body);
    }


    /**
     * Return a string representation of this ForStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ForStatement[label=" + label + ", init=" + init + ", condition=" + condition
                + ", update=" + update + ", body=" + body + "]";
    }
}
