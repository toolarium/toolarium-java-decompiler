/*
 * ClassfileModelFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile;

import com.github.toolarium.java.decompiler.model.classfile.impl.ClassFileMetadataImpl;
import com.github.toolarium.java.decompiler.model.classfile.impl.InnerClassEntryImpl;
import com.github.toolarium.java.decompiler.model.classfile.impl.MethodCodeImpl;
import com.github.toolarium.java.decompiler.model.classfile.impl.StructClassImpl;
import com.github.toolarium.java.decompiler.model.classfile.impl.StructFieldImpl;
import com.github.toolarium.java.decompiler.model.classfile.impl.StructMethodImpl;
import com.github.toolarium.java.decompiler.model.classfile.impl.StructRecordComponentImpl;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.common.IAttribute;
import com.github.toolarium.java.decompiler.model.common.IConstantPool;
import java.util.List;


/**
 * Singleton factory for classfile model objects.
 *
 * @author patrick
 */
public final class ClassfileModelFactory {

    /**
     * Private constructor.
     */
    private ClassfileModelFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ClassfileModelFactory INSTANCE = new ClassfileModelFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static ClassfileModelFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a struct field instance.
     *
     * @param name        the field name
     * @param descriptor  the type descriptor
     * @param accessFlags the access modifiers
     * @param attributes  the field attributes
     * @param annotations the field annotations
     * @param signature   the generic signature (may be null)
     * @return the struct field
     */
    public IStructField getStructField(String name, String descriptor, IAccessFlags accessFlags,
                                       List<IAttribute> attributes, List<IAnnotation> annotations,
                                       String signature) {
        return new StructFieldImpl(name, descriptor, accessFlags, attributes, annotations, signature);
    }


    /**
     * Get a method code instance.
     *
     * @param code      the raw bytecode (may be null)
     * @param maxStack  the max operand stack depth
     * @param maxLocals the max local variable count
     * @return the method code
     */
    public IMethodCode getMethodCode(byte[] code, int maxStack, int maxLocals) {
        return new MethodCodeImpl(code, maxStack, maxLocals);
    }


    /**
     * Get a class file metadata instance.
     *
     * @param majorVersion        the class file major version
     * @param minorVersion        the class file minor version
     * @param signature           the generic signature (may be null)
     * @param sourceFile          the source file name (may be null)
     * @param recordComponents    the record components
     * @param innerClassEntries   the inner class entries from the InnerClasses attribute
     * @param permittedSubclasses the permitted subclasses for sealed classes
     * @return the class file metadata
     */
    public IClassFileMetadata getClassFileMetadata(int majorVersion, int minorVersion, String signature,
                                                   String sourceFile,
                                                   List<IStructRecordComponent> recordComponents,
                                                   List<IInnerClassEntry> innerClassEntries,
                                                   List<String> permittedSubclasses) {
        return new ClassFileMetadataImpl(majorVersion, minorVersion, signature, sourceFile,
                recordComponents, innerClassEntries, permittedSubclasses);
    }


    /**
     * Get an inner class entry instance.
     *
     * @param internalName the inner class internal name
     * @param outerName    the outer class internal name, or null for anonymous/local classes
     * @param simpleName   the simple name, or null for anonymous classes
     * @param accessFlags  the access flags
     * @return the inner class entry
     */
    public IInnerClassEntry getInnerClassEntry(String internalName, String outerName, String simpleName, IAccessFlags accessFlags) {
        return new InnerClassEntryImpl(internalName, outerName, simpleName, accessFlags);
    }


    /**
     * Get a struct method instance.
     *
     * @param name                 the method name
     * @param descriptor           the method descriptor
     * @param accessFlags          the access modifiers
     * @param attributes           the method attributes
     * @param annotations          the method annotations
     * @param signature            the generic signature (may be null)
     * @param parameterNames       the parameter names from debug info
     * @param parameterAnnotations the parameter annotations indexed by position
     * @param exceptionTypes       the checked exception types
     * @param methodCode           the method code attribute (may be null)
     * @return the struct method
     */
    public IStructMethod getStructMethod(String name, String descriptor, IAccessFlags accessFlags,
                                         List<IAttribute> attributes, List<IAnnotation> annotations,
                                         String signature, List<String> parameterNames,
                                         List<List<IAnnotation>> parameterAnnotations,
                                         List<String> exceptionTypes, IMethodCode methodCode) {
        return new StructMethodImpl(name, descriptor, accessFlags, attributes, annotations,
                signature, parameterNames, parameterAnnotations, exceptionTypes, methodCode);
    }


    /**
     * Get a struct class instance.
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
     * @return the struct class
     */
    public IStructClass getStructClass(IClassFileMetadata metadata, IConstantPool constantPool,
                                       IAccessFlags accessFlags, String name, String superName,
                                       List<String> interfaceNames, List<IStructField> fields,
                                       List<IStructMethod> methods, List<IAttribute> attributes,
                                       List<IAnnotation> annotations) {
        return new StructClassImpl(metadata, constantPool, accessFlags, name, superName,
                interfaceNames, fields, methods, attributes, annotations);
    }


    /**
     * Get a struct record component instance.
     *
     * @param name        the component name
     * @param descriptor  the type descriptor
     * @param signature   the generic signature (may be null)
     * @param annotations the annotations
     * @return the struct record component
     */
    public IStructRecordComponent getStructRecordComponent(String name, String descriptor,
                                                           String signature, List<IAnnotation> annotations) {
        return new StructRecordComponentImpl(name, descriptor, signature, annotations);
    }
}
