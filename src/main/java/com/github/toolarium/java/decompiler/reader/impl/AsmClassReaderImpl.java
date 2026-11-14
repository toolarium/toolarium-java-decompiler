/*
 * AsmClassReaderImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reader.impl;

import com.github.toolarium.java.decompiler.model.classfile.ClassfileModelFactory;
import com.github.toolarium.java.decompiler.model.classfile.IClassFileMetadata;
import com.github.toolarium.java.decompiler.model.classfile.IInnerClassEntry;
import com.github.toolarium.java.decompiler.model.classfile.IMethodCode;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.model.classfile.IStructField;
import com.github.toolarium.java.decompiler.model.classfile.IStructMethod;
import com.github.toolarium.java.decompiler.model.classfile.IStructRecordComponent;
import com.github.toolarium.java.decompiler.model.common.CommonModelFactory;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.common.IAttribute;
import com.github.toolarium.java.decompiler.reader.IClassReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ASM-based implementation of {@link IClassReader}.
 * Uses the ASM visitor API to parse class files into the decompiler model.
 *
 * @author patrick
 */
public class AsmClassReaderImpl implements IClassReader {
    private static final Logger LOG = LoggerFactory.getLogger(AsmClassReaderImpl.class);
    private static final int MAX_CLASS_FILE_SIZE = 10 * 1024 * 1024;


    /**
     * Constructor for AsmClassReaderImpl.
     */
    public AsmClassReaderImpl() {
        // default constructor
    }


