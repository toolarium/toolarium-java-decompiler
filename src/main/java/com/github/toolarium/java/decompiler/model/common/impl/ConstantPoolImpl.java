/*
 * ConstantPoolImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.common.impl;

import com.github.toolarium.java.decompiler.model.common.IConstantPool;
import java.util.Arrays;
import java.util.Objects;


/**
 * Implementation of {@link IConstantPool} backed by an Object array.
 *
 * @author patrick
 */
public class ConstantPoolImpl implements IConstantPool {
    private static final String ENTRY_AT_INDEX = "Constant pool entry at index ";
    private final Object[] entries;


    /**
     * Constructor for ConstantPoolImpl.
     *
     * @param entries the constant pool entries (defensively copied, index 0 is unused)
     */
    public ConstantPoolImpl(Object[] entries) {
        Objects.requireNonNull(entries, "Constant pool entries must not be null");
        this.entries = Arrays.copyOf(entries, entries.length);
    }


    /**
     * Get the number of entries in the constant pool.
     *
     * @return the number of entries
     */
    @Override
    public int getSize() {
        return entries.length;
    }


    /**
     * Get the constant pool entry at the given index.
     *
     * @param index the constant pool index (1-based)
     * @return the entry, or null if the slot is unused (e.g., after a long/double)
     */
    @Override
    public Object getEntry(int index) {
        validateIndex(index);
        return entries[index];
    }


    /**
     * Get the UTF-8 string at the given index.
     *
     * @param index the constant pool index
     * @return the UTF-8 string
     * @throws IllegalArgumentException if the index does not point to a UTF-8 entry
     */
    @Override
    public String getUtf8(int index) {
        Object entry = getEntry(index);
        if (entry instanceof String) {
            return (String) entry;
        }
        throw new IllegalArgumentException(ENTRY_AT_INDEX + index + " is not a UTF-8 string");
    }


    /**
     * Get the class name at the given index.
     *
     * @param index the constant pool index
     * @return the class internal name
     * @throws IllegalArgumentException if the index does not point to a class entry
     */
    @Override
    public String getClassName(int index) {
        Object entry = getEntry(index);
        if (entry instanceof String) {
            return (String) entry;
        }
        throw new IllegalArgumentException(ENTRY_AT_INDEX + index + " is not a class name");
    }


    /**
     * Get the integer value at the given index.
     *
     * @param index the constant pool index
     * @return the integer value
     * @throws IllegalArgumentException if the index does not point to an integer entry
     */
    @Override
    public int getInteger(int index) {
        Object entry = getEntry(index);
        if (entry instanceof Integer) {
            return (Integer) entry;
        }
        throw new IllegalArgumentException(ENTRY_AT_INDEX + index + " is not an integer");
    }


    /**
     * Get the long value at the given index.
     *
     * @param index the constant pool index
     * @return the long value
     * @throws IllegalArgumentException if the index does not point to a long entry
     */
    @Override
    public long getLong(int index) {
        Object entry = getEntry(index);
        if (entry instanceof Long) {
            return (Long) entry;
        }
        throw new IllegalArgumentException(ENTRY_AT_INDEX + index + " is not a long");
    }


    /**
     * Get the float value at the given index.
     *
     * @param index the constant pool index
     * @return the float value
     * @throws IllegalArgumentException if the index does not point to a float entry
     */
    @Override
    public float getFloat(int index) {
        Object entry = getEntry(index);
        if (entry instanceof Float) {
            return (Float) entry;
        }
        throw new IllegalArgumentException(ENTRY_AT_INDEX + index + " is not a float");
    }


    /**
     * Get the double value at the given index.
     *
     * @param index the constant pool index
     * @return the double value
     * @throws IllegalArgumentException if the index does not point to a double entry
     */
    @Override
    public double getDouble(int index) {
        Object entry = getEntry(index);
        if (entry instanceof Double) {
            return (Double) entry;
        }
        throw new IllegalArgumentException(ENTRY_AT_INDEX + index + " is not a double");
    }


    /**
     * Get the string constant at the given index.
     *
     * @param index the constant pool index
     * @return the string value
     * @throws IllegalArgumentException if the index does not point to a string entry
     */
    @Override
    public String getString(int index) {
        Object entry = getEntry(index);
        if (entry instanceof String) {
            return (String) entry;
        }
        throw new IllegalArgumentException(ENTRY_AT_INDEX + index + " is not a string");
    }


    /**
     * Compute the hash code based on the constant pool entries.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(entries);
    }


    /**
     * Check equality based on the constant pool entries.
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
        ConstantPoolImpl other = (ConstantPoolImpl) obj;
        return Arrays.equals(entries, other.entries);
    }


    /**
     * Return a string representation including the entry count.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ConstantPool[" + entries.length + " entries]";
    }


    /**
     * Validate that the index is within range.
     *
     * @param index the index to validate
     * @throws IllegalArgumentException if the index is out of range
     */
    private void validateIndex(int index) {
        if (index < 0 || index >= entries.length) {
            throw new IllegalArgumentException("Constant pool index out of range: " + index);
        }
    }
}
