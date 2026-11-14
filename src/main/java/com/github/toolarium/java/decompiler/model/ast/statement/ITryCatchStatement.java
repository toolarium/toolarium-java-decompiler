/*
 * ITryCatchStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import java.util.List;


/**
 * Represents a try-catch-finally statement, optionally with resource declarations
 * for try-with-resources.
 *
 * @author patrick
 */
public interface ITryCatchStatement extends IStatement {

    /**
     * Get the resource declarations for try-with-resources (empty for regular try).
     *
     * @return an unmodifiable list of resource declarations
     */
    List<IVariableDeclarationStatement> getResources();


    /**
     * Get the try body.
     *
     * @return the try block statement
     */
    IStatement getTryBody();


    /**
     * Get the catch clauses.
     *
     * @return an unmodifiable list of catch clauses
     */
    List<ICatchClause> getCatchClauses();


    /**
     * Get the finally block (null if no finally).
     *
     * @return the finally statement, or null
     */
    IStatement getFinallyBody();
}
