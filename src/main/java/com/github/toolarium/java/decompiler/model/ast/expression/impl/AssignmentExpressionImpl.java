/*
 * AssignmentExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IAssignmentExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IAssignmentExpression}.
 *
 * @author patrick
 */
public class AssignmentExpressionImpl implements IAssignmentExpression {
    private final IType type;
    private final IExpression target;
    private final IExpression value;


    /**
     * Constructor for AssignmentExpressionImpl.
     *
     * @param type   the expression result type
     * @param target the left-hand side (assignment target)
     * @param value  the right-hand side (value being assigned)
     */
    public AssignmentExpressionImpl(IType type, IExpression target, IExpression value) {
        this.type = type;
        this.target = target;
        this.value = value;
    }


    /**
     * Get the result type of this assignment expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the left-hand side (assignment target).
     *
     * @return the target expression
     */
    @Override
    public IExpression getTarget() {
        return target;
    }


    /**
     * Get the right-hand side (value being assigned).
     *
     * @return the value expression
     */
    @Override
    public IExpression getValue() {
        return value;
    }


    /**
     * Accept a visitor for this assignment expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitAssignmentExpression(this);
    }


    /**
     * Compute the hash code based on type, target, and value.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, target, value);
    }


    /**
     * Check equality based on type, target, and value.
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
        AssignmentExpressionImpl other = (AssignmentExpressionImpl) obj;
        return Objects.equals(type, other.type)
                && Objects.equals(target, other.target)
                && Objects.equals(value, other.value);
    }


    /**
     * Return a string representation of this assignment expression.
     *
     * @return the string representation in the form "target = value"
     */
    @Override
    public String toString() {
        return target + " = " + value;
    }
}
