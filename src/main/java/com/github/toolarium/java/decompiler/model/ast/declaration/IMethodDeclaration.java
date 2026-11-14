/*
 * IMethodDeclaration.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.declaration;

import com.github.toolarium.java.decompiler.model.ast.IAstNode;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.List;


/**
 * Represents a method or constructor declaration.
 *
 * @author patrick
 */
public interface IMethodDeclaration extends IAstNode {

    /**
     * Get the access modifiers.
     *
     * @return the access flags
     */
    IAccessFlags getAccessFlags();


    /**
     * Get the method name.
     *
     * @return the method name
     */
    String getName();


    /**
     * Get the return type (null for constructors).
     *
     * @return the return type, or null
     */
    IType getReturnType();


    /**
     * Get the parameter declarations.
     *
     * @return an unmodifiable list of parameter declarations
     */
    List<IParameterDeclaration> getParameters();


    /**
     * Get the declared exception types.
     *
     * @return an unmodifiable list of exception types
     */
    List<IType> getThrownTypes();


    /**
     * Get the method body (null for abstract or native methods).
     *
     * @return the body statement, or null
     */
    IStatement getBody();


    /**
     * Check whether this is a constructor.
     *
     * @return true if this is a constructor
     */
    boolean isConstructor();


    /**
     * Get the annotations on this method declaration.
     *
     * @return an unmodifiable list of annotations
     */
    List<IAnnotation> getAnnotations();
}
