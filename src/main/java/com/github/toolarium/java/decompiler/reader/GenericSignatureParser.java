/*
 * GenericSignatureParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reader;

import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;


/**
 * Parses JVM generic signature strings into {@link IType} instances using ASM's
 * {@link SignatureReader} and {@link SignatureVisitor}. This utility converts
 * erased type information stored in the Signature attribute back into parameterized
 * types, generic type variables, and wildcard types.
 *
 * <p>This class is stateless. All methods are static and accept a
 * {@link TypeModelFactory} for constructing type instances.
 *
 * @author patrick
 */
public final class GenericSignatureParser {

    /**
     * Private constructor to prevent instantiation.
     */
    private GenericSignatureParser() {
    }


    /**
     * Parse a field or local variable type signature into an {@link IType}.
     * Handles parameterized types, type variables, arrays, and wildcards.
     *
     * @param signature the JVM type signature (e.g., "Ljava/util/List&lt;Ljava/lang/String;&gt;;")
     * @param factory   the type model factory for creating type instances
     * @return the parsed type
     * @throws IllegalArgumentException if the signature is null or empty
     */
    public static IType parseFieldTypeSignature(String signature, TypeModelFactory factory) {
        if (signature == null || signature.isEmpty()) {
            throw new IllegalArgumentException("Signature must not be null or empty");
        }
        TypeCollector collector = new TypeCollector(factory);
        new SignatureReader(signature).acceptType(collector);
        return collector.getResult();
    }


    /**
     * Parse a method signature to extract generic parameter types and return type.
     *
     * @param signature the JVM method signature (e.g., "(Ljava/util/List&lt;Ljava/lang/String;&gt;;)V")
     * @param factory   the type model factory for creating type instances
     * @return the parsed method signature result containing parameter types and return type
     * @throws IllegalArgumentException if the signature is null or empty
     */
    public static MethodSignatureResult parseMethodSignature(String signature, TypeModelFactory factory) {
        if (signature == null || signature.isEmpty()) {
            throw new IllegalArgumentException("Signature must not be null or empty");
        }
        MethodSignatureCollector collector = new MethodSignatureCollector(factory);
        new SignatureReader(signature).accept(collector);
        return new MethodSignatureResult(collector.getParameterTypes(), collector.getReturnType());
    }


    /**
     * Parse a class signature to extract generic superclass and interface types.
     *
     * @param signature the JVM class signature
     * @param factory   the type model factory for creating type instances
     * @return the parsed class signature result containing superclass and interface types
     * @throws IllegalArgumentException if the signature is null or empty
     */
    public static ClassSignatureResult parseClassSignature(String signature, TypeModelFactory factory) {
        if (signature == null || signature.isEmpty()) {
            throw new IllegalArgumentException("Signature must not be null or empty");
        }
        ClassSignatureCollector collector = new ClassSignatureCollector(factory);
        new SignatureReader(signature).accept(collector);
        return new ClassSignatureResult(collector.getSuperclassType(), collector.getInterfaceTypes());
    }


    /**
     * Holds the result of parsing a method signature: parameter types and return type.
     *
     * @author patrick
     */
    public static final class MethodSignatureResult {
        private final List<IType> parameterTypes;
        private final IType returnType;


        /**
         * Constructor for MethodSignatureResult.
         *
         * @param parameterTypes the generic parameter types
         * @param returnType     the generic return type
         */
        MethodSignatureResult(List<IType> parameterTypes, IType returnType) {
            this.parameterTypes = Collections.unmodifiableList(new ArrayList<>(parameterTypes));
            this.returnType = returnType;
        }


        /**
         * Get the parameter types with generic information.
         *
         * @return an unmodifiable list of parameter types
         */
        public List<IType> getParameterTypes() {
            return parameterTypes;
        }


        /**
         * Get the return type with generic information.
         *
         * @return the return type
         */
        public IType getReturnType() {
            return returnType;
        }
    }


    /**
     * Holds the result of parsing a class signature: superclass and interface types.
     *
     * @author patrick
     */
    public static final class ClassSignatureResult {
        private final IType superclassType;
        private final List<IType> interfaceTypes;


        /**
         * Constructor for ClassSignatureResult.
         *
         * @param superclassType the generic superclass type
         * @param interfaceTypes the generic interface types
         */
        ClassSignatureResult(IType superclassType, List<IType> interfaceTypes) {
            this.superclassType = superclassType;
            this.interfaceTypes = Collections.unmodifiableList(new ArrayList<>(interfaceTypes));
        }


        /**
         * Get the superclass type with generic information.
         *
         * @return the superclass type, or null if java.lang.Object
         */
        public IType getSuperclassType() {
            return superclassType;
        }


        /**
         * Get the interface types with generic information.
         *
         * @return an unmodifiable list of interface types
         */
        public List<IType> getInterfaceTypes() {
            return interfaceTypes;
        }
    }


