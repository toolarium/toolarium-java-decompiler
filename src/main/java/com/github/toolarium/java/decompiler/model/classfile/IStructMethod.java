/*
 * IStructMethod.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile;

import java.util.List;


/**
 * Represents a method declaration in a class file.
 *
 * @author patrick
 * @see IStructMember
 */
public interface IStructMethod extends IStructMember {

    /**
     * Get the generic signature of this method, if present.
     *
     * @return the generic signature, or null if not available
     */
    String getSignature();


    /**
     * Get the parameter names recovered from debug information.
     *
     * @return an unmodifiable list of parameter names, empty if not available
     */
    List<String> getParameterNames();


    /**
     * Get the parameter annotations for this method.
     * The outer list is indexed by parameter position. Each inner list contains
     * the annotations for that parameter.
     *
     * @return an unmodifiable list of parameter annotation lists
     */
    List<List<com.github.toolarium.java.decompiler.model.common.IAnnotation>> getParameterAnnotations();


    /**
     * Get the checked exception types declared by this method.
     *
     * @return an unmodifiable list of exception type internal names
     */
    List<String> getExceptionTypes();


    /**
     * Get the raw bytecode of this method.
     *
     * @return the bytecode array, or null for abstract or native methods
     */
    byte[] getCode();


    /**
     * Get the maximum operand stack depth.
     *
     * @return the max stack depth
     */
    int getMaxStack();


    /**
     * Get the maximum number of local variables.
     *
     * @return the max local variable count
     */
    int getMaxLocals();
}
