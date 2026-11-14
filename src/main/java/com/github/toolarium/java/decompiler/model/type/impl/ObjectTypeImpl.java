/*
 * ObjectTypeImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type.impl;

import com.github.toolarium.java.decompiler.model.type.IObjectType;
import java.util.Objects;


/**
 * Implementation of {@link IObjectType}.
 *
 * @author patrick
 */
public class ObjectTypeImpl implements IObjectType {
    private final String internalName;


    /**
     * Constructor for ObjectTypeImpl.
     *
     * @param internalName the JVM internal name (e.g., "java/lang/String")
     */
    public ObjectTypeImpl(String internalName) {
        this.internalName = Objects.requireNonNull(internalName, "Internal name must not be null");
    }


    /**
     * Get the JVM internal name (e.g., "java/lang/String").
     *
     * @return the internal name
     */
    @Override
    public String getInternalName() {
        return internalName;
    }


    /**
     * Get the package name in dotted form (e.g., "java.lang").
     *
     * @return the package name, empty string for the default package
     */
    @Override
    public String getPackageName() {
        int lastSlash = internalName.lastIndexOf('/');
        if (lastSlash < 0) {
            return "";
        }
        return internalName.substring(0, lastSlash).replace('/', '.');
    }


    /**
     * Get the simple class name (e.g., "String").
     *
     * @return the simple name
     */
    @Override
    public String getSimpleName() {
        int lastSlash = internalName.lastIndexOf('/');
        if (lastSlash < 0) {
            return internalName;
        }
        return internalName.substring(lastSlash + 1);
    }


    /**
     * Get the JVM type descriptor in the form "Linternal/name;".
     *
     * @return the type descriptor
     */
    @Override
    public String getDescriptor() {
        return "L" + internalName + ";";
    }


    /**
     * Get the fully qualified class name in dotted form.
     *
     * @return the display name (e.g., "java.lang.String")
     */
    @Override
    public String getDisplayName() {
        return internalName.replace('/', '.');
    }


    /**
     * Check whether this is a primitive type. Always returns false.
     *
     * @return false
     */
    @Override
    public boolean isPrimitive() {
        return false;
    }


    /**
     * Check whether this is an array type. Always returns false.
     *
     * @return false
     */
    @Override
    public boolean isArray() {
        return false;
    }


    /**
     * Check whether this is the void type. Always returns false.
     *
     * @return false
     */
    @Override
    public boolean isVoid() {
        return false;
    }


    /**
     * Get the number of array dimensions. Always returns 0.
     *
     * @return 0
     */
    @Override
    public int getDimensions() {
        return 0;
    }


    /**
     * Compute the hash code based on the internal name.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(internalName);
    }


    /**
     * Check equality based on the internal name.
     *
     * @param obj the object to compare with
     * @return true if the other object is an ObjectTypeImpl with the same internal name
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ObjectTypeImpl other = (ObjectTypeImpl) obj;
        return Objects.equals(internalName, other.internalName);
    }


    /**
     * Return a string representation including the internal name.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ObjectType[" + internalName + "]";
    }
}
