/*
 * ExpressionModelFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.ast.expression.BinaryOperator;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IArrayAccessExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IAssignmentExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IBinaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ICastExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IConstantExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IFieldAccessExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IInstanceOfExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ILambdaExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodCallExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodReferenceExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewArrayExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IUnaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IVariableExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.UnaryOperator;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link ExpressionModelFactory} and its model implementations.
 *
 * @author patrick
 */
public class ExpressionModelFactoryTest {
    private static final String MY_VAR = "myVar";
    private static final String MY_FIELD = "myField";
    private static final String MY_METHOD = "myMethod";
    private static final String OWNER = "com/example/MyClass";
    private static final String OBJ_VAR = "obj";
    private static final String OBJECT_TYPE = "java/lang/Object";
    private static final String STRING_TYPE = "java/lang/String";
    private static final String FUNCTION_TYPE = "java/util/function/Function";
    private static final String APPLY_METHOD = "apply";
    private static final String VALUE_OF_METHOD = "valueOf";
    private final ExpressionModelFactory factory = ExpressionModelFactory.getInstance();
    private final TypeModelFactory typeFactory = TypeModelFactory.getInstance();
    private final IType intType = typeFactory.getPrimitiveType('I');
    private final IType boolType = typeFactory.getPrimitiveType('Z');
    private final IType stringType = typeFactory.getObjectType(STRING_TYPE);


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        ExpressionModelFactory factory1 = ExpressionModelFactory.getInstance();
        ExpressionModelFactory factory2 = ExpressionModelFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Test constant expression creation and getters.
     */
    @Test
    void testConstantExpression() {
        IConstantExpression expr = factory.getConstantExpression(intType, 42);

        assertEquals(intType, expr.getType());
        assertEquals(42, expr.getValue());
    }


    /**
     * Test constant expression with null value.
     */
    @Test
    void testConstantExpressionNull() {
        IConstantExpression expr = factory.getConstantExpression(stringType, null);

        assertEquals(stringType, expr.getType());
        assertNull(expr.getValue());
    }


    /**
     * Test constant expression equals and hashCode.
     */
    @Test
    void testConstantExpressionEqualsHashCode() {
        IConstantExpression expr1 = factory.getConstantExpression(intType, 42);
        IConstantExpression expr2 = factory.getConstantExpression(intType, 42);
        IConstantExpression expr3 = factory.getConstantExpression(intType, 99);

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
        assertNotEquals(expr1, null);
        assertEquals(expr1, expr1);
    }


    /**
     * Test constant expression toString.
     */
    @Test
    void testConstantExpressionToString() {
        IConstantExpression expr = factory.getConstantExpression(intType, 42);
        assertTrue(expr.toString().contains("42"));
    }


    /**
     * Test variable expression creation and getters.
     */
    @Test
    void testVariableExpression() {
        IVariableExpression expr = factory.getVariableExpression(intType, MY_VAR, 1);

        assertEquals(intType, expr.getType());
        assertEquals(MY_VAR, expr.getName());
        assertEquals(1, expr.getIndex());
    }


    /**
     * Test variable expression equals and hashCode.
     */
    @Test
    void testVariableExpressionEqualsHashCode() {
        IVariableExpression expr1 = factory.getVariableExpression(intType, MY_VAR, 1);
        IVariableExpression expr2 = factory.getVariableExpression(intType, MY_VAR, 1);
        IVariableExpression expr3 = factory.getVariableExpression(intType, "otherVar", 2);

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
    }


    /**
     * Test variable expression toString.
     */
    @Test
    void testVariableExpressionToString() {
        IVariableExpression expr = factory.getVariableExpression(intType, MY_VAR, 0);
        assertTrue(expr.toString().contains(MY_VAR));
    }


    /**
     * Test binary expression creation and getters.
     */
    @Test
    void testBinaryExpression() {
        IExpression left = factory.getConstantExpression(intType, 1);
        IExpression right = factory.getConstantExpression(intType, 2);
        IBinaryExpression expr = factory.getBinaryExpression(intType, left, right, BinaryOperator.ADD);

        assertEquals(intType, expr.getType());
        assertEquals(left, expr.getLeft());
        assertEquals(right, expr.getRight());
        assertEquals(BinaryOperator.ADD, expr.getOperator());
    }


