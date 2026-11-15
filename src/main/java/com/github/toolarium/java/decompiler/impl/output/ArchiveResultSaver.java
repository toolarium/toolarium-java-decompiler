/*
 * ArchiveResultSaver.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.impl.output;

import com.github.toolarium.java.decompiler.api.IResultSaver;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An {@link IResultSaver} that collects decompiled source files into a JAR or ZIP archive.
 * Files are first written to a temporary directory via a {@link FileResultSaver},
 * then packaged into the target archive when {@link #close()} is called.
 *
 * <p>Supported archive extensions: {@code .jar}, {@code .zip}, {@code .war}, {@code .ear}.
 * JAR archives include a basic {@code MANIFEST.MF}.</p>
 *
 * <p>This class is thread-safe for concurrent {@link #saveSource} calls.</p>
 *
 * @author patrick
 */
public class ArchiveResultSaver implements IResultSaver {
    private static final Logger LOG = LoggerFactory.getLogger(ArchiveResultSaver.class);
    private static final String JAR_EXTENSION = ".jar";
    private static final String ZIP_EXTENSION = ".zip";
    private static final String WAR_EXTENSION = ".war";
    private static final String EAR_EXTENSION = ".ear";

    private final Path archivePath;
    private final Path tempDirectory;
    private final FileResultSaver delegate;
    private final boolean isJarFormat;


    /**
     * Constructor for ArchiveResultSaver.
     *
     * @param archivePath the target archive file path
     * @throws IOException if the temporary directory cannot be created
     */
    public ArchiveResultSaver(Path archivePath) throws IOException {
        this.archivePath = Objects.requireNonNull(archivePath, "Archive path must not be null");
        this.tempDirectory = Files.createTempDirectory("toolarium-decompiler-");
        this.delegate = new FileResultSaver(tempDirectory);

        String name = archivePath.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        this.isJarFormat = name.endsWith(JAR_EXTENSION)
                || name.endsWith(WAR_EXTENSION)
                || name.endsWith(EAR_EXTENSION);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Archive output to " + archivePath + " via temp directory " + tempDirectory);
        }
    }


    /**
     * Save a decompiled Java source file. The file is written to a temporary directory
     * and will be packaged into the archive on {@link #close()}.
     *
     * @param className  the internal class name (e.g., "com/example/MyClass")
     * @param sourceCode the decompiled Java source code
     * @throws IOException if the file cannot be written
     */
    @Override
    public void saveSource(String className, String sourceCode) throws IOException {
        delegate.saveSource(className, sourceCode);
    }


    /**
     * Get the number of classes saved so far.
     *
     * @return the count of saved classes
     */
    @Override
    public int getSavedCount() {
        return delegate.getSavedCount();
    }


    /**
     * Get saved results. Archive-based savers do not retain results in memory.
     *
     * @return an empty map, as results are written to an archive
     */
    @Override
    public Map<String, String> getResults() {
        return Collections.emptyMap();
    }


    /**
     * Package all saved files into the target archive, then clean up the temporary directory.
     *
     * @throws IOException if archive creation or cleanup fails
     */
    @Override
    public void close() throws IOException {
        try {
            Path parentDir = archivePath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }

            buildArchive();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Created archive with " + delegate.getSavedCount() + " files: " + archivePath);
            }
        } finally {
            cleanupTempDirectory();
        }
    }


    /**
     * Build the archive file from the temporary directory contents.
     *
     * @throws IOException if archive creation fails
     */
    private void buildArchive() throws IOException {
        try (OutputStream fileOut = Files.newOutputStream(archivePath);
             ZipOutputStream zipOut = createZipStream(fileOut)) {

            Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String entryName = tempDirectory.relativize(file).toString().replace('\\', '/');
                    zipOut.putNextEntry(new ZipEntry(entryName));
                    Files.copy(file, zipOut);
                    zipOut.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }


    /**
     * Create the appropriate zip output stream. For JAR formats, a {@link JarOutputStream}
     * with a basic manifest is created.
     *
     * @param output the underlying output stream
     * @return the zip output stream
     * @throws IOException if stream creation fails
     */
    private ZipOutputStream createZipStream(OutputStream output) throws IOException {
        if (isJarFormat) {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().putValue("Created-By", "toolarium-java-decompiler");
            return new JarOutputStream(output, manifest);
        }
        return new ZipOutputStream(output);
    }


    /**
     * Recursively delete the temporary directory and all its contents.
     */
    private void cleanupTempDirectory() {
        try {
            Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to clean up temporary directory: " + tempDirectory, ex);
            }
            LOG.warn("Failed to clean up temporary directory: " + tempDirectory);
        }
    }


    /**
     * Check whether the given file name has an archive extension.
     *
     * @param fileName the file name to check
     * @return true if the file name ends with a supported archive extension
     */
    public static boolean isArchiveExtension(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase(java.util.Locale.ROOT);
        return lower.endsWith(JAR_EXTENSION)
                || lower.endsWith(ZIP_EXTENSION)
                || lower.endsWith(WAR_EXTENSION)
                || lower.endsWith(EAR_EXTENSION);
    }
}
