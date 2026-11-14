/*
 * VariableDeclarationStatementImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IVariableDeclarationStatement;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.Objects;


/**
 * Implementation of {@link IVariableDeclarationStatement}.
 *
 * @author patrick
 */
public class VariableDeclarationStatementImpl implements IVariableDeclarationStatement {
    private final String label;
    private final IType variableType;
    private final String variableName;
    private final IExpression initializer;
    private final boolean isFinal;


    /**
     * Constructor for VariableDeclarationStatementImpl.
     *
     * @param label        the optional label, or null
     * @param variableType the variable type
     * @param variableName the variable name
     * @param initializer  the initializer expression, or null if uninitialized
     * @param isFinal      true if the variable is declared final
     */
    public VariableDeclarationStatementImpl(String label, IType variableType, String variableName,
                                            IExpression initializer, boolean isFinal) {
        this.label = label;
        this.variableType = variableType;
        this.variableName = variableName;
        this.initializer = initializer;
        this.isFinal = isFinal;
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
     * Get the variable type.
     *
     * @return the type
     */
    @Override
    public IType getVariableType() {
        return variableType;
    }


    /**
     * Get the variable name.
     *
     * @return the name
     */
    @Override
    public String getVariableName() {
        return variableName;
    }


    /**
     * Get the initializer expression.
     *
     * @return the initializer, or null if uninitialized
     */
    @Override
    public IExpression getInitializer() {
        return initializer;
    }


    /**
     * Check whether this variable is declared final.
     *
     * @return true if final
     */
    @Override
    public boolean isFinal() {
        return isFinal;
    }


    /**
     * Accept a visitor for this AST node.
     *
     * @param visitor the AST visitor
     */
    @Override
    public void accept(IAstVisitor visitor) {
        visitor.visitVariableDeclarationStatement(this);
    }


    /**
     * Return a hash code value for this VariableDeclarationStatementImpl.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, variableType, variableName, initializer, isFinal);
    }


    /**
     * Check whether this VariableDeclarationStatementImpl is equal to another object.
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
        VariableDeclarationStatementImpl other = (VariableDeclarationStatementImpl) obj;
        return isFinal == other.isFinal
                && Objects.equals(label, other.label)
                && Objects.equals(variableType, other.variableType)
                && Objects.equals(variableName, other.variableName)
                && Objects.equals(initializer, other.initializer);
    }


    /**
     * Return a string representation of this VariableDeclarationStatementImpl.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "VariableDeclarationStatement[label=" + label + ", variableType=" + variableType
                + ", variableName=" + variableName + ", initializer=" + initializer
                + ", isFinal=" + isFinal + "]";
    }
}