    /**
     * Test binary expression equals and hashCode.
     */
    @Test
    void testBinaryExpressionEqualsHashCode() {
        IExpression left = factory.getConstantExpression(intType, 1);
        IExpression right = factory.getConstantExpression(intType, 2);
        IBinaryExpression expr1 = factory.getBinaryExpression(intType, left, right, BinaryOperator.ADD);
        IBinaryExpression expr2 = factory.getBinaryExpression(intType, left, right, BinaryOperator.ADD);
        IBinaryExpression expr3 = factory.getBinaryExpression(intType, left, right, BinaryOperator.SUB);

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
    }


    /**
     * Test binary expression toString.
     */
    @Test
    void testBinaryExpressionToString() {
        IExpression left = factory.getConstantExpression(intType, 1);
        IExpression right = factory.getConstantExpression(intType, 2);
        IBinaryExpression expr = factory.getBinaryExpression(intType, left, right, BinaryOperator.ADD);
        assertNotNull(expr.toString());
    }


    /**
     * Test unary expression creation and getters.
     */
    @Test
    void testUnaryExpression() {
        IExpression operand = factory.getConstantExpression(intType, 5);
        IUnaryExpression expr = factory.getUnaryExpression(intType, operand, UnaryOperator.NEG);

        assertEquals(intType, expr.getType());
        assertEquals(operand, expr.getOperand());
        assertEquals(UnaryOperator.NEG, expr.getOperator());
    }


    /**
     * Test unary expression equals and hashCode.
     */
    @Test
    void testUnaryExpressionEqualsHashCode() {
        IExpression operand = factory.getConstantExpression(intType, 5);
        IUnaryExpression expr1 = factory.getUnaryExpression(intType, operand, UnaryOperator.NEG);
        IUnaryExpression expr2 = factory.getUnaryExpression(intType, operand, UnaryOperator.NEG);
        IUnaryExpression expr3 = factory.getUnaryExpression(intType, operand, UnaryOperator.NOT);

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
    }


    /**
     * Test unary expression toString.
     */
    @Test
    void testUnaryExpressionToString() {
        IExpression operand = factory.getConstantExpression(intType, 5);
        IUnaryExpression expr = factory.getUnaryExpression(intType, operand, UnaryOperator.NEG);
        assertNotNull(expr.toString());
    }


    /**
     * Test method call expression creation and getters.
     */
    @Test
    void testMethodCallExpression() {
        IExpression target = factory.getVariableExpression(stringType, OBJ_VAR, 1);
        IExpression arg = factory.getConstantExpression(intType, 0);
        IMethodCallExpression expr = factory.getMethodCallExpression(
                stringType, target, MY_METHOD, OWNER, Collections.singletonList(arg));

        assertEquals(stringType, expr.getType());
        assertEquals(target, expr.getTarget());
        assertEquals(MY_METHOD, expr.getMethodName());
        assertEquals(OWNER, expr.getOwner());
        assertEquals(1, expr.getArguments().size());
    }


    /**
     * Test static method call (null target).
     */
    @Test
    void testMethodCallExpressionStaticCall() {
        IMethodCallExpression expr = factory.getMethodCallExpression(
                intType, null, "parseInt", "java/lang/Integer", Collections.emptyList());

        assertNull(expr.getTarget());
        assertEquals("parseInt", expr.getMethodName());
    }


    /**
     * Test method call expression equals and hashCode.
     */
    @Test
    void testMethodCallExpressionEqualsHashCode() {
        IMethodCallExpression expr1 = factory.getMethodCallExpression(
                intType, null, MY_METHOD, OWNER, Collections.emptyList());
        IMethodCallExpression expr2 = factory.getMethodCallExpression(
                intType, null, MY_METHOD, OWNER, Collections.emptyList());
        IMethodCallExpression expr3 = factory.getMethodCallExpression(
                intType, null, "otherMethod", OWNER, Collections.emptyList());

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
    }


    /**
     * Test method call expression toString.
     */
    @Test
    void testMethodCallExpressionToString() {
        IMethodCallExpression expr = factory.getMethodCallExpression(
                intType, null, MY_METHOD, OWNER, Collections.emptyList());
        assertTrue(expr.toString().contains(MY_METHOD));
    }


