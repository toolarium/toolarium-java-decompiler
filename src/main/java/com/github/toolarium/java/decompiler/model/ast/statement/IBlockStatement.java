/*
 * IBlockStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import java.util.List;


/**
 * Represents a block of statements enclosed in braces.
 *
 * @author patrick
 */
public interface IBlockStatement extends IStatement {

    /**
     * Get the statements in this block.
     *
     * @return an unmodifiable list of statements
     */
    List<IStatement> getStatements();
}
