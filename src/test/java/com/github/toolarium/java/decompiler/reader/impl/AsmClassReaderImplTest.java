/*
 * AsmClassReaderImplTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reader.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.model.classfile.IStructField;
import com.github.toolarium.java.decompiler.model.classfile.IStructMethod;
import com.github.toolarium.java.decompiler.reader.ClassReaderFactory;
import com.github.toolarium.java.decompiler.reader.IClassReader;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link AsmClassReaderImpl}.
 *
 * @author patrick
 */
public class AsmClassReaderImplTest {
    private static final String SAMPLE_CLASS = "java/lang/String";
    private final IClassReader reader = ClassReaderFactory.getInstance().getClassReader();


    /**
     * Test reading a class from the JDK (java.lang.String).
     *
     * @throws IOException if reading fails
     */
    @Test
    void testReadStringClass() throws IOException {
        byte[] classBytes = loadClassBytes(SAMPLE_CLASS);
        IStructClass structClass = reader.readClass(classBytes);

        assertNotNull(structClass);
        assertEquals("java/lang/String", structClass.getName());
        assertEquals("java/lang/Object", structClass.getSuperName());
        assertTrue(structClass.getAccessFlags().isPublic());
        assertTrue(structClass.getAccessFlags().isFinal());
        assertFalse(structClass.getAccessFlags().isAbstract());
        assertFalse(structClass.getFields().isEmpty());
        assertFalse(structClass.getMethods().isEmpty());
        assertNotNull(structClass.getSourceFile());
    }


    /**
     * Test reading an interface class (java.io.Serializable).
     *
     * @throws IOException if reading fails
     */
    @Test
    void testReadInterfaceClass() throws IOException {
        byte[] classBytes = loadClassBytes("java/io/Serializable");
        IStructClass structClass = reader.readClass(classBytes);

        assertNotNull(structClass);
        assertEquals("java/io/Serializable", structClass.getName());
        assertTrue(structClass.getAccessFlags().isInterface());
        assertTrue(structClass.getAccessFlags().isAbstract());
    }


    /**
     * Test reading a class with implemented interfaces.
     *
     * @throws IOException if reading fails
     */
    @Test
    void testReadClassWithInterfaces() throws IOException {
        byte[] classBytes = loadClassBytes(SAMPLE_CLASS);
        IStructClass structClass = reader.readClass(classBytes);

        assertFalse(structClass.getInterfaceNames().isEmpty());
        assertTrue(structClass.getInterfaceNames().contains("java/io/Serializable"));
    }


    /**
     * Test reading fields from a class.
     *
     * @throws IOException if reading fails
     */
    @Test
    void testReadFields() throws IOException {
        byte[] classBytes = loadClassBytes("java/lang/Integer");
        IStructClass structClass = reader.readClass(classBytes);

        assertFalse(structClass.getFields().isEmpty());
        boolean foundMaxValue = false;
        for (IStructField field : structClass.getFields()) {
            assertNotNull(field.getName());
            assertNotNull(field.getDescriptor());
            if ("MAX_VALUE".equals(field.getName())) {
                foundMaxValue = true;
                assertEquals("I", field.getDescriptor());
                assertTrue(field.getAccessFlags().isPublic());
                assertTrue(field.getAccessFlags().isStatic());
                assertTrue(field.getAccessFlags().isFinal());
            }
        }
        assertTrue(foundMaxValue, "Expected to find MAX_VALUE field");
    }


    /**
     * Test reading methods from a class.
     *
     * @throws IOException if reading fails
     */
    @Test
    void testReadMethods() throws IOException {
        byte[] classBytes = loadClassBytes("java/lang/Integer");
        IStructClass structClass = reader.readClass(classBytes);

        assertFalse(structClass.getMethods().isEmpty());
        boolean foundIntValue = false;
        for (IStructMethod method : structClass.getMethods()) {
            assertNotNull(method.getName());
            assertNotNull(method.getDescriptor());
            if ("intValue".equals(method.getName())) {
                foundIntValue = true;
                assertEquals("()I", method.getDescriptor());
                assertTrue(method.getAccessFlags().isPublic());
            }
        }
        assertTrue(foundIntValue, "Expected to find intValue method");
    }


