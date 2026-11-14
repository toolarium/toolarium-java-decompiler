/*
 * UnaryOperator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;


/**
 * Unary operators for expressions.
 *
 * @author patrick
 */
public enum UnaryOperator {

    /** Negation (-). */
    NEG("-"),

    /** Bitwise complement (~). */
    NOT("~"),

    /** Logical not (!). */
    LOGICAL_NOT("!"),

    /** Pre-increment (++x). */
    PRE_INC("++"),

    /** Pre-decrement (--x). */
    PRE_DEC("--"),

    /** Post-increment (x++). */
    POST_INC("++"),
    
    /** Post-decrement (x--). */
    POST_DEC("--");

    private final String symbol;


    /**
     * Constructor for UnaryOperator.
     *
     * @param symbol the operator symbol
     */
    UnaryOperator(String symbol) {
        this.symbol = symbol;
    }


    /**
     * Get the operator symbol.
     *
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }
}
