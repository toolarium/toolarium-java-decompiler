/*
 * StructMemberImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile.impl;

import com.github.toolarium.java.decompiler.model.classfile.IStructMember;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.common.IAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Base implementation of {@link IStructMember} for fields and methods.
 *
 * @author patrick
 */
public class StructMemberImpl implements IStructMember {
    private final String name;
    private final String descriptor;
    private final IAccessFlags accessFlags;
    private final List<IAttribute> attributes;
    private final List<IAnnotation> annotations;


    /**
     * Constructor for StructMemberImpl.
     *
     * @param name        the member name
     * @param descriptor  the type descriptor
     * @param accessFlags the access modifiers
     * @param attributes  the member attributes
     * @param annotations the member annotations
     */
    public StructMemberImpl(String name, String descriptor, IAccessFlags accessFlags,
                            List<IAttribute> attributes, List<IAnnotation> annotations) {
        this.name = Objects.requireNonNull(name, "Member name must not be null");
        this.descriptor = Objects.requireNonNull(descriptor, "Member descriptor must not be null");
        this.accessFlags = Objects.requireNonNull(accessFlags, "Access flags must not be null");
        if (attributes != null) {
            this.attributes = Collections.unmodifiableList(new ArrayList<>(attributes));
        } else {
            this.attributes = Collections.emptyList();
        }
        if (annotations != null) {
            this.annotations = Collections.unmodifiableList(new ArrayList<>(annotations));
        } else {
            this.annotations = Collections.emptyList();
        }
    }


    /**
     * Get the member name.
     *
     * @return the member name
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Get the JVM type descriptor.
     *
     * @return the JVM type descriptor
     */
    @Override
    public String getDescriptor() {
        return descriptor;
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
     * Get the attributes attached to this member.
     *
     * @return an unmodifiable list of attributes
     */
    @Override
    public List<IAttribute> getAttributes() {
        return attributes;
    }


    /**
     * Get the annotations attached to this member.
     *
     * @return an unmodifiable list of annotations
     */
    @Override
    public List<IAnnotation> getAnnotations() {
        return annotations;
    }


    /**
     * Compute the hash code based on name, descriptor, access flags, attributes, and annotations.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, descriptor, accessFlags, attributes, annotations);
    }


    /**
     * Check equality based on name, descriptor, access flags, attributes, and annotations.
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
        StructMemberImpl other = (StructMemberImpl) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(descriptor, other.descriptor)
                && Objects.equals(accessFlags, other.accessFlags)
                && Objects.equals(attributes, other.attributes)
                && Objects.equals(annotations, other.annotations);
    }


    /**
     * Return a string representation including the member name and descriptor.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ", " + descriptor + "]";
    }
}
