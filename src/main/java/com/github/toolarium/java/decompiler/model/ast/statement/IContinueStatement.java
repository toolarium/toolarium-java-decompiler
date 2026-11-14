/*
 * IContinueStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;


/**
 * Represents a continue statement.
 *
 * @author patrick
 */
public interface IContinueStatement extends IStatement {

    /**
     * Get the target label (null for unlabeled continue).
     *
     * @return the target label, or null
     */
    String getTargetLabel();
}
