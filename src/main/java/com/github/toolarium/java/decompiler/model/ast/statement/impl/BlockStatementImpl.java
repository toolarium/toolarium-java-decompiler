/*
 * BlockStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IBlockStatement}.
 *
 * @author patrick
 */
public class BlockStatementImpl implements IBlockStatement {
    private final String label;
    private final List<IStatement> statements;


    /**
     * Constructor for BlockStatementImpl.
     *
     * @param label      the optional label, or null
     * @param statements the list of statements in this block
     */
    public BlockStatementImpl(String label, List<IStatement> statements) {
        this.label = label;
        this.statements = Collections.unmodifiableList(new ArrayList<>(statements));
    }


    /**
     * Get an optional label for this statement.
     *
     * @return the label, or null if unlabeled
     */
    @Override
    public String getLabel() {
        return label;
    }


    /**
     * Get the statements in this block.
     *
     * @return an unmodifiable list of statements
     */
    @Override
    public List<IStatement> getStatements() {
        return statements;
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitBlockStatement(this);
    }


    /**
     * Return a hash code value for this BlockStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, statements);
    }


    /**
     * Check whether this BlockStatementImpl is equal to another object.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BlockStatementImpl other = (BlockStatementImpl) obj;
        return Objects.equals(label, other.label)
                && Objects.equals(statements, other.statements);
    }


    /**
     * Return a string representation of this BlockStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "BlockStatement[label=" + label + ", statements=" + statements + "]";
    }
}
