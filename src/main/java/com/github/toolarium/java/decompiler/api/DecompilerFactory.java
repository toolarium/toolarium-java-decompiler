/*
 * DecompilerFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.api;

import com.github.toolarium.java.decompiler.impl.DecompilerImpl;


/**
 * Singleton factory for {@link IDecompiler} instances.
 *
 * @author patrick
 */
public final class DecompilerFactory {

    /**
     * Private constructor.
     */
    private DecompilerFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final DecompilerFactory INSTANCE = new DecompilerFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static DecompilerFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a decompiler with default settings.
     *
     * @return the decompiler
     */
    public IDecompiler getDecompiler() {
        return new DecompilerImpl();
    }


    /**
     * Get a decompiler with custom configuration.
     *
     * @param configuration the decompiler configuration
     * @return the decompiler
     */
    public IDecompiler getDecompiler(IDecompilerConfiguration configuration) {
        return new DecompilerImpl(configuration);
    }
}
