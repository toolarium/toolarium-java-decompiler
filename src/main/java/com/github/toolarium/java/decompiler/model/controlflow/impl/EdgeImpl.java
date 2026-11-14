/*
 * EdgeImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.controlflow.impl;

import com.github.toolarium.java.decompiler.model.controlflow.EdgeType;
import com.github.toolarium.java.decompiler.model.controlflow.IBasicBlock;
import com.github.toolarium.java.decompiler.model.controlflow.IEdge;
import java.util.Objects;


/**
 * Implementation of {@link IEdge}.
 *
 * @author patrick
 */
public class EdgeImpl implements IEdge {
    private final IBasicBlock source;
    private final IBasicBlock target;
    private final EdgeType type;
    private final String label;


    /**
     * Constructor for EdgeImpl.
     *
     * @param source the source block
     * @param target the target block
     * @param type   the edge type
     * @param label  the edge label (may be null)
     */
    public EdgeImpl(IBasicBlock source, IBasicBlock target, EdgeType type, String label) {
        this.source = Objects.requireNonNull(source, "Edge source must not be null");
        this.target = Objects.requireNonNull(target, "Edge target must not be null");
        this.type = Objects.requireNonNull(type, "Edge type must not be null");
        this.label = label;
    }


    /**
     * Get the source basic block of this edge.
     *
     * @return the source block
     */
    @Override
    public IBasicBlock getSource() {
        return source;
    }


    /**
     * Get the target basic block of this edge.
     *
     * @return the target block
     */
    @Override
    public IBasicBlock getTarget() {
        return target;
    }


    /**
     * Get the edge type (e.g., FALL_THROUGH, JUMP, EXCEPTION).
     *
     * @return the edge type
     */
    @Override
    public EdgeType getType() {
        return type;
    }


    /**
     * Get the label for this edge (e.g., switch case value, exception type).
     *
     * @return the edge label, or null if not applicable
     */
    @Override
    public String getLabel() {
        return label;
    }


    /**
     * Compute the hash code based on source id, target id, type, and label.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(source.getId(), target.getId(), type, label);
    }


    /**
     * Check equality based on source id, target id, type, and label.
     *
     * @param obj the object to compare with
     * @return true if the other object is an EdgeImpl with equal source, target, type, and label
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EdgeImpl other = (EdgeImpl) obj;
        return source.getId() == other.source.getId()
                && target.getId() == other.target.getId()
                && type == other.type
                && Objects.equals(label, other.label);
    }


    /**
     * Return a string representation showing source, target, and type.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "Edge[" + source.getId() + " -> " + target.getId() + ", " + type + "]";
    }
}
