/*
 * ILambdaExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;

import java.util.List;


/**
 * Represents a lambda expression (e.g., {@code (a, b) -> a + b} or {@code x -> x.toString()}).
 *
 * @author patrick
 */
public interface ILambdaExpression extends IExpression {

    /**
     * Get the functional interface method name (e.g., "apply", "run", "accept").
     *
     * @return the functional interface method name
     */
    String getFunctionalMethodName();


    /**
     * Get the parameter names of the lambda.
     *
     * @return an unmodifiable list of parameter names
     */
    List<String> getParameterNames();


    /**
     * Get the parameter types of the lambda.
     *
     * @return an unmodifiable list of parameter types as internal type descriptors
     */
    List<String> getParameterTypeDescriptors();


    /**
     * Get the captured arguments from the enclosing scope.
     *
     * @return an unmodifiable list of captured argument expressions
     */
    List<IExpression> getCapturedArguments();


    /**
     * Get the body expression of the lambda, if it is a single-expression lambda.
     * Returns null if the body is not available (e.g., the lambda calls a generated method).
     *
     * @return the body expression, or null
     */
    IExpression getBody();


    /**
     * Get the target method name that implements the lambda body.
     *
     * @return the implementation method name
     */
    String getImplementationMethodName();


    /**
     * Get the owner class of the implementation method.
     *
     * @return the owner class internal name
     */
    String getImplementationOwner();
}
