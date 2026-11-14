/*
 * ISwitchStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import java.util.List;


/**
 * Represents a switch statement.
 *
 * @author patrick
 */
public interface ISwitchStatement extends IStatement {

    /**
     * Get the switch selector expression.
     *
     * @return the selector expression
     */
    IExpression getSelector();


    /**
     * Get the switch cases.
     *
     * @return an unmodifiable list of cases
     */
    List<ISwitchCase> getCases();
}
