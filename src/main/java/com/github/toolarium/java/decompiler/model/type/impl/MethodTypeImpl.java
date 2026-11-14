/*
 * MethodTypeImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type.impl;

import com.github.toolarium.java.decompiler.model.type.IMethodType;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IMethodType}.
 *
 * @author patrick
 */
public class MethodTypeImpl implements IMethodType {
    private final IType returnType;
    private final List<IType> parameterTypes;


    /**
     * Constructor for MethodTypeImpl.
     *
     * @param returnType     the return type
     * @param parameterTypes the parameter types (defensively copied)
     */
    public MethodTypeImpl(IType returnType, List<IType> parameterTypes) {
        this.returnType = Objects.requireNonNull(returnType, "Return type must not be null");
        if (parameterTypes != null) {
            this.parameterTypes = Collections.unmodifiableList(new ArrayList<>(parameterTypes));
        } else {
            this.parameterTypes = Collections.emptyList();
        }
    }


    /**
     * Get the return type of this method type.
     *
     * @return the return type
     */
    @Override
    public IType getReturnType() {
        return returnType;
    }


    /**
     * Get the parameter types as an unmodifiable list.
     *
     * @return an unmodifiable list of parameter types
     */
    @Override
    public List<IType> getParameterTypes() {
        return parameterTypes;
    }


    /**
     * Get the JVM method type descriptor (e.g., "(II)V").
     *
     * @return the method descriptor
     */
    @Override
    public String getDescriptor() {
        StringBuilder sb = new StringBuilder("(");
        for (IType paramType : parameterTypes) {
            sb.append(paramType.getDescriptor());
        }
        sb.append(')');
        sb.append(returnType.getDescriptor());
        return sb.toString();
    }


    /**
     * Get the human-readable display name (e.g., "(int, int) -> void").
     *
     * @return the display name
     */
    @Override
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < parameterTypes.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameterTypes.get(i).getDisplayName());
        }
        sb.append(") -> ");
        sb.append(returnType.getDisplayName());
        return sb.toString();
    }


    /**
     * Check whether this is a primitive type. Always returns false.
     *
     * @return false
     */
    @Override
    public boolean isPrimitive() {
        return false;
    }


    /**
     * Check whether this is an array type. Always returns false.
     *
     * @return false
     */
    @Override
    public boolean isArray() {
        return false;
    }


    /**
     * Check whether this is the void type. Always returns false.
     *
     * @return false
     */
    @Override
    public boolean isVoid() {
        return false;
    }


    /**
     * Get the number of array dimensions. Always returns 0.
     *
     * @return 0
     */
    @Override
    public int getDimensions() {
        return 0;
    }


    /**
     * Compute the hash code based on the return type and parameter types.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(returnType, parameterTypes);
    }


    /**
     * Check equality based on the return type and parameter types.
     *
     * @param obj the object to compare with
     * @return true if the other object is a MethodTypeImpl with equal return type and parameter types
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MethodTypeImpl other = (MethodTypeImpl) obj;
        return Objects.equals(returnType, other.returnType)
                && Objects.equals(parameterTypes, other.parameterTypes);
    }


    /**
     * Return a string representation including the method descriptor.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "MethodType[" + getDescriptor() + "]";
    }
}
