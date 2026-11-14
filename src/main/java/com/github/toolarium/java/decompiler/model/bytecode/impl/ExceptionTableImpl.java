/*
 * ExceptionTableImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode.impl;

import com.github.toolarium.java.decompiler.model.bytecode.IExceptionHandler;
import com.github.toolarium.java.decompiler.model.bytecode.IExceptionTable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IExceptionTable}.
 *
 * @author patrick
 */
public class ExceptionTableImpl implements IExceptionTable {
    private final List<IExceptionHandler> handlers;


    /**
     * Constructor for ExceptionTableImpl.
     *
     * @param handlers the exception handlers (defensively copied)
     */
    public ExceptionTableImpl(List<IExceptionHandler> handlers) {
        if (handlers != null) {
            this.handlers = Collections.unmodifiableList(new ArrayList<>(handlers));
        } else {
            this.handlers = Collections.emptyList();
        }
    }


    /**
     * Get all exception handlers in order.
     *
     * @return an unmodifiable list of exception handlers
     */
    @Override
    public List<IExceptionHandler> getHandlers() {
        return handlers;
    }


    /**
     * Get the number of exception handlers.
     *
     * @return the number of handlers
     */
    @Override
    public int getSize() {
        return handlers.size();
    }


    /**
     * Get all exception handlers whose try range covers the given byte offset.
     *
     * @param offset the byte offset to check
     * @return an unmodifiable list of handlers covering that offset
     */
    @Override
    public List<IExceptionHandler> getHandlersAt(int offset) {
        List<IExceptionHandler> result = new ArrayList<>();
        for (IExceptionHandler handler : handlers) {
            if (offset >= handler.getStartOffset() && offset < handler.getEndOffset()) {
                result.add(handler);
            }
        }
        return Collections.unmodifiableList(result);
    }


    /**
     * Compute the hash code based on the handler list.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(handlers);
    }


    /**
     * Check equality based on the handler list.
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
        ExceptionTableImpl other = (ExceptionTableImpl) obj;
        return Objects.equals(handlers, other.handlers);
    }


    /**
     * Return a string representation including the handler count.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ExceptionTable[" + handlers.size() + " handlers]";
    }
}
