/*
 * DecompilerConfigurationImplTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.api.DecompilerConfigurationBuilder;
import com.github.toolarium.java.decompiler.api.IDecompilerConfiguration;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link DecompilerConfigurationImpl} equals, hashCode, and toString.
 *
 * @author patrick
 */
public class DecompilerConfigurationImplTest {


    /**
     * Test that two default configurations are equal.
     */
    @Test
    void testDefaultConfigsAreEqual() {
        IDecompilerConfiguration config1 = DecompilerConfigurationBuilder.builder().build();
        IDecompilerConfiguration config2 = DecompilerConfigurationBuilder.builder().build();

        assertEquals(config1, config2);
    }


    /**
     * Test that equal configurations have the same hash code.
     */
    @Test
    void testEqualConfigsHaveSameHashCode() {
        IDecompilerConfiguration config1 = DecompilerConfigurationBuilder.builder()
                .indentString("\t")
                .showBanner(true)
                .build();
        IDecompilerConfiguration config2 = DecompilerConfigurationBuilder.builder()
                .indentString("\t")
                .showBanner(true)
                .build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }


    /**
     * Test that configurations with different indent strings are not equal.
     */
    @Test
    void testDifferentIndentNotEqual() {
        IDecompilerConfiguration config1 = DecompilerConfigurationBuilder.builder()
                .indentString("    ")
                .build();
        IDecompilerConfiguration config2 = DecompilerConfigurationBuilder.builder()
                .indentString("\t")
                .build();

        assertNotEquals(config1, config2);
    }


    /**
     * Test that configurations with different boolean flags are not equal.
     */
    @Test
    void testDifferentFlagsNotEqual() {
        IDecompilerConfiguration config1 = DecompilerConfigurationBuilder.builder()
                .showBanner(true)
                .build();
        IDecompilerConfiguration config2 = DecompilerConfigurationBuilder.builder()
                .showBanner(false)
                .build();

        assertNotEquals(config1, config2);
    }


    /**
     * Test that configurations with different parallel threads are not equal.
     */
    @Test
    void testDifferentParallelThreadsNotEqual() {
        IDecompilerConfiguration config1 = DecompilerConfigurationBuilder.builder()
                .parallelThreads(4)
                .build();
        IDecompilerConfiguration config2 = DecompilerConfigurationBuilder.builder()
                .parallelThreads(8)
                .build();

        assertNotEquals(config1, config2);
    }


    /**
     * Test equals reflexivity: a config equals itself.
     */
    @Test
    void testEqualsReflexive() {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder().build();

        assertEquals(config, config);
    }


    /**
     * Test equals with null returns false.
     */
    @Test
    void testEqualsNullReturnsFalse() {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder().build();

        assertFalse(config.equals(null));
    }


    /**
     * Test equals with a different type returns false.
     */
    @Test
    void testEqualsDifferentTypeReturnsFalse() {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder().build();

        assertFalse(config.equals("not a config"));
    }


    /**
     * Test toString contains expected field names.
     */
    @Test
    void testToStringContainsFieldNames() {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .indentString("\t")
                .showBanner(true)
                .parallelThreads(4)
                .build();
        String str = config.toString();

        assertNotNull(str);
        assertTrue(str.contains("indentString"));
        assertTrue(str.contains("showBanner=true"));
        assertTrue(str.contains("parallelThreads=4"));
        assertTrue(str.contains("maxMethodTimeSeconds"));
        assertTrue(str.contains("removeBridgeMethods"));
        assertTrue(str.contains("reconstructEnum"));
    }


    /**
     * Test toString with default configuration.
     */
    @Test
    void testToStringDefault() {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder().build();
        String str = config.toString();

        assertNotNull(str);
        assertTrue(str.startsWith("DecompilerConfigurationImpl{"));
        assertTrue(str.endsWith("}"));
    }
}
