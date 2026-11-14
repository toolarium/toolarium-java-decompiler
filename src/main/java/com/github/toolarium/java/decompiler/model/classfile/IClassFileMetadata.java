/*
 * IClassFileMetadata.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile;

import java.util.List;


/**
 * Represents supplementary class file metadata: version information, generic signature,
 * source file name, and record components.
 *
 * @author patrick
 */
public interface IClassFileMetadata {

    /**
     * Get the class file major version.
     *
     * @return the major version
     */
    int getMajorVersion();


    /**
     * Get the class file minor version.
     *
     * @return the minor version
     */
    int getMinorVersion();


    /**
     * Get the generic signature, if present.
     *
     * @return the generic signature, or null if not available
     */
    String getSignature();


    /**
     * Get the source file name from the SourceFile attribute.
     *
     * @return the source file name, or null if not available
     */
    String getSourceFile();


    /**
     * Get the record components if the class is a record.
     *
     * @return an unmodifiable list of record components, empty if not a record
     */
    List<IStructRecordComponent> getRecordComponents();


    /**
     * Get the inner class entries from the InnerClasses attribute.
     *
     * @return an unmodifiable list of inner class entries, empty if none
     */
    List<IInnerClassEntry> getInnerClassEntries();


    /**
     * Get the permitted subclasses from the PermittedSubclasses attribute.
     * A non-empty list indicates a sealed class or interface.
     *
     * @return an unmodifiable list of permitted subclass internal names, empty if not sealed
     */
    List<String> getPermittedSubclasses();
}
