/*
 * ControlFlowModelFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.controlflow;

import com.github.toolarium.java.decompiler.model.controlflow.impl.BasicBlockImpl;
import com.github.toolarium.java.decompiler.model.controlflow.impl.ControlFlowGraphImpl;
import com.github.toolarium.java.decompiler.model.controlflow.impl.EdgeImpl;
import java.util.List;


/**
 * Singleton factory for CFG model objects.
 *
 * @author patrick
 */
public final class ControlFlowModelFactory {

    /**
     * Private constructor.
     */
    private ControlFlowModelFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ControlFlowModelFactory INSTANCE = new ControlFlowModelFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static ControlFlowModelFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a new mutable basic block.
     *
     * @param id the unique block id
     * @return the basic block
     */
    public BasicBlockImpl getBasicBlock(int id) {
        return new BasicBlockImpl(id);
    }


    /**
     * Get an edge between two blocks.
     *
     * @param source the source block
     * @param target the target block
     * @param type   the edge type
     * @param label  the edge label (may be null)
     * @return the edge
     */
    public IEdge getEdge(IBasicBlock source, IBasicBlock target, EdgeType type, String label) {
        return new EdgeImpl(source, target, type, label);
    }


    /**
     * Get a control flow graph.
     *
     * @param entryBlock the entry block
     * @param blocks     all basic blocks
     * @param edges      all edges
     * @return the control flow graph
     */
    public IControlFlowGraph getControlFlowGraph(IBasicBlock entryBlock,
                                                  List<IBasicBlock> blocks, List<IEdge> edges) {
        return new ControlFlowGraphImpl(entryBlock, blocks, edges);
    }
}
