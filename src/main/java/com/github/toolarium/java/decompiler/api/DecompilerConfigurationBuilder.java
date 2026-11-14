/*
 * DecompilerConfigurationBuilder.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.api;

import com.github.toolarium.java.decompiler.impl.DecompilerConfigurationImpl;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;


/**
 * Fluent builder for creating {@link IDecompilerConfiguration} instances.
 *
 * <p>All options have sensible defaults matching the standard decompiler behavior.
 * Only options that need to differ from the defaults need to be set.
 *
 * <p>Example usage:</p>
 * <pre>
 * IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
 *     .indentString("\t")
 *     .showBanner(true)
 *     .removeSyntheticMembers(false)
 *     .build();
 * </pre>
 *
 * @author patrick
 * @see IDecompilerConfiguration
 */
public final class DecompilerConfigurationBuilder {
    private static final String DEFAULT_INDENT = "    ";
    private static final String DEFAULT_LINE_SEPARATOR = "\n";

    private String indentString = DEFAULT_INDENT;
    private String lineSeparator = DEFAULT_LINE_SEPARATOR;
    private boolean showBanner;
    private boolean showSourceFile;
    private boolean removeBridgeMethods = true;
    private boolean removeSyntheticMembers = true;
    private boolean reconstructAssert = true;
    private boolean reconstructEnum = true;
    private boolean reconstructInnerClasses = true;
    private boolean reconstructLambda = true;
    private boolean reconstructTryWithResources = true;
    private boolean reconstructStringSwitch = true;
    private boolean reconstructRecord = true;
    private boolean reconstructTextBlocks = true;
    private boolean hideDefaultConstructor = true;
    private boolean hideEmptySuper = true;
    private boolean showOverrideAnnotation;
    private String includeClassRegex;
    private String excludeClassRegex;
    private boolean ignoreInvalidBytecode;
    private int maxMethodTimeSeconds;


    /**
     * Private constructor. Use {@link #builder()} to create an instance.
     */
    private DecompilerConfigurationBuilder() {
        // builder
    }


    /**
     * Create a new configuration builder with default settings.
     *
     * @return a new builder instance
     */
    public static DecompilerConfigurationBuilder builder() {
        return new DecompilerConfigurationBuilder();
    }


    /**
     * Set the indentation string used for each level of nesting.
     *
     * @param indentString the indent string (e.g., "    " for 4 spaces, "\t" for tab)
     * @return this builder
     */
    public DecompilerConfigurationBuilder indentString(String indentString) {
        this.indentString = indentString;
        return this;
    }


