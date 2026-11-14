/*
 * SwitchExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ISwitchExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ISwitchExpressionCase;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link ISwitchExpression}.
 *
 * @author patrick
 */
public class SwitchExpressionImpl implements ISwitchExpression {
    private final IType type;
    private final IExpression selector;
    private final List<ISwitchExpressionCase> cases;


    /**
     * Constructor for SwitchExpressionImpl.
     *
     * @param type     the result type of the switch expression
     * @param selector the selector expression
     * @param cases    the switch expression cases
     */
    public SwitchExpressionImpl(IType type, IExpression selector, List<ISwitchExpressionCase> cases) {
        this.type = type;
        this.selector = selector;
        this.cases = Collections.unmodifiableList(cases);
    }


    /**
     * Get the result type of this switch expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
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
     * Get the switch expression cases.
     *
     * @return an unmodifiable list of switch expression cases
     */
    @Override
    public List<ISwitchExpressionCase> getCases() {
        return cases;
    }


    /**
     * Accept a visitor for this switch expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitSwitchExpression(this);
    }


    /**
     * Compute the hash code based on type, selector, and cases.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, selector, cases);
    }


    /**
     * Check equality based on type, selector, and cases.
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
        SwitchExpressionImpl other = (SwitchExpressionImpl) obj;
        return Objects.equals(type, other.type)
                && Objects.equals(selector, other.selector)
                && Objects.equals(cases, other.cases);
    }


    /**
     * Return a string representation of this switch expression.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("switch (");
        result.append(selector).append(") { ");
        for (int i = 0; i < cases.size(); i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(cases.get(i));
        }
        result.append(" }");
        return result.toString();
    }
}
