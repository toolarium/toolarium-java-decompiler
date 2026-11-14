/*
 * LambdaBodyReconstructorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reconstructor.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ILambdaExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodReferenceExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IExpressionStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IReturnStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.classfile.ClassfileModelFactory;
import com.github.toolarium.java.decompiler.model.classfile.IClassFileMetadata;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.model.classfile.IStructMethod;
import com.github.toolarium.java.decompiler.model.common.CommonModelFactory;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IConstantPool;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import com.github.toolarium.java.decompiler.reconstructor.IReconstructor;
import com.github.toolarium.java.decompiler.reconstructor.ReconstructorFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;


/**
 * Tests for the {@link LambdaBodyReconstructor}.
 *
 * @author patrick
 */
public class LambdaBodyReconstructorTest {
    private static final String OBJECT_TYPE = "java/lang/Object";
    private static final String LAMBDA_METHOD = "lambda$main$0";
    private static final String TEST_CLASS = "TestClass";
    private static final String MAIN_METHOD = "main";
    private static final String ARG0 = "arg0";
    private final DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();
    private final ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();
    private final StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
    private final TypeModelFactory typeFactory = TypeModelFactory.getInstance();
    private final CommonModelFactory commonFactory = CommonModelFactory.getInstance();
    private final ClassfileModelFactory classFactory = ClassfileModelFactory.getInstance();


    /**
     * Test that the reconstructor is registered in the factory.
     */
    @Test
    void testReconstructorRegistered() {
        IReconstructor recon = ReconstructorFactory.getInstance()
                .getReconstructor("LambdaBodyReconstructor");
        assertNotNull(recon);
        assertEquals("LambdaBodyReconstructor", recon.getName());
    }


    /**
     * Test that lambda body reconstructor runs before synthetic member reconstructor.
     */
    @Test
    void testLambdaRunsBeforeSynthetic() {
        List<IReconstructor> reconstructors = ReconstructorFactory.getInstance().getReconstructors();
        int lambdaIndex = -1;
        int syntheticIndex = -1;
        for (int i = 0; i < reconstructors.size(); i++) {
            if (reconstructors.get(i).getName().equals("LambdaBodyReconstructor")) {
                lambdaIndex = i;
            }
            if (reconstructors.get(i).getName().equals("SyntheticMemberReconstructor")) {
                syntheticIndex = i;
            }
        }
        assertTrue(lambdaIndex >= 0, "LambdaBodyReconstructor should be registered");
        assertTrue(syntheticIndex >= 0, "SyntheticMemberReconstructor should be registered");
        assertTrue(lambdaIndex < syntheticIndex, "Lambda must run before Synthetic");
    }


