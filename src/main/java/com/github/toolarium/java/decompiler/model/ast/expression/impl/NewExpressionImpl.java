/*
 * NewExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link INewExpression}.
 *
 * @author patrick
 */
public class NewExpressionImpl implements INewExpression {
    private final IType type;
    private final IType instantiatedType;
    private final List<IExpression> arguments;


    /**
     * Constructor for NewExpressionImpl.
     *
     * @param type             the expression result type
     * @param instantiatedType the type being instantiated
     * @param arguments        the constructor arguments
     */
    public NewExpressionImpl(IType type, IType instantiatedType, List<IExpression> arguments) {
        this.type = type;
        this.instantiatedType = instantiatedType;
        if (arguments != null) {
            this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
        } else {
            this.arguments = Collections.emptyList();
        }
    }


    /**
     * Get the result type of this new expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the type being instantiated by this new expression.
     *
     * @return the instantiated type
     */
    @Override
    public IType getInstantiatedType() {
        return instantiatedType;
    }


    /**
     * Get the constructor arguments as an unmodifiable list.
     *
     * @return an unmodifiable list of argument expressions
     */
    @Override
    public List<IExpression> getArguments() {
        return arguments;
    }


    /**
     * Accept a visitor for this new expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitNewExpression(this);
    }


    /**
     * Compute the hash code based on type, instantiated type, and arguments.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, instantiatedType, arguments);
    }


    /**
     * Check equality based on type, instantiated type, and arguments.
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
        NewExpressionImpl other = (NewExpressionImpl) obj;
        return Objects.equals(type, other.type)
                && Objects.equals(instantiatedType, other.instantiatedType)
                && Objects.equals(arguments, other.arguments);
    }


    /**
     * Return a string representation of this new expression.
     *
     * @return the string representation in the form "new type(...)"
     */
    @Override
    public String toString() {
        return "new " + instantiatedType.getDisplayName() + "(...)";
    }
}
