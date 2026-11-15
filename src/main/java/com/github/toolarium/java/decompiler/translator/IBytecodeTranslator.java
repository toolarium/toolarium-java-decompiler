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


    /**
     * Set the maximum time in seconds allowed for translating a single method.
     * A value of zero or negative disables the limit. When the limit is exceeded,
     * the translation returns a partial result.
     *
     * @param seconds the maximum translation time in seconds
     */
    void setMaxTranslationTimeSeconds(int seconds);
}
