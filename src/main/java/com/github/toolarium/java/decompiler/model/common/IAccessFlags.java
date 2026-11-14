/*
 * IAccessFlags.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.common;


/**
 * Represents Java access modifiers from the class file access_flags field.
 *
 * @author patrick
 */
public interface IAccessFlags {

    /**
     * Get the raw access flags as an integer bitmask.
     *
     * @return the raw access flags
     */
    int getRawFlags();


    /**
     * Check whether the public modifier is set.
     *
     * @return true if public
     */
    boolean isPublic();


    /**
     * Check whether the private modifier is set.
     *
     * @return true if private
     */
    boolean isPrivate();


    /**
     * Check whether the protected modifier is set.
     *
     * @return true if protected
     */
    boolean isProtected();


    /**
     * Check whether the static modifier is set.
     *
     * @return true if static
     */
    boolean isStatic();


    /**
     * Check whether the final modifier is set.
     *
     * @return true if final
     */
    boolean isFinal();


    /**
     * Check whether the abstract modifier is set.
     *
     * @return true if abstract
     */
    boolean isAbstract();


    /**
     * Check whether the synthetic flag is set.
     *
     * @return true if synthetic
     */
    boolean isSynthetic();


    /**
     * Check whether the bridge flag is set.
     *
     * @return true if bridge
     */
    boolean isBridge();


    /**
     * Check whether the interface flag is set.
     *
     * @return true if interface
     */
    boolean isInterface();


    /**
     * Check whether the enum flag is set.
     *
     * @return true if enum
     */
    boolean isEnum();


    /**
     * Check whether the annotation flag is set.
     *
     * @return true if annotation
     */
    boolean isAnnotation();
}
