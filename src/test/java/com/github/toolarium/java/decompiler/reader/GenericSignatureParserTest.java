/*
 * GenericSignatureParserTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.type.IGenericType;
import com.github.toolarium.java.decompiler.model.type.IParameterizedType;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.IWildcardType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link GenericSignatureParser}.
 *
 * @author patrick
 */
public class GenericSignatureParserTest {
    private static final String LIST_SIGNATURE = "Ljava/util/List<Ljava/lang/String;>;";
    private final TypeModelFactory factory = TypeModelFactory.getInstance();


    /**
     * Test parsing a simple parameterized type: List&lt;String&gt;.
     */
    @Test
    void testSimpleParameterizedType() {
        IType type = GenericSignatureParser.parseFieldTypeSignature(LIST_SIGNATURE, factory);
        assertNotNull(type);
        assertTrue(type instanceof IParameterizedType);

        IParameterizedType paramType = (IParameterizedType) type;
        assertEquals("java.util.List", paramType.getRawType().getDisplayName());
        assertEquals(1, paramType.getTypeArguments().size());
        assertEquals("java.lang.String", paramType.getTypeArguments().get(0).getDisplayName());
        assertEquals("java.util.List<java.lang.String>", paramType.getDisplayName());
    }


    /**
     * Test parsing a nested parameterized type: Map&lt;String, List&lt;Integer&gt;&gt;.
     */
    @Test
    void testNestedParameterizedType() {
        String signature = "Ljava/util/Map<Ljava/lang/String;Ljava/util/List<Ljava/lang/Integer;>;>;";
        IType type = GenericSignatureParser.parseFieldTypeSignature(signature, factory);
        assertTrue(type instanceof IParameterizedType);

        IParameterizedType mapType = (IParameterizedType) type;
        assertEquals("java.util.Map", mapType.getRawType().getDisplayName());
        assertEquals(2, mapType.getTypeArguments().size());
        assertEquals("java.lang.String", mapType.getTypeArguments().get(0).getDisplayName());

        IType valueType = mapType.getTypeArguments().get(1);
        assertTrue(valueType instanceof IParameterizedType);
        IParameterizedType listIntType = (IParameterizedType) valueType;
        assertEquals("java.util.List", listIntType.getRawType().getDisplayName());
        assertEquals("java.lang.Integer", listIntType.getTypeArguments().get(0).getDisplayName());
    }


    /**
     * Test parsing a type variable reference: T.
     */
    @Test
    void testTypeVariable() {
        IType type = GenericSignatureParser.parseFieldTypeSignature("TT;", factory);
        assertTrue(type instanceof IGenericType);

        IGenericType genericType = (IGenericType) type;
        assertEquals("T", genericType.getTypeVariable());
        assertNull(genericType.getBound());
        assertEquals("T", genericType.getDisplayName());
    }


    /**
     * Test parsing an unbounded wildcard: List&lt;?&gt;.
     */
    @Test
    void testUnboundedWildcard() {
        IType type = GenericSignatureParser.parseFieldTypeSignature("Ljava/util/List<*>;", factory);
        assertTrue(type instanceof IParameterizedType);

        IParameterizedType paramType = (IParameterizedType) type;
        assertEquals(1, paramType.getTypeArguments().size());

        IType argType = paramType.getTypeArguments().get(0);
        assertTrue(argType instanceof IWildcardType);
        IWildcardType wildcard = (IWildcardType) argType;
        assertNull(wildcard.getBound());
        assertTrue(wildcard.isUpperBound());
        assertEquals("?", wildcard.getDisplayName());
    }


    /**
     * Test parsing an upper-bounded wildcard: List&lt;? extends Number&gt;.
     */
    @Test
    void testUpperBoundedWildcard() {
        String signature = "Ljava/util/List<+Ljava/lang/Number;>;";
        IType type = GenericSignatureParser.parseFieldTypeSignature(signature, factory);
        assertTrue(type instanceof IParameterizedType);

        IParameterizedType paramType = (IParameterizedType) type;
        IType argType = paramType.getTypeArguments().get(0);
        assertTrue(argType instanceof IWildcardType);

        IWildcardType wildcard = (IWildcardType) argType;
        assertNotNull(wildcard.getBound());
        assertTrue(wildcard.isUpperBound());
        assertEquals("java.lang.Number", wildcard.getBound().getDisplayName());
        assertEquals("? extends java.lang.Number", wildcard.getDisplayName());
    }


