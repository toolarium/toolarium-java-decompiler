/*
 * IfStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IIfStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import java.util.Objects;


/**
 * Implementation of {@link IIfStatement}.
 *
 * @author patrick
 */
public class IfStatementImpl implements IIfStatement {
    private final String label;
    private final IExpression condition;
    private final IStatement thenBranch;
    private final IStatement elseBranch;


    /**
     * Constructor for IfStatementImpl.
     *
     * @param label      the optional label, or null
     * @param condition  the condition expression
     * @param thenBranch the then branch statement
     * @param elseBranch the else branch statement, or null if no else
     */
    public IfStatementImpl(String label, IExpression condition, IStatement thenBranch, IStatement elseBranch) {
        this.label = label;
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
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
     * Get the if condition expression.
     *
     * @return the condition expression
     */
    @Override
    public IExpression getCondition() {
        return condition;
    }


    /**
     * Get the then branch.
     *
     * @return the then statement
     */
    @Override
    public IStatement getThenBranch() {
        return thenBranch;
    }


    /**
     * Get the else branch.
     *
     * @return the else statement, or null if no else
     */
    @Override
    public IStatement getElseBranch() {
        return elseBranch;
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitIfStatement(this);
    }


    /**
     * Return a hash code value for this IfStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, condition, thenBranch, elseBranch);
    }


    /**
     * Check whether this IfStatementImpl is equal to another object.
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
        IfStatementImpl other = (IfStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(condition, other.condition)
                && Objects.equals(thenBranch, other.thenBranch)
                && Objects.equals(elseBranch, other.elseBranch);
    }


    /**
     * Return a string representation of this IfStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "IfStatement[label=" + label + ", condition=" + condition
                + ", thenBranch=" + thenBranch + ", elseBranch=" + elseBranch + "]";
    }
}
