/*
 * ClassDeclarationImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.declaration.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IFieldDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IClassDeclaration}.
 *
 * @author patrick
 */
public class ClassDeclarationImpl implements IClassDeclaration {
    private final IAccessFlags accessFlags;
    private final String name;
    private final IType superType;
    private final List<IType> interfaceTypes;
    private final List<IFieldDeclaration> fields;
    private final List<IMethodDeclaration> methods;
    private final List<IClassDeclaration> innerClasses;
    private final List<IAnnotation> annotations;
    private final List<String> permittedSubclasses;
    private final boolean nonSealed;


    /**
     * Constructor for ClassDeclarationImpl.
     *
     * @param accessFlags         the access modifiers
     * @param name                the class name
     * @param superType           the superclass type, or null
     * @param interfaceTypes      the implemented or extended interface types
     * @param fields              the field declarations
     * @param methods             the method declarations
     * @param innerClasses        the inner class declarations
     * @param annotations         the annotations on this class
     * @param permittedSubclasses the permitted subclass names for sealed classes
     * @param nonSealed           true if this class is non-sealed
     */
    public ClassDeclarationImpl(IAccessFlags accessFlags,
                                String name,
                                IType superType,
                                List<IType> interfaceTypes,
                                List<IFieldDeclaration> fields,
                                List<IMethodDeclaration> methods,
                                List<IClassDeclaration> innerClasses,
                                List<IAnnotation> annotations,
                                List<String> permittedSubclasses,
                                boolean nonSealed) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.superType = superType;
        this.interfaceTypes = Collections.unmodifiableList(new ArrayList<>(interfaceTypes));
        this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
        this.methods = Collections.unmodifiableList(new ArrayList<>(methods));
        this.innerClasses = Collections.unmodifiableList(new ArrayList<>(innerClasses));
        this.annotations = Collections.unmodifiableList(new ArrayList<>(annotations));
        if (permittedSubclasses != null) {
            this.permittedSubclasses = Collections.unmodifiableList(new ArrayList<>(permittedSubclasses));
        } else {
            this.permittedSubclasses = Collections.emptyList();
        }
        this.nonSealed = nonSealed;
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitClassDeclaration(this);
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
     * Get the class name.
     *
     * @return the class name
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Get the superclass type.
     *
     * @return the superclass type, or null for interfaces and java.lang.Object
     */
    @Override
    public IType getSuperType() {
        return superType;
    }


    /**
     * Get the implemented or extended interface types.
     *
     * @return an unmodifiable list of interface types
     */
    @Override
    public List<IType> getInterfaceTypes() {
        return interfaceTypes;
    }


    /**
     * Get the field declarations.
     *
     * @return an unmodifiable list of field declarations
     */
    @Override
    public List<IFieldDeclaration> getFields() {
        return fields;
    }


    /**
     * Get the method declarations.
     *
     * @return an unmodifiable list of method declarations
     */
    @Override
    public List<IMethodDeclaration> getMethods() {
        return methods;
    }


    /**
     * Get the inner class declarations.
     *
     * @return an unmodifiable list of inner class declarations
     */
    @Override
    public List<IClassDeclaration> getInnerClasses() {
        return innerClasses;
    }


    /**
     * Get the annotations on this class declaration.
     *
     * @return an unmodifiable list of annotations
     */
    @Override
    public List<IAnnotation> getAnnotations() {
        return annotations;
    }


    /**
     * Get the permitted subclass names for a sealed class or interface.
     * A non-empty list indicates this is a sealed type.
     *
     * @return an unmodifiable list of permitted subclass simple names, empty if not sealed
     */
    @Override
    public List<String> getPermittedSubclasses() {
        return permittedSubclasses;
    }


    /**
     * Check whether this class is declared as non-sealed.
     * A class is non-sealed when it extends a sealed class or implements a sealed interface,
     * but is not itself sealed or final.
     *
     * @return true if this class is non-sealed
     */
    @Override
    public boolean isNonSealed() {
        return nonSealed;
    }


    /**
     * Return a hash code value for this ClassDeclarationImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(accessFlags, name, superType, interfaceTypes,
                fields, methods, innerClasses, annotations,
                permittedSubclasses, nonSealed);
    }


    /**
     * Check whether this ClassDeclarationImpl is equal to another object.
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
        ClassDeclarationImpl other = (ClassDeclarationImpl) obj;
        return Objects.equals(accessFlags, other.accessFlags)
                && Objects.equals(name, other.name)
                && Objects.equals(superType, other.superType)
                && Objects.equals(interfaceTypes, other.interfaceTypes)
                && Objects.equals(fields, other.fields)
                && Objects.equals(methods, other.methods)
                && Objects.equals(innerClasses, other.innerClasses)
                && Objects.equals(annotations, other.annotations)
                && Objects.equals(permittedSubclasses, other.permittedSubclasses)
                && nonSealed == other.nonSealed;
    }


    /**
     * Return a string representation of this ClassDeclarationImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ClassDeclaration[name=" + name + ", accessFlags=" + accessFlags + "]";
    }
}
