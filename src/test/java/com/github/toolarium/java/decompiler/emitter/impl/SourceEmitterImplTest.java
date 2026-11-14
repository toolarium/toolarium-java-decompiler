/*
 * SourceEmitterImplTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.emitter.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.controlflow.ControlFlowBuilderFactory;
import com.github.toolarium.java.decompiler.controlflow.IControlFlowBuilder;
import com.github.toolarium.java.decompiler.emitter.ISourceEmitter;
import com.github.toolarium.java.decompiler.emitter.SourceEmitterFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IFieldDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.BinaryOperator;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.UnaryOperator;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.common.CommonModelFactory;
import com.github.toolarium.java.decompiler.model.controlflow.IControlFlowGraph;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import com.github.toolarium.java.decompiler.structurer.StructurerFactory;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;


/**
 * Tests for {@link SourceEmitterImpl}.
 *
 * @author patrick
 */
public class SourceEmitterImplTest {
    private static final String NL = "\n";
    private static final String STRING_TYPE = "java/lang/String";
    private final ISourceEmitter emitter = SourceEmitterFactory.getInstance().getSourceEmitter();
    private final ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();
    private final StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
    private final TypeModelFactory typeFactory = TypeModelFactory.getInstance();


    /**
     * Test emitting a return statement.
     */
    @Test
    void testReturnStatement() {
        IStatement stmt = stmtFactory.getReturnStatement(null,
                exprFactory.getConstantExpression(typeFactory.getPrimitiveType('I'), 42));
        String source = emitter.emitStatement(stmt);
        assertEquals("return 42;" + NL, source);
    }


    /**
     * Test emitting a void return.
     */
    @Test
    void testVoidReturn() {
        IStatement stmt = stmtFactory.getReturnStatement(null, null);
        String source = emitter.emitStatement(stmt);
        assertEquals("return;" + NL, source);
    }


    /**
     * Test emitting a string constant.
     */
    @Test
    void testStringConstant() {
        IExpression expr = exprFactory.getConstantExpression(
                typeFactory.getObjectType(STRING_TYPE), "hello \"world\"");
        IStatement stmt = stmtFactory.getExpressionStatement(null, expr);
        String source = emitter.emitStatement(stmt);
        assertTrue(source.contains("\"hello \\\"world\\\"\""));
    }


    /**
     * Test emitting a binary expression.
     */
    @Test
    void testBinaryExpression() {
        IType intType = typeFactory.getPrimitiveType('I');
        IExpression left = exprFactory.getVariableExpression(intType, "a", 1);
        IExpression right = exprFactory.getConstantExpression(intType, 5);
        IExpression binary = exprFactory.getBinaryExpression(intType, left, right, BinaryOperator.ADD);
        IStatement stmt = stmtFactory.getExpressionStatement(null, binary);
        String source = emitter.emitStatement(stmt);
        assertEquals("a + 5;" + NL, source);
    }


    /**
     * Test emitting a unary expression.
     */
    @Test
    void testUnaryExpression() {
        IType intType = typeFactory.getPrimitiveType('I');
        IExpression operand = exprFactory.getVariableExpression(intType, "i", 1);
        IExpression unary = exprFactory.getUnaryExpression(intType, operand, UnaryOperator.PRE_INC);
        IStatement stmt = stmtFactory.getExpressionStatement(null, unary);
        String source = emitter.emitStatement(stmt);
        assertEquals("++i;" + NL, source);
    }


    /**
     * Test emitting a method call.
     */
    @Test
    void testMethodCall() {
        IType stringType = typeFactory.getObjectType(STRING_TYPE);
        IExpression target = exprFactory.getVariableExpression(stringType, "str", 1);
        IExpression call = exprFactory.getMethodCallExpression(
                typeFactory.getPrimitiveType('I'), target, "length",
                STRING_TYPE, Collections.emptyList());
        IStatement stmt = stmtFactory.getExpressionStatement(null, call);
        String source = emitter.emitStatement(stmt);
        assertEquals("str.length();" + NL, source);
    }


