/*
 * ArrayAccessExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IArrayAccessExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IArrayAccessExpression}.
 *
 * @author patrick
 */
public class ArrayAccessExpressionImpl implements IArrayAccessExpression {
    private final IType type;
    private final IExpression array;
    private final IExpression index;


    /**
     * Constructor for ArrayAccessExpressionImpl.
     *
     * @param type  the element type
     * @param array the array expression
     * @param index the index expression
     */
    public ArrayAccessExpressionImpl(IType type, IExpression array, IExpression index) {
        this.type = type;
        this.array = array;
        this.index = index;
    }


    /**
     * Get the result type of this array access expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the array expression being accessed.
     *
     * @return the array expression
     */
    @Override
    public IExpression getArray() {
        return array;
    }


    /**
     * Get the index expression used to access the array element.
     *
     * @return the index expression
     */
    @Override
    public IExpression getIndex() {
        return index;
    }


    /**
     * Accept a visitor for this array access expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitArrayAccessExpression(this);
    }


    /**
     * Compute the hash code based on type, array, and index.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, array, index);
    }


    /**
     * Check equality based on type, array, and index.
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
        ArrayAccessExpressionImpl other = (ArrayAccessExpressionImpl) obj;
        return Objects.equals(type, other.type)
                && Objects.equals(array, other.array)
                && Objects.equals(index, other.index);
    }


    /**
     * Return a string representation of this array access expression.
     *
     * @return the string representation in the form "array[index]"
     */
    @Override
    public String toString() {
        return array + "[" + index + "]";
    }
}
