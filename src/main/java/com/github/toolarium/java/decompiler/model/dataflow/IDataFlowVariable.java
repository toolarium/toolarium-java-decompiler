/*
 * IDataFlowVariable.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.dataflow;


/**
 * Represents a data flow variable with a versioned name in static single assignment form.
 *
 * @author patrick
 */
public interface IDataFlowVariable {

    /**
     * Get the original variable index (local variable slot).
     *
     * @return the local variable index
     */
    int getIndex();


    /**
     * Get the version number for this variable.
     *
     * @return the version number (0 for the initial definition)
     */
    int getVersion();


    /**
     * Get the original variable name, if available from debug info.
     *
     * @return the variable name, or null if unknown
     */
    String getName();


    /**
     * Get the versioned display name (e.g., "x_2" for variable x version 2).
     *
     * @return the versioned name
     */
    String getVersionedName();
}