    /**
     * Test field access expression creation and getters.
     */
    @Test
    void testFieldAccessExpression() {
        IExpression target = factory.getVariableExpression(stringType, OBJ_VAR, 1);
        IFieldAccessExpression expr = factory.getFieldAccessExpression(intType, target, MY_FIELD, OWNER);

        assertEquals(intType, expr.getType());
        assertEquals(target, expr.getTarget());
        assertEquals(MY_FIELD, expr.getFieldName());
        assertEquals(OWNER, expr.getOwner());
    }


    /**
     * Test static field access (null target).
     */
    @Test
    void testFieldAccessExpressionStatic() {
        IFieldAccessExpression expr = factory.getFieldAccessExpression(
                intType, null, "MAX_VALUE", "java/lang/Integer");

        assertNull(expr.getTarget());
        assertEquals("MAX_VALUE", expr.getFieldName());
    }


    /**
     * Test field access expression equals and hashCode.
     */
    @Test
    void testFieldAccessExpressionEqualsHashCode() {
        IFieldAccessExpression expr1 = factory.getFieldAccessExpression(intType, null, MY_FIELD, OWNER);
        IFieldAccessExpression expr2 = factory.getFieldAccessExpression(intType, null, MY_FIELD, OWNER);
        IFieldAccessExpression expr3 = factory.getFieldAccessExpression(intType, null, "otherField", OWNER);

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
    }


    /**
     * Test field access expression toString.
     */
    @Test
    void testFieldAccessExpressionToString() {
        IFieldAccessExpression expr = factory.getFieldAccessExpression(intType, null, MY_FIELD, OWNER);
        assertTrue(expr.toString().contains(MY_FIELD));
    }


    /**
     * Test cast expression creation and getters.
     */
    @Test
    void testCastExpression() {
        IExpression operand = factory.getVariableExpression(
                typeFactory.getObjectType(OBJECT_TYPE), OBJ_VAR, 1);
        ICastExpression expr = factory.getCastExpression(stringType, operand, stringType);

        assertEquals(stringType, expr.getType());
        assertEquals(operand, expr.getOperand());
        assertEquals(stringType, expr.getCastType());
    }


    /**
     * Test cast expression equals and hashCode.
     */
    @Test
    void testCastExpressionEqualsHashCode() {
        IExpression operand = factory.getVariableExpression(
                typeFactory.getObjectType(OBJECT_TYPE), OBJ_VAR, 1);
        ICastExpression expr1 = factory.getCastExpression(stringType, operand, stringType);
        ICastExpression expr2 = factory.getCastExpression(stringType, operand, stringType);
        ICastExpression expr3 = factory.getCastExpression(intType, operand, intType);

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
    }


    /**
     * Test cast expression toString.
     */
    @Test
    void testCastExpressionToString() {
        IExpression operand = factory.getVariableExpression(
                typeFactory.getObjectType(OBJECT_TYPE), OBJ_VAR, 1);
        ICastExpression expr = factory.getCastExpression(stringType, operand, stringType);
        assertNotNull(expr.toString());
    }


    /**
     * Test instanceof expression creation and getters.
     */
    @Test
    void testInstanceOfExpression() {
        IExpression operand = factory.getVariableExpression(
                typeFactory.getObjectType(OBJECT_TYPE), OBJ_VAR, 1);
        IInstanceOfExpression expr = factory.getInstanceOfExpression(boolType, operand, stringType, null);

        assertEquals(boolType, expr.getType());
        assertEquals(operand, expr.getOperand());
        assertEquals(stringType, expr.getCheckType());
        assertNull(expr.getPatternVariable());
    }


    /**
     * Test instanceof expression with pattern variable.
     */
    @Test
    void testInstanceOfExpressionWithPattern() {
        IExpression operand = factory.getVariableExpression(
                typeFactory.getObjectType(OBJECT_TYPE), OBJ_VAR, 1);
        IInstanceOfExpression expr = factory.getInstanceOfExpression(boolType, operand, stringType, "str");

        assertEquals("str", expr.getPatternVariable());
    }


    /**
     * Test instanceof expression equals and hashCode.
     */
    @Test
    void testInstanceOfExpressionEqualsHashCode() {
        IExpression operand = factory.getVariableExpression(
                typeFactory.getObjectType(OBJECT_TYPE), OBJ_VAR, 1);
        IInstanceOfExpression expr1 = factory.getInstanceOfExpression(boolType, operand, stringType, null);
        IInstanceOfExpression expr2 = factory.getInstanceOfExpression(boolType, operand, stringType, null);
        IInstanceOfExpression expr3 = factory.getInstanceOfExpression(boolType, operand, stringType, "str");

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
    }


