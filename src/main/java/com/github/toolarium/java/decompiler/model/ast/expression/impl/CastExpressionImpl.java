/*
 * CastExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.ICastExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link ICastExpression}.
 *
 * @author patrick
 */
public class CastExpressionImpl implements ICastExpression {
    private final IType type;
    private final IExpression operand;
    private final IType castType;


    /**
     * Constructor for CastExpressionImpl.
     *
     * @param type     the expression result type
     * @param operand  the expression being cast
     * @param castType the target cast type
     */
    public CastExpressionImpl(IType type, IExpression operand, IType castType) {
        this.type = type;
        this.operand = operand;
        this.castType = castType;
    }


    /**
     * Get the result type of this cast expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the expression being cast.
     *
     * @return the operand expression
     */
    @Override
    public IExpression getOperand() {
        return operand;
    }


    /**
     * Get the target type of the cast.
     *
     * @return the cast target type
     */
    @Override
    public IType getCastType() {
        return castType;
    }


    /**
     * Accept a visitor for this cast expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitCastExpression(this);
    }


    /**
     * Compute the hash code based on type, operand, and cast type.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, operand, castType);
    }


    /**
     * Check equality based on type, operand, and cast type.
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
        CastExpressionImpl other = (CastExpressionImpl) obj;
        return Objects.equals(type, other.type)
                && Objects.equals(operand, other.operand)
                && Objects.equals(castType, other.castType);
    }


    /**
     * Return a string representation of this cast expression.
     *
     * @return the string representation in the form "(castType) operand"
     */
    @Override
    public String toString() {
        return "(" + castType.getDisplayName() + ") " + operand;
    }
}
