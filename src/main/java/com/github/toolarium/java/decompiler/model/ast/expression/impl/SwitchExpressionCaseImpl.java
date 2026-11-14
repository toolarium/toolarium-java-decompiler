/*
 * SwitchExpressionCaseImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ISwitchExpressionCase;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link ISwitchExpressionCase}.
 *
 * @author patrick
 */
public class SwitchExpressionCaseImpl implements ISwitchExpressionCase {
    private final List<IExpression> values;
    private final boolean defaultCase;
    private final IExpression resultExpression;


    /**
     * Constructor for SwitchExpressionCaseImpl.
     *
     * @param values           the case value expressions (empty for default case)
     * @param defaultCase      true if this is the default case
     * @param resultExpression the result expression for this case
     */
    public SwitchExpressionCaseImpl(List<IExpression> values, boolean defaultCase, IExpression resultExpression) {
        this.values = Collections.unmodifiableList(values);
        this.defaultCase = defaultCase;
        this.resultExpression = resultExpression;
    }


    /**
     * Get the case value expressions (empty for the default case).
     *
     * @return an unmodifiable list of case value expressions
     */
    @Override
    public List<IExpression> getValues() {
        return values;
    }


    /**
     * Check whether this is the default case.
     *
     * @return true if this is the default case
     */
    @Override
    public boolean isDefault() {
        return defaultCase;
    }


    /**
     * Get the result expression for this case.
     *
     * @return the result expression
     */
    @Override
    public IExpression getResultExpression() {
        return resultExpression;
    }


    /**
     * Compute the hash code based on values, default flag, and result expression.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(values, defaultCase, resultExpression);
    }


    /**
     * Check equality based on values, default flag, and result expression.
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
        SwitchExpressionCaseImpl other = (SwitchExpressionCaseImpl) obj;
        return defaultCase == other.defaultCase
                && Objects.equals(values, other.values)
                && Objects.equals(resultExpression, other.resultExpression);
    }


    /**
     * Return a string representation of this switch expression case.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (defaultCase) {
            result.append("default");
        } else {
            result.append("case ");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(values.get(i));
            }
        }
        result.append(" -> ").append(resultExpression);
        return result.toString();
    }
}