    /**
     * Collects type information from a JVM type signature into an {@link IType}.
     * Handles class types, parameterized types, type variables, arrays, primitives,
     * and wildcards.
     *
     * @author patrick
     */
    private static class TypeCollector extends SignatureVisitor {
        private final TypeModelFactory factory;
        private IType result;
        private String className;
        private List<TypeArgumentEntry> pendingTypeArguments;
        private int arrayDimensions;


        /**
         * Constructor for TypeCollector.
         *
         * @param factory the type model factory
         */
        TypeCollector(TypeModelFactory factory) {
            super(Opcodes.ASM9);
            this.factory = factory;
        }


        /**
         * Get the built type result, wrapping in an array type if array dimensions were collected.
         *
         * @return the parsed type
         */
        IType getResult() {
            if (arrayDimensions > 0 && result != null) {
                return factory.getArrayType(result, arrayDimensions);
            }
            return result;
        }


        /**
         * Visit a primitive base type.
         *
         * @param descriptor the primitive type descriptor character
         */
        @Override
        public void visitBaseType(char descriptor) {
            result = factory.getPrimitiveType(descriptor);
        }


        /**
         * Visit a type variable reference (e.g., "T").
         *
         * @param name the type variable name
         */
        @Override
        public void visitTypeVariable(String name) {
            result = factory.getGenericType(name, null);
        }


        /**
         * Visit an array dimension. Increments the array dimension counter and returns
         * this collector so subsequent type visits build the element type.
         *
         * @return this collector for the element type
         */
        @Override
        public SignatureVisitor visitArrayType() {
            arrayDimensions++;
            return this;
        }


        /**
         * Visit the start of a class type. Begins collecting type arguments.
         *
         * @param name the internal class name
         */
        @Override
        public void visitClassType(String name) {
            className = name;
            pendingTypeArguments = new ArrayList<>();
        }


        /**
         * Visit an inner class type. Updates the class name to include the inner class.
         *
         * @param name the inner class simple name
         */
        @Override
        public void visitInnerClassType(String name) {
            className = className + "$" + name;
            pendingTypeArguments = new ArrayList<>();
        }


        /**
         * Visit an unbounded wildcard type argument ({@code ?}).
         */
        @Override
        public void visitTypeArgument() {
            pendingTypeArguments.add(new TypeArgumentEntry(null, '='));
        }


        /**
         * Visit a bounded type argument. Returns a new collector for the argument type.
         *
         * @param wildcard the wildcard indicator: '=' for exact, '+' for extends, '-' for super
         * @return a collector for the type argument
         */
        @Override
        public SignatureVisitor visitTypeArgument(char wildcard) {
            TypeCollector argumentCollector = new TypeCollector(factory);
            pendingTypeArguments.add(new TypeArgumentEntry(argumentCollector, wildcard));
            return argumentCollector;
        }


        /**
         * Visit the end of a class type. Builds the final type from the class name
         * and any collected type arguments.
         */
        @Override
        public void visitEnd() {
            IType rawType = factory.getObjectType(className);

            if (pendingTypeArguments != null && !pendingTypeArguments.isEmpty()) {
                List<IType> typeArgs = new ArrayList<>();
                for (TypeArgumentEntry entry : pendingTypeArguments) {
                    typeArgs.add(resolveTypeArgument(entry));
                }
                result = factory.getParameterizedType(rawType, typeArgs);
            } else {
                result = rawType;
            }
        }


        /**
         * Resolve a type argument entry into an {@link IType}, handling wildcards.
         *
         * @param entry the type argument entry to resolve
         * @return the resolved type
         */
        private IType resolveTypeArgument(TypeArgumentEntry entry) {
            if (entry.getCollector() == null) {
                return factory.getWildcardType(null, true);
            }

            IType argumentType = entry.getCollector().getResult();
            if (entry.getWildcard() == '+') {
                return factory.getWildcardType(argumentType, true);
            }
            if (entry.getWildcard() == '-') {
                return factory.getWildcardType(argumentType, false);
            }
            return argumentType;
        }
    }


    /**
     * Holds a pending type argument: the collector for the argument type and the wildcard indicator.
     *
     * @author patrick
     */
    private static class TypeArgumentEntry {
        private final TypeCollector collector;
        private final char wildcard;


        /**
         * Constructor for TypeArgumentEntry.
         *
         * @param collector the type collector (null for unbounded wildcards)
         * @param wildcard  the wildcard indicator character
         */
        TypeArgumentEntry(TypeCollector collector, char wildcard) {
            this.collector = collector;
            this.wildcard = wildcard;
        }


        /**
         * Get the type collector for this type argument.
         *
         * @return the collector, or null for unbounded wildcards
         */
        TypeCollector getCollector() {
            return collector;
        }


        /**
         * Get the wildcard indicator character.
         *
         * @return '=' for exact, '+' for extends, '-' for super
         */
        char getWildcard() {
            return wildcard;
        }
    }


