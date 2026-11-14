/*
 * VariableExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IVariableExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IVariableExpression}.
 *
 * @author patrick
 */
public class VariableExpressionImpl implements IVariableExpression {
    private final IType type;
    private final String name;
    private final int index;


    /**
     * Constructor for VariableExpressionImpl.
     *
     * @param type  the variable type
     * @param name  the variable name
     * @param index the local variable index
     */
    public VariableExpressionImpl(IType type, String name, int index) {
        this.type = type;
        this.name = name;
        this.index = index;
    }


    /**
     * Get the result type of this variable expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the name of the variable.
     *
     * @return the variable name
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Get the local variable index in the method frame.
     *
     * @return the variable index
     */
    @Override
    public int getIndex() {
        return index;
    }


    /**
     * Accept a visitor for this variable expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitVariableExpression(this);
    }


    /**
     * Compute the hash code based on type, name, and index.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, name, index);
    }


    /**
     * Check equality based on type, name, and index.
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
        VariableExpressionImpl other = (VariableExpressionImpl) obj;
        return index == other.index
                && Objects.equals(type, other.type)
                && Objects.equals(name, other.name);
    }


    /**
     * Return a string representation of this variable expression.
     *
     * @return the variable name
     */
    @Override
    public String toString() {
        return name;
    }
}
