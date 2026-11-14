/*
 * SwitchExpressionModelTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ISwitchExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ISwitchExpressionCase;
import com.github.toolarium.java.decompiler.model.ast.expression.IVariableExpression;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Tests for switch expression and switch expression case model classes.
 *
 * @author patrick
 */
class SwitchExpressionModelTest {
    private static final String SELECTOR_VAR = "mode";
    private static final String RESULT_ONE = "one";
    private static final String RESULT_TWO = "two";
    private static final String RESULT_DEFAULT = "unknown";
    private static final String STRING_TYPE_NAME = "java/lang/String";

    private final ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();
    private final TypeModelFactory typeFactory = TypeModelFactory.getInstance();


    /**
     * Test switch expression case creation and properties.
     */
    @Test
    void testSwitchExpressionCase() {
        IType intType = typeFactory.getPrimitiveType('I');
        IType stringType = typeFactory.getObjectType(STRING_TYPE_NAME);
        IExpression caseValue = exprFactory.getConstantExpression(intType, 1);
        IExpression resultExpr = exprFactory.getConstantExpression(stringType, RESULT_ONE);

        List<IExpression> values = new ArrayList<>();
        values.add(caseValue);
        ISwitchExpressionCase switchCase = exprFactory.getSwitchExpressionCase(values, false, resultExpr);

        assertNotNull(switchCase);
        assertEquals(1, switchCase.getValues().size());
        assertFalse(switchCase.isDefault());
        assertEquals(resultExpr, switchCase.getResultExpression());
    }


    /**
     * Test switch expression case default creation.
     */
    @Test
    void testSwitchExpressionCaseDefault() {
        IType stringType = typeFactory.getObjectType(STRING_TYPE_NAME);
        IExpression resultExpr = exprFactory.getConstantExpression(stringType, RESULT_DEFAULT);

        ISwitchExpressionCase defaultCase = exprFactory.getSwitchExpressionCase(
                Collections.emptyList(), true, resultExpr);

        assertNotNull(defaultCase);
        assertTrue(defaultCase.isDefault());
        assertTrue(defaultCase.getValues().isEmpty());
        assertEquals(resultExpr, defaultCase.getResultExpression());
    }


    /**
     * Test switch expression creation and properties.
     */
    @Test
    void testSwitchExpression() {
        IType intType = typeFactory.getPrimitiveType('I');
        IType stringType = typeFactory.getObjectType(STRING_TYPE_NAME);
        IVariableExpression selector = exprFactory.getVariableExpression(intType, SELECTOR_VAR, 1);

        List<ISwitchExpressionCase> cases = buildTestCases(intType, stringType);
        ISwitchExpression switchExpr = exprFactory.getSwitchExpression(stringType, selector, cases);

        assertNotNull(switchExpr);
        assertEquals(selector, switchExpr.getSelector());
        assertEquals(3, switchExpr.getCases().size());
        assertEquals(stringType, switchExpr.getType());
    }


    /**
     * Test switch expression equality.
     */
    @Test
    void testSwitchExpressionEquality() {
        IType intType = typeFactory.getPrimitiveType('I');
        IType stringType = typeFactory.getObjectType(STRING_TYPE_NAME);
        IVariableExpression selector = exprFactory.getVariableExpression(intType, SELECTOR_VAR, 1);

        List<ISwitchExpressionCase> cases = buildTestCases(intType, stringType);
        ISwitchExpression first = exprFactory.getSwitchExpression(stringType, selector, cases);
        ISwitchExpression second = exprFactory.getSwitchExpression(stringType, selector, cases);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }


    /**
     * Test switch expression case equality.
     */
    @Test
    void testSwitchExpressionCaseEquality() {
        IType intType = typeFactory.getPrimitiveType('I');
        IType stringType = typeFactory.getObjectType(STRING_TYPE_NAME);
        IExpression caseValue = exprFactory.getConstantExpression(intType, 1);
        IExpression resultExpr = exprFactory.getConstantExpression(stringType, RESULT_ONE);

        List<IExpression> values = new ArrayList<>();
        values.add(caseValue);
        ISwitchExpressionCase first = exprFactory.getSwitchExpressionCase(values, false, resultExpr);
        ISwitchExpressionCase second = exprFactory.getSwitchExpressionCase(values, false, resultExpr);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }


    /**
     * Test switch expression toString output.
     */
    @Test
    void testSwitchExpressionToString() {
        IType intType = typeFactory.getPrimitiveType('I');
        IType stringType = typeFactory.getObjectType(STRING_TYPE_NAME);
        IVariableExpression selector = exprFactory.getVariableExpression(intType, SELECTOR_VAR, 1);

        List<ISwitchExpressionCase> cases = buildTestCases(intType, stringType);
        ISwitchExpression switchExpr = exprFactory.getSwitchExpression(stringType, selector, cases);

        String str = switchExpr.toString();
        assertNotNull(str);
        assertTrue(str.contains("switch"));
        assertTrue(str.contains(SELECTOR_VAR));
    }


    /**
     * Test switch expression case toString output.
     */
    @Test
    void testSwitchExpressionCaseToString() {
        IType intType = typeFactory.getPrimitiveType('I');
        IType stringType = typeFactory.getObjectType(STRING_TYPE_NAME);
        IExpression caseValue = exprFactory.getConstantExpression(intType, 1);
        IExpression resultExpr = exprFactory.getConstantExpression(stringType, RESULT_ONE);

        List<IExpression> values = new ArrayList<>();
        values.add(caseValue);
        ISwitchExpressionCase switchCase = exprFactory.getSwitchExpressionCase(values, false, resultExpr);

        String str = switchCase.toString();
        assertNotNull(str);
        assertTrue(str.contains("case"));
        assertTrue(str.contains("->"));
    }


    /**
     * Build test cases for switch expression tests.
     *
     * @param intType    the integer type
     * @param stringType the string type
     * @return the list of switch expression cases
     */
    private List<ISwitchExpressionCase> buildTestCases(IType intType, IType stringType) {
        List<ISwitchExpressionCase> cases = new ArrayList<>();

        List<IExpression> values1 = new ArrayList<>();
        values1.add(exprFactory.getConstantExpression(intType, 1));
        cases.add(exprFactory.getSwitchExpressionCase(
                values1, false, exprFactory.getConstantExpression(stringType, RESULT_ONE)));

        List<IExpression> values2 = new ArrayList<>();
        values2.add(exprFactory.getConstantExpression(intType, 2));
        cases.add(exprFactory.getSwitchExpressionCase(
                values2, false, exprFactory.getConstantExpression(stringType, RESULT_TWO)));

        cases.add(exprFactory.getSwitchExpressionCase(
                Collections.emptyList(), true,
                exprFactory.getConstantExpression(stringType, RESULT_DEFAULT)));

        return cases;
    }
}
