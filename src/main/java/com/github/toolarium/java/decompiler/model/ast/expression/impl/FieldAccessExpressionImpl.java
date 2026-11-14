/*
 * FieldAccessExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IFieldAccessExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IFieldAccessExpression}.
 *
 * @author patrick
 */
public class FieldAccessExpressionImpl implements IFieldAccessExpression {
    private final IType type;
    private final IExpression target;
    private final String fieldName;
    private final String owner;


    /**
     * Constructor for FieldAccessExpressionImpl.
     *
     * @param type      the field type
     * @param target    the target object expression (null for static access)
     * @param fieldName the field name
     * @param owner     the owner class internal name
     */
    public FieldAccessExpressionImpl(IType type, IExpression target, String fieldName, String owner) {
        this.type = type;
        this.target = target;
        this.fieldName = fieldName;
        this.owner = owner;
    }


    /**
     * Get the result type of this field access expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the target object expression (null for static access).
     *
     * @return the target expression, or null for static access
     */
    @Override
    public IExpression getTarget() {
        return target;
    }


    /**
     * Get the name of the field being accessed.
     *
     * @return the field name
     */
    @Override
    public String getFieldName() {
        return fieldName;
    }


    /**
     * Get the owner class internal name.
     *
     * @return the owner class name
     */
    @Override
    public String getOwner() {
        return owner;
    }


    /**
     * Accept a visitor for this field access expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitFieldAccessExpression(this);
    }


    /**
     * Compute the hash code based on type, target, field name, and owner.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, target, fieldName, owner);
    }


    /**
     * Check equality based on type, target, field name, and owner.
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
        FieldAccessExpressionImpl other = (FieldAccessExpressionImpl) obj;
        return Objects.equals(type, other.type)
                && Objects.equals(target, other.target)
                && Objects.equals(fieldName, other.fieldName)
                && Objects.equals(owner, other.owner);
    }


    /**
     * Return a string representation of this field access expression.
     *
     * @return the string representation in the form "target.fieldName" or "owner.fieldName"
     */
    @Override
    public String toString() {
        if (target != null) {
            return target + "." + fieldName;
        }
        return owner + "." + fieldName;
    }
}
