/*
 * IUnaryExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;


/**
 * Represents a unary operation (e.g., -x, !flag, ++i).
 *
 * @author patrick
 */
public interface IUnaryExpression extends IExpression {

    /**
     * Get the operand.
     *
     * @return the operand expression
     */
    IExpression getOperand();


    /**
     * Get the operator.
     *
     * @return the unary operator
     */
    UnaryOperator getOperator();
}
