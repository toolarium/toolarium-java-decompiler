/*
 * IAnnotation.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.common;

import java.util.Map;


/**
 * Represents a Java annotation attached to a class, field, method, or parameter.
 *
 * @author patrick
 */
public interface IAnnotation {

    /**
     * Get the annotation type descriptor (e.g., "Ljava/lang/Override;").
     *
     * @return the annotation type descriptor
     */
    String getDescriptor();


    /**
     * Check whether this annotation is runtime-visible.
     *
     * @return true if the annotation is runtime-visible
     */
    boolean isVisible();


    /**
     * Get the annotation element-value pairs.
     *
     * @return an unmodifiable map of element names to their values
     */
    Map<String, Object> getValues();
}
