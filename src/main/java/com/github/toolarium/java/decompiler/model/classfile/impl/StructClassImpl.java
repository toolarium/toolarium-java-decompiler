/*
 * StructClassImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile.impl;

import com.github.toolarium.java.decompiler.model.classfile.IClassFileMetadata;
import com.github.toolarium.java.decompiler.model.classfile.IInnerClassEntry;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.model.classfile.IStructField;
import com.github.toolarium.java.decompiler.model.classfile.IStructMethod;
import com.github.toolarium.java.decompiler.model.classfile.IStructRecordComponent;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.common.IAttribute;
import com.github.toolarium.java.decompiler.model.common.IConstantPool;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IStructClass}.
 *
 * @author patrick
 */
public class StructClassImpl implements IStructClass {
    private final IClassFileMetadata metadata;
    private final IConstantPool constantPool;
    private final IAccessFlags accessFlags;
    private final String name;
    private final String superName;
    private final List<String> interfaceNames;
    private final List<IStructField> fields;
    private final List<IStructMethod> methods;
    private final List<IAttribute> attributes;
    private final List<IAnnotation> annotations;


    /**
     * Constructor for StructClassImpl.
     *
     * @param metadata       the class file metadata (version, signature, source file, record components)
     * @param constantPool   the constant pool
     * @param accessFlags    the access modifiers
     * @param name           the class internal name
     * @param superName      the superclass internal name (may be null)
     * @param interfaceNames the interface names
     * @param fields         the declared fields
     * @param methods        the declared methods
     * @param attributes     the class-level attributes
     * @param annotations    the class-level annotations
     */
    public StructClassImpl(IClassFileMetadata metadata, IConstantPool constantPool,
                           IAccessFlags accessFlags, String name, String superName,
                           List<String> interfaceNames, List<IStructField> fields,
                           List<IStructMethod> methods, List<IAttribute> attributes,
                           List<IAnnotation> annotations) {
        this.metadata = Objects.requireNonNull(metadata, "Class file metadata must not be null");
        this.constantPool = Objects.requireNonNull(constantPool, "Constant pool must not be null");
        this.accessFlags = Objects.requireNonNull(accessFlags, "Access flags must not be null");
        this.name = Objects.requireNonNull(name, "Class name must not be null");
        this.superName = superName;
        this.interfaceNames = toUnmodifiable(interfaceNames);
        this.fields = toUnmodifiable(fields);
        this.methods = toUnmodifiable(methods);
        this.attributes = toUnmodifiable(attributes);
        this.annotations = toUnmodifiable(annotations);
    }


    /**
     * Get the class file major version.
     *
     * @return the major version
     */
    @Override
    public int getMajorVersion() {
        return metadata.getMajorVersion();
    }


    /**
     * Get the class file minor version.
     *
     * @return the minor version
     */
    @Override
    public int getMinorVersion() {
        return metadata.getMinorVersion();
    }


    /**
     * Get the constant pool.
     *
     * @return the constant pool
     */
    @Override
    public IConstantPool getConstantPool() {
        return constantPool;
    }


    /**
     * Get the access modifiers.
     *
     * @return the access flags
     */
    @Override
    public IAccessFlags getAccessFlags() {
        return accessFlags;
    }


    /**
     * Get the fully qualified internal name (e.g., "java/lang/String").
     *
     * @return the class internal name
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Get the superclass internal name.
     *
     * @return the superclass name, or null for java/lang/Object
     */
    @Override
    public String getSuperName() {
        return superName;
    }


    /**
     * Get the implemented or extended interface names.
     *
     * @return an unmodifiable list of interface internal names
     */
    @Override
    public List<String> getInterfaceNames() {
        return interfaceNames;
    }


    /**
     * Get the declared fields.
     *
     * @return an unmodifiable list of fields
     */
    @Override
    public List<IStructField> getFields() {
        return fields;
    }


    /**
     * Get the declared methods.
     *
     * @return an unmodifiable list of methods
     */
    @Override
    public List<IStructMethod> getMethods() {
        return methods;
    }


    /**
     * Get the class-level attributes.
     *
     * @return an unmodifiable list of attributes
     */
    @Override
    public List<IAttribute> getAttributes() {
        return attributes;
    }


    /**
     * Get the class-level annotations.
     *
     * @return an unmodifiable list of annotations
     */
    @Override
    public List<IAnnotation> getAnnotations() {
        return annotations;
    }


    /**
     * Get the generic signature, if present.
     *
     * @return the generic signature, or null if not available
     */
    @Override
    public String getSignature() {
        return metadata.getSignature();
    }


    /**
     * Get the source file name from the SourceFile attribute.
     *
     * @return the source file name, or null if not available
     */
    @Override
    public String getSourceFile() {
        return metadata.getSourceFile();
    }


    /**
     * Get the record components if this class is a record.
     *
     * @return an unmodifiable list of record components, empty if not a record
     */
    @Override
    public List<IStructRecordComponent> getRecordComponents() {
        return metadata.getRecordComponents();
    }


    /**
     * Get the inner class entries from the InnerClasses attribute.
     *
     * @return an unmodifiable list of inner class entries, empty if none
     */
    @Override
    public List<IInnerClassEntry> getInnerClassEntries() {
        return metadata.getInnerClassEntries();
    }


    /**
     * Get the permitted subclasses from the PermittedSubclasses attribute.
     * A non-empty list indicates a sealed class or interface.
     *
     * @return an unmodifiable list of permitted subclass internal names, empty if not sealed
     */
    @Override
    public List<String> getPermittedSubclasses() {
        return metadata.getPermittedSubclasses();
    }


    /**
     * Compute the hash code based on all class fields except the constant pool.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(metadata, accessFlags, name, superName,
                interfaceNames, fields, methods, attributes, annotations);
    }


    /**
     * Check equality based on all class fields except the constant pool.
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
        StructClassImpl other = (StructClassImpl) obj;
        return Objects.equals(metadata, other.metadata)
                && Objects.equals(accessFlags, other.accessFlags)
                && Objects.equals(name, other.name)
                && Objects.equals(superName, other.superName)
                && Objects.equals(interfaceNames, other.interfaceNames)
                && Objects.equals(fields, other.fields)
                && Objects.equals(methods, other.methods)
                && Objects.equals(attributes, other.attributes)
                && Objects.equals(annotations, other.annotations);
    }


    /**
     * Return a string representation including the class name and version.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "StructClass[" + name + ", v" + metadata.getMajorVersion() + "." + metadata.getMinorVersion() + "]";
    }


    /**
     * Convert a list to an unmodifiable copy, handling null.
     *
     * @param <T>  the element type
     * @param list the source list
     * @return an unmodifiable list
     */
    private static <T> List<T> toUnmodifiable(List<T> list) {
        if (list != null) {
            return Collections.unmodifiableList(new ArrayList<>(list));
        }
        return Collections.emptyList();
    }
}
