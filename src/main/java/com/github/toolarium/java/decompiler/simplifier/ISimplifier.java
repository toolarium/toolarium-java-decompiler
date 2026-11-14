/*
 * ISimplifier.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.simplifier;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;


/**
 * Simplifies AST nodes by applying expression and statement optimization passes.
 * This includes constant folding, boolean simplification, redundant cast removal,
 * and other transformations that improve readability.
 *
 * @author patrick
 */
public interface ISimplifier {

    /**
     * Simplify a block of statements.
     *
     * @param block the block to simplify
     * @return the simplified block
     */
    IBlockStatement simplify(IBlockStatement block);


    /**
     * Simplify a single expression.
     *
     * @param expression the expression to simplify
     * @return the simplified expression
     */
    IExpression simplifyExpression(IExpression expression);
}
