/*
 * BinaryExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.BinaryOperator;
import com.github.toolarium.java.decompiler.model.ast.expression.IBinaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IBinaryExpression}.
 *
 * @author patrick
 */
public class BinaryExpressionImpl implements IBinaryExpression {
    private final IType type;
    private final IExpression left;
    private final IExpression right;
    private final BinaryOperator operator;


    /**
     * Constructor for BinaryExpressionImpl.
     *
     * @param type     the result type
     * @param left     the left operand
     * @param right    the right operand
     * @param operator the binary operator
     */
    public BinaryExpressionImpl(IType type, IExpression left, IExpression right, BinaryOperator operator) {
        this.type = type;
        this.left = left;
        this.right = right;
        this.operator = operator;
    }


    /**
     * Get the result type of this binary expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the left operand of this binary expression.
     *
     * @return the left expression
     */
    @Override
    public IExpression getLeft() {
        return left;
    }


    /**
     * Get the right operand of this binary expression.
     *
     * @return the right expression
     */
    @Override
    public IExpression getRight() {
        return right;
    }


    /**
     * Get the binary operator applied to the operands.
     *
     * @return the binary operator
     */
    @Override
    public BinaryOperator getOperator() {
        return operator;
    }


    /**
     * Accept a visitor for this binary expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitBinaryExpression(this);
    }


    /**
     * Compute the hash code based on type, left, right, and operator.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, left, right, operator);
    }


    /**
     * Check equality based on type, left, right, and operator.
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
        BinaryExpressionImpl other = (BinaryExpressionImpl) obj;
        return Objects.equals(type, other.type)
                && Objects.equals(left, other.left)
                && Objects.equals(right, other.right)
                && operator == other.operator;
    }


    /**
     * Return a string representation of this binary expression.
     *
     * @return the string representation in the form "(left operator right)"
     */
    @Override
    public String toString() {
        return "(" + left + " " + operator.getSymbol() + " " + right + ")";
    }
}
