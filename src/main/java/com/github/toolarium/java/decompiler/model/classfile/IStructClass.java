/*
 * IStructClass.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile;

import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.common.IAttribute;
import com.github.toolarium.java.decompiler.model.common.IConstantPool;
import java.util.List;


/**
 * Represents a parsed Java class file.
 *
 * @author patrick
 */
public interface IStructClass {

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
     * Get the constant pool.
     *
     * @return the constant pool
     */
    IConstantPool getConstantPool();


    /**
     * Get the access modifiers.
     *
     * @return the access flags
     */
    IAccessFlags getAccessFlags();


    /**
     * Get the fully qualified internal name (e.g., "java/lang/String").
     *
     * @return the class internal name
     */
    String getName();


    /**
     * Get the superclass internal name.
     *
     * @return the superclass name, or null for java/lang/Object
     */
    String getSuperName();


    /**
     * Get the implemented or extended interface names.
     *
     * @return an unmodifiable list of interface internal names
     */
    List<String> getInterfaceNames();


    /**
     * Get the declared fields.
     *
     * @return an unmodifiable list of fields
     */
    List<IStructField> getFields();


    /**
     * Get the declared methods.
     *
     * @return an unmodifiable list of methods
     */
    List<IStructMethod> getMethods();


    /**
     * Get the class-level attributes.
     *
     * @return an unmodifiable list of attributes
     */
    List<IAttribute> getAttributes();


    /**
     * Get the class-level annotations.
     *
     * @return an unmodifiable list of annotations
     */
    List<IAnnotation> getAnnotations();


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
     * Get the record components if this class is a record.
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
