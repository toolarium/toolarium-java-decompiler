/*
 * JavaDecompilerConfigTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Tests for {@link JavaDecompiler} CLI configuration options.
 *
 * @author patrick
 */
public class JavaDecompilerConfigTest {

    /**
     * Test that --config loads a properties file and applies its settings.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testConfigFileLoaded(@TempDir Path tempDir) throws Exception {
        Path propsFile = tempDir.resolve("decompiler.properties");
        Files.writeString(propsFile, "show.banner = true\n");

        Path classFile = writeClassFile(tempDir, "java/lang/Runnable");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(outputDir);

        JavaDecompiler.main(new String[]{
            "-c", propsFile.toString(),
            "-o", outputDir.toString(),
            classFile.toString()
        });

        Path outputFile = outputDir.resolve("Runnable.java");
        assertTrue(Files.exists(outputFile));
        String source = Files.readString(outputFile);
        assertTrue(source.contains("Decompiled by toolarium-java-decompiler"));
    }


    /**
     * Test that --parallel-threads option is accepted.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testParallelThreadsOption(@TempDir Path tempDir) throws Exception {
        Path classFile = writeClassFile(tempDir, "java/lang/Runnable");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(outputDir);

        JavaDecompiler.main(new String[]{
            "--parallel-threads", "2",
            "-o", outputDir.toString(),
            classFile.toString()
        });

        Path outputFile = outputDir.resolve("Runnable.java");
        assertTrue(Files.exists(outputFile));
    }


    /**
     * Test that sequential mode (--parallel-threads 0) works.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testSequentialMode(@TempDir Path tempDir) throws Exception {
        Path classFile = writeClassFile(tempDir, "java/lang/Runnable");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(outputDir);

        JavaDecompiler.main(new String[]{
            "--parallel-threads", "0",
            "-o", outputDir.toString(),
            classFile.toString()
        });

        Path outputFile = outputDir.resolve("Runnable.java");
        assertTrue(Files.exists(outputFile));
    }


    /**
     * Test that a nonexistent config file prints an error without crashing.
     */
    @Test
    void testConfigFileNotFound() {
        JavaDecompiler.main(new String[]{
            "-c", "nonexistent.properties",
            "nonexistent.class"
        });
    }


    /**
     * Write a real class file to a directory.
     *
     * @param baseDir      the base directory
     * @param internalName the class internal name
     * @return the path to the class file
     * @throws IOException if writing fails
     */
    private Path writeClassFile(Path baseDir, String internalName) throws IOException {
        byte[] classBytes = loadClassBytes(internalName);
        Path classFile = baseDir.resolve(internalName + ".class");
        Files.createDirectories(classFile.getParent());
        Files.write(classFile, classBytes);
        return classFile;
    }


    /**
     * Load class bytes from the classpath.
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
}
