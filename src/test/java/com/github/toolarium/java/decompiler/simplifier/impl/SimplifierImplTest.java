/*
 * SimplifierImplTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.simplifier.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.ast.expression.BinaryOperator;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IConstantExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.UnaryOperator;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IIfStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import com.github.toolarium.java.decompiler.simplifier.ISimplifier;
import com.github.toolarium.java.decompiler.simplifier.SimplifierFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link SimplifierImpl}.
 *
 * @author patrick
 */
public class SimplifierImplTest {
    private final ISimplifier simplifier = SimplifierFactory.getInstance().getSimplifier();
    private final ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();
    private final TypeModelFactory typeFactory = TypeModelFactory.getInstance();
    private final IType intType = typeFactory.getPrimitiveType('I');
    private final IType boolType = typeFactory.getPrimitiveType('Z');


    /**
     * Test constant folding: 3 + 4 -> 7.
     */
    @Test
    void testConstantFoldingAdd() {
        IExpression left = exprFactory.getConstantExpression(intType, 3);
        IExpression right = exprFactory.getConstantExpression(intType, 4);
        IExpression binary = exprFactory.getBinaryExpression(intType, left, right, BinaryOperator.ADD);

        IExpression result = simplifier.simplifyExpression(binary);

        assertTrue(result instanceof IConstantExpression);
        assertEquals(7, ((IConstantExpression) result).getValue());
    }


    /**
     * Test constant folding: 10 * 5 -> 50.
     */
    @Test
    void testConstantFoldingMultiply() {
        IExpression left = exprFactory.getConstantExpression(intType, 10);
        IExpression right = exprFactory.getConstantExpression(intType, 5);
        IExpression binary = exprFactory.getBinaryExpression(intType, left, right, BinaryOperator.MUL);

        IExpression result = simplifier.simplifyExpression(binary);

        assertTrue(result instanceof IConstantExpression);
        assertEquals(50, ((IConstantExpression) result).getValue());
    }


    /**
     * Test constant folding: 10 - 3 -> 7.
     */
    @Test
    void testConstantFoldingSub() {
        IExpression left = exprFactory.getConstantExpression(intType, 10);
        IExpression right = exprFactory.getConstantExpression(intType, 3);
        IExpression binary = exprFactory.getBinaryExpression(intType, left, right, BinaryOperator.SUB);

        IExpression result = simplifier.simplifyExpression(binary);

        assertTrue(result instanceof IConstantExpression);
        assertEquals(7, ((IConstantExpression) result).getValue());
    }


    /**
     * Test boolean constant folding: true && false -> false.
     */
    @Test
    void testBooleanFolding() {
        IExpression left = exprFactory.getConstantExpression(boolType, Boolean.TRUE);
        IExpression right = exprFactory.getConstantExpression(boolType, Boolean.FALSE);
        IExpression binary = exprFactory.getBinaryExpression(boolType, left, right, BinaryOperator.LOGICAL_AND);

        IExpression result = simplifier.simplifyExpression(binary);

        assertTrue(result instanceof IConstantExpression);
        assertEquals(Boolean.FALSE, ((IConstantExpression) result).getValue());
    }


    /**
     * Test boolean identity: x && true -> x.
     */
    @Test
    void testBooleanIdentityAndTrue() {
        IExpression variable = exprFactory.getVariableExpression(boolType, "flag", 1);
        IExpression trueConst = exprFactory.getConstantExpression(boolType, Boolean.TRUE);
        IExpression binary = exprFactory.getBinaryExpression(boolType, variable, trueConst,
                BinaryOperator.LOGICAL_AND);

        IExpression result = simplifier.simplifyExpression(binary);

        assertEquals(variable, result);
    }


    /**
     * Test boolean identity: x || false -> x.
     */
    @Test
    void testBooleanIdentityOrFalse() {
        IExpression variable = exprFactory.getVariableExpression(boolType, "flag", 1);
        IExpression falseConst = exprFactory.getConstantExpression(boolType, Boolean.FALSE);
        IExpression binary = exprFactory.getBinaryExpression(boolType, variable, falseConst,
                BinaryOperator.LOGICAL_OR);

        IExpression result = simplifier.simplifyExpression(binary);

        assertEquals(variable, result);
    }


