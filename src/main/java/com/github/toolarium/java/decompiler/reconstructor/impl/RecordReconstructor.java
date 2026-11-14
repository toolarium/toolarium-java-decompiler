/*
 * RecordReconstructor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reconstructor.impl;

import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IFieldDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IParameterDeclaration;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.model.classfile.IStructRecordComponent;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import com.github.toolarium.java.decompiler.reconstructor.IReconstructor;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Detects Java record classes (Java 16+) and removes the compiler-generated
 * boilerplate (accessor methods and component backing fields).
 * Records are identified by having record components in the class file.
 *
 * @author patrick
 */
public class RecordReconstructor implements IReconstructor {
    private static final Logger LOG = LoggerFactory.getLogger(RecordReconstructor.class);


    /**
     * Constructor for RecordReconstructor.
     */
    public RecordReconstructor() {
        // default constructor
    }


    /**
     * Get the name of this reconstructor.
     *
     * @return the name "RecordReconstructor"
     */
    @Override
    public String getName() {
        return "RecordReconstructor";
    }


    /**
     * Check whether this reconstructor applies to the given class.
     * Returns true if the class has record components.
     *
     * @param structClass the parsed class structure
     * @return true if the class is a record
     */
    @Override
    public boolean applies(IStructClass structClass) {
        return !structClass.getRecordComponents().isEmpty();
    }


    /**
     * Apply record reconstruction by removing compiler-generated accessor methods
     * and component backing fields, and adding record parameters.
     *
     * @param classDeclaration the original class declaration
     * @param structClass      the parsed class structure for metadata access
     * @return the transformed class declaration
     */
    @Override
    public IClassDeclaration reconstruct(IClassDeclaration classDeclaration, IStructClass structClass) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Reconstructing record: " + structClass.getName());
        }

        DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();

        // Build record parameters from record components
        List<IParameterDeclaration> recordParams = new ArrayList<>();
        for (IStructRecordComponent comp : structClass.getRecordComponents()) {
            recordParams.add(declFactory.getParameterDeclaration(
                    TypeModelFactory.getInstance().getObjectType(comp.getDescriptor()),
                    comp.getName(), false, comp.getAnnotations()));
        }

        // Filter out generated accessor methods
        List<IMethodDeclaration> filteredMethods = new ArrayList<>();
        for (IMethodDeclaration method : classDeclaration.getMethods()) {
            if (!isRecordGeneratedMethod(method, structClass)) {
                filteredMethods.add(method);
            }
        }

        // Filter out record component backing fields
        List<IFieldDeclaration> filteredFields = new ArrayList<>();
        for (IFieldDeclaration field : classDeclaration.getFields()) {
            if (!isRecordComponentField(field, structClass)) {
                filteredFields.add(field);
            }
        }

        return declFactory.getClassDeclaration(
                classDeclaration.getAccessFlags(),
                classDeclaration.getName(),
                classDeclaration.getSuperType(),
                classDeclaration.getInterfaceTypes(),
                filteredFields, filteredMethods,
                classDeclaration.getInnerClasses(),
                classDeclaration.getAnnotations(),
                classDeclaration.getPermittedSubclasses(), classDeclaration.isNonSealed());
    }


    /**
     * Check if a method is a compiler-generated record accessor.
     * Only zero-argument methods matching record component names are removed.
     *
     * @param method      the method
     * @param structClass the class structure
     * @return true if generated
     */
    private boolean isRecordGeneratedMethod(IMethodDeclaration method, IStructClass structClass) {
        String name = method.getName();

        // Accessor methods match record component names (must be zero-arg)
        for (IStructRecordComponent comp : structClass.getRecordComponents()) {
            if (comp.getName().equals(name) && method.getParameters().isEmpty()) {
                return true;
            }
        }

        return false;
    }


    /**
     * Check if a field is a record component backing field.
     *
     * @param field       the field
     * @param structClass the class structure
     * @return true if it backs a record component
     */
    private boolean isRecordComponentField(IFieldDeclaration field, IStructClass structClass) {
        for (IStructRecordComponent comp : structClass.getRecordComponents()) {
            if (comp.getName().equals(field.getName())) {
                return true;
            }
        }
        return false;
    }
}
