/*
 * ISwitchCase.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import java.util.List;


/**
 * Represents a single case in a switch statement.
 *
 * @author patrick
 */
public interface ISwitchCase {

    /**
     * Get the case value expressions (empty for default case).
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
     * Get the statements in this case.
     *
     * @return an unmodifiable list of statements
     */
    List<IStatement> getStatements();
}
