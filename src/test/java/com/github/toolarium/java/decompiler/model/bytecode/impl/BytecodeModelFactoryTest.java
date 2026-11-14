/*
 * BytecodeModelFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.bytecode.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.bytecode.BytecodeModelFactory;
import com.github.toolarium.java.decompiler.model.bytecode.IExceptionHandler;
import com.github.toolarium.java.decompiler.model.bytecode.IExceptionTable;
import com.github.toolarium.java.decompiler.model.bytecode.IInstruction;
import com.github.toolarium.java.decompiler.model.bytecode.IInstructionSequence;
import com.github.toolarium.java.decompiler.model.bytecode.IJumpInstruction;
import com.github.toolarium.java.decompiler.model.bytecode.ISwitchInstruction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link BytecodeModelFactory} and its model implementations.
 *
 * @author patrick
 */
public class BytecodeModelFactoryTest {
    private static final String ALOAD_0 = "aload_0";
    private static final String GOTO_MNEMONIC = "goto";
    private static final String LOOKUPSWITCH = "lookupswitch";
    private final BytecodeModelFactory factory = BytecodeModelFactory.getInstance();


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        BytecodeModelFactory factory1 = BytecodeModelFactory.getInstance();
        BytecodeModelFactory factory2 = BytecodeModelFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Test instruction creation and getters.
     */
    @Test
    void testInstruction() {
        IInstruction instr = factory.getInstruction(0, 0x2A, ALOAD_0, new int[0]);
        assertEquals(0, instr.getOffset());
        assertEquals(0x2A, instr.getOpcode());
        assertEquals(ALOAD_0, instr.getMnemonic());
        assertArrayEquals(new int[0], instr.getOperands());
        assertEquals(1, instr.getLength());
    }


    /**
     * Test instruction with operands.
     */
    @Test
    void testInstructionWithOperands() {
        IInstruction instr = factory.getInstruction(5, 0xB6, "invokevirtual", new int[]{0, 3});
        assertEquals(3, instr.getLength());
        assertArrayEquals(new int[]{0, 3}, instr.getOperands());
    }


    /**
     * Test instruction operand defensive copy.
     */
    @Test
    void testInstructionOperandDefensiveCopy() {
        int[] operands = {1, 2};
        IInstruction instr = factory.getInstruction(0, 0x10, "bipush", operands);
        operands[0] = 99;
        assertArrayEquals(new int[]{1, 2}, instr.getOperands());

        int[] returned = instr.getOperands();
        returned[0] = 99;
        assertArrayEquals(new int[]{1, 2}, instr.getOperands());
    }


    /**
     * Test instruction with null operands.
     */
    @Test
    void testInstructionNullOperands() {
        IInstruction instr = factory.getInstruction(0, 0x00, "nop", null);
        assertArrayEquals(new int[0], instr.getOperands());
        assertEquals(1, instr.getLength());
    }


    /**
     * Test instruction equals and hashCode.
     */
    @Test
    void testInstructionEqualsHashCode() {
        IInstruction instr1 = factory.getInstruction(0, 0x2A, ALOAD_0, new int[0]);
        IInstruction instr2 = factory.getInstruction(0, 0x2A, ALOAD_0, new int[0]);
        IInstruction instr3 = factory.getInstruction(1, 0x2A, ALOAD_0, new int[0]);

        assertEquals(instr1, instr2);
        assertEquals(instr1.hashCode(), instr2.hashCode());
        assertNotEquals(instr1, instr3);
        assertNotEquals(instr1, null);
        assertEquals(instr1, instr1);
    }


    /**
     * Test instruction toString.
     */
    @Test
    void testInstructionToString() {
        IInstruction instr = factory.getInstruction(10, 0x2A, ALOAD_0, new int[0]);
        assertTrue(instr.toString().contains(ALOAD_0));
        assertTrue(instr.toString().contains("10"));
    }


    /**
     * Test jump instruction.
     */
    @Test
    void testJumpInstruction() {
        IJumpInstruction jump = factory.getJumpInstruction(5, 0xA7, GOTO_MNEMONIC, new int[]{0, 10}, 15);
        assertEquals(5, jump.getOffset());
        assertEquals(0xA7, jump.getOpcode());
        assertEquals(GOTO_MNEMONIC, jump.getMnemonic());
        assertEquals(15, jump.getTargetOffset());
    }


