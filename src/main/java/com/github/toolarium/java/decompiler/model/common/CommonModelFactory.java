/*
 * CommonModelFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.common;

import com.github.toolarium.java.decompiler.model.common.impl.AccessFlagsImpl;
import com.github.toolarium.java.decompiler.model.common.impl.AnnotationImpl;
import com.github.toolarium.java.decompiler.model.common.impl.AttributeImpl;
import com.github.toolarium.java.decompiler.model.common.impl.ConstantPoolImpl;
import java.util.Map;


/**
 * Singleton factory for common model objects.
 *
 * @author patrick
 */
public final class CommonModelFactory {

    /**
     * Private constructor.
     */
    private CommonModelFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final CommonModelFactory INSTANCE = new CommonModelFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static CommonModelFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get an access flags instance.
     *
     * @param rawFlags the raw access flags bitmask
     * @return the access flags
     */
    public IAccessFlags getAccessFlags(int rawFlags) {
        return new AccessFlagsImpl(rawFlags);
    }


    /**
     * Get an attribute instance.
     *
     * @param name the attribute name
     * @param data the raw attribute data
     * @return the attribute
     */
    public IAttribute getAttribute(String name, byte[] data) {
        return new AttributeImpl(name, data);
    }


    /**
     * Get an annotation instance.
     *
     * @param descriptor the annotation type descriptor
     * @param visible    true if runtime-visible
     * @param values     the annotation element-value pairs
     * @return the annotation
     */
    public IAnnotation getAnnotation(String descriptor, boolean visible, Map<String, Object> values) {
        return new AnnotationImpl(descriptor, visible, values);
    }


    /**
     * Get a constant pool instance.
     *
     * @param entries the constant pool entries
     * @return the constant pool
     */
    public IConstantPool getConstantPool(Object[] entries) {
        return new ConstantPoolImpl(entries);
    }
}
