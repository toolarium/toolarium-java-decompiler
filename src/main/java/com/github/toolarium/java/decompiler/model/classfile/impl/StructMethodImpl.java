/*
 * StructMethodImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.classfile.impl;

import com.github.toolarium.java.decompiler.model.classfile.IMethodCode;
import com.github.toolarium.java.decompiler.model.classfile.IStructMethod;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.common.IAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IStructMethod}.
 *
 * @author patrick
 */
public class StructMethodImpl extends StructMemberImpl implements IStructMethod {
    private final String signature;
    private final List<String> parameterNames;
    private final List<List<IAnnotation>> parameterAnnotations;
    private final List<String> exceptionTypes;
    private final IMethodCode methodCode;


    /**
     * Constructor for StructMethodImpl.
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
     */
    public StructMethodImpl(String name, String descriptor, IAccessFlags accessFlags,
                            List<IAttribute> attributes, List<IAnnotation> annotations,
                            String signature, List<String> parameterNames,
                            List<List<IAnnotation>> parameterAnnotations,
                            List<String> exceptionTypes, IMethodCode methodCode) {
        super(name, descriptor, accessFlags, attributes, annotations);
        this.signature = signature;
        if (parameterNames != null) {
            this.parameterNames = Collections.unmodifiableList(new ArrayList<>(parameterNames));
        } else {
            this.parameterNames = Collections.emptyList();
        }
        if (parameterAnnotations != null) {
            List<List<IAnnotation>> immutableParamAnnotations = new ArrayList<>();
            for (List<IAnnotation> paramAnns : parameterAnnotations) {
                immutableParamAnnotations.add(Collections.unmodifiableList(new ArrayList<>(paramAnns)));
            }
            this.parameterAnnotations = Collections.unmodifiableList(immutableParamAnnotations);
        } else {
            this.parameterAnnotations = Collections.emptyList();
        }
        if (exceptionTypes != null) {
            this.exceptionTypes = Collections.unmodifiableList(new ArrayList<>(exceptionTypes));
        } else {
            this.exceptionTypes = Collections.emptyList();
        }
        this.methodCode = methodCode;
    }


    /**
     * Get the generic signature of this method, if present.
     *
     * @return the generic signature, or null if not available
     */
    @Override
    public String getSignature() {
        return signature;
    }


    /**
     * Get the parameter names recovered from debug information.
     *
     * @return an unmodifiable list of parameter names, empty if not available
     */
    @Override
    public List<String> getParameterNames() {
        return parameterNames;
    }


    /**
     * Get the parameter annotations for this method.
     * The outer list is indexed by parameter position. Each inner list contains
     * the annotations for that parameter.
     *
     * @return an unmodifiable list of parameter annotation lists
     */
    @Override
    public List<List<IAnnotation>> getParameterAnnotations() {
        return parameterAnnotations;
    }


    /**
     * Get the checked exception types declared by this method.
     *
     * @return an unmodifiable list of exception type internal names
     */
    @Override
    public List<String> getExceptionTypes() {
        return exceptionTypes;
    }


    /**
     * Get a defensive copy of the raw bytecode of this method.
     *
     * @return the bytecode array, or null for abstract or native methods
     */
    @Override
    public byte[] getCode() {
        if (methodCode != null) {
            return methodCode.getCode();
        }
        return null;
    }


    /**
     * Get the maximum operand stack depth.
     *
     * @return the max stack depth
     */
    @Override
    public int getMaxStack() {
        if (methodCode != null) {
            return methodCode.getMaxStack();
        }
        return 0;
    }


    /**
     * Get the maximum number of local variables.
     *
     * @return the max local variable count
     */
    @Override
    public int getMaxLocals() {
        if (methodCode != null) {
            return methodCode.getMaxLocals();
        }
        return 0;
    }


    /**
     * Compute the hash code based on parent member fields, signature, parameters, exceptions, and method code.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hash(signature, parameterNames, parameterAnnotations, exceptionTypes, methodCode);
        return result;
    }


    /**
     * Check equality based on parent member fields, signature, parameters, exceptions, and method code.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StructMethodImpl other = (StructMethodImpl) obj;
        return Objects.equals(signature, other.signature)
                && Objects.equals(parameterNames, other.parameterNames)
                && Objects.equals(parameterAnnotations, other.parameterAnnotations)
                && Objects.equals(exceptionTypes, other.exceptionTypes)
                && Objects.equals(methodCode, other.methodCode);
    }


    /**
     * Return a string representation including the method name and descriptor.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "StructMethod[" + getName() + ", " + getDescriptor() + "]";
    }
}