    /**
     * Test arithmetic identity: x + 0 -> x.
     */
    @Test
    void testArithmeticIdentityAddZero() {
        IExpression variable = exprFactory.getVariableExpression(intType, "count", 1);
        IExpression zero = exprFactory.getConstantExpression(intType, 0);
        IExpression binary = exprFactory.getBinaryExpression(intType, variable, zero, BinaryOperator.ADD);

        IExpression result = simplifier.simplifyExpression(binary);

        assertEquals(variable, result);
    }


    /**
     * Test arithmetic identity: x * 1 -> x.
     */
    @Test
    void testArithmeticIdentityMulOne() {
        IExpression variable = exprFactory.getVariableExpression(intType, "count", 1);
        IExpression one = exprFactory.getConstantExpression(intType, 1);
        IExpression binary = exprFactory.getBinaryExpression(intType, variable, one, BinaryOperator.MUL);

        IExpression result = simplifier.simplifyExpression(binary);

        assertEquals(variable, result);
    }


    /**
     * Test arithmetic identity: x * 0 -> 0.
     */
    @Test
    void testArithmeticMulZero() {
        IExpression variable = exprFactory.getVariableExpression(intType, "count", 1);
        IExpression zero = exprFactory.getConstantExpression(intType, 0);
        IExpression binary = exprFactory.getBinaryExpression(intType, variable, zero, BinaryOperator.MUL);

        IExpression result = simplifier.simplifyExpression(binary);

        assertTrue(result instanceof IConstantExpression);
        assertEquals(0, ((IConstantExpression) result).getValue());
    }


    /**
     * Test double negation: !!x -> x.
     */
    @Test
    void testDoubleNegation() {
        IExpression variable = exprFactory.getVariableExpression(boolType, "flag", 1);
        IExpression notVar = exprFactory.getUnaryExpression(boolType, variable, UnaryOperator.LOGICAL_NOT);
        IExpression notNotVar = exprFactory.getUnaryExpression(boolType, notVar, UnaryOperator.LOGICAL_NOT);

        IExpression result = simplifier.simplifyExpression(notNotVar);

        assertEquals(variable, result);
    }


    /**
     * Test negation of constant: !true -> false.
     */
    @Test
    void testNotTrue() {
        IExpression trueConst = exprFactory.getConstantExpression(boolType, Boolean.TRUE);
        IExpression notTrue = exprFactory.getUnaryExpression(boolType, trueConst, UnaryOperator.LOGICAL_NOT);

        IExpression result = simplifier.simplifyExpression(notTrue);

        assertTrue(result instanceof IConstantExpression);
        assertEquals(Boolean.FALSE, ((IConstantExpression) result).getValue());
    }


    /**
     * Test numeric negation: -(5) -> -5.
     */
    @Test
    void testNegateConstant() {
        IExpression five = exprFactory.getConstantExpression(intType, 5);
        IExpression negFive = exprFactory.getUnaryExpression(intType, five, UnaryOperator.NEG);

        IExpression result = simplifier.simplifyExpression(negFive);

        assertTrue(result instanceof IConstantExpression);
        assertEquals(-5, ((IConstantExpression) result).getValue());
    }


    /**
     * Test string concatenation folding: "hello" + " world" -> "hello world".
     */
    @Test
    void testStringConcatFolding() {
        IType stringType = typeFactory.getObjectType("java/lang/String");
        IExpression left = exprFactory.getConstantExpression(stringType, "hello");
        IExpression right = exprFactory.getConstantExpression(stringType, " world");
        IExpression binary = exprFactory.getBinaryExpression(stringType, left, right, BinaryOperator.ADD);

        IExpression result = simplifier.simplifyExpression(binary);

        assertTrue(result instanceof IConstantExpression);
        assertEquals("hello world", ((IConstantExpression) result).getValue());
    }


