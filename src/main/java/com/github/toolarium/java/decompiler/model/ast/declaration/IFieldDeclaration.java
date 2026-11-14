/*
 * IFieldDeclaration.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.declaration;

import com.github.toolarium.java.decompiler.model.ast.IAstNode;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.List;


/**
 * Represents a field declaration in a class.
 *
 * @author patrick
 */
public interface IFieldDeclaration extends IAstNode {

    /**
     * Get the access modifiers.
     *
     * @return the access flags
     */
    IAccessFlags getAccessFlags();


    /**
     * Get the field type.
     *
     * @return the field type
     */
    IType getFieldType();


    /**
     * Get the field name.
     *
     * @return the field name
     */
    String getName();


    /**
     * Get the initializer expression (null if not initialized inline).
     *
     * @return the initializer, or null
     */
    IExpression getInitializer();


    /**
     * Get the annotations on this field declaration.
     *
     * @return an unmodifiable list of annotations
     */
    List<IAnnotation> getAnnotations();
}