    /**
     * Read a class file from raw bytes.
     *
     * @param classBytes the class file bytes
     * @return the structured class model
     * @throws IOException if the bytes do not represent a valid class file
     */
    @Override
    public IStructClass readClass(byte[] classBytes) throws IOException {
        try {
            ClassReader reader = new ClassReader(classBytes);
            ClassCollector collector = new ClassCollector();
            reader.accept(collector, 0);
            return collector.buildStructClass();
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to read class bytes.", ex);
            }
            throw new IOException("Invalid class file format.", ex);
        }
    }


    /**
     * Read a class file from an already-parsed ASM ClassNode, avoiding a redundant parsing pass.
     *
     * @param classNode the ASM class node
     * @return the structured class model
     * @throws IOException if the class node data is invalid
     */
    @Override
    public IStructClass readClass(org.objectweb.asm.tree.ClassNode classNode) throws IOException {
        try {
            ClassCollector collector = new ClassCollector();
            classNode.accept(collector);
            return collector.buildStructClass();
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to read ClassNode.", ex);
            }
            throw new IOException("Invalid ClassNode data.", ex);
        }
    }


    /**
     * Read a class file from an input stream.
     *
     * @param inputStream the input stream containing class file bytes
     * @return the structured class model
     * @throws IOException if reading fails or the data is not a valid class file
     */
    @Override
    public IStructClass readClass(InputStream inputStream) throws IOException {
        byte[] classBytes = inputStream.readAllBytes();
        if (classBytes.length > MAX_CLASS_FILE_SIZE) {
            throw new IOException("Class file exceeds maximum allowed size of "
                    + MAX_CLASS_FILE_SIZE + " bytes: " + classBytes.length);
        }
        return readClass(classBytes);
    }


    /**
     * Internal ASM visitor that collects class structure data.
     */
    private static final class ClassCollector extends ClassVisitor {
        private int majorVersion;
        private int minorVersion;
        private int accessFlags;
        private String name;
        private String superName;
        private String[] interfaces;
        private String signature;
        private String sourceFile;
        private final List<IStructField> fields = new ArrayList<>();
        private final List<IStructMethod> methods = new ArrayList<>();
        private final List<IAnnotation> annotations = new ArrayList<>();
        private final List<IStructRecordComponent> recordComponents = new ArrayList<>();
        private final List<IInnerClassEntry> innerClassEntries = new ArrayList<>();
        private final List<String> permittedSubclasses = new ArrayList<>();


        /**
         * Constructor for ClassCollector.
         */
        ClassCollector() {
            super(Opcodes.ASM9);
        }


        /**
         * Visit the header of the class, collecting version, access flags, names, and interfaces.
         *
         * @param version    the class file version
         * @param access     the access flags
         * @param name       the internal class name
         * @param signature  the generic signature, or null
         * @param superName  the internal name of the super class
         * @param interfaces the internal names of the implemented interfaces
         */
        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            this.majorVersion = version & 0xFFFF;
            this.minorVersion = (version >> 16) & 0xFFFF;
            this.accessFlags = access;
            this.name = name;
            this.superName = superName;
            this.interfaces = interfaces;
            this.signature = signature;
        }


        /**
         * Visit the source file name of the class.
         *
         * @param source the source file name, or null
         * @param debug  the debug extension string, or null
         */
        @Override
        public void visitSource(String source, String debug) {
            this.sourceFile = source;
        }


        /**
         * Visit a class-level annotation and return a visitor to collect its values.
         *
         * @param descriptor the annotation type descriptor
         * @param visible    true if the annotation is runtime-visible
         * @return an annotation visitor for the element-value pairs
         */
        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            AnnotationCollector collector = new AnnotationCollector(descriptor, visible);
            return new AsmAnnotationVisitor(collector, annotations);
        }


        /**
         * Visit a field of the class and return a visitor to collect field metadata.
         *
         * @param access     the field access flags
         * @param name       the field name
         * @param descriptor the field type descriptor
         * @param signature  the field generic signature, or null
         * @param value      the constant initial value, or null
         * @return a field visitor for further field-level visits
         */
        @Override
        public FieldVisitor visitField(int access, String name, String descriptor,
                                       String signature, Object value) {
            return new FieldCollector(access, name, descriptor, signature);
        }


        /**
         * Visit a method of the class and return a visitor to collect bytecode and metadata.
         *
         * @param access     the method access flags
         * @param name       the method name
         * @param descriptor the method descriptor
         * @param signature  the method generic signature, or null
         * @param exceptions the internal names of the thrown exception types, or null
         * @return a method visitor that delegates to an ASM MethodNode
         */
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodNode methodNode = new MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions);
            return new MethodCollectorVisitor(methodNode, access, name, descriptor, signature, exceptions);
        }


        /**
         * Visit an inner class entry from the InnerClasses attribute.
         *
         * @param name      the internal name of the inner class
         * @param outerName the internal name of the outer class, or null
         * @param innerName the simple name of the inner class, or null for anonymous
         * @param access    the access flags of the inner class
         */
        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            ClassfileModelFactory classFactory = ClassfileModelFactory.getInstance();
            CommonModelFactory commonFactory = CommonModelFactory.getInstance();
            innerClassEntries.add(classFactory.getInnerClassEntry(name, outerName, innerName, commonFactory.getAccessFlags(access)));
        }


        /**
         * Visit a permitted subclass of the class. This is called for each permitted subclass
         * listed in the PermittedSubclasses attribute of a sealed class or interface.
         *
         * @param permittedSubclass the internal name of the permitted subclass
         */
        @Override
        public void visitPermittedSubclass(String permittedSubclass) {
            permittedSubclasses.add(permittedSubclass);
        }


        /**
         * Visit a record component of the class and return a visitor to collect its metadata.
         *
         * @param name       the record component name
         * @param descriptor the record component type descriptor
         * @param signature  the record component generic signature, or null
         * @return a record component visitor for further visits
         */
        @Override
        public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
            return new RecordComponentCollector(name, descriptor, signature);
        }


        /**
         * Build the final IStructClass from collected data.
         *
         * @return the structured class
         */
        IStructClass buildStructClass() {
            CommonModelFactory commonFactory = CommonModelFactory.getInstance();
            ClassfileModelFactory classFactory = ClassfileModelFactory.getInstance();

            List<String> interfaceList;
            if (interfaces != null) {
                interfaceList = Arrays.asList(interfaces);
            } else {
                interfaceList = Collections.emptyList();
            }

            // Use a minimal constant pool placeholder (ASM handles CP internally)
            Object[] cpEntries = {null};

            IClassFileMetadata metadata = classFactory.getClassFileMetadata(majorVersion, minorVersion,
                    signature, sourceFile, recordComponents, innerClassEntries, permittedSubclasses);
            return classFactory.getStructClass(
                    metadata,
                    commonFactory.getConstantPool(cpEntries),
                    commonFactory.getAccessFlags(accessFlags),
                    name, superName, interfaceList,
                    fields, methods,
                    Collections.emptyList(), annotations);
        }


        /**
         * Inner visitor for fields.
         */
        private final class FieldCollector extends FieldVisitor {
            private final int fieldAccess;
            private final String fieldName;
            private final String fieldDescriptor;
            private final String fieldSignature;
            private final List<IAnnotation> fieldAnnotations = new ArrayList<>();


            /**
             * Constructor for FieldCollector.
             *
             * @param access     the field access flags
             * @param name       the field name
             * @param descriptor the field descriptor
             * @param signature  the field signature
             */
            FieldCollector(int access, String name, String descriptor, String signature) {
                super(Opcodes.ASM9);
                this.fieldAccess = access;
                this.fieldName = name;
                this.fieldDescriptor = descriptor;
                this.fieldSignature = signature;
            }


            /**
             * Visit a field-level annotation and return a visitor to collect its values.
             *
             * @param descriptor the annotation type descriptor
             * @param visible    true if the annotation is runtime-visible
             * @return an annotation visitor for the element-value pairs
             */
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                AnnotationCollector collector = new AnnotationCollector(descriptor, visible);
                return new AsmAnnotationVisitor(collector, fieldAnnotations);
            }


            /**
             * Complete the field visit and add the collected field to the class fields list.
             */
            @Override
            public void visitEnd() {
                IStructField field = ClassfileModelFactory.getInstance().getStructField(
                        fieldName, fieldDescriptor,
                        CommonModelFactory.getInstance().getAccessFlags(fieldAccess),
                        Collections.emptyList(), fieldAnnotations, fieldSignature);
                fields.add(field);
            }
        }


        /**
         * Inner visitor for methods that delegates to MethodNode for bytecode collection.
         */
        private final class MethodCollectorVisitor extends MethodVisitor {
            private final MethodNode methodNode;
            private final int methodAccess;
            private final String methodName;
            private final String methodDescriptor;
            private final String methodSignature;
            private final String[] methodExceptions;
            private final List<IAnnotation> methodAnnotations = new ArrayList<>();
            private final List<List<IAnnotation>> parameterAnnotations = new ArrayList<>();


            /**
             * Constructor for MethodCollectorVisitor.
             *
             * @param methodNode the ASM method node to collect bytecode
             * @param access     the method access flags
             * @param name       the method name
             * @param descriptor the method descriptor
             * @param signature  the method signature
             * @param exceptions the method exceptions
             */
            MethodCollectorVisitor(MethodNode methodNode, int access, String name,
                                   String descriptor, String signature, String[] exceptions) {
                super(Opcodes.ASM9, methodNode);
                this.methodNode = methodNode;
                this.methodAccess = access;
                this.methodName = name;
                this.methodDescriptor = descriptor;
                this.methodSignature = signature;
                this.methodExceptions = exceptions;
            }


            /**
             * Visit a method-level annotation and return a visitor to collect its values.
             *
             * @param descriptor the annotation type descriptor
             * @param visible    true if the annotation is runtime-visible
             * @return an annotation visitor for the element-value pairs
             */
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                AnnotationCollector collector = new AnnotationCollector(descriptor, visible);
                AsmAnnotationVisitor visitor = new AsmAnnotationVisitor(collector, methodAnnotations);
                // Also delegate to the method node for its own annotation tracking
                methodNode.visitAnnotation(descriptor, visible);
                return visitor;
            }


            /**
             * Visit a parameter-level annotation and return a visitor to collect its values.
             *
             * @param parameter  the zero-based parameter index
             * @param descriptor the annotation type descriptor
             * @param visible    true if the annotation is runtime-visible
             * @return an annotation visitor for the element-value pairs
             */
            @Override
            public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
                while (parameterAnnotations.size() <= parameter) {
                    parameterAnnotations.add(new ArrayList<>());
                }
                AnnotationCollector collector = new AnnotationCollector(descriptor, visible);
                return new AsmAnnotationVisitor(collector, parameterAnnotations.get(parameter));
            }


            /**
             * Complete the method visit, build the structured method, and add it to the class methods list.
             */
            @Override
            public void visitEnd() {
                super.visitEnd();

                List<String> exceptionTypes;
                if (methodExceptions != null) {
                    exceptionTypes = Arrays.asList(methodExceptions);
                } else {
                    exceptionTypes = Collections.emptyList();
                }

                byte[] code = null;
                int maxStack = 0;
                int maxLocals = 0;
                if (methodNode.instructions != null && methodNode.instructions.size() > 0) {
                    maxStack = methodNode.maxStack;
                    maxLocals = methodNode.maxLocals;
                    // Store raw code bytes placeholder; actual bytecode is in the MethodNode
                    code = new byte[0];
                }

                List<IAttribute> attributes = new ArrayList<>();
                if (code != null) {
                    attributes.add(CommonModelFactory.getInstance().getAttribute("Code", new byte[0]));
                }

                IMethodCode methodCode = ClassfileModelFactory.getInstance().getMethodCode(code, maxStack, maxLocals);
                IStructMethod method = ClassfileModelFactory.getInstance().getStructMethod(
                        methodName, methodDescriptor,
                        CommonModelFactory.getInstance().getAccessFlags(methodAccess),
                        attributes, methodAnnotations,
                        methodSignature, Collections.emptyList(), parameterAnnotations,
                        exceptionTypes, methodCode);
                methods.add(method);
            }
        }


        /**
         * Inner visitor for record components.
         */
        private final class RecordComponentCollector extends RecordComponentVisitor {
            private final String compName;
            private final String compDescriptor;
            private final String compSignature;
            private final List<IAnnotation> compAnnotations = new ArrayList<>();


            /**
             * Constructor for RecordComponentCollector.
             *
             * @param name       the component name
             * @param descriptor the component descriptor
             * @param signature  the component signature
             */
            RecordComponentCollector(String name, String descriptor, String signature) {
                super(Opcodes.ASM9);
                this.compName = name;
                this.compDescriptor = descriptor;
                this.compSignature = signature;
            }


            /**
             * Visit a record-component-level annotation and return a visitor to collect its values.
             *
             * @param descriptor the annotation type descriptor
             * @param visible    true if the annotation is runtime-visible
             * @return an annotation visitor for the element-value pairs
             */
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                AnnotationCollector collector = new AnnotationCollector(descriptor, visible);
                return new AsmAnnotationVisitor(collector, compAnnotations);
            }


            /**
             * Complete the record component visit and add the collected component to the class.
             */
            @Override
            public void visitEnd() {
                IStructRecordComponent component = ClassfileModelFactory.getInstance()
                        .getStructRecordComponent(compName, compDescriptor, compSignature, compAnnotations);
                recordComponents.add(component);
            }
        }
    }


    /**
     * Helper to collect annotation element-value pairs.
     */
    private static final class AnnotationCollector {
        private final String descriptor;
        private final boolean visible;
        private final Map<String, Object> values = new LinkedHashMap<>();


        /**
         * Constructor for AnnotationCollector.
         *
         * @param descriptor the annotation descriptor
         * @param visible    true if runtime-visible
         */
        AnnotationCollector(String descriptor, boolean visible) {
            this.descriptor = descriptor;
            this.visible = visible;
        }


        /**
         * Build the annotation model.
         *
         * @return the annotation
         */
        IAnnotation build() {
            return CommonModelFactory.getInstance().getAnnotation(descriptor, visible, values);
        }
    }


    /**
     * ASM annotation visitor that populates an AnnotationCollector and adds the
     * finished annotation to the target list when visiting completes.
     */
    private static final class AsmAnnotationVisitor extends AnnotationVisitor {
        private final AnnotationCollector collector;
        private final List<IAnnotation> targetList;


        /**
         * Constructor for AsmAnnotationVisitor.
         *
         * @param collector  the annotation collector
         * @param targetList the list to add the built annotation to on visitEnd
         */
        AsmAnnotationVisitor(AnnotationCollector collector, List<IAnnotation> targetList) {
            super(Opcodes.ASM9);
            this.collector = collector;
            this.targetList = targetList;
        }


        /**
         * Visit a primitive or String annotation element value.
         *
         * @param name  the element name
         * @param value the element value
         */
        @Override
        public void visit(String name, Object value) {
            if (name != null) {
                collector.values.put(name, value);
            }
        }


        /**
         * Visit an enum constant annotation element value. Stores the fully qualified
         * enum constant as "owner.CONSTANT" for proper rendering.
         *
         * @param name       the element name
         * @param descriptor the enum type descriptor
         * @param value      the enum constant name
         */
        @Override
        public void visitEnum(String name, String descriptor, String value) {
            if (name != null) {
                String enumType = descriptor;
                if (enumType.startsWith("L") && enumType.endsWith(";")) {
                    enumType = enumType.substring(1, enumType.length() - 1);
                }
                collector.values.put(name, enumType + "." + value);
            }
        }


        /**
         * Visit a nested annotation element value.
         *
         * @param name       the element name
         * @param descriptor the nested annotation type descriptor
         * @return an annotation visitor for the nested annotation
         */
        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            List<IAnnotation> nestedList = new ArrayList<>();
            AnnotationCollector nestedCollector = new AnnotationCollector(descriptor, collector.visible);
            collector.values.put(name, nestedList);
            return new AsmAnnotationVisitor(nestedCollector, nestedList);
        }


        /**
         * Visit an array-valued annotation element.
         *
         * @param name the element name
         * @return an annotation visitor for the array elements
         */
        @Override
        public AnnotationVisitor visitArray(String name) {
            List<Object> arrayValues = new ArrayList<>();
            collector.values.put(name, arrayValues);
            return new ArrayAnnotationVisitor(arrayValues, collector.visible);
        }


        /**
         * Complete the annotation visit and add the built annotation to the target list.
         */
        @Override
        public void visitEnd() {
            if (targetList != null) {
                targetList.add(collector.build());
            }
        }
    }


    /**
     * ASM annotation visitor for collecting array-valued annotation elements.
     */
    private static final class ArrayAnnotationVisitor extends AnnotationVisitor {
        private final List<Object> arrayValues;
        private final boolean visible;


        /**
         * Constructor for ArrayAnnotationVisitor.
         *
         * @param arrayValues the list to collect array elements into
         * @param visible     whether the parent annotation is runtime-visible
         */
        ArrayAnnotationVisitor(List<Object> arrayValues, boolean visible) {
            super(Opcodes.ASM9);
            this.arrayValues = arrayValues;
            this.visible = visible;
        }


        /**
         * Visit a primitive or String element in the array.
         *
         * @param name  unused for array elements
         * @param value the element value
         */
        @Override
        public void visit(String name, Object value) {
            arrayValues.add(value);
        }


        /**
         * Visit an enum constant element in the array.
         *
         * @param name       unused for array elements
         * @param descriptor the enum type descriptor
         * @param value      the enum constant name
         */
        @Override
        public void visitEnum(String name, String descriptor, String value) {
            String enumType = descriptor;
            if (enumType.startsWith("L") && enumType.endsWith(";")) {
                enumType = enumType.substring(1, enumType.length() - 1);
            }
            arrayValues.add(enumType + "." + value);
        }


        /**
         * Visit a nested annotation element in the array.
         *
         * @param name       unused for array elements
         * @param descriptor the nested annotation type descriptor
         * @return an annotation visitor for the nested annotation
         */
        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            List<IAnnotation> nestedList = new ArrayList<>();
            arrayValues.add(nestedList);
            AnnotationCollector nestedCollector = new AnnotationCollector(descriptor, visible);
            return new AsmAnnotationVisitor(nestedCollector, nestedList);
        }
    }
}
