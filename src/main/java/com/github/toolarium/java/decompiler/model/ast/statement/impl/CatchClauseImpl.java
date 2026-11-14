/*
 * CatchClauseImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.statement.ICatchClause;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link ICatchClause}.
 *
 * @author patrick
 */
public class CatchClauseImpl implements ICatchClause {
    private final List<IType> exceptionTypes;
    private final String variableName;
    private final IStatement body;


    /**
     * Constructor for CatchClauseImpl.
     *
     * @param exceptionTypes the caught exception types
     * @param variableName   the exception variable name
     * @param body           the catch body statement
     */
    public CatchClauseImpl(List<IType> exceptionTypes, String variableName, IStatement body) {
        this.exceptionTypes = Collections.unmodifiableList(new ArrayList<>(exceptionTypes));
        this.variableName = variableName;
        this.body = body;
    }


    /**
     * Get the caught exception types.
     *
     * @return an unmodifiable list of exception types
     */
    @Override
    public List<IType> getExceptionTypes() {
        return exceptionTypes;
    }


    /**
     * Get the exception variable name.
     *
     * @return the variable name
     */
    @Override
    public String getVariableName() {
        return variableName;
    }


    /**
     * Get the catch body.
     *
     * @return the catch block statement
     */
    @Override
    public IStatement getBody() {
        return body;
    }


    /**
     * Return a hash code value for this CatchClauseImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(exceptionTypes, variableName, body);
    }


    /**
     * Check whether this CatchClauseImpl is equal to another object.
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
        CatchClauseImpl other = (CatchClauseImpl) obj;
        return Objects.equals(exceptionTypes, other.exceptionTypes)
                && Objects.equals(variableName, other.variableName)
                && Objects.equals(body, other.body);
    }


    /**
     * Return a string representation of this CatchClauseImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "CatchClause[exceptionTypes=" + exceptionTypes
                + ", variableName=" + variableName + ", body=" + body + "]";
    }
}
