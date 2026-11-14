/*
 * IClassSource.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.source;

import java.io.IOException;
import java.util.List;


/**
 * Abstraction for a source of Java class files (directory, JAR, single file).
 *
 * @author patrick
 */
public interface IClassSource extends AutoCloseable {

    /**
     * List all available class names in internal form (e.g., "java/lang/String").
     *
     * @return a list of class names
     * @throws IOException if listing fails
     */
    List<String> listClasses() throws IOException;


    /**
     * Read the bytecode for a given class name.
     *
     * @param className the class internal name (e.g., "java/lang/String")
     * @return the class file bytes
     * @throws IOException if the class cannot be read or does not exist
     */
    byte[] readClass(String className) throws IOException;


    /**
     * Check whether a class is available in this source.
     *
     * @param className the class internal name
     * @return true if the class exists in this source
     * @throws IOException if checking availability fails
     */
    boolean hasClass(String className) throws IOException;
}
