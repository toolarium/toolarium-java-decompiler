/*
 * DecompilerBuilder.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.api;

import com.github.toolarium.java.decompiler.impl.output.InMemoryResultSaver;
import com.github.toolarium.java.decompiler.source.ClassSourceFactory;
import com.github.toolarium.java.decompiler.source.IClassSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


/**
 * Fluent builder for configuring and running the decompiler.
 *
 * <p>Example usage:</p>
 * <pre>
 * // Decompile a JAR to an output directory
 * DecompilerBuilder.builder()
 *     .input(Path.of("mylib.jar"))
 *     .output(Path.of("output/"))
 *     .decompile();
 *
 * // Decompile to memory
 * Map&lt;String, String&gt; results = DecompilerBuilder.builder()
 *     .input(Path.of("mylib.jar"))
 *     .decompileToMemory();
 *
 * // Get a configured IDecompiler for custom use
 * IDecompiler decompiler = DecompilerBuilder.builder().build();
 * String source = decompiler.decompile(classBytes);
 * </pre>
 *
 * @author patrick
 */
public final class DecompilerBuilder {
    private Path inputPath;
    private IClassSource classSource;
    private Path outputPath;
    private IResultSaver resultSaver;
    private IDecompilerConfiguration configuration;


    /**
     * Private constructor. Use {@link #builder()} to create an instance.
     */
    private DecompilerBuilder() {
        // builder
    }


    /**
     * Create a new builder instance.
     *
     * @return the builder
     */
    public static DecompilerBuilder builder() {
        return new DecompilerBuilder();
    }


    /**
     * Set the input path to decompile. Accepts a single .class file, a directory of .class files,
     * or a JAR/ZIP archive. The input type is detected automatically from the path.
     *
     * @param inputPath the path to the input
     * @return this builder
     */
    public DecompilerBuilder input(Path inputPath) {
        this.inputPath = Objects.requireNonNull(inputPath, "inputPath");
        return this;
    }


    /**
     * Set a custom class source as input. Use this when the built-in file, directory, and JAR
     * sources are not sufficient.
     *
     * @param classSource the class source
     * @return this builder
     */
    public DecompilerBuilder input(IClassSource classSource) {
        this.classSource = Objects.requireNonNull(classSource, "classSource");
        return this;
    }


    /**
     * Set the output directory for decompiled source files. If not set, use
     * {@link #decompileToMemory()} or {@link #resultSaver(IResultSaver)} instead.
     *
     * @param outputPath the output directory path
     * @return this builder
     */
    public DecompilerBuilder output(Path outputPath) {
        this.outputPath = Objects.requireNonNull(outputPath, "outputPath");
        return this;
    }


    /**
     * Set a custom result saver. If an output path is also set, the result saver takes precedence.
     *
     * @param resultSaver the result saver
     * @return this builder
     */
    public DecompilerBuilder resultSaver(IResultSaver resultSaver) {
        this.resultSaver = Objects.requireNonNull(resultSaver, "resultSaver");
        return this;
    }


    /**
     * Set a custom decompiler configuration. Controls output formatting, feature toggles,
     * class filtering, and error handling. If not set, defaults are used.
     *
     * @param configuration the decompiler configuration
     * @return this builder
     * @see DecompilerConfigurationBuilder
     */
    public DecompilerBuilder configuration(IDecompilerConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        return this;
    }


    /**
     * Build a configured {@link IDecompiler} instance. The returned decompiler can be used
     * independently of the builder's input and output settings.
     *
     * @return the configured decompiler
     */
    public IDecompiler build() {
        if (configuration != null) {
            return DecompilerFactory.getInstance().getDecompiler(configuration);
        }
        return DecompilerFactory.getInstance().getDecompiler();
    }


    /**
     * Decompile all classes from the configured input to the configured output.
     * Requires either an input path or a class source, and either an output path or a result saver.
     *
     * @throws IOException if reading or decompiling fails
     * @throws IllegalStateException if no input or no output is configured
     */
    public void decompile() throws IOException {
        IClassSource source = resolveClassSource();
        IResultSaver saver = resolveResultSaver();
        IDecompiler decompiler = build();

        try {
            decompiler.decompileAll(source, saver);
        } finally {
            try {
                source.close();
            } catch (Exception ignored) {
                // ignore
            }
            try {
                saver.close();
            } catch (Exception ignored) {
                // ignore
            }
        }
    }


    /**
     * Decompile all classes from the configured input and return the results in memory.
     * Requires either an input path or a class source. No output path or result saver is needed.
     *
     * @return an unmodifiable map of class name to decompiled Java source
     * @throws IOException if reading or decompiling fails
     * @throws IllegalStateException if no input is configured
     */
    public Map<String, String> decompileToMemory() throws IOException {
        IClassSource source = resolveClassSource();
        InMemoryResultSaver memorySaver = new InMemoryResultSaver();
        IDecompiler decompiler = build();

        try {
            decompiler.decompileAll(source, memorySaver);
        } finally {
            try {
                source.close();
            } catch (Exception ignored) {
                // ignore
            }
        }

        return memorySaver.getResults();
    }


    /**
     * Decompile a single class from the configured input and return the source as a string.
     * Requires either an input path pointing to a single .class file, or a class source
     * with exactly one class.
     *
     * @return the decompiled Java source code
     * @throws IOException if reading or decompiling fails
     * @throws IllegalStateException if no input is configured
     */
    public String decompileToString() throws IOException {
        IClassSource source = resolveClassSource();
        IDecompiler decompiler = build();

        try {
            List<String> classes = source.listClasses();
            if (classes.isEmpty()) {
                throw new IOException("No classes found in the input source.");
            }
            return decompiler.decompile(source, classes.get(0));
        } finally {
            try {
                source.close();
            } catch (Exception ignored) {
                // ignore
            }
        }
    }


    /**
     * Resolve the configured class source from the input path or custom class source.
     *
     * @return the resolved class source
     * @throws IOException if the source cannot be created
     * @throws IllegalStateException if no input is configured
     */
    private IClassSource resolveClassSource() throws IOException {
        if (classSource != null) {
            return classSource;
        }
        if (inputPath == null) {
            throw new IllegalStateException("No input configured. Call input(Path) or input(IClassSource) before decompiling.");
        }

        ClassSourceFactory sourceFactory = ClassSourceFactory.getInstance();
        if (Files.isDirectory(inputPath)) {
            return sourceFactory.getDirectoryClassSource(inputPath);
        }

        String fileName = inputPath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".jar") || fileName.endsWith(".zip")) {
            return sourceFactory.getJarClassSource(inputPath);
        }

        return sourceFactory.getSingleFileClassSource(inputPath);
    }


    /**
     * Resolve the configured result saver from the output path or custom saver.
     *
     * @return the resolved result saver
     * @throws IllegalStateException if no output is configured
     */
    private IResultSaver resolveResultSaver() {
        if (resultSaver != null) {
            return resultSaver;
        }
        if (outputPath == null) {
            throw new IllegalStateException("No output configured. Call output(Path) or resultSaver(IResultSaver) before decompiling.");
        }

        return ResultSaverFactory.getInstance().getFileResultSaver(outputPath);
    }
}