    /**
     * Test parsing a lower-bounded wildcard: Comparator&lt;? super String&gt;.
     */
    @Test
    void testLowerBoundedWildcard() {
        String signature = "Ljava/util/Comparator<-Ljava/lang/String;>;";
        IType type = GenericSignatureParser.parseFieldTypeSignature(signature, factory);
        assertTrue(type instanceof IParameterizedType);

        IParameterizedType paramType = (IParameterizedType) type;
        IType argType = paramType.getTypeArguments().get(0);
        assertTrue(argType instanceof IWildcardType);

        IWildcardType wildcard = (IWildcardType) argType;
        assertNotNull(wildcard.getBound());
        assertFalse(wildcard.isUpperBound());
        assertEquals("java.lang.String", wildcard.getBound().getDisplayName());
        assertEquals("? super java.lang.String", wildcard.getDisplayName());
    }


    /**
     * Test parsing an array of a parameterized type: List&lt;String&gt;[].
     */
    @Test
    void testArrayOfParameterizedType() {
        String signature = "[Ljava/util/List<Ljava/lang/String;>;";
        IType type = GenericSignatureParser.parseFieldTypeSignature(signature, factory);
        assertTrue(type.isArray());
        assertEquals(1, type.getDimensions());
        assertEquals("java.util.List<java.lang.String>[]", type.getDisplayName());
    }


    /**
     * Test parsing a primitive type.
     */
    @Test
    void testPrimitiveType() {
        IType type = GenericSignatureParser.parseFieldTypeSignature("I", factory);
        assertTrue(type.isPrimitive());
        assertEquals("int", type.getDisplayName());
    }


    /**
     * Test parsing a simple object type without generics.
     */
    @Test
    void testSimpleObjectType() {
        IType type = GenericSignatureParser.parseFieldTypeSignature("Ljava/lang/String;", factory);
        assertFalse(type instanceof IParameterizedType);
        assertEquals("java.lang.String", type.getDisplayName());
    }


    /**
     * Test parsing a method signature with generic parameter and return types.
     */
    @Test
    void testMethodSignatureWithGenerics() {
        String signature = "(Ljava/util/List<Ljava/lang/String;>;)Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;";
        GenericSignatureParser.MethodSignatureResult result =
                GenericSignatureParser.parseMethodSignature(signature, factory);

        assertEquals(1, result.getParameterTypes().size());
        assertTrue(result.getParameterTypes().get(0) instanceof IParameterizedType);
        assertEquals("java.util.List<java.lang.String>",
                result.getParameterTypes().get(0).getDisplayName());

        assertTrue(result.getReturnType() instanceof IParameterizedType);
        assertEquals("java.util.Map<java.lang.String, java.lang.Integer>",
                result.getReturnType().getDisplayName());
    }


    /**
     * Test parsing a method signature with type variable parameters.
     */
    @Test
    void testMethodSignatureWithTypeVariables() {
        String signature = "<T:Ljava/lang/Object;>(TT;)TT;";
        GenericSignatureParser.MethodSignatureResult result =
                GenericSignatureParser.parseMethodSignature(signature, factory);

        assertEquals(1, result.getParameterTypes().size());
        assertTrue(result.getParameterTypes().get(0) instanceof IGenericType);
        assertEquals("T", result.getParameterTypes().get(0).getDisplayName());

        assertTrue(result.getReturnType() instanceof IGenericType);
        assertEquals("T", result.getReturnType().getDisplayName());
    }


    /**
     * Test parsing a method signature with void return type.
     */
    @Test
    void testMethodSignatureVoidReturn() {
        String signature = "(Ljava/util/List<Ljava/lang/String;>;)V";
        GenericSignatureParser.MethodSignatureResult result =
                GenericSignatureParser.parseMethodSignature(signature, factory);

        assertEquals(1, result.getParameterTypes().size());
        assertTrue(result.getReturnType().isVoid());
    }