    /**
     * Test jump instruction equals and hashCode.
     */
    @Test
    void testJumpInstructionEqualsHashCode() {
        IJumpInstruction jump1 = factory.getJumpInstruction(0, 0xA7, GOTO_MNEMONIC, new int[]{0, 5}, 5);
        IJumpInstruction jump2 = factory.getJumpInstruction(0, 0xA7, GOTO_MNEMONIC, new int[]{0, 5}, 5);
        IJumpInstruction jump3 = factory.getJumpInstruction(0, 0xA7, GOTO_MNEMONIC, new int[]{0, 5}, 10);

        assertEquals(jump1, jump2);
        assertEquals(jump1.hashCode(), jump2.hashCode());
        assertNotEquals(jump1, jump3);
    }


    /**
     * Test jump instruction toString.
     */
    @Test
    void testJumpInstructionToString() {
        IJumpInstruction jump = factory.getJumpInstruction(0, 0xA7, GOTO_MNEMONIC, null, 20);
        assertTrue(jump.toString().contains(GOTO_MNEMONIC));
        assertTrue(jump.toString().contains("20"));
    }


    /**
     * Test switch instruction.
     */
    @Test
    void testSwitchInstruction() {
        int[] keys = {1, 2, 3};
        int[] targets = {20, 30, 40};
        ISwitchInstruction sw = factory.getSwitchInstruction(10, 0xAB, LOOKUPSWITCH,
                new int[0], 50, keys, targets);

        assertEquals(10, sw.getOffset());
        assertEquals(LOOKUPSWITCH, sw.getMnemonic());
        assertEquals(50, sw.getDefaultOffset());
        assertArrayEquals(new int[]{1, 2, 3}, sw.getKeys());
        assertArrayEquals(new int[]{20, 30, 40}, sw.getTargetOffsets());
    }


    /**
     * Test switch instruction defensive copy.
     */
    @Test
    void testSwitchInstructionDefensiveCopy() {
        int[] keys = {1, 2};
        int[] targets = {10, 20};
        ISwitchInstruction sw = factory.getSwitchInstruction(0, 0xAB, LOOKUPSWITCH,
                null, 30, keys, targets);

        keys[0] = 99;
        targets[0] = 99;
        assertArrayEquals(new int[]{1, 2}, sw.getKeys());
        assertArrayEquals(new int[]{10, 20}, sw.getTargetOffsets());
    }


    /**
     * Test switch instruction equals and hashCode.
     */
    @Test
    void testSwitchInstructionEqualsHashCode() {
        ISwitchInstruction sw1 = factory.getSwitchInstruction(0, 0xAB, LOOKUPSWITCH,
                null, 30, new int[]{1}, new int[]{10});
        ISwitchInstruction sw2 = factory.getSwitchInstruction(0, 0xAB, LOOKUPSWITCH,
                null, 30, new int[]{1}, new int[]{10});
        ISwitchInstruction sw3 = factory.getSwitchInstruction(0, 0xAB, LOOKUPSWITCH,
                null, 30, new int[]{2}, new int[]{10});

        assertEquals(sw1, sw2);
        assertEquals(sw1.hashCode(), sw2.hashCode());
        assertNotEquals(sw1, sw3);
    }


    /**
     * Test switch instruction toString.
     */
    @Test
    void testSwitchInstructionToString() {
        ISwitchInstruction sw = factory.getSwitchInstruction(0, 0xAB, LOOKUPSWITCH,
                null, 50, new int[]{1, 2, 3}, new int[]{10, 20, 30});
        assertTrue(sw.toString().contains(LOOKUPSWITCH));
        assertTrue(sw.toString().contains("3"));
        assertTrue(sw.toString().contains("50"));
    }


    /**
     * Test instruction sequence.
     */
    @Test
    void testInstructionSequence() {
        IInstruction instr1 = factory.getInstruction(0, 0x2A, ALOAD_0, null);
        IInstruction instr2 = factory.getInstruction(1, 0xB1, "return", null);
        IInstructionSequence seq = factory.getInstructionSequence(Arrays.asList(instr1, instr2));

        assertEquals(2, seq.getLength());
        assertEquals(instr1, seq.getInstruction(0));
        assertEquals(instr2, seq.getInstruction(1));
        assertEquals(instr1, seq.getInstructionAtOffset(0));
        assertEquals(instr2, seq.getInstructionAtOffset(1));
        assertNull(seq.getInstructionAtOffset(99));
        assertEquals(2, seq.getInstructions().size());
    }


    /**
     * Test instruction sequence with null list.
     */
    @Test
    void testInstructionSequenceNull() {
        IInstructionSequence seq = factory.getInstructionSequence(null);
        assertEquals(0, seq.getLength());
        assertTrue(seq.getInstructions().isEmpty());
    }


