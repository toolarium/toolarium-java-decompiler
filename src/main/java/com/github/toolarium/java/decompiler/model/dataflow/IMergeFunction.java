/*
 * IMergeFunction.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.dataflow;

import java.util.List;


/**
 * Represents a merge function at a control flow join point in data flow form.
 * A merge function combines multiple definitions of a variable from different predecessors.
 *
 * @author patrick
 */
public interface IMergeFunction {

    /**
     * Get the target variable (the result of the merge function).
     *
     * @return the target data flow variable
     */
    IDataFlowVariable getTarget();


    /**
     * Get the source variables from each predecessor block.
     *
     * @return an unmodifiable list of source data flow variables
     */
    List<IDataFlowVariable> getSources();


    /**
     * Get the original variable index this merge function combines.
     *
     * @return the local variable index
     */
    int getVariableIndex();
}
