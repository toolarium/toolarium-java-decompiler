/*
 * ConstantExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IConstantExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IConstantExpression}.
 *
 * @author patrick
 */
public class ConstantExpressionImpl implements IConstantExpression {
    private final IType type;
    private final Object value;


    /**
     * Constructor for ConstantExpressionImpl.
     *
     * @param type  the expression type
     * @param value the constant value
     */
    public ConstantExpressionImpl(IType type, Object value) {
        this.type = type;
        this.value = value;
    }


    /**
     * Get the result type of this constant expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the constant value held by this expression.
     *
     * @return the value (Integer, Long, Float, Double, String, null, Boolean, or IType for class literals)
     */
    @Override
    public Object getValue() {
        return value;
    }


    /**
     * Accept a visitor for this constant expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitConstantExpression(this);
    }


    /**
     * Compute the hash code based on type and value.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }


    /**
     * Check equality based on type and value.
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
        ConstantExpressionImpl other = (ConstantExpressionImpl) obj;
        return Objects.equals(type, other.type) && Objects.equals(value, other.value);
    }


    /**
     * Return a string representation of this constant expression.
     *
     * @return the string value of the constant
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
