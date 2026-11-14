/*
 * IType.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type;


/**
 * Base interface for all types in the decompiler type system.
 *
 * @author patrick
 */
public interface IType {

    /**
     * Get the JVM type descriptor (e.g., "I", "Ljava/lang/String;", "[B").
     *
     * @return the type descriptor
     */
    String getDescriptor();


    /**
     * Get the human-readable display name (e.g., "int", "String", "byte[]").
     *
     * @return the display name
     */
    String getDisplayName();


    /**
     * Check whether this is a primitive type.
     *
     * @return true if this is a primitive type
     */
    boolean isPrimitive();


    /**
     * Check whether this is an array type.
     *
     * @return true if this is an array type
     */
    boolean isArray();


    /**
     * Check whether this is the void type.
     *
     * @return true if void
     */
    boolean isVoid();


    /**
     * Get the number of array dimensions.
     *
     * @return the number of dimensions, 0 for non-array types
     */
    int getDimensions();
}
