/*
 * ParameterizedTypeImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type.impl;

import com.github.toolarium.java.decompiler.model.type.IParameterizedType;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link IParameterizedType}.
 *
 * @author patrick
 */
public class ParameterizedTypeImpl implements IParameterizedType {
    private final IType rawType;
    private final List<IType> typeArguments;


    /**
     * Constructor for ParameterizedTypeImpl.
     *
     * @param rawType       the raw type
     * @param typeArguments the type arguments (defensively copied)
     */
    public ParameterizedTypeImpl(IType rawType, List<IType> typeArguments) {
        this.rawType = Objects.requireNonNull(rawType, "Raw type must not be null");
        if (typeArguments != null) {
            this.typeArguments = Collections.unmodifiableList(new ArrayList<>(typeArguments));
        } else {
            this.typeArguments = Collections.emptyList();
        }
    }


    /**
     * Get the raw type (e.g., the IObjectType representing List).
     *
     * @return the raw type
     */
    @Override
    public IType getRawType() {
        return rawType;
    }


    /**
     * Get the type arguments as an unmodifiable list.
     *
     * @return an unmodifiable list of type arguments
     */
    @Override
    public List<IType> getTypeArguments() {
        return typeArguments;
    }


    /**
     * Get the JVM type descriptor, delegating to the raw type.
     *
     * @return the type descriptor
     */
    @Override
    public String getDescriptor() {
        return rawType.getDescriptor();
    }


    /**
     * Get the human-readable display name including type arguments.
     *
     * @return the display name (e.g., "List&lt;String&gt;")
     */
    @Override
    public String getDisplayName() {
        if (typeArguments.isEmpty()) {
            return rawType.getDisplayName();
        }
        StringBuilder sb = new StringBuilder(rawType.getDisplayName());
        sb.append('<');
        for (int i = 0; i < typeArguments.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(typeArguments.get(i).getDisplayName());
        }
        sb.append('>');
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
     * Compute the hash code based on the raw type and type arguments.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(rawType, typeArguments);
    }


    /**
     * Check equality based on the raw type and type arguments.
     *
     * @param obj the object to compare with
     * @return true if the other object is a ParameterizedTypeImpl with equal raw type and type arguments
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ParameterizedTypeImpl other = (ParameterizedTypeImpl) obj;
        return Objects.equals(rawType, other.rawType)
                && Objects.equals(typeArguments, other.typeArguments);
    }


    /**
     * Return a string representation including the parameterized display name.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "ParameterizedType[" + getDisplayName() + "]";
    }
}
