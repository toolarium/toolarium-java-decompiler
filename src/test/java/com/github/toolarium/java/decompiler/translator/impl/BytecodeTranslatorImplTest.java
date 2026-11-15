/*
 * BytecodeTranslatorImplTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.translator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.ast.expression.BinaryOperator;
import com.github.toolarium.java.decompiler.model.ast.expression.IAssignmentExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IBinaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ICastExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IConstantExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IFieldAccessExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IInstanceOfExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodCallExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IUnaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.UnaryOperator;
import com.github.toolarium.java.decompiler.model.ast.statement.IExpressionStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IIfStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IReturnStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IThrowStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IVariableDeclarationStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IWhileStatement;
import com.github.toolarium.java.decompiler.translator.BytecodeTranslatorFactory;
import com.github.toolarium.java.decompiler.translator.IBytecodeTranslator;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;


/**
 * Tests for {@link BytecodeTranslatorImpl}.
 *
 * @author patrick
 */
public class BytecodeTranslatorImplTest {
    private static final String TEST_CLASS = "Test";
    private static final String DESC_INT = "()I";
    private static final String DESC_VOID = "()V";
    private static final String STRING_CLASS = "java/lang/String";
    private final IBytecodeTranslator translator = BytecodeTranslatorFactory.getInstance().getBytecodeTranslator();


    /**
     * Test factory returns a singleton instance.
     */
    @Test
    void testFactorySingleton() {
        BytecodeTranslatorFactory factory1 = BytecodeTranslatorFactory.getInstance();
        BytecodeTranslatorFactory factory2 = BytecodeTranslatorFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Test translation of ICONST instructions (push integer constants -1 through 5).
     */
    @Test
    void testIconstConstants() {
        MethodNode method = buildStaticIntMethod("iconst", DESC_INT);
        method.visitInsn(Opcodes.ICONST_3);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt, "Expected a return statement");
        assertTrue(returnStmt.getValue() instanceof IConstantExpression);
        assertEquals(3, ((IConstantExpression) returnStmt.getValue()).getValue());
    }


