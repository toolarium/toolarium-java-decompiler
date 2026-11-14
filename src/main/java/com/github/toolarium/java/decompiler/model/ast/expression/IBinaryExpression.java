/*
 * IBinaryExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;


/**
 * Represents a binary operation (e.g., a + b, x == y, flag &amp;&amp; other).
 *
 * @author patrick
 */
public interface IBinaryExpression extends IExpression {

    /**
     * Get the left operand.
     *
     * @return the left expression
     */
    IExpression getLeft();


    /**
     * Get the right operand.
     *
     * @return the right expression
     */
    IExpression getRight();


    /**
     * Get the operator.
     *
     * @return the binary operator
     */
    BinaryOperator getOperator();
}