    /**
     * Test instruction sequence equals and hashCode.
     */
    @Test
    void testInstructionSequenceEqualsHashCode() {
        IInstruction instr = factory.getInstruction(0, 0x00, "nop", null);
        IInstructionSequence seq1 = factory.getInstructionSequence(Collections.singletonList(instr));
        IInstructionSequence seq2 = factory.getInstructionSequence(Collections.singletonList(instr));
        IInstructionSequence seq3 = factory.getInstructionSequence(Collections.emptyList());

        assertEquals(seq1, seq2);
        assertEquals(seq1.hashCode(), seq2.hashCode());
        assertNotEquals(seq1, seq3);
    }


    /**
     * Test instruction sequence toString.
     */
    @Test
    void testInstructionSequenceToString() {
        IInstructionSequence seq = factory.getInstructionSequence(Collections.emptyList());
        assertTrue(seq.toString().contains("0"));
    }


    /**
     * Test exception handler.
     */
    @Test
    void testExceptionHandler() {
        IExceptionHandler handler = factory.getExceptionHandler(0, 10, 15, "java/io/IOException");
        assertEquals(0, handler.getStartOffset());
        assertEquals(10, handler.getEndOffset());
        assertEquals(15, handler.getHandlerOffset());
        assertEquals("java/io/IOException", handler.getCatchType());
    }


    /**
     * Test exception handler for finally (null catch type).
     */
    @Test
    void testExceptionHandlerFinally() {
        IExceptionHandler handler = factory.getExceptionHandler(0, 10, 20, null);
        assertNull(handler.getCatchType());
        assertTrue(handler.toString().contains("finally"));
    }


    /**
     * Test exception handler equals and hashCode.
     */
    @Test
    void testExceptionHandlerEqualsHashCode() {
        IExceptionHandler eh1 = factory.getExceptionHandler(0, 10, 15, "java/io/IOException");
        IExceptionHandler eh2 = factory.getExceptionHandler(0, 10, 15, "java/io/IOException");
        IExceptionHandler eh3 = factory.getExceptionHandler(0, 10, 15, "java/lang/Exception");

        assertEquals(eh1, eh2);
        assertEquals(eh1.hashCode(), eh2.hashCode());
        assertNotEquals(eh1, eh3);
    }


    /**
     * Test exception handler toString.
     */
    @Test
    void testExceptionHandlerToString() {
        IExceptionHandler handler = factory.getExceptionHandler(5, 15, 20, "java/lang/Exception");
        assertTrue(handler.toString().contains("5"));
        assertTrue(handler.toString().contains("15"));
        assertTrue(handler.toString().contains("20"));
    }


    /**
     * Test exception table.
     */
    @Test
    void testExceptionTable() {
        IExceptionHandler eh1 = factory.getExceptionHandler(0, 10, 15, "java/io/IOException");
        IExceptionHandler eh2 = factory.getExceptionHandler(5, 20, 25, null);
        IExceptionTable table = factory.getExceptionTable(Arrays.asList(eh1, eh2));

        assertEquals(2, table.getSize());
        assertEquals(2, table.getHandlers().size());

        // Test getHandlersAt
        List<IExceptionHandler> atZero = table.getHandlersAt(0);
        assertEquals(1, atZero.size());

        List<IExceptionHandler> atSeven = table.getHandlersAt(7);
        assertEquals(2, atSeven.size());

        List<IExceptionHandler> atFifteen = table.getHandlersAt(15);
        assertEquals(1, atFifteen.size());

        List<IExceptionHandler> atThirty = table.getHandlersAt(30);
        assertEquals(0, atThirty.size());
    }


    /**
     * Test exception table with null list.
     */
    @Test
    void testExceptionTableNull() {
        IExceptionTable table = factory.getExceptionTable(null);
        assertEquals(0, table.getSize());
    }


    /**
     * Test exception table equals and hashCode.
     */
    @Test
    void testExceptionTableEqualsHashCode() {
        IExceptionHandler eh = factory.getExceptionHandler(0, 10, 15, null);
        IExceptionTable table1 = factory.getExceptionTable(Collections.singletonList(eh));
        IExceptionTable table2 = factory.getExceptionTable(Collections.singletonList(eh));
        IExceptionTable table3 = factory.getExceptionTable(Collections.emptyList());

        assertEquals(table1, table2);
        assertEquals(table1.hashCode(), table2.hashCode());
        assertNotEquals(table1, table3);
    }


    /**
     * Test exception table toString.
     */
    @Test
    void testExceptionTableToString() {
        IExceptionTable table = factory.getExceptionTable(Collections.emptyList());
        assertTrue(table.toString().contains("0"));
    }
}
