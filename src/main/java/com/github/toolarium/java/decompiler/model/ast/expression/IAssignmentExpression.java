/*
 * IAssignmentExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;


/**
 * Represents an assignment expression (e.g., x = 5, arr[i] = val).
 *
 * @author patrick
 */
public interface IAssignmentExpression extends IExpression {

    /**
     * Get the left-hand side (assignment target).
     *
     * @return the target expression
     */
    IExpression getTarget();


    /**
     * Get the right-hand side (value being assigned).
     *
     * @return the value expression
     */
    IExpression getValue();
}
