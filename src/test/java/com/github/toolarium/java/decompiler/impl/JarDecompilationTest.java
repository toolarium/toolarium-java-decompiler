/*
 * JarDecompilationTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.api.DecompilerFactory;
import com.github.toolarium.java.decompiler.api.IDecompiler;
import com.github.toolarium.java.decompiler.impl.output.InMemoryResultSaver;
import com.github.toolarium.java.decompiler.source.ClassSourceFactory;
import com.github.toolarium.java.decompiler.source.IClassSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Integration tests that decompile entire JARs and verify output.
 *
 * @author patrick
 */
public class JarDecompilationTest {
    private static final String[] SAMPLE_CLASSES = {
        "java/lang/Runnable",
        "java/lang/Comparable",
        "java/lang/Iterable",
        "java/io/Serializable",
        "java/io/Closeable"
    };
    private final IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler();


    /**
     * Test decompiling a JAR with multiple interfaces.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileMultiClassJar(@TempDir Path tempDir) throws Exception {
        Path jarFile = createJarWithClasses(tempDir, SAMPLE_CLASSES);

        try (IClassSource source = ClassSourceFactory.getInstance().getJarClassSource(jarFile)) {
            InMemoryResultSaver saver = new InMemoryResultSaver();
            decompiler.decompileAll(source, saver);

            assertTrue(saver.getSavedCount() >= SAMPLE_CLASSES.length, "Expected at least " + SAMPLE_CLASSES.length + " decompiled classes");

            for (String className : SAMPLE_CLASSES) {
                assertTrue(saver.getResults().containsKey(className), "Missing decompiled class: " + className);
                String src = saver.getResults().get(className);
                assertNotNull(src);
                assertFalse(src.isEmpty());
            }
        }
    }


    /**
     * Test that all decompiled sources contain class or interface keyword.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testAllOutputsHaveClassOrInterface(@TempDir Path tempDir) throws Exception {
        Path jarFile = createJarWithClasses(tempDir, SAMPLE_CLASSES);

        try (IClassSource source = ClassSourceFactory.getInstance().getJarClassSource(jarFile)) {
            InMemoryResultSaver saver = new InMemoryResultSaver();
            decompiler.decompileAll(source, saver);

            for (Map.Entry<String, String> entry : saver.getResults().entrySet()) {
                String src = entry.getValue();
                boolean hasClassOrInterface = src.contains("class ") || src.contains("interface ");
                assertTrue(hasClassOrInterface, "Output for " + entry.getKey() + " missing class/interface keyword");
            }
        }
    }


    /**
     * Test that all decompiled sources have balanced braces.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testAllOutputsHaveBalancedBraces(@TempDir Path tempDir) throws Exception {
        Path jarFile = createJarWithClasses(tempDir, SAMPLE_CLASSES);

        try (IClassSource source = ClassSourceFactory.getInstance().getJarClassSource(jarFile)) {
            InMemoryResultSaver saver = new InMemoryResultSaver();
            decompiler.decompileAll(source, saver);

            for (Map.Entry<String, String> entry : saver.getResults().entrySet()) {
                int opens = countChar(entry.getValue(), '{');
                int closes = countChar(entry.getValue(), '}');
                assertTrue(opens == closes, entry.getKey() + " has unbalanced braces: { = " + opens + ", } = " + closes);
            }
        }
    }


    /**
     * Test decompiling to files and reading back.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileToFilesAndReadBack(@TempDir Path tempDir) throws Exception {
        Path jarFile = createJarWithClasses(tempDir, new String[]{"java/lang/Runnable"});
        Path outputDir = tempDir.resolve("output");

        try (IClassSource source = ClassSourceFactory.getInstance().getJarClassSource(jarFile)) {
            com.github.toolarium.java.decompiler.api.IResultSaver saver = com.github.toolarium.java.decompiler.api.ResultSaverFactory.getInstance().getFileResultSaver(outputDir);
            decompiler.decompileAll(source, saver);
            saver.close();
        }

        Path outputFile = outputDir.resolve("java/lang/Runnable.java");
        assertTrue(Files.exists(outputFile), "Expected output file: " + outputFile);

        String content = new String(Files.readAllBytes(outputFile));
        assertTrue(content.contains("interface"));
        assertTrue(content.contains("Runnable"));
        assertTrue(content.contains("run"));
    }


    /**
     * Test decompiling a directory of class files.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileDirectory(@TempDir Path tempDir) throws Exception {
        Path classDir = tempDir.resolve("classes");
        for (String className : SAMPLE_CLASSES) {
            writeClassFile(classDir, className);
        }

        try (IClassSource source = ClassSourceFactory.getInstance().getDirectoryClassSource(classDir)) {
            InMemoryResultSaver saver = new InMemoryResultSaver();
            decompiler.decompileAll(source, saver);

            assertTrue(saver.getSavedCount() >= SAMPLE_CLASSES.length);
        }
    }


    /**
     * Create a JAR file containing the specified classes from the runtime.
     *
     * @param tempDir    the temp directory
     * @param classNames the class internal names
     * @return the JAR file path
     * @throws Exception if creation fails
     */
    private Path createJarWithClasses(Path tempDir, String[] classNames) throws Exception {
        Path jarFile = tempDir.resolve("test.jar");
        try (OutputStream fos = Files.newOutputStream(jarFile);
             JarOutputStream jos = new JarOutputStream(fos)) {
            for (String className : classNames) {
                byte[] classBytes = loadClassBytes(className);
                jos.putNextEntry(new JarEntry(className + ".class"));
                jos.write(classBytes);
                jos.closeEntry();
            }
        }
        return jarFile;
    }


    /**
     * Write a class file from the runtime to a directory.
     *
     * @param baseDir      the base directory
     * @param internalName the class internal name
     * @throws IOException if writing fails
     */
    private void writeClassFile(Path baseDir, String internalName) throws IOException {
        byte[] classBytes = loadClassBytes(internalName);
        Path classFile = baseDir.resolve(internalName + ".class");
        Files.createDirectories(classFile.getParent());
        Files.write(classFile, classBytes);
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
     * Count occurrences of a character in a string.
     *
     * @param str the string
     * @param ch  the character
     * @return the count
     */
    private int countChar(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }
}
