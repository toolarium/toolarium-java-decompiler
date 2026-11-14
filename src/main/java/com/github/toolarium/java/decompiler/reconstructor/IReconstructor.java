/*
 * IReconstructor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reconstructor;

import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;


/**
 * Reconstructs modern Java language constructs from bytecode patterns.
 * Each reconstructor detects a specific pattern and transforms the AST accordingly.
 *
 * @author patrick
 */
public interface IReconstructor {

    /**
     * Get the name of this reconstructor.
     *
     * @return the name
     */
    String getName();


    /**
     * Check whether this reconstructor applies to the given class.
     *
     * @param structClass the parsed class structure
     * @return true if this reconstructor should be applied
     */
    boolean applies(IStructClass structClass);


    /**
     * Apply the reconstruction, transforming the class declaration.
     *
     * @param classDeclaration the original class declaration
     * @param structClass      the parsed class structure for metadata access
     * @return the transformed class declaration
     */
    IClassDeclaration reconstruct(IClassDeclaration classDeclaration, IStructClass structClass);
}
