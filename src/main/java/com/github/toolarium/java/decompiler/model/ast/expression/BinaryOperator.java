/*
 * BinaryOperator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;


/**
 * Binary operators for expressions.
 *
 * @author patrick
 */
public enum BinaryOperator {

    /** Addition (+). */
    ADD("+"),
    
    /** Subtraction (-). */
    SUB("-"),

    /** Multiplication (*). */
    MUL("*"),

    /** Division (/). */
    DIV("/"),

    /** Remainder (%). */
    REM("%"),

    /** Bitwise AND (&amp;). */
    AND("&"),

    /** Bitwise OR (|). */
    OR("|"),

    /** Bitwise XOR (^). */
    XOR("^"),

    /** Left shift (&lt;&lt;). */
    SHL("<<"),

    /** Right shift (&gt;&gt;). */
    SHR(">>"),

    /** Unsigned right shift (&gt;&gt;&gt;). */
    USHR(">>>"),

    /** Equal (==). */
    EQ("=="),

    /** Not equal (!=). */
    NE("!="),

    /** Less than (&lt;). */
    LT("<"),

    /** Greater than (&gt;). */
    GT(">"),

    /** Less than or equal (&lt;=). */
    LE("<="),

    /** Greater than or equal (&gt;=). */
    GE(">="),

    /** Logical AND (&amp;&amp;). */
    LOGICAL_AND("&&"),
    
    /** Logical OR (||). */
    LOGICAL_OR("||");

    private final String symbol;


    /**
     * Constructor for BinaryOperator.
     *
     * @param symbol the operator symbol
     */
    BinaryOperator(String symbol) {
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
