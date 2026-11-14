/*
 * StatementModelFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IBreakStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ICatchClause;
import com.github.toolarium.java.decompiler.model.ast.statement.IContinueStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IDoWhileStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IExpressionStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IForEachStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IForStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IIfStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IReturnStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchCase;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ISynchronizedStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IThrowStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ITryCatchStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IVariableDeclarationStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IWhileStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link StatementModelFactory} and its model implementations.
 *
 * @author patrick
 */
public class StatementModelFactoryTest {
    private static final String EX_VAR = "ex";
    private static final String COUNT_VAR = "count";
    private final StatementModelFactory factory = StatementModelFactory.getInstance();
    private final ExpressionModelFactory expressionFactory = ExpressionModelFactory.getInstance();
    private final TypeModelFactory typeFactory = TypeModelFactory.getInstance();
    private final IType intType = typeFactory.getPrimitiveType('I');
    private final IType boolType = typeFactory.getPrimitiveType('Z');
    private final IType stringType = typeFactory.getObjectType("java/lang/String");
    private final IExpression trueExpr = expressionFactory.getConstantExpression(boolType, true);
    private final IExpression intExpr = expressionFactory.getConstantExpression(intType, 42);
    private final IStatement returnStmt = factory.getReturnStatement(null, null);


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        StatementModelFactory factory1 = StatementModelFactory.getInstance();
        StatementModelFactory factory2 = StatementModelFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Test block statement creation and getters.
     */
    @Test
    void testBlockStatement() {
        IBlockStatement block = factory.getBlockStatement(null, Collections.singletonList(returnStmt));

        assertNull(block.getLabel());
        assertEquals(1, block.getStatements().size());
        assertEquals(returnStmt, block.getStatements().get(0));
    }


    /**
     * Test block statement with label.
     */
    @Test
    void testBlockStatementWithLabel() {
        IBlockStatement block = factory.getBlockStatement("outer", Collections.emptyList());

        assertEquals("outer", block.getLabel());
        assertTrue(block.getStatements().isEmpty());
    }


    /**
     * Test block statement equals and hashCode.
     */
    @Test
    void testBlockStatementEqualsHashCode() {
        IBlockStatement block1 = factory.getBlockStatement(null, Collections.singletonList(returnStmt));
        IBlockStatement block2 = factory.getBlockStatement(null, Collections.singletonList(returnStmt));
        IBlockStatement block3 = factory.getBlockStatement(null, Collections.emptyList());

        assertEquals(block1, block2);
        assertEquals(block1.hashCode(), block2.hashCode());
        assertNotEquals(block1, block3);
    }


    /**
     * Test block statement toString.
     */
    @Test
    void testBlockStatementToString() {
        IBlockStatement block = factory.getBlockStatement(null, Collections.emptyList());
        assertNotNull(block.toString());
    }


    /**
     * Test expression statement creation and getters.
     */
    @Test
    void testExpressionStatement() {
        IExpressionStatement stmt = factory.getExpressionStatement(null, intExpr);

        assertNull(stmt.getLabel());
        assertEquals(intExpr, stmt.getExpression());
    }