    /**
     * Test instanceof expression toString.
     */
    @Test
    void testInstanceOfExpressionToString() {
        IExpression operand = factory.getVariableExpression(
                typeFactory.getObjectType(OBJECT_TYPE), OBJ_VAR, 1);
        IInstanceOfExpression expr = factory.getInstanceOfExpression(boolType, operand, stringType, null);
        assertNotNull(expr.toString());
    }


    /**
     * Test new expression creation and getters.
     */
    @Test
    void testNewExpression() {
        IExpression arg = factory.getConstantExpression(intType, 16);
        INewExpression expr = factory.getNewExpression(stringType, stringType, Collections.singletonList(arg));

        assertEquals(stringType, expr.getType());
        assertEquals(stringType, expr.getInstantiatedType());
        assertEquals(1, expr.getArguments().size());
    }


    /**
     * Test new expression with no arguments.
     */
    @Test
    void testNewExpressionNoArgs() {
        INewExpression expr = factory.getNewExpression(stringType, stringType, Collections.emptyList());
        assertTrue(expr.getArguments().isEmpty());
    }


    /**
     * Test new expression equals and hashCode.
     */
    @Test
    void testNewExpressionEqualsHashCode() {
        INewExpression expr1 = factory.getNewExpression(stringType, stringType, Collections.emptyList());
        INewExpression expr2 = factory.getNewExpression(stringType, stringType, Collections.emptyList());
        IType otherType = typeFactory.getObjectType("java/util/ArrayList");
        INewExpression expr3 = factory.getNewExpression(otherType, otherType, Collections.emptyList());

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
    }


    /**
     * Test new expression toString.
     */
    @Test
    void testNewExpressionToString() {
        INewExpression expr = factory.getNewExpression(stringType, stringType, Collections.emptyList());
        assertNotNull(expr.toString());
    }


    /**
     * Test new array expression creation and getters.
     */
    @Test
    void testNewArrayExpression() {
        IExpression size = factory.getConstantExpression(intType, 10);
        INewArrayExpression expr = factory.getNewArrayExpression(
                typeFactory.getArrayType(intType, 1), intType,
                Collections.singletonList(size), Collections.emptyList());

        assertEquals(intType, expr.getElementType());
        assertEquals(1, expr.getDimensionSizes().size());
        assertTrue(expr.getInitializers().isEmpty());
    }


    /**
     * Test new array expression with initializers.
     */
    @Test
    void testNewArrayExpressionWithInitializers() {
        IExpression init1 = factory.getConstantExpression(intType, 1);
        IExpression init2 = factory.getConstantExpression(intType, 2);
        INewArrayExpression expr = factory.getNewArrayExpression(
                typeFactory.getArrayType(intType, 1), intType,
                Collections.emptyList(), List.of(init1, init2));

        assertTrue(expr.getDimensionSizes().isEmpty());
        assertEquals(2, expr.getInitializers().size());
    }


    /**
     * Test new array expression equals and hashCode.
     */
    @Test
    void testNewArrayExpressionEqualsHashCode() {
        IExpression size = factory.getConstantExpression(intType, 10);
        IType arrayType = typeFactory.getArrayType(intType, 1);
        INewArrayExpression expr1 = factory.getNewArrayExpression(
                arrayType, intType, Collections.singletonList(size), Collections.emptyList());
        INewArrayExpression expr2 = factory.getNewArrayExpression(
                arrayType, intType, Collections.singletonList(size), Collections.emptyList());
        IExpression otherSize = factory.getConstantExpression(intType, 20);
        INewArrayExpression expr3 = factory.getNewArrayExpression(
                arrayType, intType, Collections.singletonList(otherSize), Collections.emptyList());

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
    }


    /**
     * Test new array expression toString.
     */
    @Test
    void testNewArrayExpressionToString() {
        IExpression size = factory.getConstantExpression(intType, 10);
        INewArrayExpression expr = factory.getNewArrayExpression(
                typeFactory.getArrayType(intType, 1), intType,
                Collections.singletonList(size), Collections.emptyList());
        assertNotNull(expr.toString());
    }


