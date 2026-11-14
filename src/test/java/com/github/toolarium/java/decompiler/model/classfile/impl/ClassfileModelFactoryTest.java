/*
 * ClassfileModelFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.classfile.ClassfileModelFactory;
import com.github.toolarium.java.decompiler.model.classfile.IClassFileMetadata;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.model.classfile.IStructField;
import com.github.toolarium.java.decompiler.model.classfile.IStructMethod;
import com.github.toolarium.java.decompiler.model.classfile.IStructRecordComponent;
import com.github.toolarium.java.decompiler.model.common.CommonModelFactory;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.common.IAttribute;
import com.github.toolarium.java.decompiler.model.common.IConstantPool;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link ClassfileModelFactory} and its model implementations.
 *
 * @author patrick
 */
public class ClassfileModelFactoryTest {
    private static final String INT_DESC = "I";
    private static final String VOID_METHOD_DESC = "()V";
    private final CommonModelFactory commonFactory = CommonModelFactory.getInstance();
    private final ClassfileModelFactory factory = ClassfileModelFactory.getInstance();
    private final IAccessFlags publicFlags = commonFactory.getAccessFlags(0x0001);
    private final IAccessFlags privateFlags = commonFactory.getAccessFlags(0x0002);


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        ClassfileModelFactory factory1 = ClassfileModelFactory.getInstance();
        ClassfileModelFactory factory2 = ClassfileModelFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Test struct field creation and getters.
     */
    @Test
    void testStructField() {
        IStructField field = factory.getStructField("count", INT_DESC, publicFlags,
                Collections.emptyList(), Collections.emptyList(), null);

        assertEquals("count", field.getName());
        assertEquals(INT_DESC, field.getDescriptor());
        assertEquals(publicFlags, field.getAccessFlags());
        assertTrue(field.getAttributes().isEmpty());
        assertTrue(field.getAnnotations().isEmpty());
        assertNull(field.getSignature());
    }


    /**
     * Test struct field with signature.
     */
    @Test
    void testStructFieldWithSignature() {
        IStructField field = factory.getStructField("items", "Ljava/util/List;", privateFlags,
                null, null, "Ljava/util/List<Ljava/lang/String;>;");

        assertEquals("Ljava/util/List<Ljava/lang/String;>;", field.getSignature());
    }


    /**
     * Test struct field equals and hashCode.
     */
    @Test
    void testStructFieldEqualsHashCode() {
        IStructField field1 = factory.getStructField("name", "Ljava/lang/String;", publicFlags,
                null, null, null);
        IStructField field2 = factory.getStructField("name", "Ljava/lang/String;", publicFlags,
                null, null, null);
        IStructField field3 = factory.getStructField("other", "Ljava/lang/String;", publicFlags,
                null, null, null);

        assertEquals(field1, field2);
        assertEquals(field1.hashCode(), field2.hashCode());
        assertNotEquals(field1, field3);
        assertNotEquals(field1, null);
        assertEquals(field1, field1);
    }


    /**
     * Test struct field toString.
     */
    @Test
    void testStructFieldToString() {
        IStructField field = factory.getStructField("count", INT_DESC, publicFlags, null, null, null);
        assertTrue(field.toString().contains("count"));
    }


    /**
     * Test struct method creation and getters.
     */
    @Test
    void testStructMethod() {
        byte[] code = {0x2A, 0x01};
        List<String> paramNames = Arrays.asList("arg0", "arg1");
        List<String> exceptions = Collections.singletonList("java/io/IOException");

        IStructMethod method = factory.getStructMethod("process", "(II)V", publicFlags,
                null, null, null, paramNames, null, exceptions, factory.getMethodCode(code, 2, 3));

        assertEquals("process", method.getName());
        assertEquals("(II)V", method.getDescriptor());
        assertNull(method.getSignature());
        assertEquals(2, method.getParameterNames().size());
        assertEquals("arg0", method.getParameterNames().get(0));
        assertEquals(1, method.getExceptionTypes().size());
        assertEquals("java/io/IOException", method.getExceptionTypes().get(0));
        assertArrayEquals(new byte[]{0x2A, 0x01}, method.getCode());
        assertEquals(2, method.getMaxStack());
        assertEquals(3, method.getMaxLocals());
    }


