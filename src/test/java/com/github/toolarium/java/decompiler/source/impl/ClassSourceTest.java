/*
 * ClassSourceTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.source.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.source.ClassSourceFactory;
import com.github.toolarium.java.decompiler.source.IClassSource;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Tests for class source implementations.
 *
 * @author patrick
 */
public class ClassSourceTest {

    /**
     * Test single file class source.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testSingleFileClassSource(@TempDir Path tempDir) throws Exception {
        Path classFile = tempDir.resolve("MyClass.class");
        Files.write(classFile, new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE});

        try (IClassSource source = ClassSourceFactory.getInstance().getSingleFileClassSource(classFile)) {
            List<String> classes = source.listClasses();
            assertEquals(1, classes.size());
            assertEquals("MyClass", classes.get(0));

            assertTrue(source.hasClass("MyClass"));
            assertFalse(source.hasClass("Other"));

            byte[] bytes = source.readClass("MyClass");
            assertEquals(4, bytes.length);
            assertEquals((byte) 0xCA, bytes[0]);
        }
    }


    /**
     * Test single file class source with missing class.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testSingleFileClassSourceNotFound(@TempDir Path tempDir) throws Exception {
        Path classFile = tempDir.resolve("Test.class");
        Files.write(classFile, new byte[]{1});

        try (IClassSource source = ClassSourceFactory.getInstance().getSingleFileClassSource(classFile)) {
            assertThrows(IOException.class, () -> source.readClass("Other"));
        }
    }


    /**
     * Test directory class source.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDirectoryClassSource(@TempDir Path tempDir) throws Exception {
        // Create a directory structure with .class files
        Path pkgDir = tempDir.resolve("com/example");
        Files.createDirectories(pkgDir);
        Files.write(pkgDir.resolve("Foo.class"), new byte[]{1, 2, 3});
        Files.write(pkgDir.resolve("Bar.class"), new byte[]{4, 5, 6});
        Files.write(tempDir.resolve("Root.class"), new byte[]{7, 8});

        try (IClassSource source = ClassSourceFactory.getInstance().getDirectoryClassSource(tempDir)) {
            List<String> classes = source.listClasses();
            assertEquals(3, classes.size());
            assertTrue(classes.contains("com/example/Foo"));
            assertTrue(classes.contains("com/example/Bar"));
            assertTrue(classes.contains("Root"));

            assertTrue(source.hasClass("com/example/Foo"));
            assertFalse(source.hasClass("com/example/Missing"));

            byte[] fooBytes = source.readClass("com/example/Foo");
            assertEquals(3, fooBytes.length);
        }
    }


    /**
     * Test directory class source with missing class.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDirectoryClassSourceNotFound(@TempDir Path tempDir) throws Exception {
        try (IClassSource source = ClassSourceFactory.getInstance().getDirectoryClassSource(tempDir)) {
            assertThrows(IOException.class, () -> source.readClass("Missing"));
        }
    }


    /**
     * Test JAR class source.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testJarClassSource(@TempDir Path tempDir) throws Exception {
        Path jarFile = tempDir.resolve("test.jar");
        createTestJar(jarFile);

        try (IClassSource source = ClassSourceFactory.getInstance().getJarClassSource(jarFile)) {
            List<String> classes = source.listClasses();
            assertEquals(2, classes.size());
            assertTrue(classes.contains("com/example/Alpha"));
            assertTrue(classes.contains("com/example/Beta"));

            assertTrue(source.hasClass("com/example/Alpha"));
            assertFalse(source.hasClass("com/example/Gamma"));

            byte[] alphaBytes = source.readClass("com/example/Alpha");
            assertNotNull(alphaBytes);
            assertTrue(alphaBytes.length > 0);
        }
    }


    /**
     * Test JAR class source with missing class.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testJarClassSourceNotFound(@TempDir Path tempDir) throws Exception {
        Path jarFile = tempDir.resolve("test.jar");
        createTestJar(jarFile);

        try (IClassSource source = ClassSourceFactory.getInstance().getJarClassSource(jarFile)) {
            assertThrows(IOException.class, () -> source.readClass("com/example/Missing"));
        }
    }


    /**
     * Test JarClassSource lists class entries and ignores non-class entries.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testJarClassSourceListsEntries(@TempDir Path tempDir) throws Exception {
        Path jarFile = tempDir.resolve("multi.jar");
        try (OutputStream fos = Files.newOutputStream(jarFile);
             JarOutputStream jos = new JarOutputStream(fos)) {
            for (int i = 0; i < 10; i++) {
                jos.putNextEntry(new JarEntry("pkg/Class" + i + ".class"));
                jos.write(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, 0, 0, 0, 52});
                jos.closeEntry();
            }
            jos.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            jos.write("Manifest-Version: 1.0\n".getBytes());
            jos.closeEntry();
            jos.putNextEntry(new JarEntry("resource.txt"));
            jos.write("data".getBytes());
            jos.closeEntry();
        }

        try (IClassSource source = ClassSourceFactory.getInstance().getJarClassSource(jarFile)) {
            List<String> classes = source.listClasses();
            assertEquals(10, classes.size());
            for (int i = 0; i < 10; i++) {
                assertTrue(classes.contains("pkg/Class" + i));
            }
            assertFalse(classes.stream().anyMatch(c -> c.contains("MANIFEST")));
            assertFalse(classes.stream().anyMatch(c -> c.contains("resource")));
        }
    }


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        ClassSourceFactory factory1 = ClassSourceFactory.getInstance();
        ClassSourceFactory factory2 = ClassSourceFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Create a test JAR file with two dummy class entries.
     *
     * @param jarFile the path for the JAR file
     * @throws Exception if creation fails
     */
    private void createTestJar(Path jarFile) throws Exception {
        try (OutputStream fos = Files.newOutputStream(jarFile);
             JarOutputStream jos = new JarOutputStream(fos)) {
            jos.putNextEntry(new JarEntry("com/example/Alpha.class"));
            jos.write(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, 0, 0, 0, 52});
            jos.closeEntry();

            jos.putNextEntry(new JarEntry("com/example/Beta.class"));
            jos.write(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, 0, 0, 0, 52});
            jos.closeEntry();

            // Add a non-class entry that should be ignored
            jos.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            jos.write("Manifest-Version: 1.0\n".getBytes());
            jos.closeEntry();
        }
    }
}
