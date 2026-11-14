/*
 * TypeModelFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.type.IArrayType;
import com.github.toolarium.java.decompiler.model.type.IGenericType;
import com.github.toolarium.java.decompiler.model.type.IMethodType;
import com.github.toolarium.java.decompiler.model.type.IObjectType;
import com.github.toolarium.java.decompiler.model.type.IParameterizedType;
import com.github.toolarium.java.decompiler.model.type.IPrimitiveType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link TypeModelFactory} and its model implementations.
 *
 * @author patrick
 */
public class TypeModelFactoryTest {
    private static final String JAVA_LANG_STRING = "java/lang/String";
    private static final String TYPE_VAR_T = "T";
    private final TypeModelFactory factory = TypeModelFactory.getInstance();


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        TypeModelFactory factory1 = TypeModelFactory.getInstance();
        TypeModelFactory factory2 = TypeModelFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Test all primitive types.
     */
    @Test
    void testPrimitiveTypes() {
        assertPrimitive(factory.getPrimitiveType('Z'), 'Z', "Z", "boolean", 1, false);
        assertPrimitive(factory.getPrimitiveType('B'), 'B', "B", "byte", 1, false);
        assertPrimitive(factory.getPrimitiveType('C'), 'C', "C", "char", 1, false);
        assertPrimitive(factory.getPrimitiveType('S'), 'S', "S", "short", 1, false);
        assertPrimitive(factory.getPrimitiveType('I'), 'I', "I", "int", 1, false);
        assertPrimitive(factory.getPrimitiveType('J'), 'J', "J", "long", 2, false);
        assertPrimitive(factory.getPrimitiveType('F'), 'F', "F", "float", 1, false);
        assertPrimitive(factory.getPrimitiveType('D'), 'D', "D", "double", 2, false);
        assertPrimitive(factory.getPrimitiveType('V'), 'V', "V", "void", 1, true);
    }


    /**
     * Test invalid primitive type character.
     */
    @Test
    void testInvalidPrimitiveType() {
        assertThrows(IllegalArgumentException.class, () -> factory.getPrimitiveType('X'));
    }


    /**
     * Test primitive type equality (cached instances).
     */
    @Test
    void testPrimitiveTypeCaching() {
        IPrimitiveType int1 = factory.getPrimitiveType('I');
        IPrimitiveType int2 = factory.getPrimitiveType('I');
        assertEquals(int1, int2);
        assertEquals(int1.hashCode(), int2.hashCode());
    }


    /**
     * Test primitive type not equal to different type.
     */
    @Test
    void testPrimitiveTypeNotEqual() {
        IPrimitiveType intType = factory.getPrimitiveType('I');
        IPrimitiveType longType = factory.getPrimitiveType('J');
        assertNotEquals(intType, longType);
        assertNotEquals(intType, null);
        assertNotEquals(intType, "int");
    }


    /**
     * Test primitive type toString.
     */
    @Test
    void testPrimitiveTypeToString() {
        IPrimitiveType intType = factory.getPrimitiveType('I');
        assertTrue(intType.toString().contains("int"));
    }


    /**
     * Test primitive type interface methods.
     */
    @Test
    void testPrimitiveTypeInterfaceMethods() {
        IPrimitiveType intType = factory.getPrimitiveType('I');
        assertTrue(intType.isPrimitive());
        assertFalse(intType.isArray());
        assertFalse(intType.isVoid());
        assertEquals(0, intType.getDimensions());
    }


    /**
     * Test object type.
     */
    @Test
    void testObjectType() {
        IObjectType type = factory.getObjectType(JAVA_LANG_STRING);
        assertEquals(JAVA_LANG_STRING, type.getInternalName());
        assertEquals("Ljava/lang/String;", type.getDescriptor());
        assertEquals("java.lang.String", type.getDisplayName());
        assertEquals("java.lang", type.getPackageName());
        assertEquals("String", type.getSimpleName());
        assertFalse(type.isPrimitive());
        assertFalse(type.isArray());
        assertFalse(type.isVoid());
        assertEquals(0, type.getDimensions());
    }


    /**
     * Test object type in default package.
     */
    @Test
    void testObjectTypeDefaultPackage() {
        IObjectType type = factory.getObjectType("MyClass");
        assertEquals("MyClass", type.getSimpleName());
        assertEquals("", type.getPackageName());
        assertEquals("LMyClass;", type.getDescriptor());
    }


