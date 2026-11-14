/*
 * IBytecodeTranslator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.translator;

import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import java.util.List;
import org.objectweb.asm.tree.MethodNode;


/**
 * Translates bytecode instructions into AST statements by simulating
 * the JVM operand stack and converting operations into expressions.
 *
 * @author patrick
 */
public interface IBytecodeTranslator {

    /**
     * Translate the bytecode of a method into a list of AST statements.
     *
     * @param methodNode the ASM method node
     * @param className  the owning class internal name
     * @return the translated statements
     */
    List<IStatement> translate(MethodNode methodNode, String className);
}
