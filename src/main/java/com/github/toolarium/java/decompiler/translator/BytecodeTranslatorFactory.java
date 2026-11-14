/*
 * BytecodeTranslatorFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.translator;

import com.github.toolarium.java.decompiler.translator.impl.BytecodeTranslatorImpl;


/**
 * Singleton factory for {@link IBytecodeTranslator} instances.
 *
 * @author patrick
 */
public final class BytecodeTranslatorFactory {

    /**
     * Private constructor.
     */
    private BytecodeTranslatorFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final BytecodeTranslatorFactory INSTANCE = new BytecodeTranslatorFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static BytecodeTranslatorFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a bytecode translator.
     *
     * @return the translator
     */
    public IBytecodeTranslator getBytecodeTranslator() {
        return new BytecodeTranslatorImpl();
    }
}
