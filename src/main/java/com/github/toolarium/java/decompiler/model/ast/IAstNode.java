/*
 * IAstNode.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast;


/**
 * Base interface for all AST (Abstract Syntax Tree) nodes.
 *
 * @author patrick
 */
public interface IAstNode {

    /**
     * Accept a visitor for this node.
     *
     * @param visitor the AST visitor
     */
    void accept(IAstVisitor visitor);
}
