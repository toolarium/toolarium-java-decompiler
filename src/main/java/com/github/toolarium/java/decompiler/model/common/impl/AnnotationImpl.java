/*
 * AnnotationImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.common.impl;

import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Implementation of {@link IAnnotation} with an immutable values map.
 *
 * @author patrick
 */
public class AnnotationImpl implements IAnnotation {
    private final String descriptor;
    private final boolean visible;
    private final Map<String, Object> values;


    /**
     * Constructor for AnnotationImpl.
     *
     * @param descriptor the annotation type descriptor
     * @param visible    true if runtime-visible
     * @param values     the annotation element-value pairs (defensively copied)
     */
    public AnnotationImpl(String descriptor, boolean visible, Map<String, Object> values) {
        this.descriptor = Objects.requireNonNull(descriptor, "Annotation descriptor must not be null");
        this.visible = visible;
        if (values != null) {
            this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        } else {
            this.values = Collections.emptyMap();
        }
    }


    /**
     * Get the annotation type descriptor (e.g., "Ljava/lang/Override;").
     *
     * @return the annotation type descriptor
     */
    @Override
    public String getDescriptor() {
        return descriptor;
    }


    /**
     * Check whether this annotation is runtime-visible.
     *
     * @return true if the annotation is runtime-visible
     */
    @Override
    public boolean isVisible() {
        return visible;
    }


    /**
     * Get the annotation element-value pairs.
     *
     * @return an unmodifiable map of element names to their values
     */
    @Override
    public Map<String, Object> getValues() {
        return values;
    }


    /**
     * Compute the hash code based on descriptor, visibility, and values.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(descriptor, visible, values);
    }


    /**
     * Check equality based on descriptor, visibility, and values.
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
        AnnotationImpl other = (AnnotationImpl) obj;
        return visible == other.visible
                && Objects.equals(descriptor, other.descriptor)
                && Objects.equals(values, other.values);
    }


    /**
     * Return a string representation including descriptor, visibility, and values.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "Annotation[" + descriptor + ", visible=" + visible + ", values=" + values + "]";
    }
}
