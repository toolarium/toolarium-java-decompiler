/*
 * ISwitchExpressionCase.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;

import java.util.List;


/**
 * Represents a single case in a switch expression, containing case values
 * and a result expression (e.g., case 1 -> "one").
 *
 * @author patrick
 */
public interface ISwitchExpressionCase {

    /**
     * Get the case value expressions (empty for the default case).
     *
     * @return an unmodifiable list of case value expressions
     */
    List<IExpression> getValues();


    /**
     * Check whether this is the default case.
     *
     * @return true if this is the default case
     */
    boolean isDefault();


    /**
     * Get the result expression for this case.
     *
     * @return the result expression
     */
    IExpression getResultExpression();
}
