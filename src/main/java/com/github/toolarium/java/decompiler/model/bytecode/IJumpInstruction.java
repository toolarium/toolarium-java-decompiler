/*
 * IJumpInstruction.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode;


/**
 * Represents a branch instruction (goto, if*, jsr).
 *
 * @author patrick
 */
public interface IJumpInstruction extends IInstruction {

    /**
     * Get the target byte offset to jump to.
     *
     * @return the target byte offset
     */
    int getTargetOffset();
}
