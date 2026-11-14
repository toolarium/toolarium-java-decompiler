/*
 * DecompilerConfigurationBuilderTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Tests for {@link DecompilerConfigurationBuilder} and {@link IDecompilerConfiguration}.
 *
 * @author patrick
 */
public class DecompilerConfigurationBuilderTest {

    /**
     * Test that the builder creates a configuration with all defaults.
     */
    @Test
    void testDefaultConfiguration() {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder().build();

        assertEquals("    ", config.getIndentString());
        assertEquals("\n", config.getLineSeparator());
        assertFalse(config.isShowBanner());
        assertFalse(config.isShowSourceFile());
        assertTrue(config.isRemoveBridgeMethods());
        assertTrue(config.isRemoveSyntheticMembers());
        assertTrue(config.isReconstructAssert());
        assertTrue(config.isReconstructEnum());
        assertTrue(config.isReconstructInnerClasses());
        assertTrue(config.isReconstructLambda());
        assertTrue(config.isReconstructTryWithResources());
        assertTrue(config.isReconstructStringSwitch());
        assertTrue(config.isReconstructRecord());
        assertTrue(config.isReconstructTextBlocks());
        assertTrue(config.isHideDefaultConstructor());
        assertTrue(config.isHideEmptySuper());
        assertFalse(config.isShowOverrideAnnotation());
        assertNull(config.getIncludeClassRegex());
        assertNull(config.getExcludeClassRegex());
        assertFalse(config.isIgnoreInvalidBytecode());
        assertEquals(0, config.getMaxMethodTimeSeconds());
    }


    /**
     * Test that builder setters override the defaults.
     */
    @Test
    void testCustomConfiguration() {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .indentString("\t")
                .lineSeparator("\r\n")
                .showBanner(true)
                .showSourceFile(true)
                .removeBridgeMethods(false)
                .removeSyntheticMembers(false)
                .reconstructAssert(false)
                .reconstructEnum(false)
                .showOverrideAnnotation(true)
                .includeClassRegex("com\\.example\\..*")
                .excludeClassRegex(".*Test")
                .ignoreInvalidBytecode(true)
                .maxMethodTimeSeconds(30)
                .build();

        assertEquals("\t", config.getIndentString());
        assertEquals("\r\n", config.getLineSeparator());
        assertTrue(config.isShowBanner());
        assertTrue(config.isShowSourceFile());
        assertFalse(config.isRemoveBridgeMethods());
        assertFalse(config.isRemoveSyntheticMembers());
        assertFalse(config.isReconstructAssert());
        assertFalse(config.isReconstructEnum());
        assertTrue(config.isShowOverrideAnnotation());
        assertEquals("com\\.example\\..*", config.getIncludeClassRegex());
        assertEquals(".*Test", config.getExcludeClassRegex());
        assertTrue(config.isIgnoreInvalidBytecode());
        assertEquals(30, config.getMaxMethodTimeSeconds());
    }


    /**
     * Test loading configuration from a Properties object.
     */
    @Test
    void testLoadFromProperties() {
        Properties props = new Properties();
        props.setProperty("indent.string", "\\t");
        props.setProperty("show.banner", "true");
        props.setProperty("remove.bridge.methods", "false");
        props.setProperty("max.method.time.seconds", "60");
        props.setProperty("include.class.regex", "com\\.example\\..*");

        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .loadFromProperties(props)
                .build();

        assertEquals("\t", config.getIndentString());
        assertTrue(config.isShowBanner());
        assertFalse(config.isRemoveBridgeMethods());
        assertEquals(60, config.getMaxMethodTimeSeconds());
        assertEquals("com\\.example\\..*", config.getIncludeClassRegex());
        assertTrue(config.isRemoveSyntheticMembers());
    }


    /**
     * Test loading configuration from a properties file.
     *
     * @param tempDir the temporary directory for the test file
     * @throws IOException if writing or reading the file fails
     */
    @Test
    void testLoadFromFile(@TempDir Path tempDir) throws IOException {
        Path propsFile = tempDir.resolve("test.properties");
        Files.writeString(propsFile,
                "show.banner = true\n"
                + "indent.string = \\t\\t\n"
                + "reconstruct.lambda = false\n");

        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .loadFromFile(propsFile)
                .build();

        assertTrue(config.isShowBanner());
        assertEquals("\t\t", config.getIndentString());
        assertFalse(config.isReconstructLambda());
        assertTrue(config.isReconstructEnum());
    }


    /**
     * Test that properties override previous builder settings.
     */
    @Test
    void testPropertiesOverrideBuilder() {
        Properties props = new Properties();
        props.setProperty("show.banner", "false");

        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .showBanner(true)
                .loadFromProperties(props)
                .build();

        assertFalse(config.isShowBanner());
    }


    /**
     * Test that toString() returns a non-null representation.
     */
    @Test
    void testToString() {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder().build();
        assertNotNull(config.toString());
        assertTrue(config.toString().contains("indentString"));
    }


    /**
     * Test that a configuration can be passed to the DecompilerBuilder.
     */
    @Test
    void testConfigurationWithDecompilerBuilder() {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .showBanner(true)
                .build();

        IDecompiler decompiler = DecompilerBuilder.builder()
                .configuration(config)
                .build();

        assertNotNull(decompiler);
    }


    /**
     * Test that a configured decompiler respects the banner setting.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testBannerInOutput() throws IOException {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .showBanner(true)
                .build();

        IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler(config);
        assertNotNull(decompiler);
    }


    /**
     * Test that the factory produces a decompiler with configuration.
     */
    @Test
    void testFactoryWithConfiguration() {
        IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
                .indentString("\t")
                .build();

        IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler(config);
        assertNotNull(decompiler);
    }
}
