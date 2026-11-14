/*
 * IDataFlowTransformer.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.dataflow;

import com.github.toolarium.java.decompiler.model.controlflow.IControlFlowGraph;
import com.github.toolarium.java.decompiler.model.dataflow.IDataFlowBlock;
import java.util.List;


/**
 * Transforms a control flow graph into static single assignment data flow form.
 *
 * @author patrick
 */
public interface IDataFlowTransformer {

    /**
     * Transform the given CFG into data flow form. Inserts merge functions at join points
     * and renames variables so each variable is assigned exactly once.
     *
     * @param cfg       the control flow graph
     * @param maxLocals the number of local variable slots in the method
     * @return the data flow blocks with merge functions and versioned variables
     */
    List<IDataFlowBlock> transformToDataFlow(IControlFlowGraph cfg, int maxLocals);
}
