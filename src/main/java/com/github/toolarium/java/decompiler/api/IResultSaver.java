/*
 * IResultSaver.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.api;

import java.io.IOException;
import java.util.Map;


/**
 * Receives decompiled source code output. Implementations may write to files,
 * collect in memory, or stream to other destinations.
 *
 * @author patrick
 */
public interface IResultSaver extends AutoCloseable {

    /**
     * Save a decompiled Java source file.
     *
     * @param className  the internal class name (e.g., "com/example/MyClass")
     * @param sourceCode the decompiled Java source code
     * @throws IOException if saving fails
     */
    void saveSource(String className, String sourceCode) throws IOException;


    /**
     * Get the number of classes saved so far.
     *
     * @return the count of saved classes
     */
    int getSavedCount();


    /**
     * Get all saved results as a map from class name to source code.
     * May not be supported by all implementations (e.g., file-based savers).
     *
     * @return an unmodifiable map of class name to source, or empty if not supported
     */
    Map<String, String> getResults();
}
