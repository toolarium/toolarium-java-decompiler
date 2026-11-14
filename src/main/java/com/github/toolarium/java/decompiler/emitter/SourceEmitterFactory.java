/*
 * SourceEmitterFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.emitter;

import com.github.toolarium.java.decompiler.emitter.impl.SourceEmitterImpl;


/**
 * Singleton factory for {@link ISourceEmitter} instances.
 *
 * @author patrick
 */
public final class SourceEmitterFactory {

    /**
     * Private constructor.
     */
    private SourceEmitterFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final SourceEmitterFactory INSTANCE = new SourceEmitterFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static SourceEmitterFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a source emitter with default settings (4-space indent).
     *
     * @return the source emitter
     */
    public ISourceEmitter getSourceEmitter() {
        return new SourceEmitterImpl("    ", "\n");
    }


    /**
     * Get a source emitter with custom formatting.
     *
     * @param indent        the indentation string
     * @param lineSeparator the line separator
     * @return the source emitter
     */
    public ISourceEmitter getSourceEmitter(String indent, String lineSeparator) {
        return new SourceEmitterImpl(indent, lineSeparator);
    }
}
