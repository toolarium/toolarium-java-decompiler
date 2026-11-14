/*
 * NewArrayExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewArrayExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link INewArrayExpression}.
 *
 * @author patrick
 */
public class NewArrayExpressionImpl implements INewArrayExpression {
    private final IType type;
    private final IType elementType;
    private final List<IExpression> dimensionSizes;
    private final List<IExpression> initializers;


    /**
     * Constructor for NewArrayExpressionImpl.
     *
     * @param type           the expression result type
     * @param elementType    the array element type
     * @param dimensionSizes the dimension size expressions
     * @param initializers   the initializer expressions
     */
    public NewArrayExpressionImpl(IType type, IType elementType,
                                  List<IExpression> dimensionSizes, List<IExpression> initializers) {
        this.type = type;
        this.elementType = elementType;
        if (dimensionSizes != null) {
            this.dimensionSizes = Collections.unmodifiableList(new ArrayList<>(dimensionSizes));
        } else {
            this.dimensionSizes = Collections.emptyList();
        }
        if (initializers != null) {
            this.initializers = Collections.unmodifiableList(new ArrayList<>(initializers));
        } else {
            this.initializers = Collections.emptyList();
        }
    }


    /**
     * Get the result type of this new array expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the element type of the array being created.
     *
     * @return the array element type
     */
    @Override
    public IType getElementType() {
        return elementType;
    }


    /**
     * Get the dimension size expressions for this array creation.
     *
     * @return an unmodifiable list of dimension size expressions
     */
    @Override
    public List<IExpression> getDimensionSizes() {
        return dimensionSizes;
    }


    /**
     * Get the initializer expressions (for array initializers).
     *
     * @return an unmodifiable list of initializer expressions, empty if not an initializer
     */
    @Override
    public List<IExpression> getInitializers() {
        return initializers;
    }


    /**
     * Accept a visitor for this new array expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitNewArrayExpression(this);
    }


    /**
     * Compute the hash code based on type, element type, dimension sizes, and initializers.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, elementType, dimensionSizes, initializers);
    }


    /**
     * Check equality based on type, element type, dimension sizes, and initializers.
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
        NewArrayExpressionImpl other = (NewArrayExpressionImpl) obj;
        return Objects.equals(type, other.type)
                && Objects.equals(elementType, other.elementType)
                && Objects.equals(dimensionSizes, other.dimensionSizes)
                && Objects.equals(initializers, other.initializers);
    }


    /**
     * Return a string representation of this new array expression.
     *
     * @return the string representation in the form "new elementType[...]"
     */
    @Override
    public String toString() {
        return "new " + elementType.getDisplayName() + "[...]";
    }
}
