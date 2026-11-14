/*
 * IExceptionTable.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode;

import java.util.List;


/**
 * Represents the full exception table of a method.
 *
 * @author patrick
 */
public interface IExceptionTable {

    /**
     * Get all exception handlers in order.
     *
     * @return an unmodifiable list of exception handlers
     */
    List<IExceptionHandler> getHandlers();


    /**
     * Get the number of exception handlers.
     *
     * @return the number of handlers
     */
    int getSize();


    /**
     * Get all exception handlers whose try range covers the given byte offset.
     *
     * @param offset the byte offset to check
     * @return a list of handlers covering that offset
     */
    List<IExceptionHandler> getHandlersAt(int offset);
}
