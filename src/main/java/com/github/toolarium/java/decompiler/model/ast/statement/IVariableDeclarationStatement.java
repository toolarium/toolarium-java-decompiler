/*
 * IVariableDeclarationStatement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.type.IType;


/**
 * Represents a local variable declaration (e.g., int x = 5).
 *
 * @author patrick
 */
public interface IVariableDeclarationStatement extends IStatement {

    /**
     * Get the variable type.
     *
     * @return the type
     */
    IType getVariableType();


    /**
     * Get the variable name.
     *
     * @return the name
     */
    String getVariableName();


    /**
     * Get the initializer expression (null if uninitialized).
     *
     * @return the initializer, or null
     */
    IExpression getInitializer();


    /**
     * Check whether this variable is declared final.
     *
     * @return true if final
     */
    boolean isFinal();
}
