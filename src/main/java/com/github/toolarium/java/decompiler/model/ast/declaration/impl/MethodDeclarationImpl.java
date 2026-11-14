/*
 * MethodDeclarationImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.declaration.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IParameterDeclaration;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IMethodDeclaration}.
 *
 * @author patrick
 */
public class MethodDeclarationImpl implements IMethodDeclaration {
    private final IAccessFlags accessFlags;
    private final String name;
    private final IType returnType;
    private final List<IParameterDeclaration> parameters;
    private final List<IType> thrownTypes;
    private final IStatement body;
    private final boolean isConstructor;
    private final List<IAnnotation> annotations;


    /**
     * Constructor for MethodDeclarationImpl.
     *
     * @param accessFlags   the access modifiers
     * @param name          the method name
     * @param returnType    the return type, or null for constructors
     * @param parameters    the parameter declarations
     * @param thrownTypes   the declared exception types
     * @param body          the method body, or null for abstract or native methods
     * @param isConstructor true if this is a constructor
     * @param annotations   the annotations on this method
     */
    public MethodDeclarationImpl(IAccessFlags accessFlags,
                                 String name,
                                 IType returnType,
                                 List<IParameterDeclaration> parameters,
                                 List<IType> thrownTypes,
                                 IStatement body,
                                 boolean isConstructor,
                                 List<IAnnotation> annotations) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.returnType = returnType;
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        this.thrownTypes = Collections.unmodifiableList(new ArrayList<>(thrownTypes));
        this.body = body;
        this.isConstructor = isConstructor;
        this.annotations = Collections.unmodifiableList(new ArrayList<>(annotations));
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitMethodDeclaration(this);
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
     * Get the method name.
     *
     * @return the method name
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Get the return type.
     *
     * @return the return type, or null for constructors
     */
    @Override
    public IType getReturnType() {
        return returnType;
    }


    /**
     * Get the parameter declarations.
     *
     * @return an unmodifiable list of parameter declarations
     */
    @Override
    public List<IParameterDeclaration> getParameters() {
        return parameters;
    }


    /**
     * Get the declared exception types.
     *
     * @return an unmodifiable list of exception types
     */
    @Override
    public List<IType> getThrownTypes() {
        return thrownTypes;
    }


    /**
     * Get the method body.
     *
     * @return the body statement, or null for abstract or native methods
     */
    @Override
    public IStatement getBody() {
        return body;
    }


    /**
     * Check whether this is a constructor.
     *
     * @return true if this is a constructor
     */
    @Override
    public boolean isConstructor() {
        return isConstructor;
    }


    /**
     * Get the annotations on this method declaration.
     *
     * @return an unmodifiable list of annotations
     */
    @Override
    public List<IAnnotation> getAnnotations() {
        return annotations;
    }


    /**
     * Return a hash code value for this MethodDeclarationImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(accessFlags, name, returnType, parameters, thrownTypes, body, isConstructor, annotations);
    }


    /**
     * Check whether this MethodDeclarationImpl is equal to another object.
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
        MethodDeclarationImpl other = (MethodDeclarationImpl) obj;
        return isConstructor == other.isConstructor
                && Objects.equals(accessFlags, other.accessFlags)
                && Objects.equals(name, other.name)
                && Objects.equals(returnType, other.returnType)
                && Objects.equals(parameters, other.parameters)
                && Objects.equals(thrownTypes, other.thrownTypes)
                && Objects.equals(body, other.body)
                && Objects.equals(annotations, other.annotations);
    }


    /**
     * Return a string representation of this MethodDeclarationImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "MethodDeclaration[name=" + name + ", isConstructor=" + isConstructor + ", accessFlags=" + accessFlags + "]";
    }
}
