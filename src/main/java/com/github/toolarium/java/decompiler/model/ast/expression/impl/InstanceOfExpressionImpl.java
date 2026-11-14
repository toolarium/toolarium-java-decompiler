/*
 * InstanceOfExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IInstanceOfExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IInstanceOfExpression}.
 *
 * @author patrick
 */
public class InstanceOfExpressionImpl implements IInstanceOfExpression {
    private final IType type;
    private final IExpression operand;
    private final IType checkType;
    private final String patternVariable;


    /**
     * Constructor for InstanceOfExpressionImpl.
     *
     * @param type            the expression result type (boolean)
     * @param operand         the expression being checked
     * @param checkType       the type to check against
     * @param patternVariable the pattern variable name, or null for traditional instanceof
     */
    public InstanceOfExpressionImpl(IType type, IExpression operand, IType checkType, String patternVariable) {
        this.type = type;
        this.operand = operand;
        this.checkType = checkType;
        this.patternVariable = patternVariable;
    }


    /**
     * Get the result type of this instanceof expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the expression being checked against the type.
     *
     * @return the operand expression
     */
    @Override
    public IExpression getOperand() {
        return operand;
    }


    /**
     * Get the type to check the operand against.
     *
     * @return the check type
     */
    @Override
    public IType getCheckType() {
        return checkType;
    }


    /**
     * Get the pattern variable name for pattern matching instanceof (Java 16+).
     * Returns null if this is a traditional instanceof.
     *
     * @return the pattern variable name, or null
     */
    @Override
    public String getPatternVariable() {
        return patternVariable;
    }


    /**
     * Accept a visitor for this instanceof expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitInstanceOfExpression(this);
    }


    /**
     * Compute the hash code based on type, operand, check type, and pattern variable.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, operand, checkType, patternVariable);
    }


    /**
     * Check equality based on type, operand, check type, and pattern variable.
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
        InstanceOfExpressionImpl other = (InstanceOfExpressionImpl) obj;
        return Objects.equals(type, other.type)
                && Objects.equals(operand, other.operand)
                && Objects.equals(checkType, other.checkType)
                && Objects.equals(patternVariable, other.patternVariable);
    }


    /**
     * Return a string representation of this instanceof expression.
     *
     * @return the string representation in the form "operand instanceof type"
     */
    @Override
    public String toString() {
        String result = operand + " instanceof " + checkType.getDisplayName();
        if (patternVariable != null) {
            return result + " " + patternVariable;
        }
        return result;
    }
}
