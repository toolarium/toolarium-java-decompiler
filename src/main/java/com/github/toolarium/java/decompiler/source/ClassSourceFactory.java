/*
 * ClassSourceFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.source;

import com.github.toolarium.java.decompiler.source.impl.DirectoryClassSource;
import com.github.toolarium.java.decompiler.source.impl.JarClassSource;
import com.github.toolarium.java.decompiler.source.impl.SingleFileClassSource;
import java.io.IOException;
import java.nio.file.Path;


/**
 * Singleton factory for {@link IClassSource} instances.
 *
 * @author patrick
 */
public final class ClassSourceFactory {

    /**
     * Private constructor.
     */
    private ClassSourceFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ClassSourceFactory INSTANCE = new ClassSourceFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static ClassSourceFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a class source for a single .class file.
     *
     * @param classFile the path to the .class file
     * @return the class source
     */
    public IClassSource getSingleFileClassSource(Path classFile) {
        return new SingleFileClassSource(classFile);
    }


    /**
     * Get a class source for a directory tree of .class files.
     *
     * @param rootDirectory the root directory
     * @return the class source
     */
    public IClassSource getDirectoryClassSource(Path rootDirectory) {
        return new DirectoryClassSource(rootDirectory);
    }


    /**
     * Get a class source for a JAR or ZIP file.
     *
     * @param jarPath the path to the JAR file
     * @return the class source
     * @throws IOException if the JAR cannot be opened
     */
    public IClassSource getJarClassSource(Path jarPath) throws IOException {
        return new JarClassSource(jarPath);
    }
}
