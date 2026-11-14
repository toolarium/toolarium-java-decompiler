/*
 * TypeModelFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type;

import com.github.toolarium.java.decompiler.model.type.impl.ArrayTypeImpl;
import com.github.toolarium.java.decompiler.model.type.impl.GenericTypeImpl;
import com.github.toolarium.java.decompiler.model.type.impl.MethodTypeImpl;
import com.github.toolarium.java.decompiler.model.type.impl.ObjectTypeImpl;
import com.github.toolarium.java.decompiler.model.type.impl.ParameterizedTypeImpl;
import com.github.toolarium.java.decompiler.model.type.impl.PrimitiveTypeImpl;
import com.github.toolarium.java.decompiler.model.type.impl.WildcardTypeImpl;
import java.util.List;


/**
 * Singleton factory for type model objects. Primitive types are cached as constants.
 *
 * @author patrick
 */
public final class TypeModelFactory {

    /**
     * Private constructor.
     */
    private TypeModelFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final TypeModelFactory INSTANCE = new TypeModelFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static TypeModelFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a primitive type instance. Returns cached constants for the 8 primitives and void.
     *
     * @param typeChar the JVM type character ('Z', 'B', 'C', 'S', 'I', 'J', 'F', 'D', 'V')
     * @return the primitive type
     * @throws IllegalArgumentException if the type character is not a valid primitive
     */
    public IPrimitiveType getPrimitiveType(char typeChar) {
        switch (typeChar) {
            case 'Z': return PrimitiveTypeImpl.BOOLEAN;
            case 'B': return PrimitiveTypeImpl.BYTE;
            case 'C': return PrimitiveTypeImpl.CHAR;
            case 'S': return PrimitiveTypeImpl.SHORT;
            case 'I': return PrimitiveTypeImpl.INT;
            case 'J': return PrimitiveTypeImpl.LONG;
            case 'F': return PrimitiveTypeImpl.FLOAT;
            case 'D': return PrimitiveTypeImpl.DOUBLE;
            case 'V': return PrimitiveTypeImpl.VOID;
            default:
                throw new IllegalArgumentException("Invalid primitive type character: " + typeChar);
        }
    }


    /**
     * Get an object type instance.
     *
     * @param internalName the JVM internal name (e.g., "java/lang/String")
     * @return the object type
     */
    public IObjectType getObjectType(String internalName) {
        return new ObjectTypeImpl(internalName);
    }


    /**
     * Get an array type instance.
     *
     * @param elementType the element type
     * @param dimensions  the number of array dimensions
     * @return the array type
     */
    public IArrayType getArrayType(IType elementType, int dimensions) {
        return new ArrayTypeImpl(elementType, dimensions);
    }


    /**
     * Get a generic type instance.
     *
     * @param typeVariable the type variable name
     * @param bound        the upper bound (null if unbounded)
     * @return the generic type
     */
    public IGenericType getGenericType(String typeVariable, IType bound) {
        return new GenericTypeImpl(typeVariable, bound);
    }


    /**
     * Get a parameterized type instance.
     *
     * @param rawType       the raw type
     * @param typeArguments the type arguments
     * @return the parameterized type
     */
    public IParameterizedType getParameterizedType(IType rawType, List<IType> typeArguments) {
        return new ParameterizedTypeImpl(rawType, typeArguments);
    }


    /**
     * Get a wildcard type instance.
     *
     * @param bound      the bound type, or null for an unbounded wildcard
     * @param upperBound true for upper-bounded or unbounded, false for lower-bounded
     * @return the wildcard type
     */
    public IWildcardType getWildcardType(IType bound, boolean upperBound) {
        return new WildcardTypeImpl(bound, upperBound);
    }


    /**
     * Get a method type instance.
     *
     * @param returnType     the return type
     * @param parameterTypes the parameter types
     * @return the method type
     */
    public IMethodType getMethodType(IType returnType, List<IType> parameterTypes) {
        return new MethodTypeImpl(returnType, parameterTypes);
    }
}
