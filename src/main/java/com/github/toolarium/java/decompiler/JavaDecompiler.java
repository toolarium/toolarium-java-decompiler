/*
 * JavaDecompiler.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler;

import com.github.toolarium.java.decompiler.api.DecompilerConfigurationBuilder;
import com.github.toolarium.java.decompiler.api.DecompilerFactory;
import com.github.toolarium.java.decompiler.api.IDecompiler;
import com.github.toolarium.java.decompiler.api.IDecompilerConfiguration;
import com.github.toolarium.java.decompiler.api.IResultSaver;
import com.github.toolarium.java.decompiler.api.ResultSaverFactory;
import com.github.toolarium.java.decompiler.source.ClassSourceFactory;
import com.github.toolarium.java.decompiler.source.IClassSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


/**
 * Command-line interface for the toolarium Java decompiler.
 *
 * <p>Usage:</p>
 * <pre>
 *   java -jar toolarium-java-decompiler.jar [options] &lt;input&gt;
 * </pre>
 *
 * @author patrick
 */
@Command(name = "toolarium-java-decompiler",
         mixinStandardHelpOptions = true,
         versionProvider = JavaDecompiler.VersionProvider.class,
         description = "A modern Java bytecode decompiler.")
public final class JavaDecompiler implements Callable<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(JavaDecompiler.class);

    private static final long MAX_CLASS_FILE_SIZE = 10L * 1024L * 1024L;

    @Parameters(index = "0", description = "Input: .class file, directory, or .jar/.zip archive.")
    private Path input;

    @Option(names = {"-o", "--output"},
            description = "Output path: a directory, or a .jar/.zip/.war/.ear archive file (default: console).")
    private Path outputDir;

    @Option(names = {"-c", "--config"}, description = "Path to a .properties configuration file.")
    private Path configFile;

    @Option(names = {"-t", "--parallel-threads"},
            description = "Number of threads for parallel decompilation (0 or 1 = sequential).")
    private Integer parallelThreads;


    /**
     * Private default constructor for picocli.
     */
    private JavaDecompiler() {
        // picocli instantiates via reflection
    }


    /**
     * Main entry point.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        new CommandLine(new JavaDecompiler()).execute(args);
    }


    /**
     * Execute the decompilation.
     *
     * @return the exit code (0 = success, 1 = error)
     */
    @Override
    public Integer call() {
        try {
            run();
            return 0;
        } catch (IOException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Decompilation failed.", ex);
            }
            LOG.info("ERROR: " + ex.getMessage());
            return 1;
        }
    }


    /**
     * Run the decompiler with the configured input and output.
     *
     * @throws IOException if decompilation fails
     */
    private void run() throws IOException {
        if (!Files.exists(input)) {
            throw new IOException("Input not found: " + input);
        }

        IDecompiler decompiler = createDecompiler();
        String inputPath = input.toString();

        if (Files.isRegularFile(input) && inputPath.endsWith(".class")) {
            decompileSingleFile(decompiler);
        } else if (Files.isRegularFile(input)
                && (inputPath.endsWith(".jar") || inputPath.endsWith(".zip"))) {
            decompileArchive(decompiler);
        } else if (Files.isDirectory(input)) {
            decompileDirectory(decompiler);
        } else {
            throw new IOException("Unsupported input type: " + inputPath);
        }
    }


    /**
     * Decompile a single .class file.
     *
     * @param decompiler the decompiler
     * @throws IOException if decompilation fails
     */
    private void decompileSingleFile(IDecompiler decompiler) throws IOException {
        long fileSize = Files.size(input);
        if (fileSize > MAX_CLASS_FILE_SIZE) {
            throw new IOException("Class file exceeds maximum allowed size ("
                    + MAX_CLASS_FILE_SIZE + " bytes): " + input);
        }

        byte[] classBytes = Files.readAllBytes(input);
        String source = decompiler.decompile(classBytes);

        if (outputDir != null) {
            try (IResultSaver saver = ResultSaverFactory.getInstance().getResultSaver(outputDir)) {
                String className = input.getFileName().toString().replace(".class", "");
                saver.saveSource(className, source);
                LOG.info("Decompiled 1 class to " + outputDir);
            } catch (Exception ex) {
                throw new IOException("Failed to save output.", ex);
            }
        } else {
            LOG.info(source);
        }
    }


    /**
     * Decompile all classes in a JAR/ZIP archive.
     *
     * @param decompiler the decompiler
     * @throws IOException if decompilation fails
     */
    private void decompileArchive(IDecompiler decompiler) throws IOException {
        try (IClassSource source = ClassSourceFactory.getInstance().getJarClassSource(input)) {
            decompileSource(decompiler, source);
        } catch (Exception ex) {
            throw new IOException("Failed to process archive: " + input, ex);
        }
    }


    /**
     * Decompile all classes in a directory.
     *
     * @param decompiler the decompiler
     * @throws IOException if decompilation fails
     */
    private void decompileDirectory(IDecompiler decompiler) throws IOException {
        try (IClassSource source = ClassSourceFactory.getInstance().getDirectoryClassSource(input)) {
            decompileSource(decompiler, source);
        } catch (Exception ex) {
            throw new IOException("Failed to process directory: " + input, ex);
        }
    }


    /**
     * Decompile from a class source, writing to output dir or console.
     *
     * @param decompiler the decompiler
     * @param source     the class source
     * @throws IOException if decompilation fails
     */
    private void decompileSource(IDecompiler decompiler, IClassSource source) throws IOException {
        if (outputDir != null) {
            try (IResultSaver saver = ResultSaverFactory.getInstance().getResultSaver(outputDir)) {
                decompiler.decompileAll(source, saver);
                LOG.info("Decompiled " + saver.getSavedCount() + " classes to " + outputDir);
            } catch (Exception ex) {
                throw new IOException("Failed to save output.", ex);
            }
        } else {
            for (String className : source.listClasses()) {
                try {
                    String result = decompiler.decompile(source, className);
                    LOG.info("// " + className + ".java");
                    LOG.info(result);
                } catch (IOException ex) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Failed to decompile: " + className, ex);
                    }
                    LOG.info("// Failed to decompile: " + className);
                }
            }
        }
    }


    /**
     * Create a decompiler instance, optionally loading configuration from a properties file.
     *
     * @return the decompiler
     * @throws IOException if the configuration file cannot be read
     */
    private IDecompiler createDecompiler() throws IOException {
        DecompilerConfigurationBuilder builder = DecompilerConfigurationBuilder.builder();

        if (configFile != null) {
            if (!Files.exists(configFile)) {
                throw new IOException("Configuration file not found: " + configFile);
            }
            builder.loadFromFile(configFile);
        }

        if (parallelThreads != null) {
            builder.parallelThreads(parallelThreads);
        }

        IDecompilerConfiguration config = builder.build();
        return DecompilerFactory.getInstance().getDecompiler(config);
    }


    /**
     * Provides version information from the generated Version class.
     */
    static final class VersionProvider implements CommandLine.IVersionProvider {

        /**
         * Constructor for VersionProvider.
         */
        VersionProvider() {
            // default constructor
        }


        /**
         * Return the version strings for the CLI version flag.
         *
         * @return an array containing the version string
         */
        @Override
        public String[] getVersion() {
            return new String[]{"toolarium-java-decompiler " + Version.VERSION};
        }
    }
}