    /**
     * Test object type equals and hashCode.
     */
    @Test
    void testObjectTypeEqualsHashCode() {
        IObjectType type1 = factory.getObjectType(JAVA_LANG_STRING);
        IObjectType type2 = factory.getObjectType(JAVA_LANG_STRING);
        IObjectType type3 = factory.getObjectType("java/lang/Integer");

        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());
        assertNotEquals(type1, type3);
        assertNotEquals(type1, null);
        assertEquals(type1, type1);
    }


    /**
     * Test object type toString.
     */
    @Test
    void testObjectTypeToString() {
        IObjectType type = factory.getObjectType(JAVA_LANG_STRING);
        assertTrue(type.toString().contains(JAVA_LANG_STRING));
    }


    /**
     * Test array type.
     */
    @Test
    void testArrayType() {
        IObjectType stringType = factory.getObjectType(JAVA_LANG_STRING);
        IArrayType arrayType = factory.getArrayType(stringType, 1);

        assertEquals("[Ljava/lang/String;", arrayType.getDescriptor());
        assertEquals("java.lang.String[]", arrayType.getDisplayName());
        assertEquals(stringType, arrayType.getElementType());
        assertEquals(stringType, arrayType.getBaseType());
        assertTrue(arrayType.isArray());
        assertFalse(arrayType.isPrimitive());
        assertFalse(arrayType.isVoid());
        assertEquals(1, arrayType.getDimensions());
    }


    /**
     * Test multi-dimensional array type.
     */
    @Test
    void testMultiDimensionalArrayType() {
        IPrimitiveType intType = factory.getPrimitiveType('I');
        IArrayType arrayType = factory.getArrayType(intType, 3);

        assertEquals("[[[I", arrayType.getDescriptor());
        assertEquals("int[][][]", arrayType.getDisplayName());
        assertEquals(3, arrayType.getDimensions());
        assertEquals(intType, arrayType.getBaseType());
    }


    /**
     * Test array type invalid dimensions.
     */
    @Test
    void testArrayTypeInvalidDimensions() {
        IObjectType type = factory.getObjectType("java/lang/Object");
        assertThrows(IllegalArgumentException.class, () -> factory.getArrayType(type, 0));
    }


    /**
     * Test array type equals and hashCode.
     */
    @Test
    void testArrayTypeEqualsHashCode() {
        IPrimitiveType intType = factory.getPrimitiveType('I');
        IArrayType arr1 = factory.getArrayType(intType, 1);
        IArrayType arr2 = factory.getArrayType(intType, 1);
        IArrayType arr3 = factory.getArrayType(intType, 2);

        assertEquals(arr1, arr2);
        assertEquals(arr1.hashCode(), arr2.hashCode());
        assertNotEquals(arr1, arr3);
    }


    /**
     * Test array type toString.
     */
    @Test
    void testArrayTypeToString() {
        IPrimitiveType byteType = factory.getPrimitiveType('B');
        IArrayType arr = factory.getArrayType(byteType, 1);
        assertTrue(arr.toString().contains("byte[]"));
    }


    /**
     * Test generic type unbounded.
     */
    @Test
    void testGenericTypeUnbounded() {
        IGenericType generic = factory.getGenericType(TYPE_VAR_T, null);
        assertEquals(TYPE_VAR_T, generic.getTypeVariable());
        assertNull(generic.getBound());
        assertEquals("TT;", generic.getDescriptor());
        assertEquals(TYPE_VAR_T, generic.getDisplayName());
        assertFalse(generic.isPrimitive());
        assertFalse(generic.isArray());
        assertFalse(generic.isVoid());
        assertEquals(0, generic.getDimensions());
    }


    /**
     * Test generic type with bound.
     */
    @Test
    void testGenericTypeWithBound() {
        IObjectType bound = factory.getObjectType("java/lang/Number");
        IGenericType generic = factory.getGenericType("N", bound);
        assertEquals(bound, generic.getBound());
        assertTrue(generic.toString().contains("extends"));
    }


    /**
     * Test generic type equals and hashCode.
     */
    @Test
    void testGenericTypeEqualsHashCode() {
        IGenericType gen1 = factory.getGenericType(TYPE_VAR_T, null);
        IGenericType gen2 = factory.getGenericType(TYPE_VAR_T, null);
        IGenericType gen3 = factory.getGenericType("E", null);

        assertEquals(gen1, gen2);
        assertEquals(gen1.hashCode(), gen2.hashCode());
        assertNotEquals(gen1, gen3);
    }


    /**
     * Test generic type toString.
     */
    @Test
    void testGenericTypeToString() {
        IGenericType gen = factory.getGenericType(TYPE_VAR_T, null);
        assertTrue(gen.toString().contains(TYPE_VAR_T));
    }


    /**
     * Test parameterized type.
     */
    @Test
    void testParameterizedType() {
        IObjectType listType = factory.getObjectType("java/util/List");
        IObjectType stringType = factory.getObjectType(JAVA_LANG_STRING);
        IParameterizedType paramType = factory.getParameterizedType(listType,
                Collections.singletonList(stringType));

        assertEquals(listType, paramType.getRawType());
        assertEquals(1, paramType.getTypeArguments().size());
        assertEquals(stringType, paramType.getTypeArguments().get(0));
        assertEquals("Ljava/util/List;", paramType.getDescriptor());
        assertEquals("java.util.List<java.lang.String>", paramType.getDisplayName());
        assertFalse(paramType.isPrimitive());
        assertFalse(paramType.isArray());
    }


    /**
     * Test parameterized type with no arguments.
     */
    @Test
    void testParameterizedTypeNoArgs() {
        IObjectType rawType = factory.getObjectType("java/util/List");
        IParameterizedType paramType = factory.getParameterizedType(rawType, null);
        assertTrue(paramType.getTypeArguments().isEmpty());
        assertEquals("java.util.List", paramType.getDisplayName());
    }


    /**
     * Test parameterized type with multiple arguments.
     */
    @Test
    void testParameterizedTypeMultipleArgs() {
        IObjectType mapType = factory.getObjectType("java/util/Map");
        IObjectType stringType = factory.getObjectType(JAVA_LANG_STRING);
        IObjectType integerType = factory.getObjectType("java/lang/Integer");
        IParameterizedType paramType = factory.getParameterizedType(mapType,
                Arrays.asList(stringType, integerType));

        assertEquals("java.util.Map<java.lang.String, java.lang.Integer>", paramType.getDisplayName());
    }


    /**
     * Test parameterized type equals and hashCode.
     */
    @Test
    void testParameterizedTypeEqualsHashCode() {
        IObjectType listType = factory.getObjectType("java/util/List");
        IObjectType stringType = factory.getObjectType(JAVA_LANG_STRING);
        IObjectType intType = factory.getObjectType("java/lang/Integer");

        IParameterizedType pt1 = factory.getParameterizedType(listType, Collections.singletonList(stringType));
        IParameterizedType pt2 = factory.getParameterizedType(listType, Collections.singletonList(stringType));
        IParameterizedType pt3 = factory.getParameterizedType(listType, Collections.singletonList(intType));

        assertEquals(pt1, pt2);
        assertEquals(pt1.hashCode(), pt2.hashCode());
        assertNotEquals(pt1, pt3);
    }


    /**
     * Test parameterized type toString.
     */
    @Test
    void testParameterizedTypeToString() {
        IObjectType listType = factory.getObjectType("java/util/List");
        IObjectType stringType = factory.getObjectType(JAVA_LANG_STRING);
        IParameterizedType pt = factory.getParameterizedType(listType, Collections.singletonList(stringType));
        assertTrue(pt.toString().contains("List"));
    }


    /**
     * Test method type.
     */
    @Test
    void testMethodType() {
        IPrimitiveType intType = factory.getPrimitiveType('I');
        IPrimitiveType voidType = factory.getPrimitiveType('V');
        IObjectType stringType = factory.getObjectType(JAVA_LANG_STRING);

        IMethodType methodType = factory.getMethodType(voidType, Arrays.asList(intType, stringType));

        assertEquals(voidType, methodType.getReturnType());
        assertEquals(2, methodType.getParameterTypes().size());
        assertEquals("(ILjava/lang/String;)V", methodType.getDescriptor());
        assertEquals("(int, java.lang.String) -> void", methodType.getDisplayName());
        assertFalse(methodType.isPrimitive());
        assertFalse(methodType.isArray());
        assertFalse(methodType.isVoid());
        assertEquals(0, methodType.getDimensions());
    }


    /**
     * Test method type no parameters.
     */
    @Test
    void testMethodTypeNoParams() {
        IPrimitiveType intType = factory.getPrimitiveType('I');
        IMethodType methodType = factory.getMethodType(intType, null);

        assertTrue(methodType.getParameterTypes().isEmpty());
        assertEquals("()I", methodType.getDescriptor());
        assertEquals("() -> int", methodType.getDisplayName());
    }


    /**
     * Test method type equals and hashCode.
     */
    @Test
    void testMethodTypeEqualsHashCode() {
        IPrimitiveType intType = factory.getPrimitiveType('I');
        IPrimitiveType voidType = factory.getPrimitiveType('V');

        IMethodType mt1 = factory.getMethodType(voidType, Collections.singletonList(intType));
        IMethodType mt2 = factory.getMethodType(voidType, Collections.singletonList(intType));
        IMethodType mt3 = factory.getMethodType(intType, Collections.singletonList(intType));

        assertEquals(mt1, mt2);
        assertEquals(mt1.hashCode(), mt2.hashCode());
        assertNotEquals(mt1, mt3);
    }


    /**
     * Test method type toString.
     */
    @Test
    void testMethodTypeToString() {
        IPrimitiveType voidType = factory.getPrimitiveType('V');
        IMethodType mt = factory.getMethodType(voidType, null);
        assertTrue(mt.toString().contains("()V"));
    }


    /**
     * Helper to assert primitive type properties.
     *
     * @param type        the primitive type to check
     * @param typeChar    expected type character
     * @param descriptor  expected descriptor
     * @param displayName expected display name
     * @param size        expected stack size
     * @param isVoid      expected void flag
     */
    private void assertPrimitive(IPrimitiveType type, char typeChar, String descriptor,
                                 String displayName, int size, boolean isVoid) {
        assertEquals(typeChar, type.getTypeChar());
        assertEquals(descriptor, type.getDescriptor());
        assertEquals(displayName, type.getDisplayName());
        assertEquals(size, type.getSize());
        assertEquals(isVoid, type.isVoid());
        assertTrue(type.isPrimitive());
        assertFalse(type.isArray());
        assertEquals(0, type.getDimensions());
    }
}
