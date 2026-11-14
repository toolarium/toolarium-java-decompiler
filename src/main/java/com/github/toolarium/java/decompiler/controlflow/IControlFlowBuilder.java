/*
 * IControlFlowBuilder.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.controlflow;

import com.github.toolarium.java.decompiler.model.controlflow.IControlFlowGraph;
import org.objectweb.asm.tree.MethodNode;


/**
 * Builds a control flow graph from a method's bytecode.
 *
 * @author patrick
 */
public interface IControlFlowBuilder {

    /**
     * Build a control flow graph from an ASM method node.
     *
     * @param methodNode the ASM method node containing bytecode
     * @return the control flow graph
     */
    IControlFlowGraph buildCfg(MethodNode methodNode);
}
