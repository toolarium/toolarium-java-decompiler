/*
 * SealedClassAndTextBlockTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.emitter.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.emitter.ISourceEmitter;
import com.github.toolarium.java.decompiler.emitter.SourceEmitterFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.common.CommonModelFactory;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;


/**
 * Tests for sealed class, non-sealed class, and text block emission.
 *
 * @author patrick
 */
public class SealedClassAndTextBlockTest {
    private final ISourceEmitter emitter = SourceEmitterFactory.getInstance().getSourceEmitter();
    private final DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();
    private final ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();
    private final StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
    private final TypeModelFactory typeFactory = TypeModelFactory.getInstance();
    private final CommonModelFactory commonFactory = CommonModelFactory.getInstance();


    /**
     * Test emitting a sealed class with permits clause.
     */
    @Test
    void testSealedClassEmission() {
        IAccessFlags publicAbstract = commonFactory.getAccessFlags(0x0001 | 0x0400);

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                publicAbstract, "Shape", null, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Arrays.asList("Circle", "Rectangle", "Triangle"), false);

        String source = emitter.emitClass(classDecl);
        assertTrue(source.contains("public sealed class Shape"));
        assertTrue(source.contains("permits Circle, Rectangle, Triangle"));
        assertFalse(source.contains("abstract"));
    }


    /**
     * Test emitting a sealed interface with permits clause.
     */
    @Test
    void testSealedInterfaceEmission() {
        IAccessFlags publicInterface = commonFactory.getAccessFlags(0x0001 | 0x0200 | 0x0400);

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                publicInterface, "Expr", null, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Arrays.asList("Literal", "BinaryOp"), false);

        String source = emitter.emitClass(classDecl);
        assertTrue(source.contains("public sealed interface Expr"));
        assertTrue(source.contains("permits Literal, BinaryOp"));
    }


    /**
     * Test emitting a non-sealed class.
     */
    @Test
    void testNonSealedClassEmission() {
        IAccessFlags publicFlags = commonFactory.getAccessFlags(0x0001);
        IType superType = typeFactory.getObjectType("com/example/Shape");

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                publicFlags, "FreeformShape", superType, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), true);

        String source = emitter.emitClass(classDecl);
        assertTrue(source.contains("public non-sealed class FreeformShape"));
        assertFalse(source.contains("permits"));
    }


    /**
     * Test that a regular class emits without sealed or non-sealed.
     */
    @Test
    void testRegularClassNoSealedKeyword() {
        IAccessFlags publicFlags = commonFactory.getAccessFlags(0x0001);

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                publicFlags, "PlainClass", null, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), false);

        String source = emitter.emitClass(classDecl);
        assertTrue(source.contains("public class PlainClass"));
        assertFalse(source.contains("sealed"));
        assertFalse(source.contains("non-sealed"));
        assertFalse(source.contains("permits"));
    }


    /**
     * Test emitting a text block for a multi-line string.
     */
    @Test
    void testTextBlockEmission() {
        IType stringType = typeFactory.getObjectType("java/lang/String");
        IStatement returnStmt = stmtFactory.getReturnStatement(null,
                exprFactory.getConstantExpression(stringType, "line1\nline2\nline3"));

        String source = emitter.emitStatement(returnStmt);
        assertTrue(source.contains("\"\"\""));
        assertTrue(source.contains("line1"));
        assertTrue(source.contains("line2"));
        assertTrue(source.contains("line3"));
        assertFalse(source.contains("\\n"));
    }


    /**
     * Test that a single-line string is not emitted as a text block.
     */
    @Test
    void testSingleLineStringNotTextBlock() {
        IType stringType = typeFactory.getObjectType("java/lang/String");
        IStatement returnStmt = stmtFactory.getReturnStatement(null,
                exprFactory.getConstantExpression(stringType, "hello world"));

        String source = emitter.emitStatement(returnStmt);
        assertTrue(source.contains("\"hello world\""));
        assertFalse(source.contains("\"\"\""));
    }


    /**
     * Test text block with special characters.
     */
    @Test
    void testTextBlockWithSpecialChars() {
        IType stringType = typeFactory.getObjectType("java/lang/String");
        IStatement returnStmt = stmtFactory.getReturnStatement(null,
                exprFactory.getConstantExpression(stringType, "{\n  \"key\": \"value\"\n}"));

        String source = emitter.emitStatement(returnStmt);
        assertTrue(source.contains("\"\"\""));
        assertTrue(source.contains("\"key\": \"value\""));
    }


    /**
     * Test sealed class with extends and implements together.
     */
    @Test
    void testSealedClassWithExtendsAndImplements() {
        IAccessFlags publicFlags = commonFactory.getAccessFlags(0x0001);
        IType superType = typeFactory.getObjectType("java/lang/Number");
        IType serializableType = typeFactory.getObjectType("java/io/Serializable");

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                publicFlags, "MyNumber", superType,
                Collections.singletonList(serializableType),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Arrays.asList("IntNumber", "FloatNumber"), false);

        String source = emitter.emitClass(classDecl);
        assertTrue(source.contains("public sealed class MyNumber"));
        assertTrue(source.contains("extends Number"));
        assertTrue(source.contains("implements Serializable"));
        assertTrue(source.contains("permits IntNumber, FloatNumber"));
    }


    /**
     * Test IClassDeclaration getPermittedSubclasses for a sealed class.
     */
    @Test
    void testSealedClassDeclarationProperties() {
        IAccessFlags publicFlags = commonFactory.getAccessFlags(0x0001);

        IClassDeclaration sealed = declFactory.getClassDeclaration(
                publicFlags, "A", null, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Arrays.asList("B", "C"), false);

        assertTrue(sealed.getPermittedSubclasses().contains("B"));
        assertTrue(sealed.getPermittedSubclasses().contains("C"));
        assertFalse(sealed.isNonSealed());
    }


    /**
     * Test IClassDeclaration isNonSealed for a non-sealed class.
     */
    @Test
    void testNonSealedClassDeclarationProperties() {
        IAccessFlags publicFlags = commonFactory.getAccessFlags(0x0001);

        IClassDeclaration nonSealed = declFactory.getClassDeclaration(
                publicFlags, "D", null, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), true);

        assertTrue(nonSealed.isNonSealed());
        assertTrue(nonSealed.getPermittedSubclasses().isEmpty());
    }


    /**
     * Test IClassDeclaration properties for a regular (non-sealed, no permits) class.
     */
    @Test
    void testRegularClassDeclarationProperties() {
        IAccessFlags publicFlags = commonFactory.getAccessFlags(0x0001);

        IClassDeclaration regular = declFactory.getClassDeclaration(
                publicFlags, "E", null, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), false);

        assertFalse(regular.isNonSealed());
        assertTrue(regular.getPermittedSubclasses().isEmpty());
    }
}
