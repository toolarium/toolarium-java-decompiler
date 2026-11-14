/*
 * DeclarationModelFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.declaration;

import com.github.toolarium.java.decompiler.model.ast.declaration.impl.ClassDeclarationImpl;
import com.github.toolarium.java.decompiler.model.ast.declaration.impl.FieldDeclarationImpl;
import com.github.toolarium.java.decompiler.model.ast.declaration.impl.MethodDeclarationImpl;
import com.github.toolarium.java.decompiler.model.ast.declaration.impl.ParameterDeclarationImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.List;


/**
 * Singleton factory for AST declaration model objects.
 *
 * @author patrick
 */
public final class DeclarationModelFactory {

    /**
     * Private constructor.
     */
    private DeclarationModelFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final DeclarationModelFactory INSTANCE = new DeclarationModelFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static DeclarationModelFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a class declaration instance.
     *
     * @param accessFlags         the access modifiers
     * @param name                the class name
     * @param superType           the superclass type, or null
     * @param interfaceTypes      the implemented or extended interface types
     * @param fields              the field declarations
     * @param methods             the method declarations
     * @param innerClasses        the inner class declarations
     * @param annotations         the annotations on this class
     * @param permittedSubclasses the permitted subclass names for sealed classes
     * @param nonSealed           true if this class is non-sealed
     * @return the class declaration
     */
    public IClassDeclaration getClassDeclaration(IAccessFlags accessFlags,
                                                 String name,
                                                 IType superType,
                                                 List<IType> interfaceTypes,
                                                 List<IFieldDeclaration> fields,
                                                 List<IMethodDeclaration> methods,
                                                 List<IClassDeclaration> innerClasses,
                                                 List<IAnnotation> annotations,
                                                 List<String> permittedSubclasses,
                                                 boolean nonSealed) {
        return new ClassDeclarationImpl(accessFlags, name, superType, interfaceTypes,
                fields, methods, innerClasses, annotations, permittedSubclasses, nonSealed);
    }


    /**
     * Get a method declaration instance.
     *
     * @param accessFlags   the access modifiers
     * @param name          the method name
     * @param returnType    the return type, or null for constructors
     * @param parameters    the parameter declarations
     * @param thrownTypes   the declared exception types
     * @param body          the method body, or null for abstract or native methods
     * @param isConstructor true if this is a constructor
     * @param annotations   the annotations on this method
     * @return the method declaration
     */
    public IMethodDeclaration getMethodDeclaration(IAccessFlags accessFlags,
                                                   String name,
                                                   IType returnType,
                                                   List<IParameterDeclaration> parameters,
                                                   List<IType> thrownTypes,
                                                   IStatement body,
                                                   boolean isConstructor,
                                                   List<IAnnotation> annotations) {
        return new MethodDeclarationImpl(accessFlags, name, returnType, parameters,
                thrownTypes, body, isConstructor, annotations);
    }


    /**
     * Get a field declaration instance.
     *
     * @param accessFlags the access modifiers
     * @param fieldType   the field type
     * @param name        the field name
     * @param initializer the initializer expression, or null
     * @param annotations the annotations on this field
     * @return the field declaration
     */
    public IFieldDeclaration getFieldDeclaration(IAccessFlags accessFlags, IType fieldType,
                                                 String name, IExpression initializer,
                                                 List<IAnnotation> annotations) {
        return new FieldDeclarationImpl(accessFlags, fieldType, name, initializer, annotations);
    }


    /**
     * Get a parameter declaration instance.
     *
     * @param parameterType the parameter type
     * @param name          the parameter name
     * @param isFinal       true if the parameter is final
     * @param annotations   the annotations on this parameter
     * @return the parameter declaration
     */
    public IParameterDeclaration getParameterDeclaration(IType parameterType, String name, boolean isFinal,
                                                         List<IAnnotation> annotations) {
        return new ParameterDeclarationImpl(parameterType, name, isFinal, annotations);
    }
}
