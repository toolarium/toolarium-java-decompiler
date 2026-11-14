/*
 * StructurerFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.structurer;

import com.github.toolarium.java.decompiler.structurer.impl.StructurerImpl;


/**
 * Singleton factory for {@link IStructurer} instances.
 *
 * @author patrick
 */
public final class StructurerFactory {

    /**
     * Private constructor.
     */
    private StructurerFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final StructurerFactory INSTANCE = new StructurerFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static StructurerFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a structurer.
     *
     * @return the structurer
     */
    public IStructurer getStructurer() {
        return new StructurerImpl();
    }
}
