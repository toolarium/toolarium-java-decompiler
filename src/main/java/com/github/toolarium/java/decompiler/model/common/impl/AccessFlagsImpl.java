/*
 * AccessFlagsImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.common.impl;

import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import java.util.Objects;


/**
 * Implementation of {@link IAccessFlags} using bitwise operations on the raw flags integer.
 *
 * @author patrick
 */
public class AccessFlagsImpl implements IAccessFlags {
    private static final int ACC_PUBLIC = 0x0001;
    private static final int ACC_PRIVATE = 0x0002;
    private static final int ACC_PROTECTED = 0x0004;
    private static final int ACC_STATIC = 0x0008;
    private static final int ACC_FINAL = 0x0010;
    private static final int ACC_ABSTRACT = 0x0400;
    private static final int ACC_SYNTHETIC = 0x1000;
    private static final int ACC_BRIDGE = 0x0040;
    private static final int ACC_INTERFACE = 0x0200;
    private static final int ACC_ENUM = 0x4000;
    private static final int ACC_ANNOTATION = 0x2000;

    private final int rawFlags;


    /**
     * Constructor for AccessFlagsImpl.
     *
     * @param rawFlags the raw access flags bitmask
     */
    public AccessFlagsImpl(int rawFlags) {
        this.rawFlags = rawFlags;
    }


    /**
     * Get the raw access flags as an integer bitmask.
     *
     * @return the raw access flags
     */
    @Override
    public int getRawFlags() {
        return rawFlags;
    }


    /**
     * Check whether the public modifier is set.
     *
     * @return true if public
     */
    @Override
    public boolean isPublic() {
        return (rawFlags & ACC_PUBLIC) != 0;
    }


    /**
     * Check whether the private modifier is set.
     *
     * @return true if private
     */
    @Override
    public boolean isPrivate() {
        return (rawFlags & ACC_PRIVATE) != 0;
    }


    /**
     * Check whether the protected modifier is set.
     *
     * @return true if protected
     */
    @Override
    public boolean isProtected() {
        return (rawFlags & ACC_PROTECTED) != 0;
    }


    /**
     * Check whether the static modifier is set.
     *
     * @return true if static
     */
    @Override
    public boolean isStatic() {
        return (rawFlags & ACC_STATIC) != 0;
    }


    /**
     * Check whether the final modifier is set.
     *
     * @return true if final
     */
    @Override
    public boolean isFinal() {
        return (rawFlags & ACC_FINAL) != 0;
    }


    /**
     * Check whether the abstract modifier is set.
     *
     * @return true if abstract
     */
    @Override
    public boolean isAbstract() {
        return (rawFlags & ACC_ABSTRACT) != 0;
    }


    /**
     * Check whether the synthetic flag is set.
     *
     * @return true if synthetic
     */
    @Override
    public boolean isSynthetic() {
        return (rawFlags & ACC_SYNTHETIC) != 0;
    }


    /**
     * Check whether the bridge flag is set.
     *
     * @return true if bridge
     */
    @Override
    public boolean isBridge() {
        return (rawFlags & ACC_BRIDGE) != 0;
    }


    /**
     * Check whether the interface flag is set.
     *
     * @return true if interface
     */
    @Override
    public boolean isInterface() {
        return (rawFlags & ACC_INTERFACE) != 0;
    }


    /**
     * Check whether the enum flag is set.
     *
     * @return true if enum
     */
    @Override
    public boolean isEnum() {
        return (rawFlags & ACC_ENUM) != 0;
    }


    /**
     * Check whether the annotation flag is set.
     *
     * @return true if annotation
     */
    @Override
    public boolean isAnnotation() {
        return (rawFlags & ACC_ANNOTATION) != 0;
    }


    /**
     * Compute the hash code based on the raw flags.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(rawFlags);
    }


    /**
     * Check equality based on the raw flags.
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
        AccessFlagsImpl other = (AccessFlagsImpl) obj;
        return rawFlags == other.rawFlags;
    }


    /**
     * Return a string representation of the access flags in hexadecimal.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "AccessFlags[0x" + Integer.toHexString(rawFlags) + "]";
    }
}
