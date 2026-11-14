/*
 * DoWhileStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IDoWhileStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import java.util.Objects;


/**
 * Implementation of {@link IDoWhileStatement}.
 *
 * @author patrick
 */
public class DoWhileStatementImpl implements IDoWhileStatement {
    private final String label;
    private final IExpression condition;
    private final IStatement body;


    /**
     * Constructor for DoWhileStatementImpl.
     *
     * @param label     the optional label, or null
     * @param condition the loop condition expression
     * @param body      the loop body statement
     */
    public DoWhileStatementImpl(String label, IExpression condition, IStatement body) {
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
     * Get the do-while loop condition.
     *
     * @return the condition expression
     */
    @Override
    public IExpression getCondition() {
        return condition;
    }


    /**
     * Get the do-while loop body.
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
        visitor.visitDoWhileStatement(this);
    }


    /**
     * Return a hash code value for this DoWhileStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, condition, body);
    }


    /**
     * Check whether this DoWhileStatementImpl is equal to another object.
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
        DoWhileStatementImpl other = (DoWhileStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(condition, other.condition)
                && Objects.equals(body, other.body);
    }


    /**
     * Return a string representation of this DoWhileStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "DoWhileStatement[label=" + label + ", condition=" + condition + ", body=" + body + "]";
    }
}
