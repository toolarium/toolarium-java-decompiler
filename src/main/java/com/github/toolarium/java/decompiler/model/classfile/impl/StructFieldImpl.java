/*
 * StructFieldImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile.impl;

import com.github.toolarium.java.decompiler.model.classfile.IStructField;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.common.IAttribute;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IStructField}.
 *
 * @author patrick
 */
public class StructFieldImpl extends StructMemberImpl implements IStructField {
    private final String signature;


    /**
     * Constructor for StructFieldImpl.
     *
     * @param name        the field name
     * @param descriptor  the type descriptor
     * @param accessFlags the access modifiers
     * @param attributes  the field attributes
     * @param annotations the field annotations
     * @param signature   the generic signature (may be null)
     */
    public StructFieldImpl(String name, String descriptor, IAccessFlags accessFlags,
                           List<IAttribute> attributes, List<IAnnotation> annotations, String signature) {
        super(name, descriptor, accessFlags, attributes, annotations);
        this.signature = signature;
    }


    /**
     * Get the generic signature of this field, if present.
     *
     * @return the generic signature, or null if not available
     */
    @Override
    public String getSignature() {
        return signature;
    }


    /**
     * Compute the hash code based on the parent member fields and signature.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(signature);
    }


    /**
     * Check equality based on the parent member fields and signature.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StructFieldImpl other = (StructFieldImpl) obj;
        return Objects.equals(signature, other.signature);
    }


    /**
     * Return a string representation including the field name and descriptor.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "StructField[" + getName() + ", " + getDescriptor() + "]";
    }
}
