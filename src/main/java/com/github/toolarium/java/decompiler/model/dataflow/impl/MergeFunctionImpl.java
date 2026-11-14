/*
 * MergeFunctionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.dataflow.impl;

import com.github.toolarium.java.decompiler.model.dataflow.IDataFlowVariable;
import com.github.toolarium.java.decompiler.model.dataflow.IMergeFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IMergeFunction}.
 *
 * @author patrick
 */
public class MergeFunctionImpl implements IMergeFunction {
    private final IDataFlowVariable target;
    private final List<IDataFlowVariable> sources;
    private final int variableIndex;


    /**
     * Constructor for MergeFunctionImpl.
     *
     * @param target        the target data flow variable
     * @param sources       the source data flow variables
     * @param variableIndex the original variable index
     */
    public MergeFunctionImpl(IDataFlowVariable target, List<IDataFlowVariable> sources, int variableIndex) {
        this.target = Objects.requireNonNull(target, "Merge target must not be null");
        if (sources != null) {
            this.sources = Collections.unmodifiableList(new ArrayList<>(sources));
        } else {
            this.sources = Collections.emptyList();
        }
        this.variableIndex = variableIndex;
    }


    /**
     * Get the target variable (the result of the merge function).
     *
     * @return the target data flow variable
     */
    @Override
    public IDataFlowVariable getTarget() {
        return target;
    }


    /**
     * Get the source variables from each predecessor block as an unmodifiable list.
     *
     * @return an unmodifiable list of source data flow variables
     */
    @Override
    public List<IDataFlowVariable> getSources() {
        return sources;
    }


    /**
     * Get the original variable index this merge function combines.
     *
     * @return the local variable index
     */
    @Override
    public int getVariableIndex() {
        return variableIndex;
    }


    /**
     * Compute the hash code based on the target, sources, and variable index.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(target, sources, variableIndex);
    }


    /**
     * Check equality based on the target, sources, and variable index.
     *
     * @param obj the object to compare with
     * @return true if the other object is a MergeFunctionImpl with equal target, sources, and variable index
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MergeFunctionImpl other = (MergeFunctionImpl) obj;
        return variableIndex == other.variableIndex
                && Objects.equals(target, other.target)
                && Objects.equals(sources, other.sources);
    }


    /**
     * Return a string representation in merge function notation (e.g., "x_2 = merge(x_0, x_1)").
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(target.getVersionedName()).append(" = merge(");
        for (int i = 0; i < sources.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(sources.get(i).getVersionedName());
        }
        sb.append(')');
        return sb.toString();
    }
}
