/*
 * IEdge.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.controlflow;


/**
 * Represents an edge in the control flow graph.
 *
 * @author patrick
 */
public interface IEdge {

    /**
     * Get the source basic block.
     *
     * @return the source block
     */
    IBasicBlock getSource();


    /**
     * Get the target basic block.
     *
     * @return the target block
     */
    IBasicBlock getTarget();


    /**
     * Get the edge type.
     *
     * @return the edge type
     */
    EdgeType getType();


    /**
     * Get the label for this edge (e.g., switch case value, exception type).
     * Returns null if not applicable.
     *
     * @return the edge label, or null
     */
    String getLabel();
}