    /**
     * Test struct method with null code (abstract).
     */
    @Test
    void testStructMethodAbstract() {
        IAccessFlags abstractFlags = commonFactory.getAccessFlags(0x0400);
        IStructMethod method = factory.getStructMethod("doIt", VOID_METHOD_DESC, abstractFlags,
                null, null, null, null, null, null, null);

        assertNull(method.getCode());
        assertTrue(method.getParameterNames().isEmpty());
        assertTrue(method.getExceptionTypes().isEmpty());
    }


    /**
     * Test struct method code defensive copy.
     */
    @Test
    void testStructMethodCodeDefensiveCopy() {
        byte[] code = {0x01, 0x02};
        IStructMethod method = factory.getStructMethod("m", VOID_METHOD_DESC, publicFlags,
                null, null, null, null, null, null, factory.getMethodCode(code, 1, 1));

        code[0] = (byte) 0xFF;
        assertArrayEquals(new byte[]{0x01, 0x02}, method.getCode());

        byte[] returned = method.getCode();
        returned[0] = (byte) 0xFF;
        assertArrayEquals(new byte[]{0x01, 0x02}, method.getCode());
    }


    /**
     * Test struct method equals and hashCode.
     */
    @Test
    void testStructMethodEqualsHashCode() {
        IStructMethod method1 = factory.getStructMethod("m", VOID_METHOD_DESC, publicFlags,
                null, null, null, null, null, null, factory.getMethodCode(new byte[]{1}, 1, 1));
        IStructMethod method2 = factory.getStructMethod("m", VOID_METHOD_DESC, publicFlags,
                null, null, null, null, null, null, factory.getMethodCode(new byte[]{1}, 1, 1));
        IStructMethod method3 = factory.getStructMethod("m", VOID_METHOD_DESC, publicFlags,
                null, null, null, null, null, null, factory.getMethodCode(new byte[]{2}, 1, 1));

        assertEquals(method1, method2);
        assertEquals(method1.hashCode(), method2.hashCode());
        assertNotEquals(method1, method3);
    }


    /**
     * Test struct method toString.
     */
    @Test
    void testStructMethodToString() {
        IStructMethod method = factory.getStructMethod("init", VOID_METHOD_DESC, publicFlags,
                null, null, null, null, null, null, null);
        assertTrue(method.toString().contains("init"));
    }


    /**
     * Test struct record component.
     */
    @Test
    void testStructRecordComponent() {
        IAnnotation ann = commonFactory.getAnnotation("Ljakarta/validation/NotNull;", true, null);
        IStructRecordComponent comp = factory.getStructRecordComponent("name",
                "Ljava/lang/String;", null, Collections.singletonList(ann));

        assertEquals("name", comp.getName());
        assertEquals("Ljava/lang/String;", comp.getDescriptor());
        assertNull(comp.getSignature());
        assertEquals(1, comp.getAnnotations().size());
    }


    /**
     * Test struct record component equals and hashCode.
     */
    @Test
    void testStructRecordComponentEqualsHashCode() {
        IStructRecordComponent comp1 = factory.getStructRecordComponent("x", INT_DESC, null, null);
        IStructRecordComponent comp2 = factory.getStructRecordComponent("x", INT_DESC, null, null);
        IStructRecordComponent comp3 = factory.getStructRecordComponent("y", INT_DESC, null, null);

        assertEquals(comp1, comp2);
        assertEquals(comp1.hashCode(), comp2.hashCode());
        assertNotEquals(comp1, comp3);
    }


    /**
     * Test struct record component toString.
     */
    @Test
    void testStructRecordComponentToString() {
        IStructRecordComponent comp = factory.getStructRecordComponent("age", INT_DESC, null, null);
        assertTrue(comp.toString().contains("age"));
    }


