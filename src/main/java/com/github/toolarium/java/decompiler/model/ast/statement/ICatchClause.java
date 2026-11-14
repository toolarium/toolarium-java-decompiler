/*
 * ICatchClause.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.List;


/**
 * Represents a catch clause in a try-catch statement.
 *
 * @author patrick
 */
public interface ICatchClause {

    /**
     * Get the caught exception types (supports multi-catch with |).
     *
     * @return an unmodifiable list of exception types
     */
    List<IType> getExceptionTypes();


    /**
     * Get the exception variable name.
     *
     * @return the variable name
     */
    String getVariableName();


    /**
     * Get the catch body.
     *
     * @return the catch block statement
     */
    IStatement getBody();
}