    /**
     * Test parsing a class signature with generic superclass and interfaces.
     */
    @Test
    void testClassSignatureWithInterfaces() {
        String signature = "Ljava/lang/Object;Ljava/lang/Comparable<Ljava/lang/String;>;";
        GenericSignatureParser.ClassSignatureResult result =
                GenericSignatureParser.parseClassSignature(signature, factory);

        assertNotNull(result.getSuperclassType());
        assertEquals("java.lang.Object", result.getSuperclassType().getDisplayName());

        assertEquals(1, result.getInterfaceTypes().size());
        assertTrue(result.getInterfaceTypes().get(0) instanceof IParameterizedType);
        assertEquals("java.lang.Comparable<java.lang.String>",
                result.getInterfaceTypes().get(0).getDisplayName());
    }


    /**
     * Test parsing a class signature with type parameters.
     */
    @Test
    void testClassSignatureWithTypeParameters() {
        String signature = "<T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/lang/Iterable<TT;>;";
        GenericSignatureParser.ClassSignatureResult result =
                GenericSignatureParser.parseClassSignature(signature, factory);

        assertNotNull(result.getSuperclassType());
        assertEquals(1, result.getInterfaceTypes().size());
        assertTrue(result.getInterfaceTypes().get(0) instanceof IParameterizedType);

        IParameterizedType iterableType = (IParameterizedType) result.getInterfaceTypes().get(0);
        assertEquals("java.lang.Iterable", iterableType.getRawType().getDisplayName());
        assertTrue(iterableType.getTypeArguments().get(0) instanceof IGenericType);
        assertEquals("T", iterableType.getTypeArguments().get(0).getDisplayName());
    }


    /**
     * Test parsing a multi-dimensional array: int[][].
     */
    @Test
    void testMultiDimensionalArray() {
        IType type = GenericSignatureParser.parseFieldTypeSignature("[[I", factory);
        assertTrue(type.isArray());
        assertEquals(2, type.getDimensions());
        assertEquals("int[][]", type.getDisplayName());
    }


    /**
     * Test parsing an array of type variable: T[].
     */
    @Test
    void testArrayOfTypeVariable() {
        IType type = GenericSignatureParser.parseFieldTypeSignature("[TT;", factory);
        assertTrue(type.isArray());
        assertEquals(1, type.getDimensions());
        assertEquals("T[]", type.getDisplayName());
    }


    /**
     * Test that null signature throws an exception.
     */
    @Test
    void testNullSignatureThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> GenericSignatureParser.parseFieldTypeSignature(null, factory));
    }


    /**
     * Test that empty signature throws an exception.
     */
    @Test
    void testEmptySignatureThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> GenericSignatureParser.parseFieldTypeSignature("", factory));
    }


    /**
     * Test method signature result accessors.
     */
    @Test
    void testMethodSignatureResultAccessors() {
        String signature = "(ILjava/lang/String;)V";
        GenericSignatureParser.MethodSignatureResult result =
                GenericSignatureParser.parseMethodSignature(signature, factory);

        List<IType> paramTypes = result.getParameterTypes();
        assertNotNull(paramTypes);
        assertEquals(2, paramTypes.size());
        assertEquals("int", paramTypes.get(0).getDisplayName());
        assertEquals("java.lang.String", paramTypes.get(1).getDisplayName());
        assertTrue(result.getReturnType().isVoid());
    }


    /**
     * Test class signature result for a class with no interfaces.
     */
    @Test
    void testClassSignatureNoInterfaces() {
        String signature = "<T:Ljava/lang/Object;>Ljava/lang/Object;";
        GenericSignatureParser.ClassSignatureResult result =
                GenericSignatureParser.parseClassSignature(signature, factory);

        assertNotNull(result.getSuperclassType());
        assertTrue(result.getInterfaceTypes().isEmpty());
    }
}
