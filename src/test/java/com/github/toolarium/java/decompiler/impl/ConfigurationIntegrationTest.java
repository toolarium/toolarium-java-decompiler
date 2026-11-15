/*
 * ConfigurationIntegrationTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.api.DecompilerConfigurationBuilder;
import com.github.toolarium.java.decompiler.api.DecompilerFactory;
import com.github.toolarium.java.decompiler.api.IDecompiler;
import com.github.toolarium.java.decompiler.api.IDecompilerConfiguration;
import com.github.toolarium.java.decompiler.impl.output.InMemoryResultSaver;
import com.github.toolarium.java.decompiler.source.ClassSourceFactory;
import com.github.toolarium.java.decompiler.source.IClassSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Integration tests that verify configuration toggles actually change decompilation output.
 *
 * @author patrick
 */
public class ConfigurationIntegrationTest {
    private static final String JAVA_LANG_RUNNABLE = "java/lang/Runnable";
    private static final String JAVA_LANG_COMPARABLE = "java/lang/Comparable";


    /**
     * Test that showBanner(true) inserts the banner comment into decompiled output.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testShowBannerAddsComment() throws IOException {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .showBanner(true)
                .build();
        IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler(config);

        byte[] classBytes = loadClassBytes(JAVA_LANG_RUNNABLE);
        String source = decompiler.decompile(classBytes);

        assertTrue(source.contains("// Decompiled by toolarium-java-decompiler"));
    }


    /**
     * Test that the default configuration (showBanner false) does not include the banner.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testShowBannerFalseNoBanner() throws IOException {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .showBanner(false)
                .build();
        IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler(config);

        byte[] classBytes = loadClassBytes(JAVA_LANG_RUNNABLE);
        String source = decompiler.decompile(classBytes);

        assertFalse(source.contains("// Decompiled by toolarium-java-decompiler"));
    }


    /**
     * Test that showSourceFile(true) inserts a source file comment into decompiled output.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testShowSourceFileAddsComment() throws IOException {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .showSourceFile(true)
                .build();
        IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler(config);

        byte[] classBytes = loadClassBytes(JAVA_LANG_RUNNABLE);
        String source = decompiler.decompile(classBytes);

        assertTrue(source.contains("// Source: Runnable.java"));
    }


    /**
     * Test that a custom indent string (tab) is used in the decompiled output.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testCustomIndentString() throws IOException {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .indentString("\t")
                .build();
        IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler(config);

        byte[] classBytes = loadClassBytes(JAVA_LANG_RUNNABLE);
        String source = decompiler.decompile(classBytes);

        assertTrue(source.contains("\t"), "Output should contain tab indentation");
        assertFalse(source.contains("    "), "Output should not contain 4-space indentation");
    }


    /**
     * Test that a custom line separator (CRLF) is used in the decompiled output.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testCustomLineSeparator() throws IOException {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .lineSeparator("\r\n")
                .build();
        IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler(config);

        byte[] classBytes = loadClassBytes(JAVA_LANG_RUNNABLE);
        String source = decompiler.decompile(classBytes);

        assertTrue(source.contains("\r\n"), "Output should contain CRLF line separators");
    }


    /**
     * Test that the includeClassRegex filter restricts which classes are decompiled.
     *
     * @param tempDir the temporary directory for test class files
     * @throws Exception if an error occurs
     */
    @Test
    void testIncludeClassRegexFilters(@TempDir Path tempDir) throws Exception {
        writeClassToDir(tempDir, JAVA_LANG_RUNNABLE);
        writeClassToDir(tempDir, JAVA_LANG_COMPARABLE);

        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .includeClassRegex(".*Runnable")
                .build();
        IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler(config);

        try (IClassSource source = ClassSourceFactory.getInstance().getDirectoryClassSource(tempDir)) {
            InMemoryResultSaver saver = new InMemoryResultSaver();
            decompiler.decompileAll(source, saver);

            assertEquals(1, saver.getSavedCount());
            assertTrue(saver.getResults().containsKey(JAVA_LANG_RUNNABLE));
            assertFalse(saver.getResults().containsKey(JAVA_LANG_COMPARABLE));
        }
    }


