/*
 * DeclarationModelFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.declaration.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IFieldDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IParameterDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.common.CommonModelFactory;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link DeclarationModelFactory} and its model implementations.
 *
 * @author patrick
 */
public class DeclarationModelFactoryTest {
    private static final String MY_CLASS = "MyClass";
    private static final String MY_FIELD = "myField";
    private static final String MY_METHOD = "myMethod";
    private static final String MY_PARAM = "myParam";
    private final DeclarationModelFactory factory = DeclarationModelFactory.getInstance();
    private final TypeModelFactory typeFactory = TypeModelFactory.getInstance();
    private final CommonModelFactory commonFactory = CommonModelFactory.getInstance();
    private final ExpressionModelFactory expressionFactory = ExpressionModelFactory.getInstance();
    private final StatementModelFactory statementFactory = StatementModelFactory.getInstance();


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        DeclarationModelFactory factory1 = DeclarationModelFactory.getInstance();
        DeclarationModelFactory factory2 = DeclarationModelFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Test parameter declaration creation and getters.
     */
    @Test
    void testParameterDeclaration() {
        IType intType = typeFactory.getPrimitiveType('I');
        IParameterDeclaration param = factory.getParameterDeclaration(intType, MY_PARAM, false, Collections.emptyList());

        assertEquals(intType, param.getParameterType());
        assertEquals(MY_PARAM, param.getName());
        assertFalse(param.isFinal());
    }


    /**
     * Test parameter declaration with final modifier.
     */
    @Test
    void testParameterDeclarationFinal() {
        IType stringType = typeFactory.getObjectType("java/lang/String");
        IParameterDeclaration param = factory.getParameterDeclaration(stringType, "name", true, Collections.emptyList());

        assertTrue(param.isFinal());
        assertEquals(stringType, param.getParameterType());
    }


    /**
     * Test parameter declaration equals and hashCode.
     */
    @Test
    void testParameterDeclarationEqualsHashCode() {
        IType intType = typeFactory.getPrimitiveType('I');
        IParameterDeclaration param1 = factory.getParameterDeclaration(intType, MY_PARAM, false, Collections.emptyList());
        IParameterDeclaration param2 = factory.getParameterDeclaration(intType, MY_PARAM, false, Collections.emptyList());
        IParameterDeclaration param3 = factory.getParameterDeclaration(intType, "otherParam", false, Collections.emptyList());

        assertEquals(param1, param2);
        assertEquals(param1.hashCode(), param2.hashCode());
        assertNotEquals(param1, param3);
        assertNotEquals(param1, null);
        assertEquals(param1, param1);
    }


    /**
     * Test parameter declaration toString.
     */
    @Test
    void testParameterDeclarationToString() {
        IType intType = typeFactory.getPrimitiveType('I');
        IParameterDeclaration param = factory.getParameterDeclaration(intType, MY_PARAM, false, Collections.emptyList());

        assertTrue(param.toString().contains(MY_PARAM));
    }


