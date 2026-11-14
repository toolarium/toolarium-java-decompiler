/*
 * SimplifierFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.simplifier;

import com.github.toolarium.java.decompiler.simplifier.impl.SimplifierImpl;


/**
 * Singleton factory for {@link ISimplifier} instances.
 *
 * @author patrick
 */
public final class SimplifierFactory {

    /**
     * Private constructor.
     */
    private SimplifierFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final SimplifierFactory INSTANCE = new SimplifierFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static SimplifierFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a simplifier with all default passes enabled.
     *
     * @return the simplifier
     */
    public ISimplifier getSimplifier() {
        return new SimplifierImpl();
    }
}
