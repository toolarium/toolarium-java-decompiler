/*
 * UnaryExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IUnaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.UnaryOperator;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IUnaryExpression}.
 *
 * @author patrick
 */
public class UnaryExpressionImpl implements IUnaryExpression {
    private final IType type;
    private final IExpression operand;
    private final UnaryOperator operator;


    /**
     * Constructor for UnaryExpressionImpl.
     *
     * @param type     the result type
     * @param operand  the operand expression
     * @param operator the unary operator
     */
    public UnaryExpressionImpl(IType type, IExpression operand, UnaryOperator operator) {
        this.type = type;
        this.operand = operand;
        this.operator = operator;
    }


    /**
     * Get the result type of this unary expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the operand of this unary expression.
     *
     * @return the operand expression
     */
    @Override
    public IExpression getOperand() {
        return operand;
    }


    /**
     * Get the unary operator applied to the operand.
     *
     * @return the unary operator
     */
    @Override
    public UnaryOperator getOperator() {
        return operator;
    }


    /**
     * Accept a visitor for this unary expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitUnaryExpression(this);
    }


    /**
     * Compute the hash code based on type, operand, and operator.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, operand, operator);
    }


    /**
     * Check equality based on type, operand, and operator.
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
        UnaryExpressionImpl other = (UnaryExpressionImpl) obj;
        return Objects.equals(type, other.type)
                && Objects.equals(operand, other.operand)
                && operator == other.operator;
    }


    /**
     * Return a string representation of this unary expression.
     *
     * @return the string representation in the form "operator operand"
     */
    @Override
    public String toString() {
        return operator.getSymbol() + operand;
    }
}
