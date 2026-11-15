/*
 * ResultSaverFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.api;

import com.github.toolarium.java.decompiler.impl.output.ArchiveResultSaver;
import com.github.toolarium.java.decompiler.impl.output.FileResultSaver;
import com.github.toolarium.java.decompiler.impl.output.InMemoryResultSaver;
import java.io.IOException;
import java.nio.file.Path;


/**
 * Singleton factory for {@link IResultSaver} instances.
 *
 * @author patrick
 */
public final class ResultSaverFactory {

    /**
     * Private constructor.
     */
    private ResultSaverFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ResultSaverFactory INSTANCE = new ResultSaverFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static ResultSaverFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get an in-memory result saver.
     *
     * @return the result saver
     */
    public IResultSaver getInMemoryResultSaver() {
        return new InMemoryResultSaver();
    }


    /**
     * Get a file-based result saver.
     *
     * @param outputDirectory the output directory
     * @return the result saver
     */
    public IResultSaver getFileResultSaver(Path outputDirectory) {
        return new FileResultSaver(outputDirectory);
    }


    /**
     * Get an archive-based result saver that packages decompiled sources into a JAR or ZIP file.
     * Files are collected in a temporary directory and packaged into the archive on close.
     *
     * @param archivePath the target archive file path (must end with .jar, .zip, .war, or .ear)
     * @return the result saver
     * @throws IOException if the temporary working directory cannot be created
     */
    public IResultSaver getArchiveResultSaver(Path archivePath) throws IOException {
        return new ArchiveResultSaver(archivePath);
    }


    /**
     * Get the appropriate result saver based on the output path.
     * If the path has an archive extension (.jar, .zip, .war, .ear), an archive saver is returned.
     * Otherwise, a file-based saver writing to the path as a directory is returned.
     *
     * @param outputPath the output path (directory or archive file)
     * @return the result saver
     * @throws IOException if an archive saver's temporary directory cannot be created
     */
    public IResultSaver getResultSaver(Path outputPath) throws IOException {
        String fileName = outputPath.getFileName().toString();
        if (ArchiveResultSaver.isArchiveExtension(fileName)) {
            return new ArchiveResultSaver(outputPath);
        }
        return new FileResultSaver(outputPath);
    }
}
