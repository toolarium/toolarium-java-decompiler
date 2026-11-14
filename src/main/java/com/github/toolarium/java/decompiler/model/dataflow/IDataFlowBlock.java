/*
 * IDataFlowBlock.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.dataflow;

import com.github.toolarium.java.decompiler.model.controlflow.IBasicBlock;
import java.util.List;


/**
 * A basic block augmented with data flow merge functions for static single assignment form.
 *
 * @author patrick
 */
public interface IDataFlowBlock {

    /**
     * Get the underlying control flow graph basic block.
     *
     * @return the CFG basic block
     */
    IBasicBlock getCfgBlock();


    /**
     * Get the merge functions at the start of this block.
     *
     * @return an unmodifiable list of merge functions
     */
    List<IMergeFunction> getMergeFunctions();


    /**
     * Get the data flow variable definitions in this block (variables written to).
     *
     * @return an unmodifiable list of defined data flow variables
     */
    List<IDataFlowVariable> getDefinitions();


    /**
     * Get the data flow variable uses in this block (variables read from).
     *
     * @return an unmodifiable list of used data flow variables
     */
    List<IDataFlowVariable> getUses();
}
