/*
 * InMemoryResultSaver.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.impl.output;

import com.github.toolarium.java.decompiler.api.IResultSaver;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An {@link IResultSaver} that collects decompiled source code in memory.
 * Useful for embedding the decompiler in tools and IDEs.
 *
 * <p>This class is thread-safe. A warning is logged when the number of stored
 * results exceeds {@link #DEFAULT_MAX_ENTRIES}.</p>
 *
 * @author patrick
 */
public class InMemoryResultSaver implements IResultSaver {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryResultSaver.class);

    private static final int DEFAULT_MAX_ENTRIES = 10_000;

    private final Map<String, String> results = Collections.synchronizedMap(new LinkedHashMap<>());
    private final AtomicBoolean warned = new AtomicBoolean(false);


    /**
     * Constructor for InMemoryResultSaver.
     */
    public InMemoryResultSaver() {
        // default constructor
    }


    /**
     * Save a decompiled Java source file in memory.
     * Logs a warning when the number of stored results exceeds the maximum.
     *
     * @param className  the internal class name
     * @param sourceCode the decompiled Java source code
     */
    @Override
    public void saveSource(String className, String sourceCode) {
        results.put(className, sourceCode);

        if (results.size() > DEFAULT_MAX_ENTRIES && warned.compareAndSet(false, true)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("In-memory result count exceeded " + DEFAULT_MAX_ENTRIES
                        + " entries, consider using FileResultSaver for large inputs.");
            }
            LOG.warn("In-memory result count exceeded " + DEFAULT_MAX_ENTRIES
                    + " entries, consider using FileResultSaver for large inputs.");
        }
    }


    /**
     * Get the number of classes saved so far.
     *
     * @return the count of saved classes
     */
    @Override
    public int getSavedCount() {
        return results.size();
    }


    /**
     * Get all saved results as an unmodifiable map from class name to source code.
     *
     * @return an unmodifiable snapshot of class name to source mappings
     */
    @Override
    public Map<String, String> getResults() {
        synchronized (results) {
            return Collections.unmodifiableMap(new LinkedHashMap<>(results));
        }
    }


    /**
     * Close this result saver. No resources need to be released for in-memory storage.
     */
    @Override
    public void close() {
        // nothing to close
    }
}
