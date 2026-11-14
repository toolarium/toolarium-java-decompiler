/*
 * IDecompiler.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.api;

import com.github.toolarium.java.decompiler.source.IClassSource;
import java.io.IOException;


/**
 * Main entry point for the decompiler. Provides methods to decompile
 * individual classes or entire archives.
 *
 * @author patrick
 */
public interface IDecompiler {

    /**
     * Decompile a single class from raw bytecode.
     *
     * @param classBytes the class file bytes
     * @return the decompiled Java source code
     * @throws IOException if the bytes are not a valid class file
     */
    String decompile(byte[] classBytes) throws IOException;


    /**
     * Decompile a single class from a class source.
     *
     * @param source    the class source
     * @param className the internal class name (e.g., "java/lang/String")
     * @return the decompiled Java source code
     * @throws IOException if the class cannot be read or decompiled
     */
    String decompile(IClassSource source, String className) throws IOException;


    /**
     * Decompile all classes from a source, writing results to the saver.
     *
     * @param source the class source
     * @param saver  the result saver
     * @throws IOException if reading or decompiling fails
     */
    void decompileAll(IClassSource source, IResultSaver saver) throws IOException;
}
