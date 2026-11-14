/*
 * InnerClassEntryImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile.impl;

import com.github.toolarium.java.decompiler.model.classfile.IInnerClassEntry;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import java.util.Objects;


/**
 * Implementation of {@link IInnerClassEntry}.
 *
 * @author patrick
 */
public class InnerClassEntryImpl implements IInnerClassEntry {
    private final String internalName;
    private final String outerName;
    private final String simpleName;
    private final IAccessFlags accessFlags;


    /**
     * Constructor for InnerClassEntryImpl.
     *
     * @param internalName the inner class internal name
     * @param outerName    the outer class internal name, or null for anonymous/local classes
     * @param simpleName   the simple name, or null for anonymous classes
     * @param accessFlags  the access flags
     */
    public InnerClassEntryImpl(String internalName, String outerName,
                               String simpleName, IAccessFlags accessFlags) {
        this.internalName = Objects.requireNonNull(internalName, "Internal name must not be null");
        this.outerName = outerName;
        this.simpleName = simpleName;
        this.accessFlags = Objects.requireNonNull(accessFlags, "Access flags must not be null");
    }


    /**
     * Get the internal name of the inner class (e.g., "com/example/Outer$Inner").
     *
     * @return the inner class internal name
     */
    @Override
    public String getInternalName() {
        return internalName;
    }


    /**
     * Get the internal name of the outer class (e.g., "com/example/Outer").
     * Returns null for anonymous and local classes.
     *
     * @return the outer class internal name, or null
     */
    @Override
    public String getOuterName() {
        return outerName;
    }


    /**
     * Get the simple name of the inner class (e.g., "Inner").
     * Returns null for anonymous classes.
     *
     * @return the simple name, or null
     */
    @Override
    public String getSimpleName() {
        return simpleName;
    }


    /**
     * Get the access flags of the inner class as declared in the InnerClasses attribute.
     *
     * @return the access flags
     */
    @Override
    public IAccessFlags getAccessFlags() {
        return accessFlags;
    }


    /**
     * Compute the hash code based on internal name, outer name, and simple name.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(internalName, outerName, simpleName, accessFlags);
    }


    /**
     * Check equality based on internal name, outer name, simple name, and access flags.
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
        InnerClassEntryImpl other = (InnerClassEntryImpl) obj;
        return Objects.equals(internalName, other.internalName)
                && Objects.equals(outerName, other.outerName)
                && Objects.equals(simpleName, other.simpleName)
                && Objects.equals(accessFlags, other.accessFlags);
    }


    /**
     * Return a string representation including the inner and outer class names.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "InnerClassEntry[" + internalName + ", outer=" + outerName
                + ", simpleName=" + simpleName + "]";
    }
}
