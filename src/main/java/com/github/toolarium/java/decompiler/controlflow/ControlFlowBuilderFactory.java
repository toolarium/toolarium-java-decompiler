/*
 * ControlFlowBuilderFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.controlflow;

import com.github.toolarium.java.decompiler.controlflow.impl.ControlFlowBuilderImpl;


/**
 * Singleton factory for {@link IControlFlowBuilder} instances.
 *
 * @author patrick
 */
public final class ControlFlowBuilderFactory {

    /**
     * Private constructor.
     */
    private ControlFlowBuilderFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ControlFlowBuilderFactory INSTANCE = new ControlFlowBuilderFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static ControlFlowBuilderFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a CFG builder.
     *
     * @return the CFG builder
     */
    public IControlFlowBuilder getCfgBuilder() {
        return new ControlFlowBuilderImpl();
    }
}
