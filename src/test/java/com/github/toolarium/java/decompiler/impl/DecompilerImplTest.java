/*
 * DecompilerImplTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.api.DecompilerFactory;
import com.github.toolarium.java.decompiler.api.IDecompiler;
import com.github.toolarium.java.decompiler.api.IResultSaver;
import com.github.toolarium.java.decompiler.api.ResultSaverFactory;
import com.github.toolarium.java.decompiler.impl.output.InMemoryResultSaver;
import com.github.toolarium.java.decompiler.source.ClassSourceFactory;
import com.github.toolarium.java.decompiler.source.IClassSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Tests for {@link DecompilerImpl} and the full decompilation pipeline.
 *
 * @author patrick
 */
public class DecompilerImplTest {
    private static final String JAVA_LANG_INTEGER = "java/lang/Integer";
    private static final String JAVA_LANG_RUNNABLE = "java/lang/Runnable";
    private final IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler();


    /**
     * Test decompiling a simple JDK class (java.lang.Comparable).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileComparable() throws IOException {
        byte[] classBytes = loadClassBytes("java/lang/Comparable");
        String source = decompiler.decompile(classBytes);

        assertNotNull(source);
        assertFalse(source.isEmpty());
        assertTrue(source.contains("interface"));
        assertTrue(source.contains("Comparable"));
    }


    /**
     * Test decompiling java.lang.Integer shows class structure.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileInteger() throws IOException {
        byte[] classBytes = loadClassBytes(JAVA_LANG_INTEGER);
        String source = decompiler.decompile(classBytes);

        assertNotNull(source);
        assertTrue(source.contains("class Integer"));
        assertTrue(source.contains("public"));
        // Should contain method declarations
        assertTrue(source.contains("intValue"));
    }


    /**
     * Test decompiling an enum class.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileEnum() throws IOException {
        byte[] classBytes = loadClassBytes("java/lang/Thread$State");
        String source = decompiler.decompile(classBytes);

        assertNotNull(source);
        assertFalse(source.isEmpty());
        assertTrue(source.contains("State"));
    }


    /**
     * Test decompiling via IClassSource.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileFromClassSource(@TempDir Path tempDir) throws Exception {
        // Write a real class file to a temp directory
        byte[] classBytes = loadClassBytes(JAVA_LANG_RUNNABLE);
        Path classFile = tempDir.resolve("java/lang/Runnable.class");
        Files.createDirectories(classFile.getParent());
        Files.write(classFile, classBytes);

        try (IClassSource source = ClassSourceFactory.getInstance().getDirectoryClassSource(tempDir)) {
            String result = decompiler.decompile(source, JAVA_LANG_RUNNABLE);
            assertNotNull(result);
            assertTrue(result.contains("Runnable"));
            assertTrue(result.contains("interface"));
        }
    }


    /**
     * Test decompileAll with InMemoryResultSaver.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileAllInMemory(@TempDir Path tempDir) throws Exception {
        // Create a temp dir with two class files
        writeClassToDir(tempDir, JAVA_LANG_RUNNABLE);
        writeClassToDir(tempDir, "java/lang/Comparable");

        try (IClassSource source = ClassSourceFactory.getInstance().getDirectoryClassSource(tempDir)) {
            InMemoryResultSaver saver = new InMemoryResultSaver();
            decompiler.decompileAll(source, saver);

            assertEquals(2, saver.getSavedCount());
            assertTrue(saver.getResults().containsKey(JAVA_LANG_RUNNABLE));
            assertTrue(saver.getResults().containsKey("java/lang/Comparable"));
            assertFalse(saver.getResults().get(JAVA_LANG_RUNNABLE).isEmpty());
        }
    }


    /**
     * Test decompileAll with FileResultSaver.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileAllToFiles(@TempDir Path tempDir) throws Exception {
        Path inputDir = tempDir.resolve("input");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(inputDir);
        Files.createDirectories(outputDir);

        writeClassToDir(inputDir, JAVA_LANG_RUNNABLE);

        try (IClassSource source = ClassSourceFactory.getInstance().getDirectoryClassSource(inputDir);
             IResultSaver saver = ResultSaverFactory.getInstance().getFileResultSaver(outputDir)) {
            decompiler.decompileAll(source, saver);
            assertEquals(1, saver.getSavedCount());
        }

        Path outputFile = outputDir.resolve("java/lang/Runnable.java");
        assertTrue(Files.exists(outputFile));
        String content = new String(Files.readAllBytes(outputFile));
        assertTrue(content.contains("Runnable"));
    }


    /**
     * Test decompiling from a JAR source.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileFromJar(@TempDir Path tempDir) throws Exception {
        Path jarFile = tempDir.resolve("test.jar");
        byte[] runnableBytes = loadClassBytes(JAVA_LANG_RUNNABLE);

        try (OutputStream fos = Files.newOutputStream(jarFile);
             JarOutputStream jos = new JarOutputStream(fos)) {
            jos.putNextEntry(new JarEntry("java/lang/Runnable.class"));
            jos.write(runnableBytes);
            jos.closeEntry();
        }

        try (IClassSource source = ClassSourceFactory.getInstance().getJarClassSource(jarFile)) {
            InMemoryResultSaver saver = new InMemoryResultSaver();
            decompiler.decompileAll(source, saver);
            assertEquals(1, saver.getSavedCount());
            assertTrue(saver.getResults().get(JAVA_LANG_RUNNABLE).contains("interface"));
        }
    }


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        DecompilerFactory factory1 = DecompilerFactory.getInstance();
        DecompilerFactory factory2 = DecompilerFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Test ResultSaverFactory singleton.
     */
    @Test
    void testResultSaverFactory() {
        ResultSaverFactory factory = ResultSaverFactory.getInstance();
        assertNotNull(factory);
        assertNotNull(factory.getInMemoryResultSaver());
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
                throw new IOException("Class not found: " + internalName);
            }
            return is.readAllBytes();
        }
    }


    /**
     * Write a class file from the classpath to a directory.
     *
     * @param dir          the target directory
     * @param internalName the class internal name
     * @throws IOException if an error occurs
     */
    private void writeClassToDir(Path dir, String internalName) throws IOException {
        byte[] classBytes = loadClassBytes(internalName);
        Path classFile = dir.resolve(internalName + ".class");
        Files.createDirectories(classFile.getParent());
        Files.write(classFile, classBytes);
    }
}