    /**
     * Test struct class creation and getters.
     */
    @Test
    void testStructClass() {
        IConstantPool pool = commonFactory.getConstantPool(new Object[]{null, "test"});
        IAttribute attr = commonFactory.getAttribute("SourceFile", new byte[]{0, 1});
        IStructField field = factory.getStructField("id", INT_DESC, privateFlags, null, null, null);
        IStructMethod method = factory.getStructMethod("<init>", VOID_METHOD_DESC, publicFlags,
                null, null, null, null, null, null, factory.getMethodCode(new byte[]{0x2A}, 1, 1));

        IClassFileMetadata metadata = factory.getClassFileMetadata(61, 0, null, "MyClass.java",
                Collections.emptyList(), Collections.emptyList(), null);
        IStructClass cls = factory.getStructClass(metadata, pool, publicFlags,
                "com/example/MyClass", "java/lang/Object",
                Collections.singletonList("java/io/Serializable"),
                Collections.singletonList(field), Collections.singletonList(method),
                Collections.singletonList(attr), Collections.emptyList());

        assertEquals(61, cls.getMajorVersion());
        assertEquals(0, cls.getMinorVersion());
        assertNotNull(cls.getConstantPool());
        assertEquals(publicFlags, cls.getAccessFlags());
        assertEquals("com/example/MyClass", cls.getName());
        assertEquals("java/lang/Object", cls.getSuperName());
        assertEquals(1, cls.getInterfaceNames().size());
        assertEquals("java/io/Serializable", cls.getInterfaceNames().get(0));
        assertEquals(1, cls.getFields().size());
        assertEquals(1, cls.getMethods().size());
        assertEquals(1, cls.getAttributes().size());
        assertTrue(cls.getAnnotations().isEmpty());
        assertNull(cls.getSignature());
        assertEquals("MyClass.java", cls.getSourceFile());
        assertTrue(cls.getRecordComponents().isEmpty());
    }


    /**
     * Test struct class equals and hashCode.
     */
    @Test
    void testStructClassEqualsHashCode() {
        IConstantPool pool = commonFactory.getConstantPool(new Object[]{null});

        IClassFileMetadata metadata = factory.getClassFileMetadata(61, 0, null, null, null, null, null);
        IStructClass cls1 = factory.getStructClass(metadata, pool, publicFlags,
                "com/example/A", "java/lang/Object",
                null, null, null, null, null);
        IStructClass cls2 = factory.getStructClass(metadata, pool, publicFlags,
                "com/example/A", "java/lang/Object",
                null, null, null, null, null);
        IStructClass cls3 = factory.getStructClass(metadata, pool, publicFlags,
                "com/example/B", "java/lang/Object",
                null, null, null, null, null);

        assertEquals(cls1, cls2);
        assertEquals(cls1.hashCode(), cls2.hashCode());
        assertNotEquals(cls1, cls3);
    }


    /**
     * Test struct class toString.
     */
    @Test
    void testStructClassToString() {
        IConstantPool pool = commonFactory.getConstantPool(new Object[]{null});
        IClassFileMetadata metadata = factory.getClassFileMetadata(61, 0, null, null, null, null, null);
        IStructClass cls = factory.getStructClass(metadata, pool, publicFlags,
                "com/example/Test", null, null, null, null, null, null);
        assertTrue(cls.toString().contains("com/example/Test"));
        assertTrue(cls.toString().contains("61"));
    }


    /**
     * Test struct class list immutability.
     */
    @Test
    void testStructClassImmutability() {
        IConstantPool pool = commonFactory.getConstantPool(new Object[]{null});
        IClassFileMetadata metadata = factory.getClassFileMetadata(61, 0, null, null, null, null, null);
        IStructClass cls = factory.getStructClass(metadata, pool, publicFlags,
                "Test", null, null, null, null, null, null);

        assertTrue(cls.getInterfaceNames().isEmpty());
        assertTrue(cls.getFields().isEmpty());
        assertTrue(cls.getMethods().isEmpty());
    }
}
