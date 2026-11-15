/*
 * DirectoryClassSource.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.source.impl;

import com.github.toolarium.java.decompiler.source.IClassSource;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A class source backed by a directory tree of .class files.
 * Validates that resolved paths stay within the root directory to prevent
 * path traversal attacks from crafted class names.
 *
 * @author patrick
 */
public class DirectoryClassSource implements IClassSource {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryClassSource.class);
    private static final String CLASS_EXTENSION = ".class";

    private static final long MAX_CLASS_FILE_SIZE = 10L * 1024L * 1024L;

    private final Path rootDirectory;


    /**
     * Constructor for DirectoryClassSource.
     *
     * @param rootDirectory the root directory containing .class files
     */
    public DirectoryClassSource(Path rootDirectory) {
        this.rootDirectory = Objects.requireNonNull(rootDirectory, "Root directory must not be null");
    }


    /**
     * List all class names found by walking the directory tree.
     *
     * @return a list of class names in internal form
     * @throws IOException if walking the directory fails
     */
    @Override
    public List<String> listClasses() throws IOException {
        List<String> classes = new ArrayList<>();
        Files.walkFileTree(rootDirectory, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String fileName = file.toString().replace('\\', '/');
                if (fileName.endsWith(CLASS_EXTENSION)) {
                    String relative = rootDirectory.relativize(file).toString().replace('\\', '/');
                    String className = relative.substring(0, relative.length() - CLASS_EXTENSION.length());
                    classes.add(className);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return classes;
    }


    /**
     * Read the bytecode for a given class name from the directory tree.
     *
     * @param className the class internal name (e.g., "java/lang/String")
     * @return the class file bytes
     * @throws IOException if the class file is not found, escapes the root directory,
     *                     or exceeds the maximum allowed size
     */
    @Override
    public byte[] readClass(String className) throws IOException {
        Path classFile = rootDirectory.resolve(className + CLASS_EXTENSION).normalize();
        Path normalizedRoot = rootDirectory.normalize();

        if (!classFile.startsWith(normalizedRoot)) {
            throw new IOException("Invalid class name, resolved path escapes root directory: " + className);
        }

        if (!Files.exists(classFile)) {
            throw new IOException("Class file not found: " + className);
        }

        long fileSize = Files.size(classFile);
        if (fileSize > MAX_CLASS_FILE_SIZE) {
            throw new IOException("Class file exceeds maximum allowed size ("
                    + MAX_CLASS_FILE_SIZE + " bytes): " + className);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading class file: " + classFile);
        }
        return Files.readAllBytes(classFile);
    }


    /**
     * Check whether a class file exists in this directory, validating against path traversal.
     *
     * @param className the class internal name
     * @return true if the class file exists within the root directory
     */
    @Override
    public boolean hasClass(String className) {
        Path classFile = rootDirectory.resolve(className + CLASS_EXTENSION).normalize();
        Path normalizedRoot = rootDirectory.normalize();

        if (!classFile.startsWith(normalizedRoot)) {
            return false;
        }

        return Files.exists(classFile);
    }


    /**
     * Close this class source. No resources need to be released for a directory source.
     */
    @Override
    public void close() {
        // nothing to close for a directory
    }
}
