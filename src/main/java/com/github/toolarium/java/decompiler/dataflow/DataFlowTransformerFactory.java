/*
 * DataFlowTransformerFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.dataflow;

import com.github.toolarium.java.decompiler.dataflow.impl.DataFlowTransformerImpl;


/**
 * Singleton factory for {@link IDataFlowTransformer} instances.
 *
 * @author patrick
 */
public final class DataFlowTransformerFactory {

    /**
     * Private constructor.
     */
    private DataFlowTransformerFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final DataFlowTransformerFactory INSTANCE = new DataFlowTransformerFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static DataFlowTransformerFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a data flow transformer.
     *
     * @return the data flow transformer
     */
    public IDataFlowTransformer getDataFlowTransformer() {
        return new DataFlowTransformerImpl();
    }
}