    /**
     * Test inlining a lambda with a single return expression.
     * The lambda$ method body is: {@code return arg0 + arg1;}
     */
    @Test
    void testInlineReturnExpression() {
        IType intType = typeFactory.getPrimitiveType('I');
        IType objectType = typeFactory.getObjectType(OBJECT_TYPE);
        IAccessFlags publicFlags = commonFactory.getAccessFlags(Opcodes.ACC_PUBLIC);
        IAccessFlags privateStaticSynth = commonFactory.getAccessFlags(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC);

        IExpression addExpr = exprFactory.getBinaryExpression(
                intType,
                exprFactory.getVariableExpression(intType, ARG0, 0),
                exprFactory.getVariableExpression(intType, "arg1", 1),
                com.github.toolarium.java.decompiler.model.ast.expression.BinaryOperator.ADD);

        IStatement lambdaBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getReturnStatement(null, addExpr)));
        IMethodDeclaration lambdaMethod = declFactory.getMethodDeclaration(
                privateStaticSynth, LAMBDA_METHOD, intType,
                Collections.emptyList(), Collections.emptyList(), lambdaBody, false, Collections.emptyList());

        ILambdaExpression lambdaExpr = exprFactory.getLambdaExpression(
                objectType, "apply",
                Arrays.asList(ARG0, "arg1"),
                Arrays.asList("I", "I"),
                Collections.emptyList(), null,
                LAMBDA_METHOD, TEST_CLASS);

        IStatement mainBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(
                        stmtFactory.getExpressionStatement(null, lambdaExpr)));
        IMethodDeclaration mainMethod = declFactory.getMethodDeclaration(
                publicFlags, MAIN_METHOD, typeFactory.getPrimitiveType('V'),
                Collections.emptyList(), Collections.emptyList(), mainBody, false, Collections.emptyList());

        List<IMethodDeclaration> methods = new ArrayList<>();
        methods.add(mainMethod);
        methods.add(lambdaMethod);

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                publicFlags, TEST_CLASS, objectType, Collections.emptyList(),
                Collections.emptyList(), methods, Collections.emptyList(), Collections.emptyList(),
                null, false);

        IStructClass structClass = buildStructClass(publicFlags, privateStaticSynth);

        LambdaBodyReconstructor recon = new LambdaBodyReconstructor();
        assertTrue(recon.applies(structClass));

        IClassDeclaration result = recon.reconstruct(classDecl, structClass);
        assertNotNull(result);
        assertEquals(1, result.getMethods().size(), "Lambda method should be removed");

        IMethodDeclaration resultMain = result.getMethods().get(0);
        assertEquals(MAIN_METHOD, resultMain.getName());

        IExpressionStatement exprStmt = (IExpressionStatement) ((com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement) resultMain.getBody()).getStatements().get(0);
        ILambdaExpression resultLambda = (ILambdaExpression) exprStmt.getExpression();
        assertNotNull(resultLambda.getBody(), "Lambda body should be inlined");
    }


    /**
     * Test inlining a lambda with a single void expression statement.
     * The lambda$ method body is a single print call.
     */
    @Test
    void testInlineVoidExpression() {
        IType voidType = typeFactory.getPrimitiveType('V');
        IType objectType = typeFactory.getObjectType(OBJECT_TYPE);
        IAccessFlags publicFlags = commonFactory.getAccessFlags(Opcodes.ACC_PUBLIC);
        IAccessFlags privateStaticSynth = commonFactory.getAccessFlags(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC);

        IExpression printCall = exprFactory.getMethodCallExpression(
                voidType, null, "println", "java/io/PrintStream",
                Collections.singletonList(exprFactory.getVariableExpression(objectType, ARG0, 0)));

        IStatement lambdaBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getExpressionStatement(null, printCall)));
        IMethodDeclaration lambdaMethod = declFactory.getMethodDeclaration(
                privateStaticSynth, LAMBDA_METHOD, voidType,
                Collections.emptyList(), Collections.emptyList(), lambdaBody, false, Collections.emptyList());

        ILambdaExpression lambdaExpr = exprFactory.getLambdaExpression(
                objectType, "accept",
                Collections.singletonList(ARG0),
                Collections.singletonList("Ljava/lang/Object;"),
                Collections.emptyList(), null,
                LAMBDA_METHOD, TEST_CLASS);

        IStatement mainBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(
                        stmtFactory.getExpressionStatement(null, lambdaExpr)));
        IMethodDeclaration mainMethod = declFactory.getMethodDeclaration(
                publicFlags, MAIN_METHOD, voidType,
                Collections.emptyList(), Collections.emptyList(), mainBody, false, Collections.emptyList());

        List<IMethodDeclaration> methods = new ArrayList<>();
        methods.add(mainMethod);
        methods.add(lambdaMethod);

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                publicFlags, TEST_CLASS, objectType, Collections.emptyList(),
                Collections.emptyList(), methods, Collections.emptyList(), Collections.emptyList(),
                null, false);

        IStructClass structClass = buildStructClass(publicFlags, privateStaticSynth);

        LambdaBodyReconstructor recon = new LambdaBodyReconstructor();
        IClassDeclaration result = recon.reconstruct(classDecl, structClass);

        assertEquals(1, result.getMethods().size());
        IMethodDeclaration resultMain = result.getMethods().get(0);
        IExpressionStatement exprStmt = (IExpressionStatement) ((com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement) resultMain.getBody()).getStatements().get(0);
        ILambdaExpression resultLambda = (ILambdaExpression) exprStmt.getExpression();
        assertNotNull(resultLambda.getBody(), "Void lambda body should be inlined");
    }


    /**
     * Test that method references are not affected by lambda body inlining.
     */
    @Test
    void testMethodReferenceNotAffected() {
        IType objectType = typeFactory.getObjectType(OBJECT_TYPE);
        IType voidType = typeFactory.getPrimitiveType('V');
        IAccessFlags publicFlags = commonFactory.getAccessFlags(Opcodes.ACC_PUBLIC);

        IMethodReferenceExpression methodRef = exprFactory.getMethodReferenceExpression(
                objectType, null, "java/io/PrintStream", "println", false);

        IStatement mainBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(
                        stmtFactory.getExpressionStatement(null, methodRef)));
        IMethodDeclaration mainMethod = declFactory.getMethodDeclaration(
                publicFlags, MAIN_METHOD, voidType,
                Collections.emptyList(), Collections.emptyList(), mainBody, false, Collections.emptyList());

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                publicFlags, TEST_CLASS, objectType, Collections.emptyList(),
                Collections.emptyList(), Collections.singletonList(mainMethod),
                Collections.emptyList(), Collections.emptyList(),
                null, false);

        IConstantPool emptyPool = commonFactory.getConstantPool(new Object[0]);
        IClassFileMetadata metadata = classFactory.getClassFileMetadata(
                52, 0, null, null, Collections.emptyList(), Collections.emptyList(), null);
        IStructClass structClass = classFactory.getStructClass(
                metadata, emptyPool, publicFlags, TEST_CLASS, OBJECT_TYPE,
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        LambdaBodyReconstructor recon = new LambdaBodyReconstructor();
        IClassDeclaration result = recon.reconstruct(classDecl, structClass);

        assertEquals(1, result.getMethods().size());
        IExpressionStatement exprStmt = (IExpressionStatement) ((com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement) result.getMethods().get(0).getBody()).getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof IMethodReferenceExpression);
    }


    /**
     * Test that a lambda with a multi-statement body is not inlined.
     */
    @Test
    void testMultiStatementBodyNotInlined() {
        IType intType = typeFactory.getPrimitiveType('I');
        IType voidType = typeFactory.getPrimitiveType('V');
        IType objectType = typeFactory.getObjectType(OBJECT_TYPE);
        IAccessFlags publicFlags = commonFactory.getAccessFlags(Opcodes.ACC_PUBLIC);
        IAccessFlags privateStaticSynth = commonFactory.getAccessFlags(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC);

        List<IStatement> bodyStatements = new ArrayList<>();
        bodyStatements.add(stmtFactory.getExpressionStatement(null,
                exprFactory.getMethodCallExpression(voidType, null, "println",
                        "java/io/PrintStream", Collections.emptyList())));
        bodyStatements.add(stmtFactory.getReturnStatement(null,
                exprFactory.getConstantExpression(intType, 42)));

        IStatement lambdaBody = stmtFactory.getBlockStatement(null, bodyStatements);
        IMethodDeclaration lambdaMethod = declFactory.getMethodDeclaration(
                privateStaticSynth, LAMBDA_METHOD, intType,
                Collections.emptyList(), Collections.emptyList(), lambdaBody, false, Collections.emptyList());

        ILambdaExpression lambdaExpr = exprFactory.getLambdaExpression(
                objectType, "apply",
                Collections.singletonList(ARG0),
                Collections.singletonList("I"),
                Collections.emptyList(), null,
                LAMBDA_METHOD, TEST_CLASS);

        IStatement mainBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(
                        stmtFactory.getExpressionStatement(null, lambdaExpr)));
        IMethodDeclaration mainMethod = declFactory.getMethodDeclaration(
                publicFlags, MAIN_METHOD, voidType,
                Collections.emptyList(), Collections.emptyList(), mainBody, false, Collections.emptyList());

        List<IMethodDeclaration> methods = new ArrayList<>();
        methods.add(mainMethod);
        methods.add(lambdaMethod);

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                publicFlags, TEST_CLASS, objectType, Collections.emptyList(),
                Collections.emptyList(), methods, Collections.emptyList(), Collections.emptyList(),
                null, false);

        IStructClass structClass = buildStructClass(publicFlags, privateStaticSynth);

        LambdaBodyReconstructor recon = new LambdaBodyReconstructor();
        IClassDeclaration result = recon.reconstruct(classDecl, structClass);

        assertEquals(2, result.getMethods().size(), "Non-inlined lambda method should be kept");
        IExpressionStatement exprStmt = (IExpressionStatement) ((com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement) result.getMethods().get(0).getBody()).getStatements().get(0);
        ILambdaExpression resultLambda = (ILambdaExpression) exprStmt.getExpression();
        assertNull(resultLambda.getBody(), "Multi-statement body should not be inlined");
    }


    /**
     * Test that a class with no lambda methods is returned unchanged.
     */
    @Test
    void testNoLambdaMethodsUnchanged() {
        IType objectType = typeFactory.getObjectType(OBJECT_TYPE);
        IType voidType = typeFactory.getPrimitiveType('V');
        IAccessFlags publicFlags = commonFactory.getAccessFlags(Opcodes.ACC_PUBLIC);

        IMethodDeclaration mainMethod = declFactory.getMethodDeclaration(
                publicFlags, MAIN_METHOD, voidType,
                Collections.emptyList(), Collections.emptyList(),
                stmtFactory.getBlockStatement(null, Collections.emptyList()), false, Collections.emptyList());

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                publicFlags, TEST_CLASS, objectType, Collections.emptyList(),
                Collections.emptyList(), Collections.singletonList(mainMethod),
                Collections.emptyList(), Collections.emptyList(),
                null, false);

        IConstantPool emptyPool = commonFactory.getConstantPool(new Object[0]);
        IClassFileMetadata metadata = classFactory.getClassFileMetadata(
                52, 0, null, null, Collections.emptyList(), Collections.emptyList(), null);
        IStructClass structClass = classFactory.getStructClass(
                metadata, emptyPool, publicFlags, TEST_CLASS, OBJECT_TYPE,
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        LambdaBodyReconstructor recon = new LambdaBodyReconstructor();
        IClassDeclaration result = recon.reconstruct(classDecl, structClass);

        assertEquals(classDecl, result);
    }


    /**
     * Test inlining a lambda nested in a return statement.
     */
    @Test
    void testInlineLambdaInReturnStatement() {
        IType objectType = typeFactory.getObjectType(OBJECT_TYPE);
        IType intType = typeFactory.getPrimitiveType('I');
        IAccessFlags publicFlags = commonFactory.getAccessFlags(Opcodes.ACC_PUBLIC);
        IAccessFlags privateStaticSynth = commonFactory.getAccessFlags(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC);

        IExpression bodyExpr = exprFactory.getVariableExpression(intType, ARG0, 0);
        IStatement lambdaBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getReturnStatement(null, bodyExpr)));
        IMethodDeclaration lambdaMethod = declFactory.getMethodDeclaration(
                privateStaticSynth, LAMBDA_METHOD, intType,
                Collections.emptyList(), Collections.emptyList(), lambdaBody, false, Collections.emptyList());

        ILambdaExpression lambdaExpr = exprFactory.getLambdaExpression(
                objectType, "apply",
                Collections.singletonList(ARG0),
                Collections.singletonList("I"),
                Collections.emptyList(), null,
                LAMBDA_METHOD, TEST_CLASS);

        IStatement mainBody = stmtFactory.getBlockStatement(null,
                Collections.singletonList(stmtFactory.getReturnStatement(null, lambdaExpr)));
        IMethodDeclaration mainMethod = declFactory.getMethodDeclaration(
                publicFlags, "getMapper", objectType,
                Collections.emptyList(), Collections.emptyList(), mainBody, false, Collections.emptyList());

        List<IMethodDeclaration> methods = new ArrayList<>();
        methods.add(mainMethod);
        methods.add(lambdaMethod);

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                publicFlags, TEST_CLASS, objectType, Collections.emptyList(),
                Collections.emptyList(), methods, Collections.emptyList(), Collections.emptyList(),
                null, false);

        IStructClass structClass = buildStructClass(publicFlags, privateStaticSynth);

        LambdaBodyReconstructor recon = new LambdaBodyReconstructor();
        IClassDeclaration result = recon.reconstruct(classDecl, structClass);

        assertEquals(1, result.getMethods().size());
        IReturnStatement retStmt = (IReturnStatement) ((com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement) result.getMethods().get(0).getBody()).getStatements().get(0);
        ILambdaExpression resultLambda = (ILambdaExpression) retStmt.getValue();
        assertNotNull(resultLambda.getBody(), "Lambda in return should be inlined");
    }


    /**
     * Build a struct class with a lambda$ method for testing.
     *
     * @param publicFlags         the public access flags
     * @param privateStaticSynth  the private static synthetic access flags
     * @return the struct class
     */
    private IStructClass buildStructClass(IAccessFlags publicFlags, IAccessFlags privateStaticSynth) {
        IConstantPool emptyPool = commonFactory.getConstantPool(new Object[0]);
        IClassFileMetadata metadata = classFactory.getClassFileMetadata(
                52, 0, null, null, Collections.emptyList(), Collections.emptyList(), null);
        IStructMethod structLambda = classFactory.getStructMethod(
                LAMBDA_METHOD, "()I", privateStaticSynth,
                Collections.emptyList(), Collections.emptyList(),
                null, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), null);
        return classFactory.getStructClass(
                metadata, emptyPool, publicFlags, TEST_CLASS, OBJECT_TYPE,
                Collections.emptyList(), Collections.emptyList(),
                Collections.singletonList(structLambda),
                Collections.emptyList(), Collections.emptyList());
    }
}
