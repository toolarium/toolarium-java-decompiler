/*
 * SwitchCaseImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchCase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link ISwitchCase}.
 *
 * @author patrick
 */
public class SwitchCaseImpl implements ISwitchCase {
    private final List<IExpression> values;
    private final boolean isDefault;
    private final List<IStatement> statements;


    /**
     * Constructor for SwitchCaseImpl.
     *
     * @param values     the case value expressions (empty for default case)
     * @param isDefault  true if this is the default case
     * @param statements the list of statements in this case
     */
    public SwitchCaseImpl(List<IExpression> values, boolean isDefault, List<IStatement> statements) {
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
        this.isDefault = isDefault;
        this.statements = Collections.unmodifiableList(new ArrayList<>(statements));
    }


    /**
     * Get the case value expressions.
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
        return isDefault;
    }


    /**
     * Get the statements in this case.
     *
     * @return an unmodifiable list of statements
     */
    @Override
    public List<IStatement> getStatements() {
        return statements;
    }


    /**
     * Return a hash code value for this SwitchCaseImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(values, isDefault, statements);
    }


    /**
     * Check whether this SwitchCaseImpl is equal to another object.
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
        SwitchCaseImpl other = (SwitchCaseImpl) obj;
        return isDefault == other.isDefault
                && Objects.equals(values, other.values)
                && Objects.equals(statements, other.statements);
    }


    /**
     * Return a string representation of this SwitchCaseImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "SwitchCase[values=" + values + ", isDefault=" + isDefault + ", statements=" + statements + "]";
    }
}
