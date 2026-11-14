/*
 * StructRecordComponentImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile.impl;

import com.github.toolarium.java.decompiler.model.classfile.IStructRecordComponent;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IStructRecordComponent}.
 *
 * @author patrick
 */
public class StructRecordComponentImpl implements IStructRecordComponent {
    private final String name;
    private final String descriptor;
    private final String signature;
    private final List<IAnnotation> annotations;


    /**
     * Constructor for StructRecordComponentImpl.
     *
     * @param name        the component name
     * @param descriptor  the type descriptor
     * @param signature   the generic signature (may be null)
     * @param annotations the annotations
     */
    public StructRecordComponentImpl(String name, String descriptor, String signature,
                                     List<IAnnotation> annotations) {
        this.name = Objects.requireNonNull(name, "Record component name must not be null");
        this.descriptor = Objects.requireNonNull(descriptor, "Record component descriptor must not be null");
        this.signature = signature;
        if (annotations != null) {
            this.annotations = Collections.unmodifiableList(new ArrayList<>(annotations));
        } else {
            this.annotations = Collections.emptyList();
        }
    }


    /**
     * Get the component name.
     *
     * @return the component name
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
     * Get the generic signature, if present.
     *
     * @return the generic signature, or null if not available
     */
    @Override
    public String getSignature() {
        return signature;
    }


    /**
     * Get the annotations attached to this record component.
     *
     * @return an unmodifiable list of annotations
     */
    @Override
    public List<IAnnotation> getAnnotations() {
        return annotations;
    }


    /**
     * Compute the hash code based on name, descriptor, signature, and annotations.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, descriptor, signature, annotations);
    }


    /**
     * Check equality based on name, descriptor, signature, and annotations.
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
        StructRecordComponentImpl other = (StructRecordComponentImpl) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(descriptor, other.descriptor)
                && Objects.equals(signature, other.signature)
                && Objects.equals(annotations, other.annotations);
    }


    /**
     * Return a string representation including the component name and descriptor.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "StructRecordComponent[" + name + ", " + descriptor + "]";
    }
}