    /**
     * Test field declaration creation and getters.
     */
    @Test
    void testFieldDeclaration() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0002);
        IType intType = typeFactory.getPrimitiveType('I');
        IFieldDeclaration field = factory.getFieldDeclaration(flags, intType, MY_FIELD, null, Collections.emptyList());

        assertEquals(flags, field.getAccessFlags());
        assertEquals(intType, field.getFieldType());
        assertEquals(MY_FIELD, field.getName());
        assertNull(field.getInitializer());
    }


    /**
     * Test field declaration with initializer.
     */
    @Test
    void testFieldDeclarationWithInitializer() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0002);
        IType intType = typeFactory.getPrimitiveType('I');
        IExpression initializer = expressionFactory.getConstantExpression(intType, 42);
        IFieldDeclaration field = factory.getFieldDeclaration(flags, intType, MY_FIELD, initializer, Collections.emptyList());

        assertNotNull(field.getInitializer());
        assertEquals(initializer, field.getInitializer());
    }


    /**
     * Test field declaration equals and hashCode.
     */
    @Test
    void testFieldDeclarationEqualsHashCode() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0002);
        IType intType = typeFactory.getPrimitiveType('I');
        IFieldDeclaration field1 = factory.getFieldDeclaration(flags, intType, MY_FIELD, null, Collections.emptyList());
        IFieldDeclaration field2 = factory.getFieldDeclaration(flags, intType, MY_FIELD, null, Collections.emptyList());
        IFieldDeclaration field3 = factory.getFieldDeclaration(flags, intType, "otherField", null, Collections.emptyList());

        assertEquals(field1, field2);
        assertEquals(field1.hashCode(), field2.hashCode());
        assertNotEquals(field1, field3);
    }


    /**
     * Test field declaration toString.
     */
    @Test
    void testFieldDeclarationToString() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0002);
        IType intType = typeFactory.getPrimitiveType('I');
        IFieldDeclaration field = factory.getFieldDeclaration(flags, intType, MY_FIELD, null, Collections.emptyList());

        assertTrue(field.toString().contains(MY_FIELD));
    }


    /**
     * Test method declaration creation and getters.
     */
    @Test
    void testMethodDeclaration() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0001);
        IType returnType = typeFactory.getPrimitiveType('V');
        IStatement body = statementFactory.getReturnStatement(null, null);
        IMethodDeclaration method = factory.getMethodDeclaration(flags, MY_METHOD, returnType,
                Collections.emptyList(), Collections.emptyList(), body, false, Collections.emptyList());

        assertEquals(flags, method.getAccessFlags());
        assertEquals(MY_METHOD, method.getName());
        assertEquals(returnType, method.getReturnType());
        assertTrue(method.getParameters().isEmpty());
        assertTrue(method.getThrownTypes().isEmpty());
        assertNotNull(method.getBody());
        assertFalse(method.isConstructor());
    }


    /**
     * Test constructor declaration.
     */
    @Test
    void testConstructorDeclaration() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0001);
        IType intType = typeFactory.getPrimitiveType('I');
        IParameterDeclaration param = factory.getParameterDeclaration(intType, "value", false, Collections.emptyList());
        IStatement body = statementFactory.getReturnStatement(null, null);
        IMethodDeclaration constructor = factory.getMethodDeclaration(flags, "<init>", null,
                Collections.singletonList(param), Collections.emptyList(), body, true, Collections.emptyList());

        assertTrue(constructor.isConstructor());
        assertNull(constructor.getReturnType());
        assertEquals(1, constructor.getParameters().size());
    }


    /**
     * Test method declaration with thrown types.
     */
    @Test
    void testMethodDeclarationWithThrownTypes() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0001);
        IType returnType = typeFactory.getPrimitiveType('V');
        IType ioExceptionType = typeFactory.getObjectType("java/io/IOException");
        IMethodDeclaration method = factory.getMethodDeclaration(flags, MY_METHOD, returnType,
                Collections.emptyList(), Collections.singletonList(ioExceptionType), null, false, Collections.emptyList());

        assertEquals(1, method.getThrownTypes().size());
        assertEquals(ioExceptionType, method.getThrownTypes().get(0));
        assertNull(method.getBody());
    }


    /**
     * Test method declaration equals and hashCode.
     */
    @Test
    void testMethodDeclarationEqualsHashCode() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0001);
        IType returnType = typeFactory.getPrimitiveType('V');
        IMethodDeclaration method1 = factory.getMethodDeclaration(flags, MY_METHOD, returnType,
                Collections.emptyList(), Collections.emptyList(), null, false, Collections.emptyList());
        IMethodDeclaration method2 = factory.getMethodDeclaration(flags, MY_METHOD, returnType,
                Collections.emptyList(), Collections.emptyList(), null, false, Collections.emptyList());
        IMethodDeclaration method3 = factory.getMethodDeclaration(flags, "otherMethod", returnType,
                Collections.emptyList(), Collections.emptyList(), null, false, Collections.emptyList());

        assertEquals(method1, method2);
        assertEquals(method1.hashCode(), method2.hashCode());
        assertNotEquals(method1, method3);
    }


    /**
     * Test method declaration toString.
     */
    @Test
    void testMethodDeclarationToString() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0001);
        IType returnType = typeFactory.getPrimitiveType('V');
        IMethodDeclaration method = factory.getMethodDeclaration(flags, MY_METHOD, returnType,
                Collections.emptyList(), Collections.emptyList(), null, false, Collections.emptyList());

        assertTrue(method.toString().contains(MY_METHOD));
    }


    /**
     * Test class declaration creation and getters.
     */
    @Test
    void testClassDeclaration() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0021);
        IType superType = typeFactory.getObjectType("java/lang/Object");
        IClassDeclaration classDecl = factory.getClassDeclaration(flags, MY_CLASS, superType,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                null, false);

        assertEquals(flags, classDecl.getAccessFlags());
        assertEquals(MY_CLASS, classDecl.getName());
        assertEquals(superType, classDecl.getSuperType());
        assertTrue(classDecl.getInterfaceTypes().isEmpty());
        assertTrue(classDecl.getFields().isEmpty());
        assertTrue(classDecl.getMethods().isEmpty());
        assertTrue(classDecl.getInnerClasses().isEmpty());
    }


    /**
     * Test class declaration with interfaces.
     */
    @Test
    void testClassDeclarationWithInterfaces() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0021);
        IType superType = typeFactory.getObjectType("java/lang/Object");
        IType serializableType = typeFactory.getObjectType("java/io/Serializable");
        IType comparableType = typeFactory.getObjectType("java/lang/Comparable");
        List<IType> interfaces = List.of(serializableType, comparableType);
        IClassDeclaration classDecl = factory.getClassDeclaration(flags, MY_CLASS, superType,
                interfaces, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                null, false);

        assertEquals(2, classDecl.getInterfaceTypes().size());
    }


    /**
     * Test class declaration with fields and methods.
     */
    @Test
    void testClassDeclarationWithMembers() {
        IAccessFlags classFlags = commonFactory.getAccessFlags(0x0021);
        IAccessFlags fieldFlags = commonFactory.getAccessFlags(0x0002);
        IAccessFlags methodFlags = commonFactory.getAccessFlags(0x0001);
        IType superType = typeFactory.getObjectType("java/lang/Object");
        IType intType = typeFactory.getPrimitiveType('I');
        IType voidType = typeFactory.getPrimitiveType('V');

        IFieldDeclaration field = factory.getFieldDeclaration(fieldFlags, intType, MY_FIELD, null, Collections.emptyList());
        IMethodDeclaration method = factory.getMethodDeclaration(methodFlags, MY_METHOD, voidType,
                Collections.emptyList(), Collections.emptyList(), null, false, Collections.emptyList());

        IClassDeclaration classDecl = factory.getClassDeclaration(classFlags, MY_CLASS, superType,
                Collections.emptyList(), Collections.singletonList(field),
                Collections.singletonList(method), Collections.emptyList(), Collections.emptyList(),
                null, false);

        assertEquals(1, classDecl.getFields().size());
        assertEquals(1, classDecl.getMethods().size());
        assertEquals(field, classDecl.getFields().get(0));
        assertEquals(method, classDecl.getMethods().get(0));
    }


    /**
     * Test class declaration with null super type (interface).
     */
    @Test
    void testClassDeclarationNullSuperType() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0601);
        IClassDeclaration classDecl = factory.getClassDeclaration(flags, "MyInterface", null,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                null, false);

        assertNull(classDecl.getSuperType());
    }


    /**
     * Test class declaration equals and hashCode.
     */
    @Test
    void testClassDeclarationEqualsHashCode() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0021);
        IType superType = typeFactory.getObjectType("java/lang/Object");
        IClassDeclaration class1 = factory.getClassDeclaration(flags, MY_CLASS, superType,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                null, false);
        IClassDeclaration class2 = factory.getClassDeclaration(flags, MY_CLASS, superType,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                null, false);
        IClassDeclaration class3 = factory.getClassDeclaration(flags, "OtherClass", superType,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                null, false);

        assertEquals(class1, class2);
        assertEquals(class1.hashCode(), class2.hashCode());
        assertNotEquals(class1, class3);
    }


    /**
     * Test class declaration toString.
     */
    @Test
    void testClassDeclarationToString() {
        IAccessFlags flags = commonFactory.getAccessFlags(0x0021);
        IType superType = typeFactory.getObjectType("java/lang/Object");
        IClassDeclaration classDecl = factory.getClassDeclaration(flags, MY_CLASS, superType,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                null, false);

        assertTrue(classDecl.toString().contains(MY_CLASS));
    }
}