    /**
     * Test reading a class with generics signature.
     *
     * @throws IOException if reading fails
     */
    @Test
    void testReadGenericClass() throws IOException {
        byte[] classBytes = loadClassBytes("java/util/ArrayList");
        IStructClass structClass = reader.readClass(classBytes);

        assertEquals("java/util/ArrayList", structClass.getName());
        assertNotNull(structClass.getSignature());
        assertTrue(structClass.getSignature().contains("Ljava/util/List"));
    }


    /**
     * Test reading an abstract class.
     *
     * @throws IOException if reading fails
     */
    @Test
    void testReadAbstractClass() throws IOException {
        byte[] classBytes = loadClassBytes("java/util/AbstractList");
        IStructClass structClass = reader.readClass(classBytes);

        assertTrue(structClass.getAccessFlags().isAbstract());
        assertFalse(structClass.getAccessFlags().isInterface());
    }


    /**
     * Test reading an enum class.
     *
     * @throws IOException if reading fails
     */
    @Test
    void testReadEnumClass() throws IOException {
        byte[] classBytes = loadClassBytes("java/lang/Thread$State");
        IStructClass structClass = reader.readClass(classBytes);

        assertTrue(structClass.getAccessFlags().isEnum());
        assertTrue(structClass.getAccessFlags().isFinal());
    }


    /**
     * Test reading with an input stream.
     *
     * @throws IOException if reading fails
     */
    @Test
    void testReadFromInputStream() throws IOException {
        String resourcePath = "java/lang/Object.class";
        try (InputStream is = ClassLoader.getSystemResourceAsStream(resourcePath)) {
            assertNotNull(is, "Could not find Object.class on classpath");
            IStructClass structClass = reader.readClass(is);

            assertEquals("java/lang/Object", structClass.getName());
            assertNull(structClass.getSuperName());
        }
    }


    /**
     * Test reading invalid bytes throws IOException.
     */
    @Test
    void testReadInvalidBytes() {
        assertThrows(IOException.class, () -> reader.readClass(new byte[]{1, 2, 3}));
    }


    /**
     * Test reading the project's own classes.
     *
     * @throws IOException if reading fails
     */
    @Test
    void testReadOwnClass() throws IOException {
        byte[] classBytes = loadClassBytes(
                "com/github/toolarium/java/decompiler/reader/impl/AsmClassReaderImpl");
        IStructClass structClass = reader.readClass(classBytes);

        assertEquals("com/github/toolarium/java/decompiler/reader/impl/AsmClassReaderImpl",
                structClass.getName());
        assertTrue(structClass.getAccessFlags().isPublic());
        assertEquals("java/lang/Object", structClass.getSuperName());
    }


    /**
     * Test class version is reasonable.
     *
     * @throws IOException if reading fails
     */
    @Test
    void testClassVersion() throws IOException {
        byte[] classBytes = loadClassBytes(SAMPLE_CLASS);
        IStructClass structClass = reader.readClass(classBytes);

        // Java 8 = 52, Java 11 = 55, Java 17 = 61, Java 21 = 65
        assertTrue(structClass.getMajorVersion() >= 52,
                "Expected major version >= 52 (Java 8+), got: " + structClass.getMajorVersion());
    }


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        ClassReaderFactory factory1 = ClassReaderFactory.getInstance();
        ClassReaderFactory factory2 = ClassReaderFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Load class bytes from the system classloader.
     *
     * @param internalName the class internal name
     * @return the class bytes
     * @throws IOException if loading fails
     */
    private byte[] loadClassBytes(String internalName) throws IOException {
        String resourcePath = internalName + ".class";
        try (InputStream is = ClassLoader.getSystemResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Class not found on classpath: " + internalName);
            }
            return is.readAllBytes();
        }
    }
}
