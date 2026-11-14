/*
 * IExceptionHandler.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode;


/**
 * Represents an exception handler entry from a method's exception table.
 *
 * @author patrick
 */
public interface IExceptionHandler {

    /**
     * Get the start of the try range (inclusive byte offset).
     *
     * @return the start offset
     */
    int getStartOffset();


    /**
     * Get the end of the try range (exclusive byte offset).
     *
     * @return the end offset
     */
    int getEndOffset();


    /**
     * Get the start of the handler code (byte offset).
     *
     * @return the handler offset
     */
    int getHandlerOffset();


    /**
     * Get the caught exception type as an internal name.
     * Returns null for finally blocks (catch-all handlers).
     *
     * @return the exception type internal name, or null for finally
     */
    String getCatchType();
}
