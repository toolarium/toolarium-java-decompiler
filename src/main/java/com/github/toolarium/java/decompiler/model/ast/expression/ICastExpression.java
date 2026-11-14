/*
 * ICastExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;

import com.github.toolarium.java.decompiler.model.type.IType;


/**
 * Represents a type cast (e.g., (String) obj).
 *
 * @author patrick
 */
public interface ICastExpression extends IExpression {

    /**
     * Get the expression being cast.
     *
     * @return the operand expression
     */
    IExpression getOperand();


    /**
     * Get the target type.
     *
     * @return the cast target type
     */
    IType getCastType();
}
