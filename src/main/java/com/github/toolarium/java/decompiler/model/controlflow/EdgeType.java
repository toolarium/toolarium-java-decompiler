/*
 * EdgeType.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.controlflow;


/**
 * The type of a control flow graph edge.
 *
 * @author patrick
 */
public enum EdgeType {

    /** A normal fall-through edge to the next sequential block. */
    FALL_THROUGH,

    /** A conditional or unconditional jump edge. */
    JUMP,

    /** An exception handler edge. */
    EXCEPTION,

    /** A switch case edge. */
    SWITCH_CASE,

    /** A switch default edge. */
    SWITCH_DEFAULT
}
