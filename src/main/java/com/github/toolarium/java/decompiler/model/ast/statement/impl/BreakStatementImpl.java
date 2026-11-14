/*
 * BreakStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.statement.IBreakStatement;
import java.util.Objects;


/**
 * Implementation of {@link IBreakStatement}.
 *
 * @author patrick
 */
public class BreakStatementImpl implements IBreakStatement {
    private final String label;
    private final String targetLabel;


    /**
     * Constructor for BreakStatementImpl.
     *
     * @param label       the optional label, or null
     * @param targetLabel the target label for the break, or null for unlabeled
     */
    public BreakStatementImpl(String label, String targetLabel) {
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
     * Get the target label for this break statement.
     *
     * @return the target label, or null for unlabeled break
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
        visitor.visitBreakStatement(this);
    }


    /**
     * Return a hash code value for this BreakStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, targetLabel);
    }


    /**
     * Check whether this BreakStatementImpl is equal to another object.
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
        BreakStatementImpl other = (BreakStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(targetLabel, other.targetLabel);
    }


    /**
     * Return a string representation of this BreakStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "BreakStatement[label=" + label + ", targetLabel=" + targetLabel + "]";
    }
}
