/*
 * IStructurer.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.structurer;

import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.controlflow.IControlFlowGraph;


/**
 * Converts a control flow graph into structured AST statements.
 * This is the core of the decompiler: it reconstructs high-level control flow
 * (if/else, loops, switch, try/catch) from the flat CFG representation.
 *
 * @author patrick
 */
public interface IStructurer {

    /**
     * Structure a control flow graph into a block of AST statements.
     *
     * @param cfg the control flow graph to structure
     * @return a block statement containing the structured code
     */
    IBlockStatement structure(IControlFlowGraph cfg);
}
