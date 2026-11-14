/*
 * TryCatchStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.statement.ICatchClause;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ITryCatchStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IVariableDeclarationStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link ITryCatchStatement}.
 *
 * @author patrick
 */
public class TryCatchStatementImpl implements ITryCatchStatement {
    private final String label;
    private final List<IVariableDeclarationStatement> resources;
    private final IStatement tryBody;
    private final List<ICatchClause> catchClauses;
    private final IStatement finallyBody;


    /**
     * Constructor for TryCatchStatementImpl.
     *
     * @param label        the optional label, or null
     * @param tryBody      the try block statement
     * @param catchClauses the list of catch clauses
     * @param finallyBody  the finally block statement, or null if no finally
     */
    public TryCatchStatementImpl(String label, IStatement tryBody, List<ICatchClause> catchClauses, IStatement finallyBody) {
        this.label = label;
        this.resources = Collections.emptyList();
        this.tryBody = tryBody;
        this.catchClauses = Collections.unmodifiableList(new ArrayList<>(catchClauses));
        this.finallyBody = finallyBody;
    }


    /**
     * Constructor for TryCatchStatementImpl with resource declarations for try-with-resources.
     *
     * @param label        the optional label, or null
     * @param resources    the resource declarations
     * @param tryBody      the try block statement
     * @param catchClauses the list of catch clauses
     * @param finallyBody  the finally block statement, or null if no finally
     */
    public TryCatchStatementImpl(String label, List<IVariableDeclarationStatement> resources,
                                  IStatement tryBody, List<ICatchClause> catchClauses,
                                  IStatement finallyBody) {
        this.label = label;
        this.resources = Collections.unmodifiableList(new ArrayList<>(resources));
        this.tryBody = tryBody;
        this.catchClauses = Collections.unmodifiableList(new ArrayList<>(catchClauses));
        this.finallyBody = finallyBody;
    }


    /**
     * Get an optional label for this statement.
     *
     * @return the label, or null if unlabeled
     */
    @Override
    public String getLabel() {
        return label;
    }


    /**
     * Get the resource declarations for try-with-resources.
     *
     * @return an unmodifiable list of resource declarations, empty for regular try
     */
    @Override
    public List<IVariableDeclarationStatement> getResources() {
        return resources;
    }


    /**
     * Get the try body.
     *
     * @return the try block statement
     */
    @Override
    public IStatement getTryBody() {
        return tryBody;
    }


    /**
     * Get the catch clauses.
     *
     * @return an unmodifiable list of catch clauses
     */
    @Override
    public List<ICatchClause> getCatchClauses() {
        return catchClauses;
    }


    /**
     * Get the finally block.
     *
     * @return the finally statement, or null if no finally
     */
    @Override
    public IStatement getFinallyBody() {
        return finallyBody;
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitTryCatchStatement(this);
    }


    /**
     * Return a hash code value for this TryCatchStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, resources, tryBody, catchClauses, finallyBody);
    }


    /**
     * Check whether this TryCatchStatementImpl is equal to another object.
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
        TryCatchStatementImpl other = (TryCatchStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(resources, other.resources)
                && Objects.equals(tryBody, other.tryBody)
                && Objects.equals(catchClauses, other.catchClauses)
                && Objects.equals(finallyBody, other.finallyBody);
    }


    /**
     * Return a string representation of this TryCatchStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "TryCatchStatement[label=" + label + ", resources=" + resources
                + ", tryBody=" + tryBody + ", catchClauses=" + catchClauses
                + ", finallyBody=" + finallyBody + "]";
    }
}
