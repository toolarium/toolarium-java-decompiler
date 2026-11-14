/*
 * IMethodCallExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;

import java.util.List;


/**
 * Represents a method invocation (e.g., obj.method(a, b) or ClassName.staticMethod()).
 *
 * @author patrick
 */
public interface IMethodCallExpression extends IExpression {

    /**
     * Get the target object expression (null for static calls).
     *
     * @return the target expression, or null for static calls
     */
    IExpression getTarget();


    /**
     * Get the method name.
     *
     * @return the method name
     */
    String getMethodName();


    /**
     * Get the owner class internal name (for static calls or when target is implicit).
     *
     * @return the owner class name
     */
    String getOwner();


    /**
     * Get the method arguments.
     *
     * @return an unmodifiable list of argument expressions
     */
    List<IExpression> getArguments();
}
