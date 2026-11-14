/*
 * IConstantPool.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.common;


/**
 * Represents the constant pool of a Java class file.
 *
 * @author patrick
 */
public interface IConstantPool {

    /**
     * Get the number of entries in the constant pool.
     *
     * @return the number of entries
     */
    int getSize();


    /**
     * Get the constant pool entry at the given index.
     *
     * @param index the constant pool index (1-based)
     * @return the entry, or null if the slot is unused (e.g., after a long/double)
     */
    Object getEntry(int index);


    /**
     * Get the UTF-8 string at the given index.
     *
     * @param index the constant pool index
     * @return the UTF-8 string
     * @throws IllegalArgumentException if the index does not point to a UTF-8 entry
     */
    String getUtf8(int index);


    /**
     * Get the class name at the given index.
     *
     * @param index the constant pool index
     * @return the class internal name
     * @throws IllegalArgumentException if the index does not point to a class entry
     */
    String getClassName(int index);


    /**
     * Get the integer value at the given index.
     *
     * @param index the constant pool index
     * @return the integer value
     * @throws IllegalArgumentException if the index does not point to an integer entry
     */
    int getInteger(int index);


    /**
     * Get the long value at the given index.
     *
     * @param index the constant pool index
     * @return the long value
     * @throws IllegalArgumentException if the index does not point to a long entry
     */
    long getLong(int index);


    /**
     * Get the float value at the given index.
     *
     * @param index the constant pool index
     * @return the float value
     * @throws IllegalArgumentException if the index does not point to a float entry
     */
    float getFloat(int index);


    /**
     * Get the double value at the given index.
     *
     * @param index the constant pool index
     * @return the double value
     * @throws IllegalArgumentException if the index does not point to a double entry
     */
    double getDouble(int index);


    /**
     * Get the string constant at the given index.
     *
     * @param index the constant pool index
     * @return the string value
     * @throws IllegalArgumentException if the index does not point to a string entry
     */
    String getString(int index);
}
