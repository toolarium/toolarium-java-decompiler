/*
 * IExpression.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;

import com.github.toolarium.java.decompiler.model.ast.IAstNode;
import com.github.toolarium.java.decompiler.model.type.IType;


/**
 * Base interface for all AST expression nodes.
 *
 * @author patrick
 */
public interface IExpression extends IAstNode {

    /**
     * Get the result type of this expression.
     *
     * @return the expression type, or null if not yet resolved
     */
    IType getType();
}
