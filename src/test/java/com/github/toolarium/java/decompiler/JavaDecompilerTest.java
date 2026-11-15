/*
 * JavaDecompilerTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Tests for {@link JavaDecompiler} CLI.
 *
 * @author patrick
 */
public class JavaDecompilerTest {
    private static final String RUNNABLE_CLASS = "java/lang/Runnable";
    private static final String OUTPUT_OPTION = "-o";


    /**
     * Test help output.
     */
    @Test
    void testHelp() {
        JavaDecompiler.main(new String[]{"-h"});
        // Should not throw
    }


    /**
     * Test version output.
     */
    @Test
    void testVersion() {
        JavaDecompiler.main(new String[]{"--version"});
        // Should not throw
        assertNotNull(Version.VERSION);
    }


    /**
     * Test no arguments shows usage.
     */
    @Test
    void testNoArgs() {
        JavaDecompiler.main(new String[]{});
        // Should not throw
    }


    /**
     * Test decompile a single class file to console.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileSingleFileToConsole(@TempDir Path tempDir) throws Exception {
        Path classFile = writeClassFile(tempDir, RUNNABLE_CLASS);
        JavaDecompiler.main(new String[]{classFile.toString()});
        // Should not throw
    }


    /**
     * Test decompile a single class file to output directory.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileSingleFileToDir(@TempDir Path tempDir) throws Exception {
        Path classFile = writeClassFile(tempDir, RUNNABLE_CLASS);
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(outputDir);

        JavaDecompiler.main(new String[]{OUTPUT_OPTION, outputDir.toString(), classFile.toString()});

        Path outputFile = outputDir.resolve("Runnable.java");
        assertTrue(Files.exists(outputFile));
    }


    /**
     * Test decompile a directory of class files.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileDirectory(@TempDir Path tempDir) throws Exception {
        Path inputDir = tempDir.resolve("classes");
        writeClassFile(inputDir, RUNNABLE_CLASS);
        writeClassFile(inputDir, "java/lang/Comparable");

        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(outputDir);

        JavaDecompiler.main(new String[]{OUTPUT_OPTION, outputDir.toString(), inputDir.toString()});

        assertTrue(Files.exists(outputDir.resolve("java/lang/Runnable.java")));
        assertTrue(Files.exists(outputDir.resolve("java/lang/Comparable.java")));
    }


    /**
     * Test decompile a JAR file.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileJar(@TempDir Path tempDir) throws Exception {
        Path jarFile = createTestJar(tempDir);
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(outputDir);

        JavaDecompiler.main(new String[]{OUTPUT_OPTION, outputDir.toString(), jarFile.toString()});

        assertTrue(Files.exists(outputDir.resolve("java/lang/Runnable.java")));
    }


    /**
     * Test run method with nonexistent input.
     */
    @Test
    void testNonexistentInput() {
        JavaDecompiler.main(new String[]{"nonexistent.class"});
        // Should print error but not throw
    }


    /**
     * Test decompile a single class file to a JAR archive output.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileSingleFileToJar(@TempDir Path tempDir) throws Exception {
        Path classFile = writeClassFile(tempDir, RUNNABLE_CLASS);
        Path outputJar = tempDir.resolve("output.jar");

        JavaDecompiler.main(new String[]{OUTPUT_OPTION, outputJar.toString(), classFile.toString()});

        assertTrue(Files.exists(outputJar));
        try (ZipFile zipFile = new ZipFile(outputJar.toFile())) {
            ZipEntry manifest = zipFile.getEntry("META-INF/MANIFEST.MF");
            assertNotNull(manifest, "JAR should contain a manifest");
            assertNotNull(zipFile.getEntry("Runnable.java"), "JAR should contain Runnable.java");
        }
    }


    /**
     * Test decompile a JAR input to a ZIP archive output.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileJarToZip(@TempDir Path tempDir) throws Exception {
        Path jarFile = createTestJar(tempDir);
        Path outputZip = tempDir.resolve("output.zip");

        JavaDecompiler.main(new String[]{OUTPUT_OPTION, outputZip.toString(), jarFile.toString()});

        assertTrue(Files.exists(outputZip));
        try (ZipFile zipFile = new ZipFile(outputZip.toFile())) {
            List<String> entryNames = new ArrayList<>();
            zipFile.entries().asIterator().forEachRemaining(entry -> entryNames.add(entry.getName()));
            assertTrue(entryNames.contains("java/lang/Runnable.java"),
                    "ZIP should contain java/lang/Runnable.java");
        }
    }


    /**
     * Test decompile a directory to a WAR archive output.
     *
     * @param tempDir the temp directory
     * @throws Exception if an error occurs
     */
    @Test
    void testDecompileDirectoryToWar(@TempDir Path tempDir) throws Exception {
        Path inputDir = tempDir.resolve("classes");
        writeClassFile(inputDir, RUNNABLE_CLASS);

        Path outputWar = tempDir.resolve("output.war");

        JavaDecompiler.main(new String[]{OUTPUT_OPTION, outputWar.toString(), inputDir.toString()});

        assertTrue(Files.exists(outputWar));
        try (ZipFile zipFile = new ZipFile(outputWar.toFile())) {
            ZipEntry manifest = zipFile.getEntry("META-INF/MANIFEST.MF");
            assertNotNull(manifest, "WAR should contain a manifest");
        }
    }


    /**
     * Test unknown option.
     */
    @Test
    void testUnknownOption() {
        JavaDecompiler.main(new String[]{"--unknown"});
        // Should print error but not throw
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
     * Create a test JAR with a single class.
     *
     * @param tempDir the temp directory
     * @return the JAR path
     * @throws Exception if creation fails
     */
    private Path createTestJar(Path tempDir) throws Exception {
        Path jarFile = tempDir.resolve("test.jar");
        byte[] classBytes = loadClassBytes(RUNNABLE_CLASS);

        try (OutputStream fos = Files.newOutputStream(jarFile);
             JarOutputStream jos = new JarOutputStream(fos)) {
            jos.putNextEntry(new JarEntry("java/lang/Runnable.class"));
            jos.write(classBytes);
            jos.closeEntry();
        }
        return jarFile;
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
