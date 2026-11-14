/*
 * IBreakStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;


/**
 * Represents a break statement.
 *
 * @author patrick
 */
public interface IBreakStatement extends IStatement {

    /**
     * Get the target label (null for unlabeled break).
     *
     * @return the target label, or null
     */
    String getTargetLabel();
}
