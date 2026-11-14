/*
 * DataFlowModelFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.dataflow;

import com.github.toolarium.java.decompiler.model.controlflow.IBasicBlock;
import com.github.toolarium.java.decompiler.model.dataflow.impl.DataFlowBlockImpl;
import com.github.toolarium.java.decompiler.model.dataflow.impl.DataFlowVariableImpl;
import com.github.toolarium.java.decompiler.model.dataflow.impl.MergeFunctionImpl;
import java.util.List;


/**
 * Singleton factory for data flow model objects.
 *
 * @author patrick
 */
public final class DataFlowModelFactory {

    /**
     * Private constructor.
     */
    private DataFlowModelFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final DataFlowModelFactory INSTANCE = new DataFlowModelFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static DataFlowModelFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a data flow variable.
     *
     * @param index   the local variable index
     * @param version the version number
     * @param name    the variable name (may be null)
     * @return the data flow variable
     */
    public IDataFlowVariable getDataFlowVariable(int index, int version, String name) {
        return new DataFlowVariableImpl(index, version, name);
    }


    /**
     * Get a merge function.
     *
     * @param target        the target data flow variable
     * @param sources       the source data flow variables
     * @param variableIndex the original variable index
     * @return the merge function
     */
    public IMergeFunction getMergeFunction(IDataFlowVariable target, List<IDataFlowVariable> sources, int variableIndex) {
        return new MergeFunctionImpl(target, sources, variableIndex);
    }


    /**
     * Get a data flow block wrapper.
     *
     * @param cfgBlock the underlying CFG block
     * @return the data flow block
     */
    public DataFlowBlockImpl getDataFlowBlock(IBasicBlock cfgBlock) {
        return new DataFlowBlockImpl(cfgBlock);
    }
}
