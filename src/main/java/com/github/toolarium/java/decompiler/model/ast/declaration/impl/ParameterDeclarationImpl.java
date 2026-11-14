/*
 * ParameterDeclarationImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.declaration.impl;

import com.github.toolarium.java.decompiler.model.ast.declaration.IParameterDeclaration;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IParameterDeclaration}.
 *
 * @author patrick
 */
public class ParameterDeclarationImpl implements IParameterDeclaration {
    private final IType parameterType;
    private final String name;
    private final boolean isFinal;
    private final List<IAnnotation> annotations;


    /**
     * Constructor for ParameterDeclarationImpl.
     *
     * @param parameterType the parameter type
     * @param name          the parameter name
     * @param isFinal       true if the parameter is final
     * @param annotations   the annotations on this parameter
     */
    public ParameterDeclarationImpl(IType parameterType, String name, boolean isFinal,
                                    List<IAnnotation> annotations) {
        this.parameterType = parameterType;
        this.name = name;
        this.isFinal = isFinal;
        this.annotations = Collections.unmodifiableList(new ArrayList<>(annotations));
    }


    /**
     * Get the parameter type.
     *
     * @return the parameter type
     */
    @Override
    public IType getParameterType() {
        return parameterType;
    }


    /**
     * Get the parameter name.
     *
     * @return the parameter name
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Check whether this parameter is final.
     *
     * @return true if final
     */
    @Override
    public boolean isFinal() {
        return isFinal;
    }


    /**
     * Get the annotations on this parameter.
     *
     * @return an unmodifiable list of annotations
     */
    @Override
    public List<IAnnotation> getAnnotations() {
        return annotations;
    }


    /**
     * Return a hash code value for this ParameterDeclarationImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(parameterType, name, isFinal, annotations);
    }


    /**
     * Check whether this ParameterDeclarationImpl is equal to another object.
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
        ParameterDeclarationImpl other = (ParameterDeclarationImpl) obj;
        return isFinal == other.isFinal
                && Objects.equals(parameterType, other.parameterType)
                && Objects.equals(name, other.name)
                && Objects.equals(annotations, other.annotations);
    }


    /**
     * Return a string representation of this ParameterDeclarationImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ParameterDeclaration[name=" + name + ", type=" + parameterType + ", isFinal=" + isFinal + "]";
    }
}
