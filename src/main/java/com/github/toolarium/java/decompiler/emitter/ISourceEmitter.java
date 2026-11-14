/*
 * ISourceEmitter.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.emitter;

import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;


/**
 * Emits formatted Java source code from AST nodes.
 *
 * @author patrick
 */
public interface ISourceEmitter {

    /**
     * Emit a class declaration as Java source code.
     *
     * @param classDeclaration the class declaration
     * @return the formatted Java source
     */
    String emitClass(IClassDeclaration classDeclaration);


    /**
     * Emit a class declaration with package and import statements.
     *
     * @param classDeclaration  the class declaration
     * @param fullInternalName  the full internal class name (e.g., "com/example/MyClass")
     * @return the formatted Java source with package and imports
     */
    String emitClassWithPackage(IClassDeclaration classDeclaration, String fullInternalName);


    /**
     * Emit a statement as Java source code.
     *
     * @param statement the statement
     * @return the formatted Java source
     */
    String emitStatement(IStatement statement);
}