    /**
     * Test emitting an if-else statement.
     */
    @Test
    void testIfElse() {
        IType boolType = typeFactory.getPrimitiveType('Z');
        IExpression condition = exprFactory.getVariableExpression(boolType, "flag", 1);
        IStatement thenStmt = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getReturnStatement(null,
                        exprFactory.getConstantExpression(typeFactory.getPrimitiveType('I'), 1))));
        IStatement elseStmt = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getReturnStatement(null,
                        exprFactory.getConstantExpression(typeFactory.getPrimitiveType('I'), 0))));
        IStatement ifStmt = stmtFactory.getIfStatement(null, condition, thenStmt, elseStmt);
        String source = emitter.emitStatement(ifStmt);
        assertTrue(source.contains("if (flag)"));
        assertTrue(source.contains("return 1;"));
        assertTrue(source.contains("} else {"));
        assertTrue(source.contains("return 0;"));
    }


    /**
     * Test emitting a while loop.
     */
    @Test
    void testWhileLoop() {
        IType boolType = typeFactory.getPrimitiveType('Z');
        IExpression condition = exprFactory.getConstantExpression(boolType, true);
        IStatement body = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getBreakStatement(null, null)));
        IStatement whileStmt = stmtFactory.getWhileStatement(null, condition, body);
        String source = emitter.emitStatement(whileStmt);
        assertTrue(source.contains("while (true)"));
        assertTrue(source.contains("break;"));
    }


    /**
     * Test emitting a class declaration.
     */
    @Test
    void testClassDeclaration() {
        DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();
        IType intType = typeFactory.getPrimitiveType('I');
        IType stringType = typeFactory.getObjectType(STRING_TYPE);

        IFieldDeclaration field = declFactory.getFieldDeclaration(
                CommonModelFactory.getInstance().getAccessFlags(0x0002),
                intType, "count",
                exprFactory.getConstantExpression(intType, 0),
                Collections.emptyList());

        IStatement body = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getReturnStatement(null,
                        exprFactory.getVariableExpression(intType, "count", 0))));
        IMethodDeclaration method = declFactory.getMethodDeclaration(
                CommonModelFactory.getInstance().getAccessFlags(0x0001),
                "getCount", intType, Collections.emptyList(),
                Collections.emptyList(), body, false, Collections.emptyList());

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                CommonModelFactory.getInstance().getAccessFlags(0x0001),
                "MyClass", null,
                Collections.singletonList(stringType),
                Collections.singletonList(field),
                Collections.singletonList(method),
                Collections.emptyList(), Collections.emptyList(),
                null, false);

        String source = emitter.emitClass(classDecl);
        assertTrue(source.contains("public class MyClass"));
        assertTrue(source.contains("implements String"));
        assertTrue(source.contains("private int count = 0;"));
        assertTrue(source.contains("public int getCount()"));
        assertTrue(source.contains("return count;"));
    }


    /**
     * Test emitting a try-catch statement.
     */
    @Test
    void testTryCatch() {
        IType exType = typeFactory.getObjectType("java/lang/Exception");
        IStatement tryBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getExpressionStatement(null,
                        exprFactory.getMethodCallExpression(null, null,
                                "doSomething", "this", Collections.emptyList()))));

        IStatement catchBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getThrowStatement(null,
                        exprFactory.getVariableExpression(exType, "ex", 1))));

        IStatement tryCatch = stmtFactory.getTryCatchStatement(null, tryBody,
                Collections.singletonList(stmtFactory.getCatchClause(
                        Collections.singletonList(exType), "ex", catchBody)),
                null);

        String source = emitter.emitStatement(tryCatch);
        assertTrue(source.contains("try {"));
        assertTrue(source.contains("doSomething()"));
        assertTrue(source.contains("} catch (Exception ex)"));
        assertTrue(source.contains("throw ex;"));
    }


    /**
     * Test end-to-end: .class bytecode to structured source.
     */
    @Test
    void testEndToEndSimpleMethod() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "simple", "()V", null, null);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(1, 1);

        IControlFlowBuilder cfgBuilder = ControlFlowBuilderFactory.getInstance().getCfgBuilder();
        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);

        IBlockStatement structured = StructurerFactory.getInstance().getStructurer().structure(cfg);
        String source = emitter.emitStatement(structured);

        assertNotNull(source);
        assertFalse(source.isEmpty());
        assertTrue(source.contains("return;"));
    }


    /**
     * Test end-to-end: conditional method.
     */
    @Test
    void testEndToEndConditional() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "cond", "(I)V", null, null);
        Label elseLabel = new Label();
        Label endLabel = new Label();

        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitJumpInsn(Opcodes.IFEQ, elseLabel);
        method.visitInsn(Opcodes.RETURN);
        method.visitLabel(elseLabel);
        method.visitInsn(Opcodes.RETURN);
        method.visitLabel(endLabel);
        method.visitMaxs(1, 2);

        IControlFlowBuilder cfgBuilder = ControlFlowBuilderFactory.getInstance().getCfgBuilder();
        IControlFlowGraph cfg = cfgBuilder.buildCfg(method);

        IBlockStatement structured = StructurerFactory.getInstance().getStructurer().structure(cfg);
        String source = emitter.emitStatement(structured);

        assertNotNull(source);
        assertTrue(source.contains("if ("));
    }


    /**
     * Test emitting a for statement.
     */
    @Test
    void testForStatement() {
        IType intType = typeFactory.getPrimitiveType('I');
        IStatement init = stmtFactory.getVariableDeclarationStatement(
                null, intType, "i",
                exprFactory.getConstantExpression(intType, 0), false);
        IExpression condition = exprFactory.getBinaryExpression(
                typeFactory.getPrimitiveType('Z'),
                exprFactory.getVariableExpression(intType, "i", 1),
                exprFactory.getConstantExpression(intType, 10),
                BinaryOperator.LT);
        IExpression update = exprFactory.getUnaryExpression(intType,
                exprFactory.getVariableExpression(intType, "i", 1), UnaryOperator.POST_INC);
        IStatement body = stmtFactory.getBlockStatement(null, Collections.emptyList());
        IStatement forStmt = stmtFactory.getForStatement(null, init, condition, update, body);
        String source = emitter.emitStatement(forStmt);

        assertTrue(source.contains("for ("));
        assertTrue(source.contains("int i = 0"));
        assertTrue(source.contains("i < 10"));
        assertTrue(source.contains("i++"));
    }


    /**
     * Test emitting a do-while statement.
     */
    @Test
    void testDoWhileStatement() {
        IType boolType = typeFactory.getPrimitiveType('Z');
        IExpression condition = exprFactory.getVariableExpression(boolType, "running", 1);
        IStatement body = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getExpressionStatement(null,
                        exprFactory.getMethodCallExpression(null, null,
                                "process", "this", Collections.emptyList()))));
        IStatement doWhile = stmtFactory.getDoWhileStatement(null, condition, body);
        String source = emitter.emitStatement(doWhile);

        assertTrue(source.contains("do {"));
        assertTrue(source.contains("process()"));
        assertTrue(source.contains("} while (running)"));
    }


    /**
     * Test emitting a switch statement.
     */
    @Test
    void testSwitchStatement() {
        IType intType = typeFactory.getPrimitiveType('I');
        IExpression selector = exprFactory.getVariableExpression(intType, "value", 1);

        IStatement breakStmt = stmtFactory.getBreakStatement(null, null);
        com.github.toolarium.java.decompiler.model.ast.statement.ISwitchCase case1 =
                stmtFactory.getSwitchCase(
                        Collections.singletonList(exprFactory.getConstantExpression(intType, 1)),
                        false,
                        Collections.singletonList(breakStmt));
        com.github.toolarium.java.decompiler.model.ast.statement.ISwitchCase defaultCase =
                stmtFactory.getSwitchCase(Collections.emptyList(), true,
                        Collections.singletonList(breakStmt));

        IStatement switchStmt = stmtFactory.getSwitchStatement(null, selector,
                Arrays.asList(case1, defaultCase));
        String source = emitter.emitStatement(switchStmt);

        assertTrue(source.contains("switch (value)"));
        assertTrue(source.contains("case 1:"));
        assertTrue(source.contains("default:"));
        assertTrue(source.contains("break;"));
    }


    /**
     * Test emitting a throw statement.
     */
    @Test
    void testThrowStatement() {
        IType exType = typeFactory.getObjectType("java/lang/RuntimeException");
        IExpression newExpr = exprFactory.getNewExpression(exType, exType,
                Collections.singletonList(
                        exprFactory.getConstantExpression(
                                typeFactory.getObjectType(STRING_TYPE), "error")));
        IStatement throwStmt = stmtFactory.getThrowStatement(null, newExpr);
        String source = emitter.emitStatement(throwStmt);

        assertTrue(source.contains("throw new RuntimeException(\"error\")"));
    }


    /**
     * Test emitting a continue statement with a label.
     */
    @Test
    void testContinueWithLabel() {
        IStatement continueStmt = stmtFactory.getContinueStatement(null, "outer");
        String source = emitter.emitStatement(continueStmt);
        assertEquals("continue outer;" + NL, source);
    }


    /**
     * Test emitting a variable declaration statement.
     */
    @Test
    void testVariableDeclaration() {
        IType intType = typeFactory.getPrimitiveType('I');
        IStatement varDecl = stmtFactory.getVariableDeclarationStatement(
                null, intType, "count",
                exprFactory.getConstantExpression(intType, 0), false);
        String source = emitter.emitStatement(varDecl);
        assertEquals("int count = 0;" + NL, source);
    }


    /**
     * Test emitting a final variable declaration statement.
     */
    @Test
    void testFinalVariableDeclaration() {
        IType stringType = typeFactory.getObjectType(STRING_TYPE);
        IStatement varDecl = stmtFactory.getVariableDeclarationStatement(
                null, stringType, "name",
                exprFactory.getConstantExpression(stringType, "hello"), true);
        String source = emitter.emitStatement(varDecl);
        assertTrue(source.contains("final"));
        assertTrue(source.contains("String name"));
        assertTrue(source.contains("\"hello\""));
    }


    /**
     * Test emitting a synchronized statement.
     */
    @Test
    void testSynchronizedStatement() {
        IType objectType = typeFactory.getObjectType("java/lang/Object");
        IExpression lock = exprFactory.getVariableExpression(objectType, "lock", 1);
        IStatement body = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getReturnStatement(null, null)));
        IStatement syncStmt = stmtFactory.getSynchronizedStatement(null, lock, body);
        String source = emitter.emitStatement(syncStmt);

        assertTrue(source.contains("synchronized (lock)"));
        assertTrue(source.contains("return;"));
    }


    /**
     * Test emitting a try-catch-finally statement.
     */
    @Test
    void testTryCatchFinally() {
        IType exType = typeFactory.getObjectType("java/lang/Exception");
        IStatement tryBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getReturnStatement(null,
                        exprFactory.getConstantExpression(typeFactory.getPrimitiveType('I'), 1))));
        IStatement catchBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getReturnStatement(null,
                        exprFactory.getConstantExpression(typeFactory.getPrimitiveType('I'), -1))));
        IStatement finallyBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getExpressionStatement(null,
                        exprFactory.getMethodCallExpression(null, null,
                                "cleanup", "this", Collections.emptyList()))));

        IStatement tryCatch = stmtFactory.getTryCatchStatement(null, tryBody,
                Collections.singletonList(stmtFactory.getCatchClause(
                        Collections.singletonList(exType), "ex", catchBody)),
                finallyBody);
        String source = emitter.emitStatement(tryCatch);

        assertTrue(source.contains("try {"));
        assertTrue(source.contains("return 1;"));
        assertTrue(source.contains("} catch (Exception ex)"));
        assertTrue(source.contains("return -1;"));
        assertTrue(source.contains("} finally {"));
        assertTrue(source.contains("cleanup()"));
    }


    /**
     * Test emitting a for-each statement.
     */
    @Test
    void testForEachStatement() {
        IType stringType = typeFactory.getObjectType(STRING_TYPE);
        IType listType = typeFactory.getObjectType("java/util/List");
        IExpression iterable = exprFactory.getVariableExpression(listType, "items", 1);
        IStatement body = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getExpressionStatement(null,
                        exprFactory.getMethodCallExpression(null, null,
                                "process", "this",
                                Collections.singletonList(
                                        exprFactory.getVariableExpression(stringType, "item", 2))))));
        IStatement forEach = stmtFactory.getForEachStatement(null, stringType, "item", iterable, body);
        String source = emitter.emitStatement(forEach);

        assertTrue(source.contains("for (String item : items)"));
        assertTrue(source.contains("process(item)"));
    }


    /**
     * Test emitting a try-with-resources statement.
     */
    @Test
    void testTryWithResources() {
        IType streamType = typeFactory.getObjectType("java/io/InputStream");
        IType intType = typeFactory.getPrimitiveType('I');

        IExpression newStream = exprFactory.getNewExpression(
                streamType, streamType, Collections.emptyList());
        IStatement resourceDecl = stmtFactory.getVariableDeclarationStatement(
                null, streamType, "stream", newStream, false);

        IStatement tryBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getReturnStatement(null,
                        exprFactory.getConstantExpression(intType, 0))));

        IStatement twr = stmtFactory.getTryCatchStatement(
                null, Collections.singletonList(
                        (com.github.toolarium.java.decompiler.model.ast.statement.IVariableDeclarationStatement) resourceDecl),
                tryBody, Collections.emptyList(), null);
        String source = emitter.emitStatement(twr);

        assertTrue(source.contains("try (InputStream stream = new InputStream())"));
        assertTrue(source.contains("return 0;"));
        assertFalse(source.contains("finally"));
    }


    /**
     * Test emitting a lambda expression with single parameter.
     */
    @Test
    void testLambdaExpressionSingleParam() {
        IType funcType = typeFactory.getObjectType("java/util/function/Function");
        IStatement stmt = stmtFactory.getExpressionStatement(null,
                exprFactory.getLambdaExpression(funcType, "apply",
                        Arrays.asList("x"), Arrays.asList("Ljava/lang/Object;"),
                        Collections.emptyList(), null,
                        "lambda$test$0", "com/example/MyClass"));
        String source = emitter.emitStatement(stmt);

        assertTrue(source.contains("x -> "));
    }


    /**
     * Test emitting a lambda expression with multiple parameters.
     */
    @Test
    void testLambdaExpressionMultipleParams() {
        IType funcType = typeFactory.getObjectType("java/util/function/BiFunction");
        IStatement stmt = stmtFactory.getExpressionStatement(null,
                exprFactory.getLambdaExpression(funcType, "apply",
                        Arrays.asList("a", "b"), Arrays.asList("Ljava/lang/Object;", "Ljava/lang/Object;"),
                        Collections.emptyList(), null,
                        "lambda$test$0", "com/example/MyClass"));
        String source = emitter.emitStatement(stmt);

        assertTrue(source.contains("(a, b) -> "));
    }


    /**
     * Test emitting a lambda expression with body expression.
     */
    @Test
    void testLambdaExpressionWithBody() {
        IType intType = typeFactory.getPrimitiveType('I');
        IType funcType = typeFactory.getObjectType("java/util/function/IntUnaryOperator");
        IExpression body = exprFactory.getBinaryExpression(intType,
                exprFactory.getVariableExpression(intType, "x", 0),
                exprFactory.getConstantExpression(intType, 1),
                BinaryOperator.ADD);
        IStatement stmt = stmtFactory.getExpressionStatement(null,
                exprFactory.getLambdaExpression(funcType, "applyAsInt",
                        Arrays.asList("x"), Arrays.asList("I"),
                        Collections.emptyList(), body,
                        "lambda$test$0", "com/example/MyClass"));
        String source = emitter.emitStatement(stmt);

        assertTrue(source.contains("x -> x + 1"));
    }


    /**
     * Test emitting a static method reference.
     */
    @Test
    void testMethodReferenceStatic() {
        IType funcType = typeFactory.getObjectType("java/util/function/Function");
        IStatement stmt = stmtFactory.getExpressionStatement(null,
                exprFactory.getMethodReferenceExpression(
                        funcType, null, "java/lang/String", "valueOf", false));
        String source = emitter.emitStatement(stmt);

        assertTrue(source.contains("String::valueOf"));
    }


    /**
     * Test emitting an instance method reference with qualifier.
     */
    @Test
    void testMethodReferenceInstance() {
        IType funcType = typeFactory.getObjectType("java/util/function/Consumer");
        IExpression qualifier = exprFactory.getFieldAccessExpression(
                typeFactory.getObjectType("java/io/PrintStream"),
                exprFactory.getVariableExpression(typeFactory.getObjectType("java/lang/System"), "System", 0),
                "out", "java/lang/System");
        IStatement stmt = stmtFactory.getExpressionStatement(null,
                exprFactory.getMethodReferenceExpression(
                        funcType, qualifier, "java/io/PrintStream", "println", false));
        String source = emitter.emitStatement(stmt);

        assertTrue(source.contains("::println"));
    }


    /**
     * Test emitting a constructor reference.
     */
    @Test
    void testMethodReferenceConstructor() {
        IType funcType = typeFactory.getObjectType("java/util/function/Supplier");
        IStatement stmt = stmtFactory.getExpressionStatement(null,
                exprFactory.getMethodReferenceExpression(
                        funcType, null, "java/util/ArrayList", "<init>", true));
        String source = emitter.emitStatement(stmt);

        assertTrue(source.contains("ArrayList::new"));
    }


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        SourceEmitterFactory factory1 = SourceEmitterFactory.getInstance();
        SourceEmitterFactory factory2 = SourceEmitterFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }
}
