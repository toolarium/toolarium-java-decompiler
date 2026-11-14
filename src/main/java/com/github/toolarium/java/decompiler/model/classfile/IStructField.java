/*
 * IStructField.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile;


/**
 * Represents a field declaration in a class file.
 *
 * @author patrick
 * @see IStructMember
 */
public interface IStructField extends IStructMember {

    /**
     * Get the generic signature of this field, if present.
     *
     * @return the generic signature, or null if not available
     */
    String getSignature();
}
