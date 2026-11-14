/*
 * ResultSaverFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.api;

import com.github.toolarium.java.decompiler.impl.output.FileResultSaver;
import com.github.toolarium.java.decompiler.impl.output.InMemoryResultSaver;
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
}
