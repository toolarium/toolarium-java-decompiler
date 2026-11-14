/*
 * IAttribute.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.common;


/**
 * Represents a class file attribute (e.g., "Code", "LineNumberTable", "SourceFile").
 *
 * @author patrick
 */
public interface IAttribute {

    /**
     * Get the attribute name.
     *
     * @return the attribute name
     */
    String getName();


    /**
     * Get the raw attribute data.
     *
     * @return the raw attribute data as a byte array
     */
    byte[] getData();
}
