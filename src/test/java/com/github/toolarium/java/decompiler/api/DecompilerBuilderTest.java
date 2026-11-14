/*
 * DecompilerBuilderTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Tests for {@link DecompilerBuilder}.
 *
 * @author patrick
 */
class DecompilerBuilderTest {

    /**
     * Test that the builder creates a valid decompiler.
     */
    @Test
    void testBuildReturnsDecompiler() {
        IDecompiler decompiler = DecompilerBuilder.builder().build();
        assertNotNull(decompiler);
    }


    /**
     * Test decompiling a single class file to a string.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileToString() throws IOException {
        Path classFile = getClassFilePath(String.class);
        String source = DecompilerBuilder.builder()
                .input(classFile)
                .decompileToString();
        assertNotNull(source);
        assertFalse(source.isEmpty());
        assertTrue(source.contains("class"));
    }


    /**
     * Test decompiling to memory returns a populated map.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileToMemory() throws IOException {
        Path classFile = getClassFilePath(String.class);
        Map<String, String> results = DecompilerBuilder.builder()
                .input(classFile)
                .decompileToMemory();
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }


    /**
     * Test decompiling to an output directory.
     *
     * @param tempDir the temporary directory for output
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileToDirectory(@TempDir Path tempDir) throws IOException {
        Path classFile = getClassFilePath(Comparable.class);

        DecompilerBuilder.builder()
                .input(classFile)
                .output(tempDir)
                .decompile();

        // At least one .java file should have been written
        long javaFileCount = Files.walk(tempDir)
                .filter(p -> p.toString().endsWith(".java"))
                .count();
        assertTrue(javaFileCount > 0);
    }


    /**
     * Test decompiling with a custom result saver.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileWithCustomSaver() throws IOException {
        Path classFile = getClassFilePath(Comparable.class);
        IResultSaver saver = ResultSaverFactory.getInstance().getInMemoryResultSaver();

        DecompilerBuilder.builder()
                .input(classFile)
                .resultSaver(saver)
                .decompile();

        assertTrue(saver.getSavedCount() > 0);
    }


    /**
     * Test that decompile throws when no input is configured.
     */
    @Test
    void testDecompileWithoutInputThrows() {
        DecompilerBuilder builder = DecompilerBuilder.builder()
                .output(Path.of("output"));
        assertThrows(IllegalStateException.class, builder::decompile);
    }


    /**
     * Test that decompile throws when no output is configured.
     */
    @Test
    void testDecompileWithoutOutputThrows() {
        Path classFile = getClassFilePath(String.class);
        DecompilerBuilder builder = DecompilerBuilder.builder()
                .input(classFile);
        assertThrows(IllegalStateException.class, builder::decompile);
    }


    /**
     * Test that decompileToMemory throws when no input is configured.
     */
    @Test
    void testDecompileToMemoryWithoutInputThrows() {
        DecompilerBuilder builder = DecompilerBuilder.builder();
        assertThrows(IllegalStateException.class, builder::decompileToMemory);
    }


    /**
     * Test that decompileToString throws when no input is configured.
     */
    @Test
    void testDecompileToStringWithoutInputThrows() {
        DecompilerBuilder builder = DecompilerBuilder.builder();
        assertThrows(IllegalStateException.class, builder::decompileToString);
    }


    /**
     * Test that input(Path) rejects null.
     */
    @Test
    void testInputPathRejectsNull() {
        assertThrows(NullPointerException.class, () ->
                DecompilerBuilder.builder().input((Path) null));
    }


    /**
     * Test that output(Path) rejects null.
     */
    @Test
    void testOutputRejectsNull() {
        assertThrows(NullPointerException.class, () ->
                DecompilerBuilder.builder().output(null));
    }


    /**
     * Test that builder methods return the same builder for chaining.
     */
    @Test
    void testFluentChaining() {
        DecompilerBuilder builder = DecompilerBuilder.builder();
        DecompilerBuilder returned = builder.input(Path.of("test.class"));
        assertEquals(builder, returned);
    }


    /**
     * Test decompiling a directory input to memory.
     *
     * @param tempDir the temporary directory
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileDirectoryToMemory(@TempDir Path tempDir) throws IOException {
        // Copy a class file into the temp directory
        Path classFile = getClassFilePath(Comparable.class);
        byte[] classBytes = Files.readAllBytes(classFile);
        Path targetDir = tempDir.resolve("java").resolve("lang");
        Files.createDirectories(targetDir);
        Files.write(targetDir.resolve("Comparable.class"), classBytes);

        Map<String, String> results = DecompilerBuilder.builder()
                .input(tempDir)
                .decompileToMemory();
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }


    /**
     * Resolve the file system path for a JDK class file.
     * Writes the class bytes to a temporary file to handle bootstrap and module-path classes.
     *
     * @param clazz the class to locate
     * @return the path to the .class file
     * @throws RuntimeException if the class file cannot be located
     */
    private Path getClassFilePath(Class<?> clazz) {
        String resourceName = "/" + clazz.getName().replace('.', '/') + ".class";
        try {
            Path tempFile = Files.createTempFile("test-class-", ".class");
            tempFile.toFile().deleteOnExit();
            try (java.io.InputStream is = clazz.getResourceAsStream(resourceName)) {
                if (is != null) {
                    Files.write(tempFile, is.readAllBytes());
                    return tempFile;
                }
            }
        } catch (IOException ex) {
            // fall through to system classloader
        }

        String systemResourceName = clazz.getName().replace('.', '/') + ".class";
        try {
            Path tempFile = Files.createTempFile("test-class-", ".class");
            tempFile.toFile().deleteOnExit();
            try (java.io.InputStream is = ClassLoader.getSystemResourceAsStream(systemResourceName)) {
                if (is != null) {
                    Files.write(tempFile, is.readAllBytes());
                    return tempFile;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Cannot locate class file for " + clazz.getName(), ex);
        }

        throw new RuntimeException("Cannot locate class file for " + clazz.getName());
    }
}
