/*
 * IStructMember.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile;

import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.common.IAttribute;
import java.util.List;


/**
 * Base interface for class members (fields and methods).
 *
 * @author patrick
 */
public interface IStructMember {

    /**
     * Get the member name.
     *
     * @return the member name
     */
    String getName();


    /**
     * Get the type descriptor.
     *
     * @return the JVM type descriptor
     */
    String getDescriptor();


    /**
     * Get the access modifiers.
     *
     * @return the access flags
     */
    IAccessFlags getAccessFlags();


    /**
     * Get the attributes attached to this member.
     *
     * @return an unmodifiable list of attributes
     */
    List<IAttribute> getAttributes();


    /**
     * Get the annotations attached to this member.
     *
     * @return an unmodifiable list of annotations
     */
    List<IAnnotation> getAnnotations();
}
