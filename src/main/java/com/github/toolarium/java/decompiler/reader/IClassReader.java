/*
 * IClassReader.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reader;

import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import java.io.IOException;
import java.io.InputStream;


/**
 * Reads Java class file bytes and produces a structured {@link IStructClass} model.
 *
 * @author patrick
 */
public interface IClassReader {

    /**
     * Read a class file from raw bytes.
     *
     * @param classBytes the class file bytes
     * @return the structured class model
     * @throws IOException if the bytes do not represent a valid class file
     */
    IStructClass readClass(byte[] classBytes) throws IOException;


    /**
     * Read a class file from an input stream.
     *
     * @param inputStream the input stream containing class file bytes
     * @return the structured class model
     * @throws IOException if reading fails or the data is not a valid class file
     */
    IStructClass readClass(InputStream inputStream) throws IOException;


    /**
     * Read a class file from an already-parsed ASM {@link org.objectweb.asm.tree.ClassNode}.
     * This avoids a redundant parsing pass when the caller has already built the tree.
     *
     * @param classNode the ASM class node
     * @return the structured class model
     * @throws IOException if the class node data is invalid
     */
    IStructClass readClass(org.objectweb.asm.tree.ClassNode classNode) throws IOException;
}