    /**
     * Test expression statement equals and hashCode.
     */
    @Test
    void testExpressionStatementEqualsHashCode() {
        IExpressionStatement stmt1 = factory.getExpressionStatement(null, intExpr);
        IExpressionStatement stmt2 = factory.getExpressionStatement(null, intExpr);
        IExpressionStatement stmt3 = factory.getExpressionStatement(null, trueExpr);

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test expression statement toString.
     */
    @Test
    void testExpressionStatementToString() {
        IExpressionStatement stmt = factory.getExpressionStatement(null, intExpr);
        assertNotNull(stmt.toString());
    }


    /**
     * Test return statement creation and getters.
     */
    @Test
    void testReturnStatement() {
        IReturnStatement stmt = factory.getReturnStatement(null, intExpr);

        assertNull(stmt.getLabel());
        assertEquals(intExpr, stmt.getValue());
    }


    /**
     * Test void return statement (null value).
     */
    @Test
    void testReturnStatementVoid() {
        IReturnStatement stmt = factory.getReturnStatement(null, null);
        assertNull(stmt.getValue());
    }


    /**
     * Test return statement equals and hashCode.
     */
    @Test
    void testReturnStatementEqualsHashCode() {
        IReturnStatement stmt1 = factory.getReturnStatement(null, intExpr);
        IReturnStatement stmt2 = factory.getReturnStatement(null, intExpr);
        IReturnStatement stmt3 = factory.getReturnStatement(null, null);

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test return statement toString.
     */
    @Test
    void testReturnStatementToString() {
        IReturnStatement stmt = factory.getReturnStatement(null, intExpr);
        assertNotNull(stmt.toString());
    }


    /**
     * Test if statement creation and getters.
     */
    @Test
    void testIfStatement() {
        IIfStatement stmt = factory.getIfStatement(null, trueExpr, returnStmt, null);

        assertNull(stmt.getLabel());
        assertEquals(trueExpr, stmt.getCondition());
        assertEquals(returnStmt, stmt.getThenBranch());
        assertNull(stmt.getElseBranch());
    }


    /**
     * Test if statement with else branch.
     */
    @Test
    void testIfStatementWithElse() {
        IStatement elseStmt = factory.getReturnStatement(null, intExpr);
        IIfStatement stmt = factory.getIfStatement(null, trueExpr, returnStmt, elseStmt);

        assertNotNull(stmt.getElseBranch());
        assertEquals(elseStmt, stmt.getElseBranch());
    }


    /**
     * Test if statement equals and hashCode.
     */
    @Test
    void testIfStatementEqualsHashCode() {
        IIfStatement stmt1 = factory.getIfStatement(null, trueExpr, returnStmt, null);
        IIfStatement stmt2 = factory.getIfStatement(null, trueExpr, returnStmt, null);
        IIfStatement stmt3 = factory.getIfStatement(null, trueExpr, returnStmt, returnStmt);

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test if statement toString.
     */
    @Test
    void testIfStatementToString() {
        IIfStatement stmt = factory.getIfStatement(null, trueExpr, returnStmt, null);
        assertNotNull(stmt.toString());
    }


    /**
     * Test while statement creation and getters.
     */
    @Test
    void testWhileStatement() {
        IWhileStatement stmt = factory.getWhileStatement(null, trueExpr, returnStmt);

        assertNull(stmt.getLabel());
        assertEquals(trueExpr, stmt.getCondition());
        assertEquals(returnStmt, stmt.getBody());
    }


    /**
     * Test while statement equals and hashCode.
     */
    @Test
    void testWhileStatementEqualsHashCode() {
        IWhileStatement stmt1 = factory.getWhileStatement(null, trueExpr, returnStmt);
        IWhileStatement stmt2 = factory.getWhileStatement(null, trueExpr, returnStmt);
        IExpression falseExpr = expressionFactory.getConstantExpression(boolType, false);
        IWhileStatement stmt3 = factory.getWhileStatement(null, falseExpr, returnStmt);

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test while statement toString.
     */
    @Test
    void testWhileStatementToString() {
        IWhileStatement stmt = factory.getWhileStatement(null, trueExpr, returnStmt);
        assertNotNull(stmt.toString());
    }


    /**
     * Test do-while statement creation and getters.
     */
    @Test
    void testDoWhileStatement() {
        IDoWhileStatement stmt = factory.getDoWhileStatement(null, trueExpr, returnStmt);

        assertNull(stmt.getLabel());
        assertEquals(trueExpr, stmt.getCondition());
        assertEquals(returnStmt, stmt.getBody());
    }


    /**
     * Test do-while statement equals and hashCode.
     */
    @Test
    void testDoWhileStatementEqualsHashCode() {
        IDoWhileStatement stmt1 = factory.getDoWhileStatement(null, trueExpr, returnStmt);
        IDoWhileStatement stmt2 = factory.getDoWhileStatement(null, trueExpr, returnStmt);
        IExpression falseExpr = expressionFactory.getConstantExpression(boolType, false);
        IDoWhileStatement stmt3 = factory.getDoWhileStatement(null, falseExpr, returnStmt);

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test do-while statement toString.
     */
    @Test
    void testDoWhileStatementToString() {
        IDoWhileStatement stmt = factory.getDoWhileStatement(null, trueExpr, returnStmt);
        assertNotNull(stmt.toString());
    }


    /**
     * Test for statement creation and getters.
     */
    @Test
    void testForStatement() {
        IStatement init = factory.getVariableDeclarationStatement(null, intType, "i", intExpr, false);
        IExpression condition = expressionFactory.getConstantExpression(boolType, true);
        IExpression update = expressionFactory.getConstantExpression(intType, 1);
        IForStatement stmt = factory.getForStatement(null, init, condition, update, returnStmt);

        assertNull(stmt.getLabel());
        assertEquals(init, stmt.getInit());
        assertEquals(condition, stmt.getCondition());
        assertEquals(update, stmt.getUpdate());
        assertEquals(returnStmt, stmt.getBody());
    }


    /**
     * Test for statement with null optional parts (infinite loop).
     */
    @Test
    void testForStatementInfiniteLoop() {
        IForStatement stmt = factory.getForStatement(null, null, null, null, returnStmt);

        assertNull(stmt.getInit());
        assertNull(stmt.getCondition());
        assertNull(stmt.getUpdate());
    }


    /**
     * Test for statement equals and hashCode.
     */
    @Test
    void testForStatementEqualsHashCode() {
        IForStatement stmt1 = factory.getForStatement(null, null, trueExpr, null, returnStmt);
        IForStatement stmt2 = factory.getForStatement(null, null, trueExpr, null, returnStmt);
        IForStatement stmt3 = factory.getForStatement(null, null, null, null, returnStmt);

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test for statement toString.
     */
    @Test
    void testForStatementToString() {
        IForStatement stmt = factory.getForStatement(null, null, trueExpr, null, returnStmt);
        assertNotNull(stmt.toString());
    }


    /**
     * Test for-each statement creation and getters.
     */
    @Test
    void testForEachStatement() {
        IExpression iterable = expressionFactory.getVariableExpression(stringType, "list", 1);
        IForEachStatement stmt = factory.getForEachStatement(null, stringType, "item", iterable, returnStmt);

        assertNull(stmt.getLabel());
        assertEquals(stringType, stmt.getVariableType());
        assertEquals("item", stmt.getVariableName());
        assertEquals(iterable, stmt.getIterable());
        assertEquals(returnStmt, stmt.getBody());
    }


    /**
     * Test for-each statement equals and hashCode.
     */
    @Test
    void testForEachStatementEqualsHashCode() {
        IExpression iterable = expressionFactory.getVariableExpression(stringType, "list", 1);
        IForEachStatement stmt1 = factory.getForEachStatement(null, stringType, "item", iterable, returnStmt);
        IForEachStatement stmt2 = factory.getForEachStatement(null, stringType, "item", iterable, returnStmt);
        IForEachStatement stmt3 = factory.getForEachStatement(null, stringType, "other", iterable, returnStmt);

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test for-each statement toString.
     */
    @Test
    void testForEachStatementToString() {
        IExpression iterable = expressionFactory.getVariableExpression(stringType, "list", 1);
        IForEachStatement stmt = factory.getForEachStatement(null, stringType, "item", iterable, returnStmt);
        assertNotNull(stmt.toString());
    }


    /**
     * Test switch statement creation and getters.
     */
    @Test
    void testSwitchStatement() {
        IExpression caseValue = expressionFactory.getConstantExpression(intType, 1);
        ISwitchCase switchCase = factory.getSwitchCase(
                Collections.singletonList(caseValue), false, Collections.singletonList(returnStmt));
        ISwitchCase defaultCase = factory.getSwitchCase(
                Collections.emptyList(), true, Collections.singletonList(returnStmt));
        ISwitchStatement stmt = factory.getSwitchStatement(null, intExpr, List.of(switchCase, defaultCase));

        assertNull(stmt.getLabel());
        assertEquals(intExpr, stmt.getSelector());
        assertEquals(2, stmt.getCases().size());
    }


    /**
     * Test switch case creation and getters.
     */
    @Test
    void testSwitchCase() {
        IExpression caseValue = expressionFactory.getConstantExpression(intType, 1);
        ISwitchCase regularCase = factory.getSwitchCase(
                Collections.singletonList(caseValue), false, Collections.singletonList(returnStmt));

        assertFalse(regularCase.isDefault());
        assertEquals(1, regularCase.getValues().size());
        assertEquals(1, regularCase.getStatements().size());
    }


    /**
     * Test switch default case.
     */
    @Test
    void testSwitchDefaultCase() {
        ISwitchCase defaultCase = factory.getSwitchCase(
                Collections.emptyList(), true, Collections.singletonList(returnStmt));

        assertTrue(defaultCase.isDefault());
        assertTrue(defaultCase.getValues().isEmpty());
    }


    /**
     * Test switch statement equals and hashCode.
     */
    @Test
    void testSwitchStatementEqualsHashCode() {
        ISwitchCase switchCase = factory.getSwitchCase(
                Collections.emptyList(), true, Collections.singletonList(returnStmt));
        ISwitchStatement stmt1 = factory.getSwitchStatement(null, intExpr, Collections.singletonList(switchCase));
        ISwitchStatement stmt2 = factory.getSwitchStatement(null, intExpr, Collections.singletonList(switchCase));
        ISwitchStatement stmt3 = factory.getSwitchStatement(null, intExpr, Collections.emptyList());

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test switch case equals and hashCode.
     */
    @Test
    void testSwitchCaseEqualsHashCode() {
        ISwitchCase case1 = factory.getSwitchCase(Collections.emptyList(), true, Collections.singletonList(returnStmt));
        ISwitchCase case2 = factory.getSwitchCase(Collections.emptyList(), true, Collections.singletonList(returnStmt));
        ISwitchCase case3 = factory.getSwitchCase(Collections.emptyList(), false, Collections.singletonList(returnStmt));

        assertEquals(case1, case2);
        assertEquals(case1.hashCode(), case2.hashCode());
        assertNotEquals(case1, case3);
    }


    /**
     * Test switch statement toString.
     */
    @Test
    void testSwitchStatementToString() {
        ISwitchStatement stmt = factory.getSwitchStatement(null, intExpr, Collections.emptyList());
        assertNotNull(stmt.toString());
    }


    /**
     * Test try-catch statement creation and getters.
     */
    @Test
    void testTryCatchStatement() {
        IType exceptionType = typeFactory.getObjectType("java/io/IOException");
        ICatchClause catchClause = factory.getCatchClause(
                Collections.singletonList(exceptionType), EX_VAR, returnStmt);
        ITryCatchStatement stmt = factory.getTryCatchStatement(
                null, returnStmt, Collections.singletonList(catchClause), null);

        assertNull(stmt.getLabel());
        assertEquals(returnStmt, stmt.getTryBody());
        assertEquals(1, stmt.getCatchClauses().size());
        assertNull(stmt.getFinallyBody());
    }


    /**
     * Test try-catch statement with finally.
     */
    @Test
    void testTryCatchStatementWithFinally() {
        IStatement finallyBody = factory.getBlockStatement(null, Collections.emptyList());
        ITryCatchStatement stmt = factory.getTryCatchStatement(
                null, returnStmt, Collections.emptyList(), finallyBody);

        assertNotNull(stmt.getFinallyBody());
        assertEquals(finallyBody, stmt.getFinallyBody());
        assertTrue(stmt.getCatchClauses().isEmpty());
    }


    /**
     * Test catch clause creation and getters.
     */
    @Test
    void testCatchClause() {
        IType exceptionType = typeFactory.getObjectType("java/io/IOException");
        ICatchClause clause = factory.getCatchClause(
                Collections.singletonList(exceptionType), EX_VAR, returnStmt);

        assertEquals(1, clause.getExceptionTypes().size());
        assertEquals(exceptionType, clause.getExceptionTypes().get(0));
        assertEquals(EX_VAR, clause.getVariableName());
        assertEquals(returnStmt, clause.getBody());
    }


    /**
     * Test catch clause with multiple exception types (multi-catch).
     */
    @Test
    void testCatchClauseMultiCatch() {
        IType ioException = typeFactory.getObjectType("java/io/IOException");
        IType sqlException = typeFactory.getObjectType("java/sql/SQLException");
        ICatchClause clause = factory.getCatchClause(
                List.of(ioException, sqlException), EX_VAR, returnStmt);

        assertEquals(2, clause.getExceptionTypes().size());
    }


    /**
     * Test try-catch statement equals and hashCode.
     */
    @Test
    void testTryCatchStatementEqualsHashCode() {
        ITryCatchStatement stmt1 = factory.getTryCatchStatement(
                null, returnStmt, Collections.emptyList(), null);
        ITryCatchStatement stmt2 = factory.getTryCatchStatement(
                null, returnStmt, Collections.emptyList(), null);
        ITryCatchStatement stmt3 = factory.getTryCatchStatement(
                null, returnStmt, Collections.emptyList(), returnStmt);

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test catch clause equals and hashCode.
     */
    @Test
    void testCatchClauseEqualsHashCode() {
        IType exceptionType = typeFactory.getObjectType("java/io/IOException");
        ICatchClause clause1 = factory.getCatchClause(
                Collections.singletonList(exceptionType), EX_VAR, returnStmt);
        ICatchClause clause2 = factory.getCatchClause(
                Collections.singletonList(exceptionType), EX_VAR, returnStmt);
        ICatchClause clause3 = factory.getCatchClause(
                Collections.singletonList(exceptionType), "other", returnStmt);

        assertEquals(clause1, clause2);
        assertEquals(clause1.hashCode(), clause2.hashCode());
        assertNotEquals(clause1, clause3);
    }


    /**
     * Test try-catch statement toString.
     */
    @Test
    void testTryCatchStatementToString() {
        ITryCatchStatement stmt = factory.getTryCatchStatement(
                null, returnStmt, Collections.emptyList(), null);
        assertNotNull(stmt.toString());
    }


    /**
     * Test throw statement creation and getters.
     */
    @Test
    void testThrowStatement() {
        IExpression exception = expressionFactory.getNewExpression(
                typeFactory.getObjectType("java/lang/RuntimeException"),
                typeFactory.getObjectType("java/lang/RuntimeException"),
                Collections.emptyList());
        IThrowStatement stmt = factory.getThrowStatement(null, exception);

        assertNull(stmt.getLabel());
        assertEquals(exception, stmt.getExpression());
    }


    /**
     * Test throw statement equals and hashCode.
     */
    @Test
    void testThrowStatementEqualsHashCode() {
        IThrowStatement stmt1 = factory.getThrowStatement(null, intExpr);
        IThrowStatement stmt2 = factory.getThrowStatement(null, intExpr);
        IThrowStatement stmt3 = factory.getThrowStatement(null, trueExpr);

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test throw statement toString.
     */
    @Test
    void testThrowStatementToString() {
        IThrowStatement stmt = factory.getThrowStatement(null, intExpr);
        assertNotNull(stmt.toString());
    }


    /**
     * Test break statement creation and getters.
     */
    @Test
    void testBreakStatement() {
        IBreakStatement stmt = factory.getBreakStatement(null, null);

        assertNull(stmt.getLabel());
        assertNull(stmt.getTargetLabel());
    }


    /**
     * Test labeled break statement.
     */
    @Test
    void testBreakStatementLabeled() {
        IBreakStatement stmt = factory.getBreakStatement(null, "outer");
        assertEquals("outer", stmt.getTargetLabel());
    }


    /**
     * Test break statement equals and hashCode.
     */
    @Test
    void testBreakStatementEqualsHashCode() {
        IBreakStatement stmt1 = factory.getBreakStatement(null, null);
        IBreakStatement stmt2 = factory.getBreakStatement(null, null);
        IBreakStatement stmt3 = factory.getBreakStatement(null, "outer");

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test break statement toString.
     */
    @Test
    void testBreakStatementToString() {
        IBreakStatement stmt = factory.getBreakStatement(null, null);
        assertNotNull(stmt.toString());
    }


    /**
     * Test continue statement creation and getters.
     */
    @Test
    void testContinueStatement() {
        IContinueStatement stmt = factory.getContinueStatement(null, null);

        assertNull(stmt.getLabel());
        assertNull(stmt.getTargetLabel());
    }


    /**
     * Test labeled continue statement.
     */
    @Test
    void testContinueStatementLabeled() {
        IContinueStatement stmt = factory.getContinueStatement(null, "loop");
        assertEquals("loop", stmt.getTargetLabel());
    }


    /**
     * Test continue statement equals and hashCode.
     */
    @Test
    void testContinueStatementEqualsHashCode() {
        IContinueStatement stmt1 = factory.getContinueStatement(null, null);
        IContinueStatement stmt2 = factory.getContinueStatement(null, null);
        IContinueStatement stmt3 = factory.getContinueStatement(null, "loop");

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test continue statement toString.
     */
    @Test
    void testContinueStatementToString() {
        IContinueStatement stmt = factory.getContinueStatement(null, null);
        assertNotNull(stmt.toString());
    }


    /**
     * Test variable declaration statement creation and getters.
     */
    @Test
    void testVariableDeclarationStatement() {
        IVariableDeclarationStatement stmt = factory.getVariableDeclarationStatement(
                null, intType, COUNT_VAR, intExpr, false);

        assertNull(stmt.getLabel());
        assertEquals(intType, stmt.getVariableType());
        assertEquals(COUNT_VAR, stmt.getVariableName());
        assertEquals(intExpr, stmt.getInitializer());
        assertFalse(stmt.isFinal());
    }


    /**
     * Test variable declaration statement with final modifier.
     */
    @Test
    void testVariableDeclarationStatementFinal() {
        IVariableDeclarationStatement stmt = factory.getVariableDeclarationStatement(
                null, stringType, "name", null, true);

        assertTrue(stmt.isFinal());
        assertNull(stmt.getInitializer());
    }


    /**
     * Test variable declaration statement equals and hashCode.
     */
    @Test
    void testVariableDeclarationStatementEqualsHashCode() {
        IVariableDeclarationStatement stmt1 = factory.getVariableDeclarationStatement(
                null, intType, COUNT_VAR, intExpr, false);
        IVariableDeclarationStatement stmt2 = factory.getVariableDeclarationStatement(
                null, intType, COUNT_VAR, intExpr, false);
        IVariableDeclarationStatement stmt3 = factory.getVariableDeclarationStatement(
                null, intType, "other", intExpr, false);

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test variable declaration statement toString.
     */
    @Test
    void testVariableDeclarationStatementToString() {
        IVariableDeclarationStatement stmt = factory.getVariableDeclarationStatement(
                null, intType, COUNT_VAR, intExpr, false);
        assertTrue(stmt.toString().contains(COUNT_VAR));
    }


    /**
     * Test synchronized statement creation and getters.
     */
    @Test
    void testSynchronizedStatement() {
        IExpression lock = expressionFactory.getVariableExpression(
                typeFactory.getObjectType("java/lang/Object"), "lock", 1);
        ISynchronizedStatement stmt = factory.getSynchronizedStatement(null, lock, returnStmt);

        assertNull(stmt.getLabel());
        assertEquals(lock, stmt.getLock());
        assertEquals(returnStmt, stmt.getBody());
    }


    /**
     * Test synchronized statement equals and hashCode.
     */
    @Test
    void testSynchronizedStatementEqualsHashCode() {
        IExpression lock = expressionFactory.getVariableExpression(
                typeFactory.getObjectType("java/lang/Object"), "lock", 1);
        ISynchronizedStatement stmt1 = factory.getSynchronizedStatement(null, lock, returnStmt);
        ISynchronizedStatement stmt2 = factory.getSynchronizedStatement(null, lock, returnStmt);
        IExpression otherLock = expressionFactory.getVariableExpression(
                typeFactory.getObjectType("java/lang/Object"), "otherLock", 2);
        ISynchronizedStatement stmt3 = factory.getSynchronizedStatement(null, otherLock, returnStmt);

        assertEquals(stmt1, stmt2);
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
        assertNotEquals(stmt1, stmt3);
    }


    /**
     * Test synchronized statement toString.
     */
    @Test
    void testSynchronizedStatementToString() {
        IExpression lock = expressionFactory.getVariableExpression(
                typeFactory.getObjectType("java/lang/Object"), "lock", 1);
        ISynchronizedStatement stmt = factory.getSynchronizedStatement(null, lock, returnStmt);
        assertNotNull(stmt.toString());
    }
}
