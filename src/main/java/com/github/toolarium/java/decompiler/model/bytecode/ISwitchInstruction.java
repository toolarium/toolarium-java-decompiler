/*
 * ISwitchInstruction.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode;


/**
 * Represents a switch instruction (tableswitch or lookupswitch).
 *
 * @author patrick
 */
public interface ISwitchInstruction extends IInstruction {

    /**
     * Get the default branch target offset.
     *
     * @return the default target offset
     */
    int getDefaultOffset();


    /**
     * Get the case keys.
     * For tableswitch this is the contiguous range; for lookupswitch these are the match values.
     *
     * @return the case keys
     */
    int[] getKeys();


    /**
     * Get the branch target offsets, one per key.
     *
     * @return the target offsets
     */
    int[] getTargetOffsets();
}
