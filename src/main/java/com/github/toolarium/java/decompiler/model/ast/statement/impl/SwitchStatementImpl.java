/*
 * SwitchStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchCase;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link ISwitchStatement}.
 *
 * @author patrick
 */
public class SwitchStatementImpl implements ISwitchStatement {
    private final String label;
    private final IExpression selector;
    private final List<ISwitchCase> cases;


    /**
     * Constructor for SwitchStatementImpl.
     *
     * @param label    the optional label, or null
     * @param selector the switch selector expression
     * @param cases    the list of switch cases
     */
    public SwitchStatementImpl(String label, IExpression selector, List<ISwitchCase> cases) {
        this.label = label;
        this.selector = selector;
        this.cases = Collections.unmodifiableList(new ArrayList<>(cases));
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
     * Get the switch selector expression.
     *
     * @return the selector expression
     */
    @Override
    public IExpression getSelector() {
        return selector;
    }


    /**
     * Get the switch cases.
     *
     * @return an unmodifiable list of cases
     */
    @Override
    public List<ISwitchCase> getCases() {
        return cases;
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitSwitchStatement(this);
    }


    /**
     * Return a hash code value for this SwitchStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, selector, cases);
    }


    /**
     * Check whether this SwitchStatementImpl is equal to another object.
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
        SwitchStatementImpl other = (SwitchStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(selector, other.selector)
                && Objects.equals(cases, other.cases);
    }


    /**
     * Return a string representation of this SwitchStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "SwitchStatement[label=" + label + ", selector=" + selector + ", cases=" + cases + "]";
    }
}