    /**
     * Test that the excludeClassRegex filter skips matching classes during batch decompilation.
     *
     * @param tempDir the temporary directory for test class files
     * @throws Exception if an error occurs
     */
    @Test
    void testExcludeClassRegexFilters(@TempDir Path tempDir) throws Exception {
        writeClassToDir(tempDir, JAVA_LANG_RUNNABLE);
        writeClassToDir(tempDir, JAVA_LANG_COMPARABLE);

        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .excludeClassRegex(".*Comparable")
                .build();
        IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler(config);

        try (IClassSource source = ClassSourceFactory.getInstance().getDirectoryClassSource(tempDir)) {
            InMemoryResultSaver saver = new InMemoryResultSaver();
            decompiler.decompileAll(source, saver);

            assertEquals(1, saver.getSavedCount());
            assertTrue(saver.getResults().containsKey(JAVA_LANG_RUNNABLE));
            assertFalse(saver.getResults().containsKey(JAVA_LANG_COMPARABLE));
        }
    }


    /**
     * Test that the default parallelThreads value equals the number of available processors.
     */
    @Test
    void testParallelThreadsDefault() {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder().build();
        assertEquals(Runtime.getRuntime().availableProcessors(), config.getParallelThreads());
    }


    /**
     * Test that parallelThreads(1) (sequential mode) still produces correct output.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testParallelThreadsSequential() throws IOException {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .parallelThreads(1)
                .build();
        IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler(config);

        byte[] classBytes = loadClassBytes(JAVA_LANG_RUNNABLE);
        String source = decompiler.decompile(classBytes);

        assertNotNull(source);
        assertTrue(source.contains("Runnable"));
        assertTrue(source.contains("interface"));
    }


    /**
     * Test that parallel decompilation with 4 threads produces the same output
     * as sequential decompilation.
     *
     * @param tempDir the temporary directory for test class files
     * @throws Exception if an error occurs
     */
    @Test
    void testParallelDecompilationProducesCorrectOutput(@TempDir Path tempDir) throws Exception {
        writeClassToDir(tempDir, JAVA_LANG_RUNNABLE);
        writeClassToDir(tempDir, JAVA_LANG_COMPARABLE);
        writeClassToDir(tempDir, "java/lang/AutoCloseable");

        // Decompile sequentially
        IDecompilerConfiguration seqConfig = DecompilerConfigurationBuilder.builder()
                .parallelThreads(1)
                .build();
        IDecompiler seqDecompiler = DecompilerFactory.getInstance().getDecompiler(seqConfig);

        InMemoryResultSaver seqSaver = new InMemoryResultSaver();
        try (IClassSource source = ClassSourceFactory.getInstance().getDirectoryClassSource(tempDir)) {
            seqDecompiler.decompileAll(source, seqSaver);
        }

        // Decompile in parallel with 4 threads
        IDecompilerConfiguration parConfig = DecompilerConfigurationBuilder.builder()
                .parallelThreads(4)
                .build();
        IDecompiler parDecompiler = DecompilerFactory.getInstance().getDecompiler(parConfig);

        InMemoryResultSaver parSaver = new InMemoryResultSaver();
        try (IClassSource source = ClassSourceFactory.getInstance().getDirectoryClassSource(tempDir)) {
            parDecompiler.decompileAll(source, parSaver);
        }

        // Both should produce the same number of results
        assertEquals(seqSaver.getSavedCount(), parSaver.getSavedCount());

        // Both should produce identical source for every class
        for (String className : seqSaver.getResults().keySet()) {
            assertTrue(parSaver.getResults().containsKey(className),
                    "Parallel results should contain class: " + className);
            assertEquals(seqSaver.getResults().get(className),
                    parSaver.getResults().get(className),
                    "Source should be identical for class: " + className);
        }
    }


    /**
     * Load class bytes from the system classloader.
     *
     * @param internalName the class internal name (e.g., "java/lang/Runnable")
     * @return the class bytes
     * @throws IOException if the class cannot be found or read
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
     * Write a class file loaded from the classpath into a directory structure.
     *
     * @param dir          the root directory
     * @param internalName the class internal name (e.g., "java/lang/Runnable")
     * @throws IOException if writing fails
     */
    private void writeClassToDir(Path dir, String internalName) throws IOException {
        byte[] classBytes = loadClassBytes(internalName);
        Path classFile = dir.resolve(internalName + ".class");
        Files.createDirectories(classFile.getParent());
        Files.write(classFile, classBytes);
    }
}
