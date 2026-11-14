/*
 * FieldDeclarationImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.declaration.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.declaration.IFieldDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IFieldDeclaration}.
 *
 * @author patrick
 */
public class FieldDeclarationImpl implements IFieldDeclaration {
    private final IAccessFlags accessFlags;
    private final IType fieldType;
    private final String name;
    private final IExpression initializer;
    private final List<IAnnotation> annotations;


    /**
     * Constructor for FieldDeclarationImpl.
     *
     * @param accessFlags the access modifiers
     * @param fieldType   the field type
     * @param name        the field name
     * @param initializer the initializer expression, or null
     * @param annotations the annotations on this field
     */
    public FieldDeclarationImpl(IAccessFlags accessFlags, IType fieldType, String name,
                                IExpression initializer, List<IAnnotation> annotations) {
        this.accessFlags = accessFlags;
        this.fieldType = fieldType;
        this.name = name;
        this.initializer = initializer;
        this.annotations = Collections.unmodifiableList(new ArrayList<>(annotations));
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitFieldDeclaration(this);
    }


    /**
     * Get the access modifiers.
     *
     * @return the access flags
     */
    @Override
    public IAccessFlags getAccessFlags() {
        return accessFlags;
    }


    /**
     * Get the field type.
     *
     * @return the field type
     */
    @Override
    public IType getFieldType() {
        return fieldType;
    }


    /**
     * Get the field name.
     *
     * @return the field name
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Get the initializer expression.
     *
     * @return the initializer, or null if not initialized inline
     */
    @Override
    public IExpression getInitializer() {
        return initializer;
    }


    /**
     * Get the annotations on this field declaration.
     *
     * @return an unmodifiable list of annotations
     */
    @Override
    public List<IAnnotation> getAnnotations() {
        return annotations;
    }


    /**
     * Return a hash code value for this FieldDeclarationImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(accessFlags, fieldType, name, initializer, annotations);
    }


    /**
     * Check whether this FieldDeclarationImpl is equal to another object.
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
        FieldDeclarationImpl other = (FieldDeclarationImpl) obj;
        return Objects.equals(accessFlags, other.accessFlags)
                && Objects.equals(fieldType, other.fieldType)
                && Objects.equals(name, other.name)
                && Objects.equals(initializer, other.initializer)
                && Objects.equals(annotations, other.annotations);
    }


    /**
     * Return a string representation of this FieldDeclarationImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "FieldDeclaration[name=" + name + ", type=" + fieldType + ", accessFlags=" + accessFlags + "]";
    }
}
