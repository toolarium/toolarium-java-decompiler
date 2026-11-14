/*
 * InnerClassReconstructor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reconstructor.impl;

import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IFieldDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.model.classfile.IStructField;
import com.github.toolarium.java.decompiler.model.classfile.IStructMethod;
import com.github.toolarium.java.decompiler.reconstructor.IReconstructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Detects inner class artifacts and cleans them up:
 * removes the synthetic this$0 field (outer class reference),
 * removes access$NNN synthetic accessor methods,
 * and cleans up val$xxx captured variable fields from anonymous classes.
 *
 * @author patrick
 */
public class InnerClassReconstructor implements IReconstructor {
    private static final Logger LOG = LoggerFactory.getLogger(InnerClassReconstructor.class);
    private static final String OUTER_REF_PREFIX = "this$";
    private static final String ACCESSOR_PREFIX = "access$";
    private static final String CAPTURED_VAR_PREFIX = "val$";


    /**
     * Constructor for InnerClassReconstructor.
     */
    public InnerClassReconstructor() {
        // default constructor
    }


    /**
     * Get the name of this reconstructor.
     *
     * @return the name "InnerClassReconstructor"
     */
    @Override
    public String getName() {
        return "InnerClassReconstructor";
    }


    /**
     * Check whether this reconstructor applies to the given class.
     * Returns true if the class name contains '$', has a this$0 field, or has access$ methods.
     *
     * @param structClass the parsed class structure
     * @return true if the class has inner class artifacts
     */
    @Override
    public boolean applies(IStructClass structClass) {
        // Check if the class has inner class indicators:
        // this$0 field, or class name contains $, or has access$ methods
        String className = structClass.getName();
        if (className.contains("$")) {
            return true;
        }

        for (IStructField field : structClass.getFields()) {
            if (field.getName().startsWith(OUTER_REF_PREFIX)) {
                return true;
            }
        }

        for (IStructMethod method : structClass.getMethods()) {
            if (method.getName().startsWith(ACCESSOR_PREFIX)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Apply inner class reconstruction by removing the synthetic this$0 field,
     * access$NNN accessor methods, and val$xxx captured variable fields.
     *
     * @param classDeclaration the original class declaration
     * @param structClass      the parsed class structure for metadata access
     * @return the transformed class declaration
     */
    @Override
    public IClassDeclaration reconstruct(IClassDeclaration classDeclaration, IStructClass structClass) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cleaning inner class artifacts: " + structClass.getName());
        }

        // Filter out synthetic inner class fields
        List<IFieldDeclaration> filteredFields = new ArrayList<>();
        for (IFieldDeclaration field : classDeclaration.getFields()) {
            if (!isInnerClassField(field.getName())) {
                filteredFields.add(field);
            }
        }

        // Collect synthetic accessor method signatures
        Set<String> accessorSignatures = new HashSet<>();
        for (IStructMethod sm : structClass.getMethods()) {
            if (sm.getName().startsWith(ACCESSOR_PREFIX) && sm.getAccessFlags().isSynthetic()) {
                accessorSignatures.add(sm.getName() + sm.getDescriptor());
            }
        }

        // Filter out synthetic accessor methods by matching name and parameter count
        List<IMethodDeclaration> filteredMethods = new ArrayList<>();
        for (IMethodDeclaration method : classDeclaration.getMethods()) {
            if (!isDefinitelyAccessor(method, structClass, accessorSignatures)) {
                filteredMethods.add(method);
            }
        }

        return DeclarationModelFactory.getInstance().getClassDeclaration(
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
     * Check whether all bytecode methods matching the given declaration by name
     * and parameter count are in the accessor signature set. Returns false if
     * any match is not an accessor, preventing removal of ambiguously-matched methods.
     *
     * @param method             the AST method declaration
     * @param structClass        the bytecode class structure
     * @param accessorSignatures the set of accessor method keys (name+descriptor)
     * @return true only if every matching bytecode method is an accessor
     */
    private boolean isDefinitelyAccessor(IMethodDeclaration method, IStructClass structClass,
                                          Set<String> accessorSignatures) {
        String name = method.getName();
        int paramCount = method.getParameters().size();
        boolean foundAny = false;
        for (IStructMethod sm : structClass.getMethods()) {
            if (sm.getName().equals(name) && countDescriptorParams(sm.getDescriptor()) == paramCount) {
                foundAny = true;
                String key = sm.getName() + sm.getDescriptor();
                if (!accessorSignatures.contains(key)) {
                    return false;
                }
            }
        }
        return foundAny;
    }


    /**
     * Count the number of parameters in a JVM method descriptor.
     *
     * @param descriptor the method descriptor, e.g. "(II)V"
     * @return the number of parameters
     */
    private int countDescriptorParams(String descriptor) {
        int count = 0;
        int idx = 1;
        while (idx < descriptor.length() && descriptor.charAt(idx) != ')') {
            char ch = descriptor.charAt(idx);
            if (ch == 'L') {
                int semiIdx = descriptor.indexOf(';', idx);
                if (semiIdx < 0) {
                    break;
                }
                idx = semiIdx + 1;
            } else if (ch == '[') {
                idx++;
                continue;
            } else {
                idx++;
            }
            count++;
        }
        return count;
    }


    /**
     * Check if a field name is an inner class artifact.
     *
     * @param fieldName the field name
     * @return true if it should be removed
     */
    private boolean isInnerClassField(String fieldName) {
        // this$0, this$1, etc. - outer class reference
        if (fieldName.startsWith(OUTER_REF_PREFIX)) {
            return true;
        }
        // val$xxx - captured variable from enclosing scope (anonymous/local classes)
        if (fieldName.startsWith(CAPTURED_VAR_PREFIX)) {
            return true;
        }
        return false;
    }
}
