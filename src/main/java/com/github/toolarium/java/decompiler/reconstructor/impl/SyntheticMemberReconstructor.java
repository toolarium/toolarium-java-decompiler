/*
 * SyntheticMemberReconstructor.java
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
 * Removes synthetic and bridge members from the decompiled output.
 * These are compiler-generated artifacts not present in the original source code.
 *
 * @author patrick
 */
public class SyntheticMemberReconstructor implements IReconstructor {
    private static final Logger LOG = LoggerFactory.getLogger(SyntheticMemberReconstructor.class);


    /**
     * Constructor for SyntheticMemberReconstructor.
     */
    public SyntheticMemberReconstructor() {
        // default constructor
    }


    /**
     * Get the name of this reconstructor.
     *
     * @return the name "SyntheticMemberReconstructor"
     */
    @Override
    public String getName() {
        return "SyntheticMemberReconstructor";
    }


    /**
     * Check whether this reconstructor applies to the given class.
     * Always returns true, as any class may contain synthetic members.
     *
     * @param structClass the parsed class structure
     * @return true always
     */
    @Override
    public boolean applies(IStructClass structClass) {
        // Always apply: check for any synthetic members
        return true;
    }


    /**
     * Apply synthetic member reconstruction by removing synthetic and bridge
     * fields and methods from the decompiled output.
     *
     * @param classDeclaration the original class declaration
     * @param structClass      the parsed class structure for metadata access
     * @return the transformed class declaration
     */
    @Override
    public IClassDeclaration reconstruct(IClassDeclaration classDeclaration, IStructClass structClass) {
        // Collect names of synthetic fields and methods
        Set<String> syntheticFields = new HashSet<>();
        for (IStructField field : structClass.getFields()) {
            if (field.getAccessFlags().isSynthetic()) {
                syntheticFields.add(field.getName());
            }
        }

        Set<String> syntheticMethods = new HashSet<>();
        for (IStructMethod method : structClass.getMethods()) {
            if (method.getAccessFlags().isSynthetic() || method.getAccessFlags().isBridge()) {
                syntheticMethods.add(method.getName() + method.getDescriptor());
            }
        }

        if (syntheticFields.isEmpty() && syntheticMethods.isEmpty()) {
            return classDeclaration;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing " + syntheticFields.size() + " synthetic fields and "
                    + syntheticMethods.size() + " synthetic/bridge methods.");
        }

        List<IFieldDeclaration> filteredFields = new ArrayList<>();
        for (IFieldDeclaration field : classDeclaration.getFields()) {
            if (!syntheticFields.contains(field.getName())) {
                filteredFields.add(field);
            }
        }

        List<IMethodDeclaration> filteredMethods = new ArrayList<>();
        for (IMethodDeclaration method : classDeclaration.getMethods()) {
            if (!isDefinitelySynthetic(method, structClass, syntheticMethods)) {
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
     * and parameter count are in the synthetic set. Returns false if any match
     * is not synthetic, preventing removal of ambiguously-matched methods.
     *
     * @param method           the AST method declaration
     * @param structClass      the bytecode class structure
     * @param syntheticMethods the set of synthetic method keys (name+descriptor)
     * @return true only if every matching bytecode method is synthetic
     */
    private boolean isDefinitelySynthetic(IMethodDeclaration method, IStructClass structClass,
                                          Set<String> syntheticMethods) {
        String name = method.getName();
        int paramCount = method.getParameters().size();
        boolean foundAny = false;
        for (IStructMethod sm : structClass.getMethods()) {
            if (sm.getName().equals(name) && countDescriptorParams(sm.getDescriptor()) == paramCount) {
                foundAny = true;
                String key = sm.getName() + sm.getDescriptor();
                if (!syntheticMethods.contains(key)) {
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
}