    /**
     * Test assignment expression creation and getters.
     */
    @Test
    void testAssignmentExpression() {
        IExpression target = factory.getVariableExpression(intType, MY_VAR, 1);
        IExpression value = factory.getConstantExpression(intType, 42);
        IAssignmentExpression expr = factory.getAssignmentExpression(intType, target, value);

        assertEquals(intType, expr.getType());
        assertEquals(target, expr.getTarget());
        assertEquals(value, expr.getValue());
    }


    /**
     * Test assignment expression equals and hashCode.
     */
    @Test
    void testAssignmentExpressionEqualsHashCode() {
        IExpression target = factory.getVariableExpression(intType, MY_VAR, 1);
        IExpression value1 = factory.getConstantExpression(intType, 42);
        IExpression value2 = factory.getConstantExpression(intType, 99);
        IAssignmentExpression expr1 = factory.getAssignmentExpression(intType, target, value1);
        IAssignmentExpression expr2 = factory.getAssignmentExpression(intType, target, value1);
        IAssignmentExpression expr3 = factory.getAssignmentExpression(intType, target, value2);

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
    }


    /**
     * Test assignment expression toString.
     */
    @Test
    void testAssignmentExpressionToString() {
        IExpression target = factory.getVariableExpression(intType, MY_VAR, 1);
        IExpression value = factory.getConstantExpression(intType, 42);
        IAssignmentExpression expr = factory.getAssignmentExpression(intType, target, value);
        assertNotNull(expr.toString());
    }


    /**
     * Test array access expression creation and getters.
     */
    @Test
    void testArrayAccessExpression() {
        IExpression array = factory.getVariableExpression(typeFactory.getArrayType(intType, 1), "arr", 1);
        IExpression index = factory.getConstantExpression(intType, 0);
        IArrayAccessExpression expr = factory.getArrayAccessExpression(intType, array, index);

        assertEquals(intType, expr.getType());
        assertEquals(array, expr.getArray());
        assertEquals(index, expr.getIndex());
    }


    /**
     * Test array access expression equals and hashCode.
     */
    @Test
    void testArrayAccessExpressionEqualsHashCode() {
        IExpression array = factory.getVariableExpression(typeFactory.getArrayType(intType, 1), "arr", 1);
        IExpression index1 = factory.getConstantExpression(intType, 0);
        IExpression index2 = factory.getConstantExpression(intType, 1);
        IArrayAccessExpression expr1 = factory.getArrayAccessExpression(intType, array, index1);
        IArrayAccessExpression expr2 = factory.getArrayAccessExpression(intType, array, index1);
        IArrayAccessExpression expr3 = factory.getArrayAccessExpression(intType, array, index2);

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
    }


    /**
     * Test array access expression toString.
     */
    @Test
    void testArrayAccessExpressionToString() {
        IExpression array = factory.getVariableExpression(typeFactory.getArrayType(intType, 1), "arr", 1);
        IExpression index = factory.getConstantExpression(intType, 0);
        IArrayAccessExpression expr = factory.getArrayAccessExpression(intType, array, index);
        assertNotNull(expr.toString());
    }


    /**
     * Test binary operator enum values and symbols.
     */
    @Test
    void testBinaryOperatorValues() {
        assertEquals(19, BinaryOperator.values().length);
        assertEquals("+", BinaryOperator.ADD.getSymbol());
        assertEquals("-", BinaryOperator.SUB.getSymbol());
        assertEquals("*", BinaryOperator.MUL.getSymbol());
        assertEquals("/", BinaryOperator.DIV.getSymbol());
        assertEquals("%", BinaryOperator.REM.getSymbol());
        assertEquals("&", BinaryOperator.AND.getSymbol());
        assertEquals("|", BinaryOperator.OR.getSymbol());
        assertEquals("^", BinaryOperator.XOR.getSymbol());
        assertEquals("<<", BinaryOperator.SHL.getSymbol());
        assertEquals(">>", BinaryOperator.SHR.getSymbol());
        assertEquals(">>>", BinaryOperator.USHR.getSymbol());
        assertEquals("==", BinaryOperator.EQ.getSymbol());
        assertEquals("!=", BinaryOperator.NE.getSymbol());
        assertEquals("<", BinaryOperator.LT.getSymbol());
        assertEquals(">", BinaryOperator.GT.getSymbol());
        assertEquals("<=", BinaryOperator.LE.getSymbol());
        assertEquals(">=", BinaryOperator.GE.getSymbol());
        assertEquals("&&", BinaryOperator.LOGICAL_AND.getSymbol());
        assertEquals("||", BinaryOperator.LOGICAL_OR.getSymbol());
    }


