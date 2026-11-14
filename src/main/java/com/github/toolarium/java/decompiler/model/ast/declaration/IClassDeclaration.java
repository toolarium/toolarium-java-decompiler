/*
 * IClassDeclaration.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.declaration;

import com.github.toolarium.java.decompiler.model.ast.IAstNode;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.List;


/**
 * Represents a class, interface, or enum declaration.
 *
 * @author patrick
 */
public interface IClassDeclaration extends IAstNode {

    /**
     * Get the access modifiers.
     *
     * @return the access flags
     */
    IAccessFlags getAccessFlags();


    /**
     * Get the class name (simple name).
     *
     * @return the class name
     */
    String getName();


    /**
     * Get the superclass type (null for interfaces and java.lang.Object).
     *
     * @return the superclass type, or null
     */
    IType getSuperType();


    /**
     * Get the implemented or extended interface types.
     *
     * @return an unmodifiable list of interface types
     */
    List<IType> getInterfaceTypes();


    /**
     * Get the field declarations.
     *
     * @return an unmodifiable list of field declarations
     */
    List<IFieldDeclaration> getFields();


    /**
     * Get the method declarations.
     *
     * @return an unmodifiable list of method declarations
     */
    List<IMethodDeclaration> getMethods();


    /**
     * Get the inner class declarations.
     *
     * @return an unmodifiable list of inner class declarations
     */
    List<IClassDeclaration> getInnerClasses();


    /**
     * Get the annotations on this class declaration.
     *
     * @return an unmodifiable list of annotations
     */
    List<IAnnotation> getAnnotations();


    /**
     * Get the permitted subclass names for a sealed class or interface.
     * A non-empty list indicates this is a sealed type.
     *
     * @return an unmodifiable list of permitted subclass simple names, empty if not sealed
     */
    List<String> getPermittedSubclasses();


    /**
     * Check whether this class is declared as non-sealed.
     * A class is non-sealed when it extends a sealed class or implements a sealed interface,
     * but is not itself sealed or final.
     *
     * @return true if this class is non-sealed
     */
    boolean isNonSealed();
}