    /**
     * Test ICONST_M1 pushes -1.
     */
    @Test
    void testIconstMinusOne() {
        MethodNode method = buildStaticIntMethod("iconstM1", DESC_INT);
        method.visitInsn(Opcodes.ICONST_M1);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IConstantExpression);
        assertEquals(-1, ((IConstantExpression) returnStmt.getValue()).getValue());
    }


    /**
     * Test BIPUSH instruction (push byte value).
     */
    @Test
    void testBipush() {
        MethodNode method = buildStaticIntMethod("bipush", DESC_INT);
        method.visitIntInsn(Opcodes.BIPUSH, 42);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IConstantExpression);
        assertEquals(42, ((IConstantExpression) returnStmt.getValue()).getValue());
    }


    /**
     * Test SIPUSH instruction (push short value).
     */
    @Test
    void testSipush() {
        MethodNode method = buildStaticIntMethod("sipush", DESC_INT);
        method.visitIntInsn(Opcodes.SIPUSH, 300);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IConstantExpression);
        assertEquals(300, ((IConstantExpression) returnStmt.getValue()).getValue());
    }


    /**
     * Test LDC instruction for String constant.
     */
    @Test
    void testLdcString() {
        MethodNode method = buildStaticMethod("ldcString", "()Ljava/lang/String;");
        method.visitLdcInsn("hello");
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IConstantExpression);
        assertEquals("hello", ((IConstantExpression) returnStmt.getValue()).getValue());
    }


    /**
     * Test LDC instruction for integer constant.
     */
    @Test
    void testLdcInteger() {
        MethodNode method = buildStaticIntMethod("ldcInt", DESC_INT);
        method.visitLdcInsn(100000);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IConstantExpression);
        assertEquals(100000, ((IConstantExpression) returnStmt.getValue()).getValue());
    }


    /**
     * Test LDC instruction for long constant.
     */
    @Test
    void testLdcLong() {
        MethodNode method = buildStaticMethod("ldcLong", "()J");
        method.visitLdcInsn(999999999L);
        method.visitInsn(Opcodes.LRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IConstantExpression);
        assertEquals(999999999L, ((IConstantExpression) returnStmt.getValue()).getValue());
    }


    /**
     * Test LDC instruction for double constant.
     */
    @Test
    void testLdcDouble() {
        MethodNode method = buildStaticMethod("ldcDouble", "()D");
        method.visitLdcInsn(3.14d);
        method.visitInsn(Opcodes.DRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IConstantExpression);
        assertEquals(3.14d, ((IConstantExpression) returnStmt.getValue()).getValue());
    }


    /**
     * Test ACONST_NULL pushes null.
     */
    @Test
    void testAconstNull() {
        MethodNode method = buildStaticMethod("aconstNull", "()Ljava/lang/Object;");
        method.visitInsn(Opcodes.ACONST_NULL);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IConstantExpression);
        IConstantExpression constExpr = (IConstantExpression) returnStmt.getValue();
        assertEquals(null, constExpr.getValue());
    }


    /**
     * Test LCONST_0 and LCONST_1 push long constants.
     */
    @Test
    void testLconstConstants() {
        MethodNode method = buildStaticMethod("lconst", "()J");
        method.visitInsn(Opcodes.LCONST_1);
        method.visitInsn(Opcodes.LRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IConstantExpression);
        assertEquals(1L, ((IConstantExpression) returnStmt.getValue()).getValue());
    }


    /**
     * Test FCONST_0 and FCONST_1 push float constants.
     */
    @Test
    void testFconstConstants() {
        MethodNode method = buildStaticMethod("fconst", "()F");
        method.visitInsn(Opcodes.FCONST_1);
        method.visitInsn(Opcodes.FRETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IConstantExpression);
        assertEquals(1.0f, ((IConstantExpression) returnStmt.getValue()).getValue());
    }


    /**
     * Test DCONST_0 and DCONST_1 push double constants.
     */
    @Test
    void testDconstConstants() {
        MethodNode method = buildStaticMethod("dconst", "()D");
        method.visitInsn(Opcodes.DCONST_0);
        method.visitInsn(Opcodes.DRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IConstantExpression);
        assertEquals(0.0d, ((IConstantExpression) returnStmt.getValue()).getValue());
    }


    /**
     * Test IADD: pops two ints, pushes the sum.
     */
    @Test
    void testIadd() {
        MethodNode method = buildStaticIntMethod("iadd", DESC_INT);
        method.visitInsn(Opcodes.ICONST_2);
        method.visitInsn(Opcodes.ICONST_3);
        method.visitInsn(Opcodes.IADD);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IBinaryExpression);
        IBinaryExpression binExpr = (IBinaryExpression) returnStmt.getValue();
        assertEquals(BinaryOperator.ADD, binExpr.getOperator());
    }


    /**
     * Test ISUB: pops two ints, pushes the difference.
     */
    @Test
    void testIsub() {
        MethodNode method = buildStaticIntMethod("isub", DESC_INT);
        method.visitInsn(Opcodes.ICONST_5);
        method.visitInsn(Opcodes.ICONST_2);
        method.visitInsn(Opcodes.ISUB);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IBinaryExpression);
        assertEquals(BinaryOperator.SUB, ((IBinaryExpression) returnStmt.getValue()).getOperator());
    }


    /**
     * Test IMUL: pops two ints, pushes the product.
     */
    @Test
    void testImul() {
        MethodNode method = buildStaticIntMethod("imul", DESC_INT);
        method.visitInsn(Opcodes.ICONST_3);
        method.visitInsn(Opcodes.ICONST_4);
        method.visitInsn(Opcodes.IMUL);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IBinaryExpression);
        assertEquals(BinaryOperator.MUL, ((IBinaryExpression) returnStmt.getValue()).getOperator());
    }


    /**
     * Test IDIV: pops two ints, pushes the quotient.
     */
    @Test
    void testIdiv() {
        MethodNode method = buildStaticIntMethod("idiv", DESC_INT);
        method.visitIntInsn(Opcodes.BIPUSH, 10);
        method.visitInsn(Opcodes.ICONST_2);
        method.visitInsn(Opcodes.IDIV);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IBinaryExpression);
        assertEquals(BinaryOperator.DIV, ((IBinaryExpression) returnStmt.getValue()).getOperator());
    }


    /**
     * Test IREM: pops two ints, pushes the remainder.
     */
    @Test
    void testIrem() {
        MethodNode method = buildStaticIntMethod("irem", DESC_INT);
        method.visitIntInsn(Opcodes.BIPUSH, 10);
        method.visitInsn(Opcodes.ICONST_3);
        method.visitInsn(Opcodes.IREM);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IBinaryExpression);
        assertEquals(BinaryOperator.REM, ((IBinaryExpression) returnStmt.getValue()).getOperator());
    }


    /**
     * Test bitwise AND (IAND).
     */
    @Test
    void testBitwiseAnd() {
        MethodNode method = buildStaticIntMethod("bitwiseAnd", DESC_INT);
        method.visitInsn(Opcodes.ICONST_5);
        method.visitInsn(Opcodes.ICONST_3);
        method.visitInsn(Opcodes.IAND);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IBinaryExpression);
        assertEquals(BinaryOperator.AND, ((IBinaryExpression) returnStmt.getValue()).getOperator());
    }


    /**
     * Test bitwise OR (IOR).
     */
    @Test
    void testBitwiseOr() {
        MethodNode method = buildStaticIntMethod("bitwiseOr", DESC_INT);
        method.visitInsn(Opcodes.ICONST_5);
        method.visitInsn(Opcodes.ICONST_3);
        method.visitInsn(Opcodes.IOR);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IBinaryExpression);
        assertEquals(BinaryOperator.OR, ((IBinaryExpression) returnStmt.getValue()).getOperator());
    }


    /**
     * Test bitwise XOR (IXOR).
     */
    @Test
    void testBitwiseXor() {
        MethodNode method = buildStaticIntMethod("bitwiseXor", DESC_INT);
        method.visitInsn(Opcodes.ICONST_5);
        method.visitInsn(Opcodes.ICONST_3);
        method.visitInsn(Opcodes.IXOR);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IBinaryExpression);
        assertEquals(BinaryOperator.XOR, ((IBinaryExpression) returnStmt.getValue()).getOperator());
    }


    /**
     * Test left shift (ISHL).
     */
    @Test
    void testShiftLeft() {
        MethodNode method = buildStaticIntMethod("shiftLeft", DESC_INT);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.ICONST_3);
        method.visitInsn(Opcodes.ISHL);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IBinaryExpression);
        assertEquals(BinaryOperator.SHL, ((IBinaryExpression) returnStmt.getValue()).getOperator());
    }


    /**
     * Test negation (INEG).
     */
    @Test
    void testNegation() {
        MethodNode method = buildStaticIntMethod("negate", DESC_INT);
        method.visitInsn(Opcodes.ICONST_5);
        method.visitInsn(Opcodes.INEG);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IUnaryExpression);
        assertEquals(UnaryOperator.NEG, ((IUnaryExpression) returnStmt.getValue()).getOperator());
    }


    /**
     * Test IFEQ: compares value against zero.
     */
    @Test
    void testIfeqCondition() {
        // static int test(int x) { if (x == 0) return 1; return 0; }
        MethodNode method = buildStaticIntMethod("ifeq", "(I)I");
        method.maxLocals = 1;
        Label elseLabel = new Label();

        method.visitVarInsn(Opcodes.ILOAD, 0);
        method.visitJumpInsn(Opcodes.IFEQ, elseLabel);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitInsn(Opcodes.IRETURN);
        method.visitLabel(elseLabel);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        // Should produce an if statement or conditional return
        boolean hasConditional = false;
        for (IStatement stmt : result) {
            if (stmt instanceof IIfStatement) {
                hasConditional = true;
                IIfStatement ifStmt = (IIfStatement) stmt;
                assertNotNull(ifStmt.getCondition());
            }
        }
        assertTrue(hasConditional, "Expected an if statement from IFEQ");
    }


    /**
     * Test IF_ICMPEQ: compares two int values for equality.
     */
    @Test
    void testIfIcmpeqCondition() {
        // static int test(int a, int b) { if (a == b) return 1; return 0; }
        MethodNode method = buildStaticIntMethod("ifIcmpeq", "(II)I");
        method.maxLocals = 2;
        Label elseLabel = new Label();

        method.visitVarInsn(Opcodes.ILOAD, 0);
        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitJumpInsn(Opcodes.IF_ICMPEQ, elseLabel);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitInsn(Opcodes.IRETURN);
        method.visitLabel(elseLabel);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 2);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        boolean hasConditional = false;
        for (IStatement stmt : result) {
            if (stmt instanceof IIfStatement) {
                hasConditional = true;
            }
        }
        assertTrue(hasConditional, "Expected an if statement from IF_ICMPEQ");
    }


    /**
     * Test IFNULL: null check condition.
     */
    @Test
    void testIfnullCondition() {
        // static int test(Object o) { if (o == null) return 1; return 0; }
        MethodNode method = buildStaticIntMethod("ifnull", "(Ljava/lang/Object;)I");
        method.maxLocals = 1;
        Label elseLabel = new Label();

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitJumpInsn(Opcodes.IFNULL, elseLabel);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitInsn(Opcodes.IRETURN);
        method.visitLabel(elseLabel);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        assertNotNull(result);
        boolean hasConditional = false;
        for (IStatement stmt : result) {
            if (stmt instanceof IIfStatement) {
                hasConditional = true;
                IBinaryExpression condition = (IBinaryExpression) ((IIfStatement) stmt).getCondition();
                assertEquals(BinaryOperator.NE, condition.getOperator());
            }
        }
        assertTrue(hasConditional, "Expected an if statement from IFNULL");
    }


    /**
     * Test INVOKEVIRTUAL: instance method call.
     */
    @Test
    void testInvokeVirtual() {
        // static int test(String s) { return s.length(); }
        MethodNode method = buildStaticIntMethod("invokeVirtual", "(Ljava/lang/String;)I");
        method.maxLocals = 1;
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, STRING_CLASS, "length", DESC_INT, false);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IMethodCallExpression);
        IMethodCallExpression callExpr = (IMethodCallExpression) returnStmt.getValue();
        assertEquals("length", callExpr.getMethodName());
        assertEquals(STRING_CLASS, callExpr.getOwner());
        assertTrue(callExpr.getArguments().isEmpty());
    }


    /**
     * Test INVOKESTATIC: static method call.
     */
    @Test
    void testInvokeStatic() {
        // static int test(String s) { return Integer.parseInt(s); }
        MethodNode method = buildStaticIntMethod("invokeStatic", "(Ljava/lang/String;)I");
        method.maxLocals = 1;
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "parseInt",
                "(Ljava/lang/String;)I", false);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IMethodCallExpression);
        IMethodCallExpression callExpr = (IMethodCallExpression) returnStmt.getValue();
        assertEquals("parseInt", callExpr.getMethodName());
        assertEquals("java/lang/Integer", callExpr.getOwner());
        assertEquals(1, callExpr.getArguments().size());
    }


    /**
     * Test INVOKEVIRTUAL for void method becomes expression statement.
     */
    @Test
    void testInvokeVirtualVoid() {
        // static void test(java.io.PrintStream ps) { ps.println(); }
        MethodNode method = buildStaticMethod("invokeVoid", "(Ljava/io/PrintStream;)V");
        method.maxLocals = 1;
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", DESC_VOID, false);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IExpressionStatement exprStmt = findFirst(result, IExpressionStatement.class);
        assertNotNull(exprStmt, "Expected expression statement for void method call");
        assertTrue(exprStmt.getExpression() instanceof IMethodCallExpression);
        assertEquals("println", ((IMethodCallExpression) exprStmt.getExpression()).getMethodName());
    }


    /**
     * Test GETFIELD: instance field access.
     */
    @Test
    void testGetfield() {
        // int test() { return this.value; }
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "getfield", DESC_INT, null, null);
        method.maxLocals = 1;
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD, TEST_CLASS, "value", "I");
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IFieldAccessExpression);
        IFieldAccessExpression fieldExpr = (IFieldAccessExpression) returnStmt.getValue();
        assertEquals("value", fieldExpr.getFieldName());
        assertEquals("Test", fieldExpr.getOwner());
        assertNotNull(fieldExpr.getTarget());
    }


    /**
     * Test PUTFIELD: instance field store becomes assignment.
     */
    @Test
    void testPutfield() {
        // void test(int v) { this.value = v; }
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "putfield", "(I)V", null, null);
        method.maxLocals = 2;
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitFieldInsn(Opcodes.PUTFIELD, TEST_CLASS, "value", "I");
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(2, 2);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IExpressionStatement exprStmt = findFirst(result, IExpressionStatement.class);
        assertNotNull(exprStmt, "Expected expression statement for PUTFIELD assignment");
        assertTrue(exprStmt.getExpression() instanceof IAssignmentExpression);
        IAssignmentExpression assignExpr = (IAssignmentExpression) exprStmt.getExpression();
        assertTrue(assignExpr.getTarget() instanceof IFieldAccessExpression);
        assertEquals("value", ((IFieldAccessExpression) assignExpr.getTarget()).getFieldName());
    }


    /**
     * Test GETSTATIC: static field access (null target).
     */
    @Test
    void testGetstatic() {
        MethodNode method = buildStaticMethod("getstatic", "()Ljava/io/PrintStream;");
        method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IFieldAccessExpression);
        IFieldAccessExpression fieldExpr = (IFieldAccessExpression) returnStmt.getValue();
        assertEquals("out", fieldExpr.getFieldName());
        assertEquals("java/lang/System", fieldExpr.getOwner());
        assertEquals(null, fieldExpr.getTarget());
    }


    /**
     * Test PUTSTATIC: static field store becomes assignment.
     */
    @Test
    void testPutstatic() {
        MethodNode method = buildStaticMethod("putstatic", DESC_VOID);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitFieldInsn(Opcodes.PUTSTATIC, TEST_CLASS, "counter", "I");
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IExpressionStatement exprStmt = findFirst(result, IExpressionStatement.class);
        assertNotNull(exprStmt, "Expected expression statement for PUTSTATIC");
        assertTrue(exprStmt.getExpression() instanceof IAssignmentExpression);
    }


    /**
     * Test NEW + INVOKESPECIAL: object creation.
     */
    @Test
    void testNewObject() {
        MethodNode method = buildStaticMethod("newObj", "()Ljava/lang/Object;");
        method.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
        method.visitInsn(Opcodes.DUP);
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", DESC_VOID, false);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof INewExpression);
        INewExpression newExpr = (INewExpression) returnStmt.getValue();
        assertNotNull(newExpr.getInstantiatedType());
    }


    /**
     * Test CHECKCAST: type cast expression.
     */
    @Test
    void testCheckcast() {
        // static String test(Object o) { return (String) o; }
        MethodNode method = buildStaticMethod("checkcast", "(Ljava/lang/Object;)Ljava/lang/String;");
        method.maxLocals = 1;
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitTypeInsn(Opcodes.CHECKCAST, STRING_CLASS);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof ICastExpression);
        ICastExpression castExpr = (ICastExpression) returnStmt.getValue();
        assertNotNull(castExpr.getCastType());
    }


    /**
     * Test INSTANCEOF: produces boolean result.
     */
    @Test
    void testInstanceof() {
        // static boolean test(Object o) { return o instanceof String; }
        MethodNode method = buildStaticIntMethod("instanceOf", "(Ljava/lang/Object;)Z");
        method.maxLocals = 1;
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitTypeInsn(Opcodes.INSTANCEOF, STRING_CLASS);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IInstanceOfExpression);
        IInstanceOfExpression instanceOfExpr = (IInstanceOfExpression) returnStmt.getValue();
        assertNotNull(instanceOfExpr.getCheckType());
    }


    /**
     * Test DUP: duplicates top of stack.
     */
    @Test
    void testDup() {
        // static void test(Object o) { if (o == o) return; }
        // DUP essentially: push value twice
        MethodNode method = buildStaticMethod("dup", DESC_VOID);
        method.visitInsn(Opcodes.ICONST_5);
        method.visitInsn(Opcodes.DUP);
        method.visitInsn(Opcodes.IADD);
        // 5 + 5 on stack, pop via return
        method.visitInsn(Opcodes.POP);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        // The result should contain at least a return
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    /**
     * Test SWAP: swaps top two stack values.
     */
    @Test
    void testSwap() {
        MethodNode method = buildStaticIntMethod("swap", DESC_INT);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.ICONST_2);
        method.visitInsn(Opcodes.SWAP);
        // Now stack is: [1 on top, 2 below], ISUB pops 1 and 2 -> 2 - 1
        method.visitInsn(Opcodes.ISUB);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertTrue(returnStmt.getValue() instanceof IBinaryExpression);
        assertEquals(BinaryOperator.SUB, ((IBinaryExpression) returnStmt.getValue()).getOperator());
    }


    /**
     * Test POP: pops top value and may create expression statement.
     */
    @Test
    void testPop() {
        MethodNode method = buildStaticMethod("pop", DESC_VOID);
        method.visitInsn(Opcodes.ICONST_5);
        method.visitInsn(Opcodes.POP);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        // POP should create an expression statement
        IExpressionStatement exprStmt = findFirst(result, IExpressionStatement.class);
        assertNotNull(exprStmt, "Expected expression statement from POP");
    }


    /**
     * Test void RETURN statement.
     */
    @Test
    void testVoidReturn() {
        MethodNode method = buildStaticMethod("voidReturn", DESC_VOID);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(0, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertEquals(null, returnStmt.getValue());
    }


    /**
     * Test IRETURN: returns int value.
     */
    @Test
    void testIntReturn() {
        MethodNode method = buildStaticIntMethod("intReturn", DESC_INT);
        method.visitInsn(Opcodes.ICONST_5);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IReturnStatement returnStmt = findFirst(result, IReturnStatement.class);
        assertNotNull(returnStmt);
        assertNotNull(returnStmt.getValue());
    }


    /**
     * Test ATHROW: throws exception.
     */
    @Test
    void testAthrow() {
        MethodNode method = buildStaticMethod("athrow", DESC_VOID);
        method.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
        method.visitInsn(Opcodes.DUP);
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", DESC_VOID, false);
        method.visitInsn(Opcodes.ATHROW);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IThrowStatement throwStmt = findFirst(result, IThrowStatement.class);
        assertNotNull(throwStmt, "Expected a throw statement");
        assertNotNull(throwStmt.getExpression());
    }


    /**
     * Test IINC with increment of 1 produces post-increment.
     */
    @Test
    void testIincIncrement() {
        MethodNode method = buildStaticMethod("iinc", "(I)V");
        method.maxLocals = 1;
        method.visitIincInsn(0, 1);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IExpressionStatement exprStmt = findFirst(result, IExpressionStatement.class);
        assertNotNull(exprStmt, "Expected expression statement for IINC");
        assertTrue(exprStmt.getExpression() instanceof IUnaryExpression);
        assertEquals(UnaryOperator.POST_INC, ((IUnaryExpression) exprStmt.getExpression()).getOperator());
    }


    /**
     * Test IINC with increment of -1 produces post-decrement.
     */
    @Test
    void testIincDecrement() {
        MethodNode method = buildStaticMethod("iincDec", "(I)V");
        method.maxLocals = 1;
        method.visitIincInsn(0, -1);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IExpressionStatement exprStmt = findFirst(result, IExpressionStatement.class);
        assertNotNull(exprStmt);
        assertTrue(exprStmt.getExpression() instanceof IUnaryExpression);
        assertEquals(UnaryOperator.POST_DEC, ((IUnaryExpression) exprStmt.getExpression()).getOperator());
    }


    /**
     * Test IINC with arbitrary increment produces addition assignment.
     */
    @Test
    void testIincArbitraryIncrement() {
        MethodNode method = buildStaticMethod("iincArb", "(I)V");
        method.maxLocals = 1;
        method.visitIincInsn(0, 5);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IExpressionStatement exprStmt = findFirst(result, IExpressionStatement.class);
        assertNotNull(exprStmt);
        assertTrue(exprStmt.getExpression() instanceof IAssignmentExpression);
    }


    /**
     * Test variable store creates a declaration for new variable.
     */
    @Test
    void testVariableStore() {
        // static void test() { int x = 42; }
        MethodNode method = buildStaticMethod("varStore", DESC_VOID);
        method.maxLocals = 1;
        method.visitIntInsn(Opcodes.BIPUSH, 42);
        method.visitVarInsn(Opcodes.ISTORE, 0);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        IVariableDeclarationStatement declStmt = findFirst(result, IVariableDeclarationStatement.class);
        assertNotNull(declStmt, "Expected variable declaration");
        assertNotNull(declStmt.getInitializer());
    }


    /**
     * Test empty method produces only a return statement.
     */
    @Test
    void testEmptyMethod() {
        MethodNode method = buildStaticMethod("empty", DESC_VOID);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(0, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof IReturnStatement);
    }


    /**
     * Test translation of a simple loop (while with GOTO back-edge).
     */
    @Test
    void testSimpleLoop() {
        // static void test() { int i = 0; while(true) { i++; } }
        MethodNode method = buildStaticMethod("loop", DESC_VOID);
        method.maxLocals = 1;
        Label loopStart = new Label();

        method.visitInsn(Opcodes.ICONST_0);
        method.visitVarInsn(Opcodes.ISTORE, 0);
        method.visitLabel(loopStart);
        method.visitIincInsn(0, 1);
        method.visitJumpInsn(Opcodes.GOTO, loopStart);
        method.visitMaxs(1, 1);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        IWhileStatement whileStmt = findFirst(result, IWhileStatement.class);
        assertNotNull(whileStmt, "Expected a while loop from back-edge GOTO");
    }


    /**
     * Test translation using a real JDK class (Integer.intValue).
     *
     * @throws IOException if loading fails
     */
    @Test
    void testRealClassIntValue() throws IOException {
        ClassNode classNode = loadClassNode("java/lang/Integer");
        MethodNode intValueMethod = null;
        for (MethodNode method : classNode.methods) {
            if ("intValue".equals(method.name)) {
                intValueMethod = method;
                break;
            }
        }
        assertNotNull(intValueMethod, "Could not find intValue method");

        List<IStatement> result = translator.translate(intValueMethod, "java/lang/Integer");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    /**
     * Test translation using a real JDK class (String.length).
     *
     * @throws IOException if loading fails
     */
    @Test
    void testRealClassStringLength() throws IOException {
        ClassNode classNode = loadClassNode(STRING_CLASS);
        MethodNode lengthMethod = null;
        for (MethodNode method : classNode.methods) {
            if ("length".equals(method.name)) {
                lengthMethod = method;
                break;
            }
        }
        assertNotNull(lengthMethod, "Could not find length method");

        List<IStatement> result = translator.translate(lengthMethod, STRING_CLASS);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    /**
     * Test that maxLocals exceeding limit is handled gracefully.
     */
    @Test
    void testMaxLocalsExceedsLimit() {
        MethodNode method = buildStaticMethod("hugeLocals", DESC_VOID);
        method.maxLocals = 2000;
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(0, 2000);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    /**
     * Test that setMaxTranslationTimeSeconds can be set to a positive value and zero.
     */
    @Test
    void testSetMaxTranslationTimeSeconds() {
        IBytecodeTranslator freshTranslator = BytecodeTranslatorFactory.getInstance().getBytecodeTranslator();

        // Setting a positive limit should not throw
        freshTranslator.setMaxTranslationTimeSeconds(10);

        // Translation should still work normally with a generous limit
        MethodNode method = buildStaticIntMethod("timeLimited", DESC_INT);
        method.visitInsn(Opcodes.ICONST_5);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = freshTranslator.translate(method, TEST_CLASS);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // Setting zero should disable the limit without error
        freshTranslator.setMaxTranslationTimeSeconds(0);

        result = freshTranslator.translate(method, TEST_CLASS);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    /**
     * Test that a negative value for setMaxTranslationTimeSeconds disables the limit.
     */
    @Test
    void testSetMaxTranslationTimeSecondsNegative() {
        IBytecodeTranslator freshTranslator = BytecodeTranslatorFactory.getInstance().getBytecodeTranslator();

        // Setting a negative value should disable the limit
        freshTranslator.setMaxTranslationTimeSeconds(-5);

        MethodNode method = buildStaticIntMethod("noLimit", DESC_INT);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 0);

        List<IStatement> result = freshTranslator.translate(method, TEST_CLASS);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    /**
     * Test stack underflow produces placeholder instead of crash.
     */
    @Test
    void testStackUnderflow() {
        MethodNode method = buildStaticIntMethod("underflow", DESC_INT);
        // Pop from empty stack via IADD (needs 2 values, we have 0)
        method.visitInsn(Opcodes.IADD);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 0);

        List<IStatement> result = translator.translate(method, TEST_CLASS);

        // Should not crash; should produce a return with placeholder expression
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    /**
     * Build a static method node with the given name and descriptor, returning int.
     *
     * @param name the method name
     * @param desc the method descriptor
     * @return the method node
     */
    private MethodNode buildStaticIntMethod(String name, String desc) {
        return new MethodNode(Opcodes.ACC_STATIC, name, desc, null, null);
    }


    /**
     * Build a static method node with the given name and descriptor.
     *
     * @param name the method name
     * @param desc the method descriptor
     * @return the method node
     */
    private MethodNode buildStaticMethod(String name, String desc) {
        return new MethodNode(Opcodes.ACC_STATIC, name, desc, null, null);
    }


    /**
     * Find the first statement of the given type in the list.
     *
     * @param <T>   the statement type
     * @param statements the statement list
     * @param clazz the class to find
     * @return the first matching statement, or null
     */
    @SuppressWarnings("unchecked")
    private <T extends IStatement> T findFirst(List<IStatement> statements, Class<T> clazz) {
        for (IStatement stmt : statements) {
            if (clazz.isInstance(stmt)) {
                return (T) stmt;
            }
        }
        return null;
    }


    /**
     * Load a ClassNode from the classpath.
     *
     * @param internalName the class internal name
     * @return the class node
     * @throws IOException if loading fails
     */
    private ClassNode loadClassNode(String internalName) throws IOException {
        String resourcePath = internalName + ".class";
        try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Class not found: " + internalName);
            }
            ClassReader reader = new ClassReader(inputStream);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);
            return classNode;
        }
    }
}
