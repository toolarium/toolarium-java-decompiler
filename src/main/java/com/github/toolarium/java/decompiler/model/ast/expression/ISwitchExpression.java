/*
 * ISwitchExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;

import java.util.List;


/**
 * Represents a switch expression (Java 14+) that evaluates to a value,
 * using arrow syntax (e.g., switch (x) { case 1 -> "one"; default -> "other"; }).
 *
 * @author patrick
 */
public interface ISwitchExpression extends IExpression {

    /**
     * Get the switch selector expression.
     *
     * @return the selector expression
     */
    IExpression getSelector();


    /**
     * Get the switch expression cases.
     *
     * @return an unmodifiable list of switch expression cases
     */
    List<ISwitchExpressionCase> getCases();
}
