/*
 * MethodReferenceExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodReferenceExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IMethodReferenceExpression}.
 *
 * @author patrick
 */
public class MethodReferenceExpressionImpl implements IMethodReferenceExpression {
    private final IType type;
    private final IExpression qualifier;
    private final String owner;
    private final String methodName;
    private final boolean constructorReference;


    /**
     * Constructor for MethodReferenceExpressionImpl.
     *
     * @param type                 the functional interface type
     * @param qualifier            the qualifier expression for instance method references (may be null)
     * @param owner                the owner class internal name
     * @param methodName           the referenced method name
     * @param constructorReference true if this is a constructor reference
     */
    public MethodReferenceExpressionImpl(IType type, IExpression qualifier, String owner,
                                         String methodName, boolean constructorReference) {
        this.type = type;
        this.qualifier = qualifier;
        this.owner = owner;
        this.methodName = methodName;
        this.constructorReference = constructorReference;
    }


    /**
     * Get the result type of this method reference expression.
     *
     * @return the functional interface type
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the qualifier expression for instance method references.
     *
     * @return the qualifier expression, or null for class-level references
     */
    @Override
    public IExpression getQualifier() {
        return qualifier;
    }


    /**
     * Get the owner class internal name.
     *
     * @return the owner class internal name
     */
    @Override
    public String getOwner() {
        return owner;
    }


    /**
     * Get the referenced method name.
     *
     * @return the method name
     */
    @Override
    public String getMethodName() {
        return methodName;
    }


    /**
     * Check whether this is a constructor reference.
     *
     * @return true if this is a constructor reference
     */
    @Override
    public boolean isConstructorReference() {
        return constructorReference;
    }


    /**
     * Accept a visitor for this method reference expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitMethodReferenceExpression(this);
    }


    /**
     * Compute the hash code based on all fields.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, qualifier, owner, methodName, constructorReference);
    }


    /**
     * Check equality based on all fields.
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
        MethodReferenceExpressionImpl other = (MethodReferenceExpressionImpl) obj;
        return constructorReference == other.constructorReference
                && Objects.equals(type, other.type)
                && Objects.equals(qualifier, other.qualifier)
                && Objects.equals(owner, other.owner)
                && Objects.equals(methodName, other.methodName);
    }


    /**
     * Return a string representation of this method reference expression.
     *
     * @return the string representation in the form "Owner::method" or "qualifier::method"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (qualifier != null) {
            sb.append(qualifier);
        } else {
            int lastSlash = owner.lastIndexOf('/');
            if (lastSlash >= 0) {
                sb.append(owner.substring(lastSlash + 1));
            } else {
                sb.append(owner);
            }
        }
        sb.append("::");
        if (constructorReference) {
            sb.append("new");
        } else {
            sb.append(methodName);
        }
        return sb.toString();
    }
}