    /**
     * Set the line separator used in emitted source code.
     *
     * @param lineSeparator the line separator (e.g., "\n" or "\r\n")
     * @return this builder
     */
    public DecompilerConfigurationBuilder lineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
        return this;
    }


    /**
     * Set whether to emit a banner comment at the top of each decompiled file.
     *
     * @param showBanner true to show the banner
     * @return this builder
     */
    public DecompilerConfigurationBuilder showBanner(boolean showBanner) {
        this.showBanner = showBanner;
        return this;
    }


    /**
     * Set whether to emit a source file comment.
     *
     * @param showSourceFile true to show the source file comment
     * @return this builder
     */
    public DecompilerConfigurationBuilder showSourceFile(boolean showSourceFile) {
        this.showSourceFile = showSourceFile;
        return this;
    }


    /**
     * Set whether to remove bridge methods from the output.
     *
     * @param removeBridgeMethods true to remove bridge methods
     * @return this builder
     */
    public DecompilerConfigurationBuilder removeBridgeMethods(boolean removeBridgeMethods) {
        this.removeBridgeMethods = removeBridgeMethods;
        return this;
    }


    /**
     * Set whether to remove synthetic members from the output.
     *
     * @param removeSyntheticMembers true to remove synthetic members
     * @return this builder
     */
    public DecompilerConfigurationBuilder removeSyntheticMembers(boolean removeSyntheticMembers) {
        this.removeSyntheticMembers = removeSyntheticMembers;
        return this;
    }


    /**
     * Set whether to reconstruct assert statements.
     *
     * @param reconstructAssert true to enable assert reconstruction
     * @return this builder
     */
    public DecompilerConfigurationBuilder reconstructAssert(boolean reconstructAssert) {
        this.reconstructAssert = reconstructAssert;
        return this;
    }


    /**
     * Set whether to reconstruct enum declarations.
     *
     * @param reconstructEnum true to enable enum reconstruction
     * @return this builder
     */
    public DecompilerConfigurationBuilder reconstructEnum(boolean reconstructEnum) {
        this.reconstructEnum = reconstructEnum;
        return this;
    }


    /**
     * Set whether to reconstruct inner classes.
     *
     * @param reconstructInnerClasses true to enable inner class reconstruction
     * @return this builder
     */
    public DecompilerConfigurationBuilder reconstructInnerClasses(boolean reconstructInnerClasses) {
        this.reconstructInnerClasses = reconstructInnerClasses;
        return this;
    }


    /**
     * Set whether to reconstruct lambda expressions.
     *
     * @param reconstructLambda true to enable lambda reconstruction
     * @return this builder
     */
    public DecompilerConfigurationBuilder reconstructLambda(boolean reconstructLambda) {
        this.reconstructLambda = reconstructLambda;
        return this;
    }


    /**
     * Set whether to reconstruct try-with-resources statements.
     *
     * @param reconstructTryWithResources true to enable try-with-resources reconstruction
     * @return this builder
     */
    public DecompilerConfigurationBuilder reconstructTryWithResources(boolean reconstructTryWithResources) {
        this.reconstructTryWithResources = reconstructTryWithResources;
        return this;
    }


    /**
     * Set whether to reconstruct string switch statements.
     *
     * @param reconstructStringSwitch true to enable string switch reconstruction
     * @return this builder
     */
    public DecompilerConfigurationBuilder reconstructStringSwitch(boolean reconstructStringSwitch) {
        this.reconstructStringSwitch = reconstructStringSwitch;
        return this;
    }


    /**
     * Set whether to reconstruct record declarations.
     *
     * @param reconstructRecord true to enable record reconstruction
     * @return this builder
     */
    public DecompilerConfigurationBuilder reconstructRecord(boolean reconstructRecord) {
        this.reconstructRecord = reconstructRecord;
        return this;
    }


    /**
     * Set whether to reconstruct text blocks from multi-line strings.
     *
     * @param reconstructTextBlocks true to enable text block reconstruction
     * @return this builder
     */
    public DecompilerConfigurationBuilder reconstructTextBlocks(boolean reconstructTextBlocks) {
        this.reconstructTextBlocks = reconstructTextBlocks;
        return this;
    }


    /**
     * Set whether to hide the default constructor when it has an empty body.
     *
     * @param hideDefaultConstructor true to hide empty default constructors
     * @return this builder
     */
    public DecompilerConfigurationBuilder hideDefaultConstructor(boolean hideDefaultConstructor) {
        this.hideDefaultConstructor = hideDefaultConstructor;
        return this;
    }


    /**
     * Set whether to hide empty super() calls in constructors.
     *
     * @param hideEmptySuper true to hide empty super() calls
     * @return this builder
     */
    public DecompilerConfigurationBuilder hideEmptySuper(boolean hideEmptySuper) {
        this.hideEmptySuper = hideEmptySuper;
        return this;
    }


    /**
     * Set whether to add @Override annotations on overriding methods.
     *
     * @param showOverrideAnnotation true to add @Override annotations
     * @return this builder
     */
    public DecompilerConfigurationBuilder showOverrideAnnotation(boolean showOverrideAnnotation) {
        this.showOverrideAnnotation = showOverrideAnnotation;
        return this;
    }


    /**
     * Set a regex pattern for including classes during batch decompilation.
     * Only classes whose fully qualified name matches this pattern will be decompiled.
     *
     * @param includeClassRegex the include regex, or null for no filter
     * @return this builder
     */
    public DecompilerConfigurationBuilder includeClassRegex(String includeClassRegex) {
        this.includeClassRegex = includeClassRegex;
        return this;
    }


    /**
     * Set a regex pattern for excluding classes during batch decompilation.
     * Classes whose fully qualified name matches this pattern will be skipped.
     *
     * @param excludeClassRegex the exclude regex, or null for no filter
     * @return this builder
     */
    public DecompilerConfigurationBuilder excludeClassRegex(String excludeClassRegex) {
        this.excludeClassRegex = excludeClassRegex;
        return this;
    }


    /**
     * Set whether to continue decompilation when invalid bytecode is encountered.
     *
     * @param ignoreInvalidBytecode true to ignore invalid bytecode errors
     * @return this builder
     */
    public DecompilerConfigurationBuilder ignoreInvalidBytecode(boolean ignoreInvalidBytecode) {
        this.ignoreInvalidBytecode = ignoreInvalidBytecode;
        return this;
    }


    /**
     * Set the maximum time in seconds allowed for decompiling a single method.
     * A value of 0 means no time limit.
     *
     * @param maxMethodTimeSeconds the maximum method time in seconds
     * @return this builder
     */
    public DecompilerConfigurationBuilder maxMethodTimeSeconds(int maxMethodTimeSeconds) {
        this.maxMethodTimeSeconds = maxMethodTimeSeconds;
        return this;
    }


    /**
     * Load configuration values from a properties object. Property keys use dot notation
     * matching the configuration option names (e.g., "indent.string", "show.banner").
     * Only properties that are present will override the current builder state.
     *
     * @param properties the properties to load
     * @return this builder
     * @see #loadFromFile(Path)
     */
    public DecompilerConfigurationBuilder loadFromProperties(Properties properties) {
        String value;

        value = properties.getProperty("indent.string");
        if (value != null) {
            indentString = unescapeWhitespace(value);
        }

        value = properties.getProperty("line.separator");
        if (value != null) {
            lineSeparator = unescapeWhitespace(value);
        }

        loadBooleanProperty(properties, "show.banner");
        loadBooleanProperty(properties, "show.source.file");
        loadBooleanProperty(properties, "remove.bridge.methods");
        loadBooleanProperty(properties, "remove.synthetic.members");
        loadBooleanProperty(properties, "reconstruct.assert");
        loadBooleanProperty(properties, "reconstruct.enum");
        loadBooleanProperty(properties, "reconstruct.inner.classes");
        loadBooleanProperty(properties, "reconstruct.lambda");
        loadBooleanProperty(properties, "reconstruct.try.with.resources");
        loadBooleanProperty(properties, "reconstruct.string.switch");
        loadBooleanProperty(properties, "reconstruct.record");
        loadBooleanProperty(properties, "reconstruct.text.blocks");
        loadBooleanProperty(properties, "hide.default.constructor");
        loadBooleanProperty(properties, "hide.empty.super");
        loadBooleanProperty(properties, "show.override.annotation");
        loadBooleanProperty(properties, "ignore.invalid.bytecode");

        value = properties.getProperty("include.class.regex");
        if (value != null) {
            includeClassRegex = value;
        }

        value = properties.getProperty("exclude.class.regex");
        if (value != null) {
            excludeClassRegex = value;
        }

        value = properties.getProperty("max.method.time.seconds");
        if (value != null) {
            maxMethodTimeSeconds = Integer.parseInt(value.trim());
        }

        return this;
    }


    /**
     * Load configuration values from a properties file. This is a convenience method
     * that reads the file and delegates to {@link #loadFromProperties(Properties)}.
     *
     * @param propertiesFile the path to the properties file
     * @return this builder
     * @throws IOException if the file cannot be read
     * @see #loadFromProperties(Properties)
     */
    public DecompilerConfigurationBuilder loadFromFile(Path propertiesFile) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(propertiesFile)) {
            properties.load(in);
        }
        return loadFromProperties(properties);
    }


    /**
     * Build an immutable {@link IDecompilerConfiguration} from the current builder state.
     *
     * @return the configuration
     */
    public IDecompilerConfiguration build() {
        DecompilerConfigurationImpl.OutputFormattingOptions formatting =
                new DecompilerConfigurationImpl.OutputFormattingOptions(
                        indentString, lineSeparator, showBanner, showSourceFile);

        DecompilerConfigurationImpl.ExtraFeatureToggles extraToggles =
                new DecompilerConfigurationImpl.ExtraFeatureToggles(
                        reconstructTextBlocks, hideDefaultConstructor,
                        hideEmptySuper, showOverrideAnnotation);

        DecompilerConfigurationImpl.FeatureToggleOptions features =
                new DecompilerConfigurationImpl.FeatureToggleOptions(
                        removeBridgeMethods, removeSyntheticMembers,
                        reconstructAssert, reconstructEnum,
                        reconstructInnerClasses, reconstructLambda,
                        reconstructTryWithResources, reconstructStringSwitch,
                        reconstructRecord, extraToggles);

        DecompilerConfigurationImpl.FilterAndErrorOptions filterAndError =
                new DecompilerConfigurationImpl.FilterAndErrorOptions(
                        includeClassRegex, excludeClassRegex,
                        ignoreInvalidBytecode, maxMethodTimeSeconds);

        return new DecompilerConfigurationImpl(formatting, features, filterAndError);
    }


    /**
     * Load a boolean property and apply it to the corresponding builder field.
     *
     * @param properties the properties
     * @param key        the property key
     */
    private void loadBooleanProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            return;
        }

        boolean boolValue = Boolean.parseBoolean(value.trim());

        if ("show.banner".equals(key)) {
            showBanner = boolValue;
        } else if ("show.source.file".equals(key)) {
            showSourceFile = boolValue;
        } else if ("remove.bridge.methods".equals(key)) {
            removeBridgeMethods = boolValue;
        } else if ("remove.synthetic.members".equals(key)) {
            removeSyntheticMembers = boolValue;
        } else if ("reconstruct.assert".equals(key)) {
            reconstructAssert = boolValue;
        } else if ("reconstruct.enum".equals(key)) {
            reconstructEnum = boolValue;
        } else if ("reconstruct.inner.classes".equals(key)) {
            reconstructInnerClasses = boolValue;
        } else if ("reconstruct.lambda".equals(key)) {
            reconstructLambda = boolValue;
        } else if ("reconstruct.try.with.resources".equals(key)) {
            reconstructTryWithResources = boolValue;
        } else if ("reconstruct.string.switch".equals(key)) {
            reconstructStringSwitch = boolValue;
        } else if ("reconstruct.record".equals(key)) {
            reconstructRecord = boolValue;
        } else if ("reconstruct.text.blocks".equals(key)) {
            reconstructTextBlocks = boolValue;
        } else if ("hide.default.constructor".equals(key)) {
            hideDefaultConstructor = boolValue;
        } else if ("hide.empty.super".equals(key)) {
            hideEmptySuper = boolValue;
        } else if ("show.override.annotation".equals(key)) {
            showOverrideAnnotation = boolValue;
        } else if ("ignore.invalid.bytecode".equals(key)) {
            ignoreInvalidBytecode = boolValue;
        }
    }


    /**
     * Unescape whitespace literals in a property value.
     * Converts literal escape sequences like "\t" and "\n" to their character equivalents.
     *
     * @param value the property value
     * @return the unescaped value
     */
    private String unescapeWhitespace(String value) {
        return value.replace("\\t", "\t").replace("\\n", "\n").replace("\\r", "\r");
    }
}
