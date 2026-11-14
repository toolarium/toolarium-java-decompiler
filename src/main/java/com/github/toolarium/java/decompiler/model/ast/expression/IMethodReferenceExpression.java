/*
 * IMethodReferenceExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;


/**
 * Represents a method reference expression (e.g., {@code String::valueOf},
 * {@code System.out::println}, or {@code Integer::new}).
 *
 * @author patrick
 */
public interface IMethodReferenceExpression extends IExpression {

    /**
     * Get the qualifier expression for instance method references (e.g., the "list" in "list::add").
     * Returns null for static or unbound method references.
     *
     * @return the qualifier expression, or null for class-level references
     */
    IExpression getQualifier();


    /**
     * Get the owner class internal name (e.g., "java/lang/String").
     *
     * @return the owner class internal name
     */
    String getOwner();


    /**
     * Get the referenced method name (e.g., "valueOf", "println", or "&lt;init&gt;" for constructor refs).
     *
     * @return the method name
     */
    String getMethodName();


    /**
     * Check whether this is a constructor reference (e.g., {@code ArrayList::new}).
     *
     * @return true if this is a constructor reference
     */
    boolean isConstructorReference();
}
