/*
 * RoundTripDecompilationTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.api.DecompilerFactory;
import com.github.toolarium.java.decompiler.api.IDecompiler;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;


/**
 * Round-trip decompilation tests that verify real JDK classes produce
 * reasonable Java source output.
 *
 * @author patrick
 */
public class RoundTripDecompilationTest {
    private static final String CLASS_KEYWORD = "class ";
    private static final String INTERFACE_KEYWORD = "interface ";
    private final IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler();


    /**
     * Test decompiling java.lang.Object (root of hierarchy, no superclass).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileObject() throws IOException {
        String source = decompileClass("java/lang/Object");
        assertContainsAll(source, CLASS_KEYWORD, "Object", "public");
        assertTrue(source.contains("equals") || source.contains("hashCode"));
    }


    /**
     * Test decompiling java.lang.String (final class with interfaces).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileString() throws IOException {
        String source = decompileClass("java/lang/String");
        assertContainsAll(source, "final", CLASS_KEYWORD, "String");
        assertTrue(source.contains("implements"));
        assertTrue(source.contains("charAt") || source.contains("length"));
    }


    /**
     * Test decompiling java.util.ArrayList (generics, extends AbstractList).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileArrayList() throws IOException {
        String source = decompileClass("java/util/ArrayList");
        assertContainsAll(source, CLASS_KEYWORD, "ArrayList");
        assertTrue(source.contains("add") || source.contains("get") || source.contains("size"));
    }


    /**
     * Test decompiling java.util.HashMap (complex generics, inner classes).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileHashMap() throws IOException {
        String source = decompileClass("java/util/HashMap");
        assertContainsAll(source, CLASS_KEYWORD, "HashMap");
        assertTrue(source.contains("put") || source.contains("get"));
    }


    /**
     * Test decompiling java.lang.Comparable (generic interface).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileComparable() throws IOException {
        String source = decompileClass("java/lang/Comparable");
        assertContainsAll(source, INTERFACE_KEYWORD, "Comparable", "compareTo");
    }


    /**
     * Test decompiling java.lang.Iterable (interface with default method).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileIterable() throws IOException {
        String source = decompileClass("java/lang/Iterable");
        assertContainsAll(source, INTERFACE_KEYWORD, "Iterable", "iterator");
    }


    /**
     * Test decompiling java.util.AbstractList (abstract class).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileAbstractList() throws IOException {
        String source = decompileClass("java/util/AbstractList");
        assertContainsAll(source, "abstract", CLASS_KEYWORD, "AbstractList");
    }


    /**
     * Test decompiling java.lang.Thread$State (enum).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileThreadState() throws IOException {
        String source = decompileClass("java/lang/Thread$State");
        assertContainsAll(source, "enum ", "State");
        // Enum reconstructor should have cleaned it
        assertFalse(source.contains("$VALUES"));
    }


    /**
     * Test decompiling java.lang.Integer (autoboxing, static methods).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileInteger() throws IOException {
        String source = decompileClass("java/lang/Integer");
        assertContainsAll(source, CLASS_KEYWORD, "Integer", "intValue");
        assertTrue(source.contains("parseInt") || source.contains("valueOf"));
    }


    /**
     * Test decompiling java.lang.StringBuilder (method chaining).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileStringBuilder() throws IOException {
        String source = decompileClass("java/lang/StringBuilder");
        assertContainsAll(source, CLASS_KEYWORD, "StringBuilder");
        assertTrue(source.contains("append") || source.contains("toString"));
    }


    /**
     * Test decompiling java.io.IOException (exception class).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileIOException() throws IOException {
        String source = decompileClass("java/io/IOException");
        assertContainsAll(source, CLASS_KEYWORD, "IOException");
    }


    /**
     * Test decompiling java.lang.Runnable (functional interface).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileRunnable() throws IOException {
        String source = decompileClass("java/lang/Runnable");
        assertContainsAll(source, INTERFACE_KEYWORD, "Runnable", "run");
    }


    /**
     * Test decompiling java.util.Collections (utility class with static methods).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileCollections() throws IOException {
        String source = decompileClass("java/util/Collections");
        assertContainsAll(source, CLASS_KEYWORD, "Collections");
    }


    /**
     * Test decompiling java.lang.annotation.Retention (annotation).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileAnnotation() throws IOException {
        String source = decompileClass("java/lang/annotation/Retention");
        assertNotNull(source);
        assertFalse(source.isEmpty());
        assertTrue(source.contains("Retention"));
    }


    /**
     * Test decompiling java.util.Map$Entry (inner interface).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileMapEntry() throws IOException {
        String source = decompileClass("java/util/Map$Entry");
        assertContainsAll(source, INTERFACE_KEYWORD, "Entry");
        assertTrue(source.contains("getKey") || source.contains("getValue"));
    }


    /**
     * Test that output never contains raw bytecode mnemonics in class/method declarations.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testOutputDoesNotContainBytecodeArtifacts() throws IOException {
        String source = decompileClass("java/lang/Runnable");
        assertFalse(source.contains("aload_0"));
        assertFalse(source.contains("invokevirtual"));
        assertFalse(source.contains("invokeinterface"));
    }


    /**
     * Test that decompiled output has balanced braces.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testBalancedBraces() throws IOException {
        String source = decompileClass("java/lang/Comparable");
        int openBraces = 0;
        int closeBraces = 0;
        for (char ch : source.toCharArray()) {
            if (ch == '{') {
                openBraces++;
            } else if (ch == '}') {
                closeBraces++;
            }
        }
        assertTrue(openBraces == closeBraces,
                "Braces should be balanced: { = " + openBraces + ", } = " + closeBraces);
    }


    /**
     * Decompile a class from the system classloader.
     *
     * @param internalName the class internal name
     * @return the decompiled source
     * @throws IOException if decompilation fails
     */
    private String decompileClass(String internalName) throws IOException {
        String resourcePath = internalName + ".class";
        try (InputStream is = ClassLoader.getSystemResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Class not found: " + internalName);
            }
            byte[] classBytes = is.readAllBytes();
            String source = decompiler.decompile(classBytes);
            assertNotNull(source, "Decompilation returned null for " + internalName);
            assertFalse(source.isEmpty(), "Decompilation returned empty for " + internalName);
            return source;
        }
    }


    /**
     * Assert that a source string contains all expected substrings.
     *
     * @param source   the source code
     * @param expected the expected substrings
     */
    private void assertContainsAll(String source, String... expected) {
        for (String exp : expected) {
            assertTrue(source.contains(exp), "Expected source to contain: " + exp);
        }
    }
}
