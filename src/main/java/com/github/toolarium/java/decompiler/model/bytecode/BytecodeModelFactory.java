/*
 * BytecodeModelFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode;

import com.github.toolarium.java.decompiler.model.bytecode.impl.ExceptionHandlerImpl;
import com.github.toolarium.java.decompiler.model.bytecode.impl.ExceptionTableImpl;
import com.github.toolarium.java.decompiler.model.bytecode.impl.InstructionImpl;
import com.github.toolarium.java.decompiler.model.bytecode.impl.InstructionSequenceImpl;
import com.github.toolarium.java.decompiler.model.bytecode.impl.JumpInstructionImpl;
import com.github.toolarium.java.decompiler.model.bytecode.impl.SwitchInstructionImpl;
import java.util.List;


/**
 * Singleton factory for bytecode model objects.
 *
 * @author patrick
 */
public final class BytecodeModelFactory {

    /**
     * Private constructor.
     */
    private BytecodeModelFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final BytecodeModelFactory INSTANCE = new BytecodeModelFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static BytecodeModelFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get an instruction instance.
     *
     * @param offset   the byte offset
     * @param opcode   the opcode
     * @param mnemonic the mnemonic
     * @param operands the operands
     * @return the instruction
     */
    public IInstruction getInstruction(int offset, int opcode, String mnemonic, int[] operands) {
        return new InstructionImpl(offset, opcode, mnemonic, operands);
    }


    /**
     * Get a jump instruction instance.
     *
     * @param offset       the byte offset
     * @param opcode       the opcode
     * @param mnemonic     the mnemonic
     * @param operands     the operands
     * @param targetOffset the target byte offset
     * @return the jump instruction
     */
    public IJumpInstruction getJumpInstruction(int offset, int opcode, String mnemonic,
                                               int[] operands, int targetOffset) {
        return new JumpInstructionImpl(offset, opcode, mnemonic, operands, targetOffset);
    }


    /**
     * Get a switch instruction instance.
     *
     * @param offset        the byte offset
     * @param opcode        the opcode
     * @param mnemonic      the mnemonic
     * @param operands      the operands
     * @param defaultOffset the default target offset
     * @param keys          the case keys
     * @param targetOffsets the target offsets
     * @return the switch instruction
     */
    public ISwitchInstruction getSwitchInstruction(int offset, int opcode, String mnemonic,
                                                   int[] operands, int defaultOffset,
                                                   int[] keys, int[] targetOffsets) {
        return new SwitchInstructionImpl(offset, opcode, mnemonic, operands, defaultOffset, keys, targetOffsets);
    }


    /**
     * Get an instruction sequence instance.
     *
     * @param instructions the instructions
     * @return the instruction sequence
     */
    public IInstructionSequence getInstructionSequence(List<IInstruction> instructions) {
        return new InstructionSequenceImpl(instructions);
    }


    /**
     * Get an exception handler instance.
     *
     * @param startOffset   the try range start
     * @param endOffset     the try range end
     * @param handlerOffset the handler start
     * @param catchType     the exception type (null for finally)
     * @return the exception handler
     */
    public IExceptionHandler getExceptionHandler(int startOffset, int endOffset,
                                                 int handlerOffset, String catchType) {
        return new ExceptionHandlerImpl(startOffset, endOffset, handlerOffset, catchType);
    }


    /**
     * Get an exception table instance.
     *
     * @param handlers the exception handlers
     * @return the exception table
     */
    public IExceptionTable getExceptionTable(List<IExceptionHandler> handlers) {
        return new ExceptionTableImpl(handlers);
    }
}
