/*
 * ForEachStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IForEachStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IForEachStatement}.
 *
 * @author patrick
 */
public class ForEachStatementImpl implements IForEachStatement {
    private final String label;
    private final IType variableType;
    private final String variableName;
    private final IExpression iterable;
    private final IStatement body;


    /**
     * Constructor for ForEachStatementImpl.
     *
     * @param label        the optional label, or null
     * @param variableType the loop variable type
     * @param variableName the loop variable name
     * @param iterable     the iterable expression
     * @param body         the loop body statement
     */
    public ForEachStatementImpl(String label, IType variableType, String variableName, IExpression iterable, IStatement body) {
        this.label = label;
        this.variableType = variableType;
        this.variableName = variableName;
        this.iterable = iterable;
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
     * Get the loop variable type.
     *
     * @return the variable type
     */
    @Override
    public IType getVariableType() {
        return variableType;
    }


    /**
     * Get the loop variable name.
     *
     * @return the variable name
     */
    @Override
    public String getVariableName() {
        return variableName;
    }


    /**
     * Get the iterable expression.
     *
     * @return the iterable expression
     */
    @Override
    public IExpression getIterable() {
        return iterable;
    }


    /**
     * Get the for-each loop body.
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
        visitor.visitForEachStatement(this);
    }


    /**
     * Return a hash code value for this ForEachStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, variableType, variableName, iterable, body);
    }


    /**
     * Check whether this ForEachStatementImpl is equal to another object.
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
        ForEachStatementImpl other = (ForEachStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(variableType, other.variableType)
                && Objects.equals(variableName, other.variableName)
                && Objects.equals(iterable, other.iterable)
                && Objects.equals(body, other.body);
    }


    /**
     * Return a string representation of this ForEachStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ForEachStatement[label=" + label + ", variableType=" + variableType
                + ", variableName=" + variableName + ", iterable=" + iterable + ", body=" + body + "]";
    }
}
