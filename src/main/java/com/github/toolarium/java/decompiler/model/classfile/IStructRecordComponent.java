/*
 * IStructRecordComponent.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile;

import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import java.util.List;


/**
 * Represents a record component in a Java record class.
 *
 * @author patrick
 */
public interface IStructRecordComponent {

    /**
     * Get the component name.
     *
     * @return the component name
     */
    String getName();


    /**
     * Get the type descriptor.
     *
     * @return the JVM type descriptor
     */
    String getDescriptor();


    /**
     * Get the generic signature, if present.
     *
     * @return the generic signature, or null if not available
     */
    String getSignature();


    /**
     * Get the annotations attached to this record component.
     *
     * @return an unmodifiable list of annotations
     */
    List<IAnnotation> getAnnotations();
}