    /**
     * Test unary operator enum values and symbols.
     */
    @Test
    void testUnaryOperatorValues() {
        assertEquals(7, UnaryOperator.values().length);
        assertEquals("-", UnaryOperator.NEG.getSymbol());
        assertEquals("~", UnaryOperator.NOT.getSymbol());
        assertEquals("!", UnaryOperator.LOGICAL_NOT.getSymbol());
        assertEquals("++", UnaryOperator.PRE_INC.getSymbol());
        assertEquals("--", UnaryOperator.PRE_DEC.getSymbol());
        assertEquals("++", UnaryOperator.POST_INC.getSymbol());
        assertEquals("--", UnaryOperator.POST_DEC.getSymbol());
    }


    /**
     * Test lambda expression creation and getters.
     */
    @Test
    void testLambdaExpression() {
        IType funcType = typeFactory.getObjectType(FUNCTION_TYPE);
        List<String> paramNames = Arrays.asList("arg0");
        List<String> paramDescs = Arrays.asList("Ljava/lang/Object;");
        ILambdaExpression expr = factory.getLambdaExpression(funcType, APPLY_METHOD,
                paramNames, paramDescs, Collections.emptyList(), null,
                "lambda$test$0", "com/example/MyClass");

        assertEquals(funcType, expr.getType());
        assertEquals(APPLY_METHOD, expr.getFunctionalMethodName());
        assertEquals(1, expr.getParameterNames().size());
        assertEquals("arg0", expr.getParameterNames().get(0));
        assertEquals(1, expr.getParameterTypeDescriptors().size());
        assertEquals("Ljava/lang/Object;", expr.getParameterTypeDescriptors().get(0));
        assertTrue(expr.getCapturedArguments().isEmpty());
        assertNull(expr.getBody());
        assertEquals("lambda$test$0", expr.getImplementationMethodName());
        assertEquals("com/example/MyClass", expr.getImplementationOwner());
    }


    /**
     * Test lambda expression with captured arguments.
     */
    @Test
    void testLambdaExpressionWithCaptures() {
        IType funcType = typeFactory.getObjectType("java/util/function/Supplier");
        IExpression captured = factory.getVariableExpression(stringType, "name", 1);
        ILambdaExpression expr = factory.getLambdaExpression(funcType, "get",
                Collections.emptyList(), Collections.emptyList(),
                Collections.singletonList(captured), null,
                "lambda$test$1", "com/example/MyClass");

        assertEquals(1, expr.getCapturedArguments().size());
        assertEquals(captured, expr.getCapturedArguments().get(0));
    }


    /**
     * Test lambda expression with body expression.
     */
    @Test
    void testLambdaExpressionWithBody() {
        IType funcType = typeFactory.getObjectType("java/util/function/IntUnaryOperator");
        IExpression body = factory.getBinaryExpression(intType,
                factory.getVariableExpression(intType, "arg0", 0),
                factory.getConstantExpression(intType, 1),
                BinaryOperator.ADD);
        ILambdaExpression expr = factory.getLambdaExpression(funcType, "applyAsInt",
                Arrays.asList("arg0"), Arrays.asList("I"),
                Collections.emptyList(), body,
                "lambda$test$2", "com/example/MyClass");

        assertNotNull(expr.getBody());
        assertEquals(body, expr.getBody());
    }


    /**
     * Test lambda expression equals and hashCode.
     */
    @Test
    void testLambdaExpressionEqualsHashCode() {
        IType funcType = typeFactory.getObjectType(FUNCTION_TYPE);
        List<String> paramNames = Arrays.asList("arg0");
        List<String> paramDescs = Arrays.asList("Ljava/lang/Object;");
        ILambdaExpression expr1 = factory.getLambdaExpression(funcType, APPLY_METHOD,
                paramNames, paramDescs, Collections.emptyList(), null,
                "lambda$test$0", OWNER);
        ILambdaExpression expr2 = factory.getLambdaExpression(funcType, "apply",
                paramNames, paramDescs, Collections.emptyList(), null,
                "lambda$test$0", OWNER);
        ILambdaExpression expr3 = factory.getLambdaExpression(funcType, "apply",
                paramNames, paramDescs, Collections.emptyList(), null,
                "lambda$other$0", OWNER);

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
        assertNotEquals(expr1, null);
        assertEquals(expr1, expr1);
    }


