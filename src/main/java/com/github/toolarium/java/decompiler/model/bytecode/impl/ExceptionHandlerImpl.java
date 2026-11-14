/*
 * ExceptionHandlerImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode.impl;

import com.github.toolarium.java.decompiler.model.bytecode.IExceptionHandler;
import java.util.Objects;


/**
 * Implementation of {@link IExceptionHandler}.
 *
 * @author patrick
 */
public class ExceptionHandlerImpl implements IExceptionHandler {
    private final int startOffset;
    private final int endOffset;
    private final int handlerOffset;
    private final String catchType;


    /**
     * Constructor for ExceptionHandlerImpl.
     *
     * @param startOffset   the start of the try range (inclusive)
     * @param endOffset     the end of the try range (exclusive)
     * @param handlerOffset the start of the handler code
     * @param catchType     the caught exception type (null for finally)
     */
    public ExceptionHandlerImpl(int startOffset, int endOffset, int handlerOffset, String catchType) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.handlerOffset = handlerOffset;
        this.catchType = catchType;
    }


    /**
     * Get the start of the try range (inclusive byte offset).
     *
     * @return the start offset
     */
    @Override
    public int getStartOffset() {
        return startOffset;
    }


    /**
     * Get the end of the try range (exclusive byte offset).
     *
     * @return the end offset
     */
    @Override
    public int getEndOffset() {
        return endOffset;
    }


    /**
     * Get the start of the handler code (byte offset).
     *
     * @return the handler offset
     */
    @Override
    public int getHandlerOffset() {
        return handlerOffset;
    }


    /**
     * Get the caught exception type as an internal name.
     * Returns null for finally blocks (catch-all handlers).
     *
     * @return the exception type internal name, or null for finally
     */
    @Override
    public String getCatchType() {
        return catchType;
    }


    /**
     * Compute the hash code based on start offset, end offset, handler offset, and catch type.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(startOffset, endOffset, handlerOffset, catchType);
    }


    /**
     * Check equality based on start offset, end offset, handler offset, and catch type.
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
        ExceptionHandlerImpl other = (ExceptionHandlerImpl) obj;
        return startOffset == other.startOffset
                && endOffset == other.endOffset
                && handlerOffset == other.handlerOffset
                && Objects.equals(catchType, other.catchType);
    }


    /**
     * Return a string representation including offset range, handler offset, and catch type.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        String type = catchType;
        if (type == null) {
            type = "finally";
        }
        return "ExceptionHandler[" + startOffset + "-" + endOffset + " -> " + handlerOffset + ", " + type + "]";
    }
}
