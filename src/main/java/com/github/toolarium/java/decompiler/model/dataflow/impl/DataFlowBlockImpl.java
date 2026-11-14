/*
 * DataFlowBlockImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.dataflow.impl;

import com.github.toolarium.java.decompiler.model.controlflow.IBasicBlock;
import com.github.toolarium.java.decompiler.model.dataflow.IDataFlowBlock;
import com.github.toolarium.java.decompiler.model.dataflow.IDataFlowVariable;
import com.github.toolarium.java.decompiler.model.dataflow.IMergeFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IDataFlowBlock}.
 *
 * @author patrick
 */
public class DataFlowBlockImpl implements IDataFlowBlock {
    private final IBasicBlock cfgBlock;
    private final List<IMergeFunction> mergeFunctions = new ArrayList<>();
    private final List<IDataFlowVariable> definitions = new ArrayList<>();
    private final List<IDataFlowVariable> uses = new ArrayList<>();


    /**
     * Constructor for DataFlowBlockImpl.
     *
     * @param cfgBlock the underlying CFG basic block
     */
    public DataFlowBlockImpl(IBasicBlock cfgBlock) {
        this.cfgBlock = Objects.requireNonNull(cfgBlock, "CFG block must not be null");
    }


    /**
     * Get the underlying control flow graph basic block.
     *
     * @return the CFG basic block
     */
    @Override
    public IBasicBlock getCfgBlock() {
        return cfgBlock;
    }


    /**
     * Get the merge functions at the start of this block as an unmodifiable list.
     *
     * @return an unmodifiable list of merge functions
     */
    @Override
    public List<IMergeFunction> getMergeFunctions() {
        return Collections.unmodifiableList(mergeFunctions);
    }


    /**
     * Get the data flow variable definitions in this block as an unmodifiable list.
     *
     * @return an unmodifiable list of defined data flow variables
     */
    @Override
    public List<IDataFlowVariable> getDefinitions() {
        return Collections.unmodifiableList(definitions);
    }


    /**
     * Get the data flow variable uses in this block as an unmodifiable list.
     *
     * @return an unmodifiable list of used data flow variables
     */
    @Override
    public List<IDataFlowVariable> getUses() {
        return Collections.unmodifiableList(uses);
    }


    /**
     * Add a merge function to this block.
     *
     * @param mergeFunction the merge function
     */
    public void addMergeFunction(IMergeFunction mergeFunction) {
        mergeFunctions.add(Objects.requireNonNull(mergeFunction));
    }


    /**
     * Add a variable definition to this block.
     *
     * @param variable the defined variable
     */
    public void addDefinition(IDataFlowVariable variable) {
        definitions.add(Objects.requireNonNull(variable));
    }


    /**
     * Add a variable use to this block.
     *
     * @param variable the used variable
     */
    public void addUse(IDataFlowVariable variable) {
        uses.add(Objects.requireNonNull(variable));
    }


    /**
     * Compute the hash code based on the underlying block id.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(cfgBlock.getId());
    }


    /**
     * Check equality based on the underlying block id.
     *
     * @param obj the object to compare with
     * @return true if the other object is a DataFlowBlockImpl with the same block id
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DataFlowBlockImpl other = (DataFlowBlockImpl) obj;
        return cfgBlock.getId() == other.cfgBlock.getId();
    }


    /**
     * Return a string representation including block id and counts of merges, definitions, and uses.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "DataFlowBlock[id=" + cfgBlock.getId()
                + ", merges=" + mergeFunctions.size()
                + ", defs=" + definitions.size()
                + ", uses=" + uses.size() + "]";
    }
}
