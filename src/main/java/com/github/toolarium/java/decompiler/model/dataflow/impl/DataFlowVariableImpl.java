/*
 * DataFlowVariableImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.dataflow.impl;

import com.github.toolarium.java.decompiler.model.dataflow.IDataFlowVariable;
import java.util.Objects;


/**
 * Implementation of {@link IDataFlowVariable}.
 *
 * @author patrick
 */
public class DataFlowVariableImpl implements IDataFlowVariable {
    private final int index;
    private final int version;
    private final String name;


    /**
     * Constructor for DataFlowVariableImpl.
     *
     * @param index   the local variable index
     * @param version the version number
     * @param name    the variable name (may be null)
     */
    public DataFlowVariableImpl(int index, int version, String name) {
        this.index = index;
        this.version = version;
        this.name = name;
    }


    /**
     * Get the original variable index (local variable slot).
     *
     * @return the local variable index
     */
    @Override
    public int getIndex() {
        return index;
    }


    /**
     * Get the version number for this variable.
     *
     * @return the version number (0 for the initial definition)
     */
    @Override
    public int getVersion() {
        return version;
    }


    /**
     * Get the original variable name, if available from debug info.
     *
     * @return the variable name, or null if unknown
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Get the versioned display name (e.g., "x_2" for variable x version 2).
     *
     * @return the versioned name
     */
    @Override
    public String getVersionedName() {
        String baseName = name;
        if (baseName == null) {
            baseName = "var" + index;
        }
        return baseName + "_" + version;
    }


    /**
     * Compute the hash code based on the variable index and version.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(index, version);
    }


    /**
     * Check equality based on the variable index and version.
     *
     * @param obj the object to compare with
     * @return true if the other object is a DataFlowVariableImpl with the same index and version
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DataFlowVariableImpl other = (DataFlowVariableImpl) obj;
        return index == other.index && version == other.version;
    }


    /**
     * Return the versioned name as the string representation.
     *
     * @return the versioned name string
     */
    @Override
    public String toString() {
        return getVersionedName();
    }
}
