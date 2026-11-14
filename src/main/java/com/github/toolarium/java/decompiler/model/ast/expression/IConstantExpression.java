/*
 * IConstantExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;


/**
 * Represents a constant literal (integer, long, float, double, string, null, boolean, class).
 *
 * @author patrick
 */
public interface IConstantExpression extends IExpression {

    /**
     * Get the constant value.
     *
     * @return the value (Integer, Long, Float, Double, String, null, Boolean, or IType for class literals)
     */
    Object getValue();
}
