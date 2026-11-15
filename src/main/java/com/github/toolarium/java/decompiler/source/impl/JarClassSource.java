/*
 * JarClassSource.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.source.impl;

import com.github.toolarium.java.decompiler.source.IClassSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A class source backed by a JAR or ZIP file.
 * Filters out entries with path traversal sequences and enforces a maximum
 * decompressed entry size to guard against zip bombs.
 *
 * @author patrick
 */
public class JarClassSource implements IClassSource {
    private static final Logger LOG = LoggerFactory.getLogger(JarClassSource.class);
    private static final String CLASS_EXTENSION = ".class";
    private static final String PATH_TRAVERSAL_SEGMENT = "..";
    private static final long MAX_CLASS_FILE_SIZE = 10L * 1024L * 1024L;
    private static final int MAX_JAR_ENTRIES = 500_000;
    private static final int BUFFER_SIZE = 8192;

    private final JarFile jarFile;


    /**
     * Constructor for JarClassSource.
     *
     * @param jarPath the path to the JAR file
     * @throws IOException if the JAR file cannot be opened
     */
    public JarClassSource(Path jarPath) throws IOException {
        Objects.requireNonNull(jarPath, "JAR path must not be null");
        this.jarFile = new JarFile(jarPath.toFile());
    }


    /**
     * List all class names available in the JAR file.
     * Entries with path traversal sequences are skipped.
     *
     * @return a list of class names in internal form
     * @throws IOException if listing the JAR entries fails
     */
    @Override
    public List<String> listClasses() throws IOException {
        List<String> classes = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        int entryCount = 0;
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            entryCount++;
            if (entryCount > MAX_JAR_ENTRIES) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("JAR entry count exceeded limit of " + MAX_JAR_ENTRIES + ", stopping enumeration.");
                }
                LOG.warn("JAR contains more than " + MAX_JAR_ENTRIES + " entries, only the first " + MAX_JAR_ENTRIES + " are processed.");
                break;
            }
            String entryName = entry.getName();

            if (containsPathTraversal(entryName)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skipping JAR entry with path traversal: " + entryName);
                }
                continue;
            }

            if (entryName.endsWith(CLASS_EXTENSION) && !entry.isDirectory()) {
                String className = entryName.substring(0, entryName.length() - CLASS_EXTENSION.length());
                classes.add(className);
            }
        }
        return classes;
    }


    /**
     * Read the bytecode for a given class name from the JAR file.
     *
     * @param className the class internal name (e.g., "java/lang/String")
     * @return the class file bytes
     * @throws IOException if the class is not found in the JAR, contains a path traversal,
     *                     or the decompressed size exceeds the allowed maximum
     */
    @Override
    public byte[] readClass(String className) throws IOException {
        String entryName = className + CLASS_EXTENSION;

        if (containsPathTraversal(entryName)) {
            throw new IOException("Invalid class name with path traversal: " + className);
        }

        JarEntry entry = jarFile.getJarEntry(entryName);
        if (entry == null) {
            throw new IOException("Class not found in JAR: " + className);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading class from JAR: " + className);
        }
        return readBounded(entry);
    }


    /**
     * Check whether a class is available in this JAR file.
     *
     * @param className the class internal name
     * @return true if the class exists in the JAR
     */
    @Override
    public boolean hasClass(String className) {
        String entryName = className + CLASS_EXTENSION;
        if (containsPathTraversal(entryName)) {
            return false;
        }
        return jarFile.getJarEntry(entryName) != null;
    }


    /**
     * Close the underlying JAR file and release its resources.
     *
     * @throws IOException if closing the JAR file fails
     */
    @Override
    public void close() throws IOException {
        jarFile.close();
    }


    /**
     * Check whether an entry name contains path traversal segments.
     *
     * @param entryName the JAR entry name to check
     * @return true if the name contains ".." path segments
     */
    private boolean containsPathTraversal(String entryName) {
        String normalized = entryName.replace('\\', '/');
        int start = 0;
        while (start <= normalized.length()) {
            int slashIdx = normalized.indexOf('/', start);
            int end;
            if (slashIdx < 0) {
                end = normalized.length();
            } else {
                end = slashIdx;
            }
            int segLen = end - start;
            if (segLen == 2 && normalized.charAt(start) == '.' && normalized.charAt(start + 1) == '.') {
                return true;
            }
            start = end + 1;
        }
        return false;
    }


    /**
     * Read a JAR entry with a bounded size to prevent zip bomb decompression.
     *
     * @param entry the JAR entry to read
     * @return the decompressed bytes
     * @throws IOException if reading fails or the entry exceeds the size limit
     */
    private byte[] readBounded(JarEntry entry) throws IOException {
        try (InputStream is = jarFile.getInputStream(entry)) {
            long declaredSize = entry.getSize();
            int initialCapacity = BUFFER_SIZE;
            if (declaredSize > 0 && declaredSize <= MAX_CLASS_FILE_SIZE) {
                initialCapacity = (int) declaredSize;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream(initialCapacity);
            byte[] buf = new byte[BUFFER_SIZE];
            long totalRead = 0;
            int bytesRead = is.read(buf);
            while (bytesRead != -1) {
                totalRead += bytesRead;
                if (totalRead > MAX_CLASS_FILE_SIZE) {
                    throw new IOException("JAR entry exceeds maximum allowed size ("
                            + MAX_CLASS_FILE_SIZE + " bytes): " + entry.getName());
                }
                baos.write(buf, 0, bytesRead);
                bytesRead = is.read(buf);
            }
            return baos.toByteArray();
        }
    }
}
