/*
 * IFieldAccessExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;


/**
 * Represents a field access (e.g., obj.field or ClassName.staticField).
 *
 * @author patrick
 */
public interface IFieldAccessExpression extends IExpression {

    /**
     * Get the target object expression (null for static access).
     *
     * @return the target expression, or null for static access
     */
    IExpression getTarget();


    /**
     * Get the field name.
     *
     * @return the field name
     */
    String getFieldName();


    /**
     * Get the owner class internal name.
     *
     * @return the owner class name
     */
    String getOwner();
}
