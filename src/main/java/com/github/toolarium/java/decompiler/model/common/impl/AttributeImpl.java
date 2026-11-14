/*
 * AttributeImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.common.impl;

import com.github.toolarium.java.decompiler.model.common.IAttribute;
import java.util.Arrays;
import java.util.Objects;


/**
 * Implementation of {@link IAttribute} with defensive byte array copying.
 *
 * @author patrick
 */
public class AttributeImpl implements IAttribute {
    private final String name;
    private final byte[] data;


    /**
     * Constructor for AttributeImpl.
     *
     * @param name the attribute name
     * @param data the raw attribute data (defensively copied)
     */
    public AttributeImpl(String name, byte[] data) {
        this.name = Objects.requireNonNull(name, "Attribute name must not be null");
        if (data != null) {
            this.data = Arrays.copyOf(data, data.length);
        } else {
            this.data = new byte[0];
        }
    }


    /**
     * Get the attribute name.
     *
     * @return the attribute name
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Get a defensive copy of the raw attribute data.
     *
     * @return the raw attribute data as a byte array
     */
    @Override
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }


    /**
     * Compute the hash code based on name and data.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }


    /**
     * Check equality based on name and data.
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
        AttributeImpl other = (AttributeImpl) obj;
        return Objects.equals(name, other.name) && Arrays.equals(data, other.data);
    }


    /**
     * Return a string representation including name and data length.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "Attribute[" + name + ", " + data.length + " bytes]";
    }
}
