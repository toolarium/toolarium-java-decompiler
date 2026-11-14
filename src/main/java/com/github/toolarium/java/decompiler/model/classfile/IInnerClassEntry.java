/*
 * IInnerClassEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile;

import com.github.toolarium.java.decompiler.model.common.IAccessFlags;


/**
 * Represents an entry from the InnerClasses attribute of a class file.
 * Each entry describes a relationship between an inner class and its enclosing class.
 *
 * @author patrick
 */
public interface IInnerClassEntry {

    /**
     * Get the internal name of the inner class (e.g., "com/example/Outer$Inner").
     *
     * @return the inner class internal name
     */
    String getInternalName();


    /**
     * Get the internal name of the outer class (e.g., "com/example/Outer").
     * Returns null for anonymous and local classes.
     *
     * @return the outer class internal name, or null
     */
    String getOuterName();


    /**
     * Get the simple name of the inner class (e.g., "Inner").
     * Returns null for anonymous classes.
     *
     * @return the simple name, or null
     */
    String getSimpleName();


    /**
     * Get the access flags of the inner class as declared in the InnerClasses attribute.
     *
     * @return the access flags
     */
    IAccessFlags getAccessFlags();
}
