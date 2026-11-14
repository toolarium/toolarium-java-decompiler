/*
 * LambdaExpressionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ILambdaExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link ILambdaExpression}.
 *
 * @author patrick
 */
public class LambdaExpressionImpl implements ILambdaExpression {
    private final IType type;
    private final String functionalMethodName;
    private final List<String> parameterNames;
    private final List<String> parameterTypeDescriptors;
    private final List<IExpression> capturedArguments;
    private final IExpression body;
    private final String implementationMethodName;
    private final String implementationOwner;


    /**
     * Constructor for LambdaExpressionImpl.
     *
     * @param type                     the functional interface type
     * @param functionalMethodName     the functional interface method name
     * @param parameterNames           the lambda parameter names
     * @param parameterTypeDescriptors the lambda parameter type descriptors
     * @param capturedArguments        the captured arguments from the enclosing scope
     * @param body                     the body expression (may be null)
     * @param implementationMethodName the implementation method name
     * @param implementationOwner      the implementation owner class
     */
    public LambdaExpressionImpl(IType type, String functionalMethodName,
                                List<String> parameterNames, List<String> parameterTypeDescriptors,
                                List<IExpression> capturedArguments, IExpression body,
                                String implementationMethodName, String implementationOwner) {
        this.type = type;
        this.functionalMethodName = functionalMethodName;
        List<String> safeParamNames;
        if (parameterNames != null) {
            safeParamNames = parameterNames;
        } else {
            safeParamNames = Collections.emptyList();
        }
        this.parameterNames = Collections.unmodifiableList(new ArrayList<>(safeParamNames));

        List<String> safeParamTypes;
        if (parameterTypeDescriptors != null) {
            safeParamTypes = parameterTypeDescriptors;
        } else {
            safeParamTypes = Collections.emptyList();
        }
        this.parameterTypeDescriptors = Collections.unmodifiableList(new ArrayList<>(safeParamTypes));

        List<IExpression> safeCaptured;
        if (capturedArguments != null) {
            safeCaptured = capturedArguments;
        } else {
            safeCaptured = Collections.emptyList();
        }
        this.capturedArguments = Collections.unmodifiableList(new ArrayList<>(safeCaptured));
        this.body = body;
        this.implementationMethodName = implementationMethodName;
        this.implementationOwner = implementationOwner;
    }


    /**
     * Get the result type of this lambda expression.
     *
     * @return the functional interface type
     */
    @Override
    public IType getType() {
        return type;
    }


    /**
     * Get the functional interface method name.
     *
     * @return the functional interface method name
     */
    @Override
    public String getFunctionalMethodName() {
        return functionalMethodName;
    }


    /**
     * Get the parameter names of the lambda.
     *
     * @return an unmodifiable list of parameter names
     */
    @Override
    public List<String> getParameterNames() {
        return parameterNames;
    }


    /**
     * Get the parameter type descriptors of the lambda.
     *
     * @return an unmodifiable list of parameter type descriptors
     */
    @Override
    public List<String> getParameterTypeDescriptors() {
        return parameterTypeDescriptors;
    }


    /**
     * Get the captured arguments from the enclosing scope.
     *
     * @return an unmodifiable list of captured argument expressions
     */
    @Override
    public List<IExpression> getCapturedArguments() {
        return capturedArguments;
    }


    /**
     * Get the body expression of the lambda.
     *
     * @return the body expression, or null
     */
    @Override
    public IExpression getBody() {
        return body;
    }


    /**
     * Get the target method name that implements the lambda body.
     *
     * @return the implementation method name
     */
    @Override
    public String getImplementationMethodName() {
        return implementationMethodName;
    }


    /**
     * Get the owner class of the implementation method.
     *
     * @return the owner class internal name
     */
    @Override
    public String getImplementationOwner() {
        return implementationOwner;
    }


    /**
     * Accept a visitor for this lambda expression node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitLambdaExpression(this);
    }


    /**
     * Compute the hash code based on all fields.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, functionalMethodName, parameterNames,
                parameterTypeDescriptors, capturedArguments, body,
                implementationMethodName, implementationOwner);
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
        LambdaExpressionImpl other = (LambdaExpressionImpl) obj;
        return Objects.equals(type, other.type)
                && Objects.equals(functionalMethodName, other.functionalMethodName)
                && Objects.equals(parameterNames, other.parameterNames)
                && Objects.equals(parameterTypeDescriptors, other.parameterTypeDescriptors)
                && Objects.equals(capturedArguments, other.capturedArguments)
                && Objects.equals(body, other.body)
                && Objects.equals(implementationMethodName, other.implementationMethodName)
                && Objects.equals(implementationOwner, other.implementationOwner);
    }


    /**
     * Return a string representation of this lambda expression.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < parameterNames.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameterNames.get(i));
        }
        sb.append(") -> ");
        if (body != null) {
            sb.append(body);
        } else {
            sb.append(implementationOwner).append('.').append(implementationMethodName);
        }
        return sb.toString();
    }
}
