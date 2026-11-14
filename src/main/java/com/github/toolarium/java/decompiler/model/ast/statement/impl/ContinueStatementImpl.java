/*
 * ContinueStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.statement.IContinueStatement;
import java.util.Objects;


/**
 * Implementation of {@link IContinueStatement}.
 *
 * @author patrick
 */
public class ContinueStatementImpl implements IContinueStatement {
    private final String label;
    private final String targetLabel;


    /**
     * Constructor for ContinueStatementImpl.
     *
     * @param label       the optional label, or null
     * @param targetLabel the target label for the continue, or null for unlabeled
     */
    public ContinueStatementImpl(String label, String targetLabel) {
        this.label = label;
        this.targetLabel = targetLabel;
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
     * Get the target label for this continue statement.
     *
     * @return the target label, or null for unlabeled continue
     */
    @Override
    public String getTargetLabel() {
        return targetLabel;
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitContinueStatement(this);
    }


    /**
     * Return a hash code value for this ContinueStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, targetLabel);
    }


    /**
     * Check whether this ContinueStatementImpl is equal to another object.
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
        ContinueStatementImpl other = (ContinueStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(targetLabel, other.targetLabel);
    }


    /**
     * Return a string representation of this ContinueStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ContinueStatement[label=" + label + ", targetLabel=" + targetLabel + "]";
    }
}
