/*
 * ResultSaverTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.impl.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.api.IResultSaver;
import com.github.toolarium.java.decompiler.api.ResultSaverFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Direct unit tests for {@link InMemoryResultSaver}, {@link FileResultSaver},
 * and {@link ArchiveResultSaver}.
 *
 * @author patrick
 */
public class ResultSaverTest {
    private static final String FOO_CLASS = "com/example/Foo";
    private static final String FOO_SOURCE = "class Foo {}";


    /**
     * Test that InMemoryResultSaver stores and retrieves a single saved source.
     *
     * @throws Exception if an error occurs
     */
    @Test
    void testInMemorySaveAndRetrieve() throws Exception {
        try (IResultSaver saver = ResultSaverFactory.getInstance().getInMemoryResultSaver()) {
            saver.saveSource(FOO_CLASS, "public class Foo {}");

            Map<String, String> results = saver.getResults();
            assertEquals(1, results.size());
            assertEquals("public class Foo {}", results.get(FOO_CLASS));
        }
    }


    /**
     * Test that InMemoryResultSaver correctly tracks multiple saves.
     *
     * @throws Exception if an error occurs
     */
    @Test
    void testInMemoryMultipleSaves() throws Exception {
        try (IResultSaver saver = ResultSaverFactory.getInstance().getInMemoryResultSaver()) {
            saver.saveSource(FOO_CLASS, FOO_SOURCE);
            saver.saveSource("com/example/Bar", "class Bar {}");
            saver.saveSource("com/example/Baz", "class Baz {}");

            assertEquals(3, saver.getSavedCount());

            Map<String, String> results = saver.getResults();
            assertEquals(3, results.size());
            assertEquals(FOO_SOURCE, results.get(FOO_CLASS));
            assertEquals("class Bar {}", results.get("com/example/Bar"));
            assertEquals("class Baz {}", results.get("com/example/Baz"));
        }
    }


    /**
     * Test that InMemoryResultSaver close() can be called multiple times without error.
     *
     * @throws Exception if an error occurs
     */
    @Test
    void testInMemoryCloseIsIdempotent() throws Exception {
        IResultSaver saver = ResultSaverFactory.getInstance().getInMemoryResultSaver();
        saver.saveSource(FOO_CLASS, FOO_SOURCE);
        saver.close();
        saver.close();
        saver.close();
        assertEquals(1, saver.getSavedCount());
    }


    /**
     * Test that InMemoryResultSaver getResults() returns an unmodifiable snapshot.
     *
     * @throws Exception if an error occurs
     */
    @Test
    void testInMemoryGetResultsReturnsUnmodifiable() throws Exception {
        try (IResultSaver saver = ResultSaverFactory.getInstance().getInMemoryResultSaver()) {
            saver.saveSource(FOO_CLASS, FOO_SOURCE);

            Map<String, String> results = saver.getResults();
            assertThrows(UnsupportedOperationException.class,
                    () -> results.put("illegal", "value"));
        }
    }


    /**
     * Test that FileResultSaver creates a .java file in the output directory.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testFileSaveCreatesFile(@TempDir Path tempDir) throws Exception {
        try (IResultSaver saver = ResultSaverFactory.getInstance().getFileResultSaver(tempDir)) {
            saver.saveSource("MyClass", "public class MyClass {}");

            Path outputFile = tempDir.resolve("MyClass.java");
            assertTrue(Files.exists(outputFile));
            assertEquals("public class MyClass {}", Files.readString(outputFile));
        }
    }


    /**
     * Test that FileResultSaver creates subdirectories for package-qualified class names.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testFileSaveCreatesSubdirectories(@TempDir Path tempDir) throws Exception {
        try (IResultSaver saver = ResultSaverFactory.getInstance().getFileResultSaver(tempDir)) {
            saver.saveSource(FOO_CLASS, "package com.example;\npublic class Foo {}");

            Path outputFile = tempDir.resolve("com").resolve("example").resolve("Foo.java");
            assertTrue(Files.exists(outputFile));
            assertTrue(Files.isDirectory(tempDir.resolve("com").resolve("example")));
        }
    }


    /**
     * Test that FileResultSaver overwrites an existing file when saving the same class again.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testFileOverwriteExistingFile(@TempDir Path tempDir) throws Exception {
        try (IResultSaver saver = ResultSaverFactory.getInstance().getFileResultSaver(tempDir)) {
            saver.saveSource("MyClass", "version 1");
            saver.saveSource("MyClass", "version 2");

            Path outputFile = tempDir.resolve("MyClass.java");
            assertEquals("version 2", Files.readString(outputFile));
            assertEquals(2, saver.getSavedCount());
        }
    }


    /**
     * Test that FileResultSaver tracks the saved count correctly.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testFileGetSavedCount(@TempDir Path tempDir) throws Exception {
        try (IResultSaver saver = ResultSaverFactory.getInstance().getFileResultSaver(tempDir)) {
            assertEquals(0, saver.getSavedCount());

            saver.saveSource("Foo", FOO_SOURCE);
            assertEquals(1, saver.getSavedCount());

            saver.saveSource("Bar", "class Bar {}");
            assertEquals(2, saver.getSavedCount());
        }
    }


    /**
     * Test that FileResultSaver close() can be called multiple times without error.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testFileCloseIsIdempotent(@TempDir Path tempDir) throws Exception {
        IResultSaver saver = ResultSaverFactory.getInstance().getFileResultSaver(tempDir);
        saver.saveSource("Foo", FOO_SOURCE);
        saver.close();
        saver.close();
        saver.close();
        assertEquals(1, saver.getSavedCount());
    }


    /**
     * Test that FileResultSaver rejects path traversal attempts.
     *
     * @param tempDir the temporary directory for the test
     */
    @Test
    void testFileRejectsPathTraversal(@TempDir Path tempDir) {
        IResultSaver saver = ResultSaverFactory.getInstance().getFileResultSaver(tempDir);
        assertThrows(IOException.class,
                () -> saver.saveSource("../../etc/passwd", "malicious"));
    }