    /**
     * Test if(true) elimination: if(true) {x} -> x.
     */
    @Test
    void testIfTrueElimination() {
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        IExpression trueConst = exprFactory.getConstantExpression(boolType, Boolean.TRUE);
        IStatement thenStmt = stmtFactory.getReturnStatement(null,
                exprFactory.getConstantExpression(intType, 1));
        IStatement ifStmt = stmtFactory.getIfStatement(null, trueConst,
                stmtFactory.getBlockStatement(null, Collections.singletonList(thenStmt)), null);

        IBlockStatement block = stmtFactory.getBlockStatement(null, Collections.singletonList(ifStmt));
        IBlockStatement result = simplifier.simplify(block);

        assertNotNull(result);
        // The if should be eliminated, leaving the then branch content
        assertFalse(result.getStatements().isEmpty());
        // The result should not contain an IIfStatement anymore
        boolean hasIf = false;
        for (IStatement stmt : result.getStatements()) {
            if (stmt instanceof IIfStatement) {
                hasIf = true;
            }
        }
        assertFalse(hasIf, "if(true) should be eliminated");
    }


    /**
     * Test while(false) elimination.
     */
    @Test
    void testWhileFalseElimination() {
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        IExpression falseConst = exprFactory.getConstantExpression(boolType, Boolean.FALSE);
        IStatement body = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getBreakStatement(null, null)));
        IStatement whileStmt = stmtFactory.getWhileStatement(null, falseConst, body);

        IBlockStatement block = stmtFactory.getBlockStatement(null, Collections.singletonList(whileStmt));
        IBlockStatement result = simplifier.simplify(block);

        // while(false) should be eliminated (empty block removed)
        assertTrue(result.getStatements().isEmpty());
    }


    /**
     * Test empty block removal.
     */
    @Test
    void testEmptyBlockRemoval() {
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        IStatement emptyBlock = stmtFactory.getBlockStatement(null, new ArrayList<>());
        IStatement retStmt = stmtFactory.getReturnStatement(null, null);

        List<IStatement> stmts = new ArrayList<>();
        stmts.add(emptyBlock);
        stmts.add(retStmt);
        IBlockStatement block = stmtFactory.getBlockStatement(null, stmts);

        IBlockStatement result = simplifier.simplify(block);

        assertEquals(1, result.getStatements().size());
    }


    /**
     * Test comparison folding: 5 == 5 -> true.
     */
    @Test
    void testComparisonFolding() {
        IExpression five1 = exprFactory.getConstantExpression(intType, 5);
        IExpression five2 = exprFactory.getConstantExpression(intType, 5);
        IExpression eq = exprFactory.getBinaryExpression(boolType, five1, five2, BinaryOperator.EQ);

        IExpression result = simplifier.simplifyExpression(eq);

        assertTrue(result instanceof IConstantExpression);
        assertEquals(Boolean.TRUE, ((IConstantExpression) result).getValue());
    }


    /**
     * Test that deeply nested statements do not cause a stack overflow.
     * The simplifier has a MAX_SIMPLIFICATION_DEPTH guard at 500.
     */
    @Test
    void testSimplifyStatementsDeeplyNestedDoesNotCrash() {
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        IExpression trueConst = exprFactory.getConstantExpression(boolType, Boolean.TRUE);

        IStatement inner = stmtFactory.getReturnStatement(null,
                exprFactory.getConstantExpression(intType, 0));
        for (int i = 0; i < 600; i++) {
            IStatement ifStmt = stmtFactory.getIfStatement(null, trueConst,
                    stmtFactory.getBlockStatement(null, Collections.singletonList(inner)), null);
            inner = ifStmt;
        }

        IBlockStatement block = stmtFactory.getBlockStatement(null, Collections.singletonList(inner));
        IBlockStatement result = simplifier.simplify(block);

        assertNotNull(result);
    }


    /**
     * Test that simplifying an empty block returns an empty block.
     */
    @Test
    void testSimplifyEmptyBlock() {
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        IBlockStatement block = stmtFactory.getBlockStatement(null, new ArrayList<>());
        IBlockStatement result = simplifier.simplify(block);

        assertNotNull(result);
        assertTrue(result.getStatements().isEmpty());
    }


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        SimplifierFactory factory1 = SimplifierFactory.getInstance();
        SimplifierFactory factory2 = SimplifierFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }
}