    /**
     * Collects parameter types and return type from a method signature.
     *
     * @author patrick
     */
    private static class MethodSignatureCollector extends SignatureVisitor {
        private final TypeModelFactory factory;
        private final List<TypeCollector> parameterCollectors;
        private TypeCollector returnCollector;


        /**
         * Constructor for MethodSignatureCollector.
         *
         * @param factory the type model factory
         */
        MethodSignatureCollector(TypeModelFactory factory) {
            super(Opcodes.ASM9);
            this.factory = factory;
            this.parameterCollectors = new ArrayList<>();
        }


        /**
         * Visit a formal type parameter declaration. Ignored for type extraction.
         *
         * @param name the type parameter name
         */
        @Override
        public void visitFormalTypeParameter(String name) {
            // Formal type parameters are not extracted
        }


        /**
         * Visit the class bound of a formal type parameter. Consumes the bound without storing it.
         *
         * @return a collector that consumes the bound
         */
        @Override
        public SignatureVisitor visitClassBound() {
            return new TypeCollector(factory);
        }


        /**
         * Visit an interface bound of a formal type parameter. Consumes the bound without storing it.
         *
         * @return a collector that consumes the bound
         */
        @Override
        public SignatureVisitor visitInterfaceBound() {
            return new TypeCollector(factory);
        }


        /**
         * Visit a method parameter type. Creates and returns a collector for the parameter type.
         *
         * @return a collector for the parameter type
         */
        @Override
        public SignatureVisitor visitParameterType() {
            TypeCollector collector = new TypeCollector(factory);
            parameterCollectors.add(collector);
            return collector;
        }


        /**
         * Visit the method return type. Creates and returns a collector for the return type.
         *
         * @return a collector for the return type
         */
        @Override
        public SignatureVisitor visitReturnType() {
            returnCollector = new TypeCollector(factory);
            return returnCollector;
        }


        /**
         * Visit a thrown exception type. Consumes the exception type without storing it.
         *
         * @return a collector that consumes the exception type
         */
        @Override
        public SignatureVisitor visitExceptionType() {
            return new TypeCollector(factory);
        }


        /**
         * Get the parsed parameter types.
         *
         * @return the list of parameter types
         */
        List<IType> getParameterTypes() {
            List<IType> types = new ArrayList<>();
            for (TypeCollector collector : parameterCollectors) {
                types.add(collector.getResult());
            }
            return types;
        }


        /**
         * Get the parsed return type.
         *
         * @return the return type, or void if not available
         */
        IType getReturnType() {
            if (returnCollector != null) {
                return returnCollector.getResult();
            }
            return factory.getPrimitiveType('V');
        }
    }


    /**
     * Collects superclass and interface types from a class signature.
     *
     * @author patrick
     */
    private static class ClassSignatureCollector extends SignatureVisitor {
        private final TypeModelFactory factory;
        private TypeCollector superclassCollector;
        private final List<TypeCollector> interfaceCollectors;


        /**
         * Constructor for ClassSignatureCollector.
         *
         * @param factory the type model factory
         */
        ClassSignatureCollector(TypeModelFactory factory) {
            super(Opcodes.ASM9);
            this.factory = factory;
            this.interfaceCollectors = new ArrayList<>();
        }


        /**
         * Visit a formal type parameter declaration. Ignored for type extraction.
         *
         * @param name the type parameter name
         */
        @Override
        public void visitFormalTypeParameter(String name) {
            // Formal type parameters are not extracted
        }


        /**
         * Visit the class bound of a formal type parameter. Consumes the bound without storing it.
         *
         * @return a collector that consumes the bound
         */
        @Override
        public SignatureVisitor visitClassBound() {
            return new TypeCollector(factory);
        }


        /**
         * Visit an interface bound of a formal type parameter. Consumes the bound without storing it.
         *
         * @return a collector that consumes the bound
         */
        @Override
        public SignatureVisitor visitInterfaceBound() {
            return new TypeCollector(factory);
        }


        /**
         * Visit the superclass type. Creates and returns a collector for the superclass type.
         *
         * @return a collector for the superclass type
         */
        @Override
        public SignatureVisitor visitSuperclass() {
            superclassCollector = new TypeCollector(factory);
            return superclassCollector;
        }


        /**
         * Visit an interface type. Creates and returns a collector for the interface type.
         *
         * @return a collector for the interface type
         */
        @Override
        public SignatureVisitor visitInterface() {
            TypeCollector collector = new TypeCollector(factory);
            interfaceCollectors.add(collector);
            return collector;
        }


        /**
         * Get the parsed superclass type.
         *
         * @return the superclass type, or null if not available
         */
        IType getSuperclassType() {
            if (superclassCollector != null) {
                return superclassCollector.getResult();
            }
            return null;
        }


        /**
         * Get the parsed interface types.
         *
         * @return the list of interface types
         */
        List<IType> getInterfaceTypes() {
            List<IType> types = new ArrayList<>();
            for (TypeCollector collector : interfaceCollectors) {
                types.add(collector.getResult());
            }
            return types;
        }
    }
}
