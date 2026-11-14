/*
 * ClassFileMetadataImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile.impl;

import com.github.toolarium.java.decompiler.model.classfile.IClassFileMetadata;
import com.github.toolarium.java.decompiler.model.classfile.IInnerClassEntry;
import com.github.toolarium.java.decompiler.model.classfile.IStructRecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IClassFileMetadata}.
 *
 * @author patrick
 */
public class ClassFileMetadataImpl implements IClassFileMetadata {
    private final int majorVersion;
    private final int minorVersion;
    private final String signature;
    private final String sourceFile;
    private final List<IStructRecordComponent> recordComponents;
    private final List<IInnerClassEntry> innerClassEntries;
    private final List<String> permittedSubclasses;


    /**
     * Constructor for ClassFileMetadataImpl.
     *
     * @param majorVersion        the class file major version
     * @param minorVersion        the class file minor version
     * @param signature           the generic signature (may be null)
     * @param sourceFile          the source file name (may be null)
     * @param recordComponents    the record components
     * @param innerClassEntries   the inner class entries from the InnerClasses attribute
     * @param permittedSubclasses the permitted subclasses for sealed classes
     */
    public ClassFileMetadataImpl(int majorVersion, int minorVersion, String signature,
                                 String sourceFile, List<IStructRecordComponent> recordComponents,
                                 List<IInnerClassEntry> innerClassEntries,
                                 List<String> permittedSubclasses) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.signature = signature;
        this.sourceFile = sourceFile;
        if (recordComponents != null) {
            this.recordComponents = Collections.unmodifiableList(new ArrayList<>(recordComponents));
        } else {
            this.recordComponents = Collections.emptyList();
        }
        if (innerClassEntries != null) {
            this.innerClassEntries = Collections.unmodifiableList(new ArrayList<>(innerClassEntries));
        } else {
            this.innerClassEntries = Collections.emptyList();
        }
        if (permittedSubclasses != null) {
            this.permittedSubclasses = Collections.unmodifiableList(new ArrayList<>(permittedSubclasses));
        } else {
            this.permittedSubclasses = Collections.emptyList();
        }
    }


    /**
     * Get the class file major version.
     *
     * @return the major version
     */
    @Override
    public int getMajorVersion() {
        return majorVersion;
    }


    /**
     * Get the class file minor version.
     *
     * @return the minor version
     */
    @Override
    public int getMinorVersion() {
        return minorVersion;
    }


    /**
     * Get the generic signature, if present.
     *
     * @return the generic signature, or null if not available
     */
    @Override
    public String getSignature() {
        return signature;
    }


    /**
     * Get the source file name from the SourceFile attribute.
     *
     * @return the source file name, or null if not available
     */
    @Override
    public String getSourceFile() {
        return sourceFile;
    }


    /**
     * Get the record components if the class is a record.
     *
     * @return an unmodifiable list of record components, empty if not a record
     */
    @Override
    public List<IStructRecordComponent> getRecordComponents() {
        return recordComponents;
    }


    /**
     * Get the inner class entries from the InnerClasses attribute.
     *
     * @return an unmodifiable list of inner class entries, empty if none
     */
    @Override
    public List<IInnerClassEntry> getInnerClassEntries() {
        return innerClassEntries;
    }


    /**
     * Get the permitted subclasses from the PermittedSubclasses attribute.
     * A non-empty list indicates a sealed class or interface.
     *
     * @return an unmodifiable list of permitted subclass internal names, empty if not sealed
     */
    @Override
    public List<String> getPermittedSubclasses() {
        return permittedSubclasses;
    }


    /**
     * Compute the hash code based on all metadata fields.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(majorVersion, minorVersion, signature, sourceFile,
                recordComponents, innerClassEntries, permittedSubclasses);
    }


    /**
     * Check equality based on all metadata fields.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ClassFileMetadataImpl other = (ClassFileMetadataImpl) obj;
        return majorVersion == other.majorVersion
                && minorVersion == other.minorVersion
                && Objects.equals(signature, other.signature)
                && Objects.equals(sourceFile, other.sourceFile)
                && Objects.equals(recordComponents, other.recordComponents)
                && Objects.equals(innerClassEntries, other.innerClassEntries)
                && Objects.equals(permittedSubclasses, other.permittedSubclasses);
    }


    /**
     * Return a string representation of this class file metadata.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ClassFileMetadata[v" + majorVersion + "." + minorVersion
                + ", signature=" + signature + ", sourceFile=" + sourceFile
                + ", recordComponents=" + recordComponents
                + ", innerClassEntries=" + innerClassEntries
                + ", permittedSubclasses=" + permittedSubclasses + "]";
    }
}
