/*
 * IParameterDeclaration.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.declaration;

import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.List;


/**
 * Represents a method parameter declaration.
 *
 * @author patrick
 */
public interface IParameterDeclaration {

    /**
     * Get the parameter type.
     *
     * @return the parameter type
     */
    IType getParameterType();


    /**
     * Get the parameter name.
     *
     * @return the parameter name
     */
    String getName();


    /**
     * Check whether this parameter is final.
     *
     * @return true if final
     */
    boolean isFinal();


    /**
     * Get the annotations on this parameter.
     *
     * @return an unmodifiable list of annotations
     */
    List<IAnnotation> getAnnotations();
}
