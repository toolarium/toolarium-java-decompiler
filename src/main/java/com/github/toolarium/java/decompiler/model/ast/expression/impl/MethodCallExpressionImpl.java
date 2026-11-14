/*
 * MethodCallExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodCallExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IMethodCallExpression}.
 *
 * @author patrick
 */
public class MethodCallExpressionImpl implements IMethodCallExpression {
    private final IType type;
    private final IExpression target;
    private final String methodName;
    private final String owner;
    private final List<IExpression> arguments;


    /**
     * Constructor for MethodCallExpressionImpl.
     *
     * @param type       the return type
     * @param target     the target object expression (null for static calls)
     * @param methodName the method name
     * @param owner      the owner class internal name
     * @param arguments  the method arguments
     */
    public MethodCallExpressionImpl(IType type, IExpression target, String methodName,
                                    String owner, List<IExpression> arguments) {
        this.type = type;
        this.target = target;
        this.methodName = methodName;
        this.owner = owner;
        if (arguments != null) {
            this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
        } else {
            this.arguments = Collections.emptyList();
        }
    }


    /**
     * Get the result type of this method call expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the target object expression (null for static calls).
     *
     * @return the target expression, or null for static calls
     */
    @Override
    public IExpression getTarget() {
        return target;
    }


    /**
     * Get the name of the method being called.
     *
     * @return the method name
     */
    @Override
    public String getMethodName() {
        return methodName;
    }


    /**
     * Get the owner class internal name (for static calls or when target is implicit).
     *
     * @return the owner class name
     */
    @Override
    public String getOwner() {
        return owner;
    }


    /**
     * Get the method arguments as an unmodifiable list.
     *
     * @return an unmodifiable list of argument expressions
     */
    @Override
    public List<IExpression> getArguments() {
        return arguments;
    }


    /**
     * Accept a visitor for this method call expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitMethodCallExpression(this);
    }


    /**
     * Compute the hash code based on type, target, method name, owner, and arguments.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, target, methodName, owner, arguments);
    }


    /**
     * Check equality based on type, target, method name, owner, and arguments.
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
        MethodCallExpressionImpl other = (MethodCallExpressionImpl) obj;
        return Objects.equals(type, other.type)
                && Objects.equals(target, other.target)
                && Objects.equals(methodName, other.methodName)
                && Objects.equals(owner, other.owner)
                && Objects.equals(arguments, other.arguments);
    }


    /**
     * Return a string representation of this method call expression.
     *
     * @return the string representation in the form "target.methodName(args)"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (target != null) {
            sb.append(target).append('.');
        }
        sb.append(methodName).append('(');
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(arguments.get(i));
        }
        sb.append(')');
        return sb.toString();
    }
}
