/*
 * SingleFileClassSource.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.source.impl;

import com.github.toolarium.java.decompiler.source.IClassSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A class source backed by a single .class file.
 *
 * @author patrick
 */
public class SingleFileClassSource implements IClassSource {
    private static final Logger LOG = LoggerFactory.getLogger(SingleFileClassSource.class);
    private static final long MAX_CLASS_FILE_SIZE = 10L * 1024L * 1024L;

    private final Path classFile;
    private final String className;


    /**
     * Constructor for SingleFileClassSource.
     *
     * @param classFile the path to the .class file
     */
    public SingleFileClassSource(Path classFile) {
        this.classFile = Objects.requireNonNull(classFile, "Class file path must not be null");
        String fileName = classFile.getFileName().toString();
        if (fileName.endsWith(".class")) {
            this.className = fileName.substring(0, fileName.length() - ".class".length());
        } else {
            this.className = fileName;
        }
    }


    /**
     * List the single class name available from this file source.
     *
     * @return a singleton list containing the class name
     * @throws IOException if listing fails
     */
    @Override
    public List<String> listClasses() throws IOException {
        return Collections.singletonList(className);
    }


    /**
     * Read the bytecode for the given class name from this single file.
     *
     * @param className the class internal name
     * @return the class file bytes
     * @throws IOException if the class name does not match or the file exceeds the maximum allowed size
     */
    @Override
    public byte[] readClass(String className) throws IOException {
        if (!this.className.equals(className)) {
            throw new IOException("Class not found: " + className);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading class file: " + classFile);
        }
        long fileSize = Files.size(classFile);
        if (fileSize > MAX_CLASS_FILE_SIZE) {
            throw new IOException("Class file exceeds maximum allowed size ("
                    + MAX_CLASS_FILE_SIZE + " bytes): " + classFile);
        }
        return Files.readAllBytes(classFile);
    }


    /**
     * Check whether the given class name matches this single file source.
     *
     * @param className the class internal name
     * @return true if the class name matches
     */
    @Override
    public boolean hasClass(String className) {
        return this.className.equals(className);
    }


    /**
     * Close this class source. No resources need to be released for a single file.
     */
    @Override
    public void close() {
        // nothing to close for a single file
    }
}
