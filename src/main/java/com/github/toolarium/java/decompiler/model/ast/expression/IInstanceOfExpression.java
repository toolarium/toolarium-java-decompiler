/*
 * IInstanceOfExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;

import com.github.toolarium.java.decompiler.model.type.IType;


/**
 * Represents an instanceof check (e.g., obj instanceof String).
 *
 * @author patrick
 */
public interface IInstanceOfExpression extends IExpression {

    /**
     * Get the expression being checked.
     *
     * @return the operand expression
     */
    IExpression getOperand();


    /**
     * Get the type to check against.
     *
     * @return the check type
     */
    IType getCheckType();


    /**
     * Get the pattern variable name for pattern matching instanceof (Java 16+).
     * Returns null if this is a traditional instanceof.
     *
     * @return the pattern variable name, or null
     */
    String getPatternVariable();
}