    /**
     * Test lambda expression toString.
     */
    @Test
    void testLambdaExpressionToString() {
        IType funcType = typeFactory.getObjectType("java/util/function/Function");
        ILambdaExpression expr = factory.getLambdaExpression(funcType, "apply",
                Arrays.asList("x"), Arrays.asList("Ljava/lang/Object;"),
                Collections.emptyList(), null,
                "lambda$test$0", OWNER);
        String str = expr.toString();
        assertTrue(str.contains("x"));
        assertTrue(str.contains("->"));
    }


    /**
     * Test method reference expression for static method.
     */
    @Test
    void testMethodReferenceExpressionStatic() {
        IType funcType = typeFactory.getObjectType("java/util/function/Function");
        IMethodReferenceExpression expr = factory.getMethodReferenceExpression(
                funcType, null, STRING_TYPE, VALUE_OF_METHOD, false);

        assertEquals(funcType, expr.getType());
        assertNull(expr.getQualifier());
        assertEquals(STRING_TYPE, expr.getOwner());
        assertEquals(VALUE_OF_METHOD, expr.getMethodName());
        assertTrue(!expr.isConstructorReference());
    }


    /**
     * Test method reference expression for instance method with qualifier.
     */
    @Test
    void testMethodReferenceExpressionInstance() {
        IType funcType = typeFactory.getObjectType("java/util/function/Consumer");
        IExpression qualifier = factory.getVariableExpression(
                typeFactory.getObjectType("java/io/PrintStream"), "out", 1);
        IMethodReferenceExpression expr = factory.getMethodReferenceExpression(
                funcType, qualifier, "java/io/PrintStream", "println", false);

        assertEquals(qualifier, expr.getQualifier());
        assertEquals("println", expr.getMethodName());
        assertTrue(!expr.isConstructorReference());
    }


    /**
     * Test method reference expression for constructor reference.
     */
    @Test
    void testMethodReferenceExpressionConstructor() {
        IType funcType = typeFactory.getObjectType("java/util/function/Supplier");
        IMethodReferenceExpression expr = factory.getMethodReferenceExpression(
                funcType, null, "java/util/ArrayList", "<init>", true);

        assertTrue(expr.isConstructorReference());
        assertEquals("<init>", expr.getMethodName());
    }


    /**
     * Test method reference expression equals and hashCode.
     */
    @Test
    void testMethodReferenceExpressionEqualsHashCode() {
        IType funcType = typeFactory.getObjectType("java/util/function/Function");
        IMethodReferenceExpression expr1 = factory.getMethodReferenceExpression(
                funcType, null, STRING_TYPE, VALUE_OF_METHOD, false);
        IMethodReferenceExpression expr2 = factory.getMethodReferenceExpression(
                funcType, null, STRING_TYPE, VALUE_OF_METHOD, false);
        IMethodReferenceExpression expr3 = factory.getMethodReferenceExpression(
                funcType, null, "java/lang/Integer", "parseInt", false);

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());
        assertNotEquals(expr1, expr3);
        assertNotEquals(expr1, null);
        assertEquals(expr1, expr1);
    }


    /**
     * Test method reference expression toString.
     */
    @Test
    void testMethodReferenceExpressionToString() {
        IType funcType = typeFactory.getObjectType("java/util/function/Function");
        IMethodReferenceExpression expr = factory.getMethodReferenceExpression(
                funcType, null, STRING_TYPE, VALUE_OF_METHOD, false);
        String str = expr.toString();
        assertTrue(str.contains("String"));
        assertTrue(str.contains("::"));
        assertTrue(str.contains(VALUE_OF_METHOD));
    }


    /**
     * Test method reference expression toString for constructor reference.
     */
    @Test
    void testMethodReferenceExpressionToStringConstructor() {
        IType funcType = typeFactory.getObjectType("java/util/function/Supplier");
        IMethodReferenceExpression expr = factory.getMethodReferenceExpression(
                funcType, null, "java/util/ArrayList", "<init>", true);
        String str = expr.toString();
        assertTrue(str.contains("ArrayList"));
        assertTrue(str.contains("::"));
        assertTrue(str.contains("new"));
    }
}
