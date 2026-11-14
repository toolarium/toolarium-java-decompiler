/*
 * IStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.IAstNode;


/**
 * Base interface for all AST statement nodes.
 *
 * @author patrick
 */
public interface IStatement extends IAstNode {

    /**
     * Get an optional label for this statement (used by break/continue).
     *
     * @return the label, or null if unlabeled
     */
    String getLabel();
}
