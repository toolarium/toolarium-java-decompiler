/*
 * FileResultSaver.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.impl.output;

import com.github.toolarium.java.decompiler.api.IResultSaver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An {@link IResultSaver} that writes decompiled source code to the filesystem.
 * Validates that output paths stay within the configured output directory to
 * prevent path traversal from crafted class names.
 *
 * <p>This class is thread-safe.</p>
 *
 * @author patrick
 */
public class FileResultSaver implements IResultSaver {
    private static final Logger LOG = LoggerFactory.getLogger(FileResultSaver.class);
    private final Path outputDirectory;
    private final AtomicInteger savedCount;


    /**
     * Constructor for FileResultSaver.
     *
     * @param outputDirectory the root directory for output files
     */
    public FileResultSaver(Path outputDirectory) {
        this.outputDirectory = Objects.requireNonNull(outputDirectory, "Output directory must not be null");
        this.savedCount = new AtomicInteger(0);
    }


    /**
     * Save a decompiled Java source file to the filesystem.
     * Validates that the output path stays within the configured output directory.
     *
     * @param className  the internal class name (e.g., "com/example/MyClass")
     * @param sourceCode the decompiled Java source code
     * @throws IOException if the file cannot be written or the class name resolves outside the output directory
     */
    @Override
    public void saveSource(String className, String sourceCode) throws IOException {
        String filePath = className.replace('/', java.io.File.separatorChar) + ".java";
        Path outputFile = outputDirectory.resolve(filePath).normalize();
        Path normalizedBase = outputDirectory.normalize();

        if (!outputFile.startsWith(normalizedBase)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping class with path traversal: " + className);
            }
            throw new IOException("Invalid class name, resolved path escapes output directory: " + className);
        }

        Path parentDir = outputFile.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }

        Files.write(outputFile, sourceCode.getBytes(StandardCharsets.UTF_8));
        savedCount.incrementAndGet();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Saved decompiled source: " + outputFile);
        }
    }


    /**
     * Get the number of classes saved so far.
     *
     * @return the count of saved classes
     */
    @Override
    public int getSavedCount() {
        return savedCount.get();
    }


    /**
     * Get saved results. File-based savers do not retain results in memory.
     *
     * @return an empty map, as results are written directly to disk
     */
    @Override
    public Map<String, String> getResults() {
        return Collections.emptyMap();
    }


    /**
     * Close this result saver. No resources need to be released for file-based saving.
     */
    @Override
    public void close() {
        // nothing to close
    }
}