    /**
     * Test that FileResultSaver getResults() returns an empty map.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testFileGetResultsReturnsEmptyMap(@TempDir Path tempDir) throws Exception {
        try (IResultSaver saver = ResultSaverFactory.getInstance().getFileResultSaver(tempDir)) {
            saver.saveSource("Foo", FOO_SOURCE);
            assertTrue(saver.getResults().isEmpty());
        }
    }


    /**
     * Test that InMemoryResultSaver drops entries after the hard limit of 100,000.
     *
     * @throws Exception if an error occurs
     */
    @Test
    void testInMemoryHardMaxEntriesDrop() throws Exception {
        try (IResultSaver saver = ResultSaverFactory.getInstance().getInMemoryResultSaver()) {
            for (int i = 0; i < 100_001; i++) {
                saver.saveSource("Class" + i, "");
            }

            assertEquals(100_000, saver.getSavedCount());
            assertEquals(100_000, saver.getResults().size());
            assertTrue(saver.getResults().containsKey("Class0"));
            assertTrue(saver.getResults().containsKey("Class99999"));
            assertFalse(saver.getResults().containsKey("Class100000"));
        }
    }


    /**
     * Test that ArchiveResultSaver creates a ZIP file with the saved sources.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testArchiveSaveCreatesZip(@TempDir Path tempDir) throws Exception {
        Path zipPath = tempDir.resolve("output.zip");
        try (IResultSaver saver = ResultSaverFactory.getInstance().getArchiveResultSaver(zipPath)) {
            saver.saveSource(FOO_CLASS, FOO_SOURCE);
            saver.saveSource("com/example/Bar", "class Bar {}");
        }

        assertTrue(Files.exists(zipPath));
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            List<String> entryNames = new ArrayList<>();
            zipFile.entries().asIterator().forEachRemaining(e -> entryNames.add(e.getName()));
            assertTrue(entryNames.contains("com/example/Foo.java"));
            assertTrue(entryNames.contains("com/example/Bar.java"));
        }
    }


    /**
     * Test that ArchiveResultSaver creates a JAR file with a manifest.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testArchiveSaveCreatesJar(@TempDir Path tempDir) throws Exception {
        Path jarPath = tempDir.resolve("output.jar");
        try (IResultSaver saver = ResultSaverFactory.getInstance().getArchiveResultSaver(jarPath)) {
            saver.saveSource("MyClass", "public class MyClass {}");
        }

        assertTrue(Files.exists(jarPath));
        try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
            ZipEntry manifest = zipFile.getEntry("META-INF/MANIFEST.MF");
            assertNotNull(manifest, "JAR should contain a manifest");
            assertNotNull(zipFile.getEntry("MyClass.java"));
        }
    }


    /**
     * Test that ArchiveResultSaver tracks the saved count correctly.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testArchiveGetSavedCount(@TempDir Path tempDir) throws Exception {
        Path zipPath = tempDir.resolve("output.zip");
        try (IResultSaver saver = ResultSaverFactory.getInstance().getArchiveResultSaver(zipPath)) {
            assertEquals(0, saver.getSavedCount());
            saver.saveSource("Foo", FOO_SOURCE);
            assertEquals(1, saver.getSavedCount());
            saver.saveSource("Bar", "class Bar {}");
            assertEquals(2, saver.getSavedCount());
        }
    }


    /**
     * Test that ArchiveResultSaver getResults() returns an empty map.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testArchiveGetResultsReturnsEmptyMap(@TempDir Path tempDir) throws Exception {
        Path zipPath = tempDir.resolve("output.zip");
        try (IResultSaver saver = ResultSaverFactory.getInstance().getArchiveResultSaver(zipPath)) {
            saver.saveSource("Foo", FOO_SOURCE);
            assertTrue(saver.getResults().isEmpty());
        }
    }


    /**
     * Test that ResultSaverFactory.getResultSaver detects archive extensions.
     *
     * @param tempDir the temporary directory for the test
     * @throws Exception if an error occurs
     */
    @Test
    void testFactoryDetectsArchiveExtension(@TempDir Path tempDir) throws Exception {
        Path jarPath = tempDir.resolve("output.jar");
        try (IResultSaver saver = ResultSaverFactory.getInstance().getResultSaver(jarPath)) {
            assertTrue(saver instanceof ArchiveResultSaver);
        }

        try (IResultSaver saver = ResultSaverFactory.getInstance().getResultSaver(tempDir)) {
            assertTrue(saver instanceof FileResultSaver);
        }
    }


    /**
     * Test that ArchiveResultSaver.isArchiveExtension recognizes supported extensions.
     */
    @Test
    void testIsArchiveExtension() {
        assertTrue(ArchiveResultSaver.isArchiveExtension("output.jar"));
        assertTrue(ArchiveResultSaver.isArchiveExtension("output.zip"));
        assertTrue(ArchiveResultSaver.isArchiveExtension("output.war"));
        assertTrue(ArchiveResultSaver.isArchiveExtension("output.ear"));
        assertTrue(ArchiveResultSaver.isArchiveExtension("OUTPUT.JAR"));
        assertFalse(ArchiveResultSaver.isArchiveExtension("output.txt"));
        assertFalse(ArchiveResultSaver.isArchiveExtension(null));
    }
}
