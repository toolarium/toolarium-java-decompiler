/*
 * DecompilerConfigurationImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.impl;

import com.github.toolarium.java.decompiler.api.IDecompilerConfiguration;


/**
 * Immutable implementation of {@link IDecompilerConfiguration}.
 * Instances are created via {@link com.github.toolarium.java.decompiler.api.DecompilerConfigurationBuilder}.
 *
 * @author patrick
 */
public final class DecompilerConfigurationImpl implements IDecompilerConfiguration {
    private final String indentString;
    private final String lineSeparator;
    private final boolean showBanner;
    private final boolean showSourceFile;
    private final boolean removeBridgeMethods;
    private final boolean removeSyntheticMembers;
    private final boolean reconstructAssert;
    private final boolean reconstructEnum;
    private final boolean reconstructInnerClasses;
    private final boolean reconstructLambda;
    private final boolean reconstructTryWithResources;
    private final boolean reconstructStringSwitch;
    private final boolean reconstructRecord;
    private final boolean reconstructTextBlocks;
    private final boolean hideDefaultConstructor;
    private final boolean hideEmptySuper;
    private final boolean showOverrideAnnotation;
    private final String includeClassRegex;
    private final String excludeClassRegex;
    private final boolean ignoreInvalidBytecode;
    private final int maxMethodTimeSeconds;


    /**
     * Constructor for DecompilerConfigurationImpl.
     *
     * @param outputFormatting the output formatting options
     * @param featureToggles   the decompilation feature toggles
     * @param filterAndError   the filtering and error handling options
     */
    public DecompilerConfigurationImpl(OutputFormattingOptions outputFormatting,
                                       FeatureToggleOptions featureToggles,
                                       FilterAndErrorOptions filterAndError) {
        this.indentString = outputFormatting.getIndentString();
        this.lineSeparator = outputFormatting.getLineSeparator();
        this.showBanner = outputFormatting.isShowBanner();
        this.showSourceFile = outputFormatting.isShowSourceFile();
        this.removeBridgeMethods = featureToggles.isRemoveBridgeMethods();
        this.removeSyntheticMembers = featureToggles.isRemoveSyntheticMembers();
        this.reconstructAssert = featureToggles.isReconstructAssert();
        this.reconstructEnum = featureToggles.isReconstructEnum();
        this.reconstructInnerClasses = featureToggles.isReconstructInnerClasses();
        this.reconstructLambda = featureToggles.isReconstructLambda();
        this.reconstructTryWithResources = featureToggles.isReconstructTryWithResources();
        this.reconstructStringSwitch = featureToggles.isReconstructStringSwitch();
        this.reconstructRecord = featureToggles.isReconstructRecord();
        this.reconstructTextBlocks = featureToggles.isReconstructTextBlocks();
        this.hideDefaultConstructor = featureToggles.isHideDefaultConstructor();
        this.hideEmptySuper = featureToggles.isHideEmptySuper();
        this.showOverrideAnnotation = featureToggles.isShowOverrideAnnotation();
        this.includeClassRegex = filterAndError.getIncludeClassRegex();
        this.excludeClassRegex = filterAndError.getExcludeClassRegex();
        this.ignoreInvalidBytecode = filterAndError.isIgnoreInvalidBytecode();
        this.maxMethodTimeSeconds = filterAndError.getMaxMethodTimeSeconds();
    }


    /**
     * Get the indentation string used for each level of nesting.
     *
     * @return the indent string
     */
    @Override
    public String getIndentString() {
        return indentString;
    }


    /**
     * Get the line separator used in emitted source code.
     *
     * @return the line separator
     */
    @Override
    public String getLineSeparator() {
        return lineSeparator;
    }


    /**
     * Whether to emit a banner comment at the top of each decompiled file.
     *
     * @return true if the banner should be shown
     */
    @Override
    public boolean isShowBanner() {
        return showBanner;
    }


    /**
     * Whether to emit a source file comment showing the original file name.
     *
     * @return true if the source file comment should be shown
     */
    @Override
    public boolean isShowSourceFile() {
        return showSourceFile;
    }


    /**
     * Whether to remove bridge methods from the decompiled output.
     *
     * @return true if bridge methods should be removed
     */
    @Override
    public boolean isRemoveBridgeMethods() {
        return removeBridgeMethods;
    }


    /**
     * Whether to remove synthetic members from the decompiled output.
     *
     * @return true if synthetic members should be removed
     */
    @Override
    public boolean isRemoveSyntheticMembers() {
        return removeSyntheticMembers;
    }


    /**
     * Whether to reconstruct assert statements.
     *
     * @return true if assert reconstruction is enabled
     */
    @Override
    public boolean isReconstructAssert() {
        return reconstructAssert;
    }


    /**
     * Whether to reconstruct enum declarations.
     *
     * @return true if enum reconstruction is enabled
     */
    @Override
    public boolean isReconstructEnum() {
        return reconstructEnum;
    }


    /**
     * Whether to reconstruct inner classes.
     *
     * @return true if inner class reconstruction is enabled
     */
    @Override
    public boolean isReconstructInnerClasses() {
        return reconstructInnerClasses;
    }


    /**
     * Whether to reconstruct lambda expressions.
     *
     * @return true if lambda reconstruction is enabled
     */
    @Override
    public boolean isReconstructLambda() {
        return reconstructLambda;
    }


    /**
     * Whether to reconstruct try-with-resources statements.
     *
     * @return true if try-with-resources reconstruction is enabled
     */
    @Override
    public boolean isReconstructTryWithResources() {
        return reconstructTryWithResources;
    }


    /**
     * Whether to reconstruct string switch statements.
     *
     * @return true if string switch reconstruction is enabled
     */
    @Override
    public boolean isReconstructStringSwitch() {
        return reconstructStringSwitch;
    }


    /**
     * Whether to reconstruct record declarations.
     *
     * @return true if record reconstruction is enabled
     */
    @Override
    public boolean isReconstructRecord() {
        return reconstructRecord;
    }


    /**
     * Whether to reconstruct text blocks.
     *
     * @return true if text block reconstruction is enabled
     */
    @Override
    public boolean isReconstructTextBlocks() {
        return reconstructTextBlocks;
    }


    /**
     * Whether to hide the default constructor when it has an empty body.
     *
     * @return true if empty default constructors should be hidden
     */
    @Override
    public boolean isHideDefaultConstructor() {
        return hideDefaultConstructor;
    }


    /**
     * Whether to hide empty super() calls in constructors.
     *
     * @return true if empty super() calls should be hidden
     */
    @Override
    public boolean isHideEmptySuper() {
        return hideEmptySuper;
    }


    /**
     * Whether to add @Override annotations on overriding methods.
     *
     * @return true if @Override annotations should be added
     */
    @Override
    public boolean isShowOverrideAnnotation() {
        return showOverrideAnnotation;
    }


    /**
     * Get the include class regex filter.
     *
     * @return the include regex, or null for no filter
     */
    @Override
    public String getIncludeClassRegex() {
        return includeClassRegex;
    }


    /**
     * Get the exclude class regex filter.
     *
     * @return the exclude regex, or null for no filter
     */
    @Override
    public String getExcludeClassRegex() {
        return excludeClassRegex;
    }


    /**
     * Whether to continue decompilation when invalid bytecode is encountered.
     *
     * @return true if invalid bytecode should be ignored
     */
    @Override
    public boolean isIgnoreInvalidBytecode() {
        return ignoreInvalidBytecode;
    }


    /**
     * Get the maximum time in seconds allowed for decompiling a single method.
     *
     * @return the maximum method time in seconds (0 = no limit)
     */
    @Override
    public int getMaxMethodTimeSeconds() {
        return maxMethodTimeSeconds;
    }


    /**
     * Return a string representation of this configuration.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "DecompilerConfigurationImpl{"
                + "indentString='" + indentString + "'"
                + ", lineSeparator='" + escapeLineSeparator(lineSeparator) + "'"
                + ", showBanner=" + showBanner
                + ", showSourceFile=" + showSourceFile
                + ", removeBridgeMethods=" + removeBridgeMethods
                + ", removeSyntheticMembers=" + removeSyntheticMembers
                + ", reconstructAssert=" + reconstructAssert
                + ", reconstructEnum=" + reconstructEnum
                + ", reconstructInnerClasses=" + reconstructInnerClasses
                + ", reconstructLambda=" + reconstructLambda
                + ", reconstructTryWithResources=" + reconstructTryWithResources
                + ", reconstructStringSwitch=" + reconstructStringSwitch
                + ", reconstructRecord=" + reconstructRecord
                + ", reconstructTextBlocks=" + reconstructTextBlocks
                + ", hideDefaultConstructor=" + hideDefaultConstructor
                + ", hideEmptySuper=" + hideEmptySuper
                + ", showOverrideAnnotation=" + showOverrideAnnotation
                + ", includeClassRegex=" + includeClassRegex
                + ", excludeClassRegex=" + excludeClassRegex
                + ", ignoreInvalidBytecode=" + ignoreInvalidBytecode
                + ", maxMethodTimeSeconds=" + maxMethodTimeSeconds
                + "}";
    }


    /**
     * Escape a line separator string for display.
     *
     * @param separator the line separator
     * @return the escaped string
     */
    private String escapeLineSeparator(String separator) {
        return separator.replace("\r", "\\r").replace("\n", "\\n");
    }


    /**
     * Holds output formatting options for the constructor parameter grouping.
     *
     * @author patrick
     */
    public static final class OutputFormattingOptions {
        private final String indentString;
        private final String lineSeparator;
        private final boolean showBanner;
        private final boolean showSourceFile;


        /**
         * Constructor for OutputFormattingOptions.
         *
         * @param indentString   the indent string
         * @param lineSeparator  the line separator
         * @param showBanner     whether to show the banner
         * @param showSourceFile whether to show the source file comment
         */
        public OutputFormattingOptions(String indentString, String lineSeparator,
                                       boolean showBanner, boolean showSourceFile) {
            this.indentString = indentString;
            this.lineSeparator = lineSeparator;
            this.showBanner = showBanner;
            this.showSourceFile = showSourceFile;
        }


        /**
         * Get the indent string.
         *
         * @return the indent string
         */
        public String getIndentString() {
            return indentString;
        }


        /**
         * Get the line separator.
         *
         * @return the line separator
         */
        public String getLineSeparator() {
            return lineSeparator;
        }


        /**
         * Whether to show the banner.
         *
         * @return true if the banner should be shown
         */
        public boolean isShowBanner() {
            return showBanner;
        }


        /**
         * Whether to show the source file comment.
         *
         * @return true if the source file comment should be shown
         */
        public boolean isShowSourceFile() {
            return showSourceFile;
        }
    }


    /**
     * Holds decompilation feature toggle options for the constructor parameter grouping.
     *
     * @author patrick
     */
    public static final class FeatureToggleOptions {
        private final boolean removeBridgeMethods;
        private final boolean removeSyntheticMembers;
        private final boolean reconstructAssert;
        private final boolean reconstructEnum;
        private final boolean reconstructInnerClasses;
        private final boolean reconstructLambda;
        private final boolean reconstructTryWithResources;
        private final boolean reconstructStringSwitch;
        private final boolean reconstructRecord;
        private final boolean reconstructTextBlocks;
        private final boolean hideDefaultConstructor;
        private final boolean hideEmptySuper;
        private final boolean showOverrideAnnotation;


        /**
         * Constructor for FeatureToggleOptions.
         *
         * @param removeBridge    whether to remove bridge methods
         * @param removeSynthetic whether to remove synthetic members
         * @param assertToggle    whether to reconstruct assert statements
         * @param enumToggle      whether to reconstruct enum declarations
         * @param innerClasses    whether to reconstruct inner classes
         * @param lambda          whether to reconstruct lambda expressions
         * @param tryResources    whether to reconstruct try-with-resources
         * @param stringSwitch    whether to reconstruct string switch
         * @param record          whether to reconstruct records
         * @param textBlocks      whether to reconstruct text blocks
         */
        public FeatureToggleOptions(boolean removeBridge, boolean removeSynthetic,
                                     boolean assertToggle, boolean enumToggle,
                                     boolean innerClasses, boolean lambda,
                                     boolean tryResources, boolean stringSwitch,
                                     boolean record, boolean textBlocks) {
            this.removeBridgeMethods = removeBridge;
            this.removeSyntheticMembers = removeSynthetic;
            this.reconstructAssert = assertToggle;
            this.reconstructEnum = enumToggle;
            this.reconstructInnerClasses = innerClasses;
            this.reconstructLambda = lambda;
            this.reconstructTryWithResources = tryResources;
            this.reconstructStringSwitch = stringSwitch;
            this.reconstructRecord = record;
            this.reconstructTextBlocks = textBlocks;
            this.hideDefaultConstructor = true;
            this.hideEmptySuper = true;
            this.showOverrideAnnotation = false;
        }


        /**
         * Constructor for FeatureToggleOptions with all fields.
         *
         * @param removeBridge    whether to remove bridge methods
         * @param removeSynthetic whether to remove synthetic members
         * @param assertToggle    whether to reconstruct assert statements
         * @param enumToggle      whether to reconstruct enum declarations
         * @param innerClasses    whether to reconstruct inner classes
         * @param lambda          whether to reconstruct lambda expressions
         * @param tryResources    whether to reconstruct try-with-resources
         * @param stringSwitch    whether to reconstruct string switch
         * @param record          whether to reconstruct records
         * @param extraToggles    the extra feature toggles (text blocks, hide default ctor, hide empty super, show override)
         */
        public FeatureToggleOptions(boolean removeBridge, boolean removeSynthetic,
                                     boolean assertToggle, boolean enumToggle,
                                     boolean innerClasses, boolean lambda,
                                     boolean tryResources, boolean stringSwitch,
                                     boolean record, ExtraFeatureToggles extraToggles) {
            this.removeBridgeMethods = removeBridge;
            this.removeSyntheticMembers = removeSynthetic;
            this.reconstructAssert = assertToggle;
            this.reconstructEnum = enumToggle;
            this.reconstructInnerClasses = innerClasses;
            this.reconstructLambda = lambda;
            this.reconstructTryWithResources = tryResources;
            this.reconstructStringSwitch = stringSwitch;
            this.reconstructRecord = record;
            this.reconstructTextBlocks = extraToggles.isReconstructTextBlocks();
            this.hideDefaultConstructor = extraToggles.isHideDefaultConstructor();
            this.hideEmptySuper = extraToggles.isHideEmptySuper();
            this.showOverrideAnnotation = extraToggles.isShowOverrideAnnotation();
        }


        /**
         * Whether to remove bridge methods.
         *
         * @return true if bridge methods should be removed
         */
        public boolean isRemoveBridgeMethods() {
            return removeBridgeMethods;
        }


        /**
         * Whether to remove synthetic members.
         *
         * @return true if synthetic members should be removed
         */
        public boolean isRemoveSyntheticMembers() {
            return removeSyntheticMembers;
        }


        /**
         * Whether to reconstruct assert statements.
         *
         * @return true if assert reconstruction is enabled
         */
        public boolean isReconstructAssert() {
            return reconstructAssert;
        }


        /**
         * Whether to reconstruct enum declarations.
         *
         * @return true if enum reconstruction is enabled
         */
        public boolean isReconstructEnum() {
            return reconstructEnum;
        }


        /**
         * Whether to reconstruct inner classes.
         *
         * @return true if inner class reconstruction is enabled
         */
        public boolean isReconstructInnerClasses() {
            return reconstructInnerClasses;
        }


        /**
         * Whether to reconstruct lambda expressions.
         *
         * @return true if lambda reconstruction is enabled
         */
        public boolean isReconstructLambda() {
            return reconstructLambda;
        }


        /**
         * Whether to reconstruct try-with-resources statements.
         *
         * @return true if try-with-resources reconstruction is enabled
         */
        public boolean isReconstructTryWithResources() {
            return reconstructTryWithResources;
        }


        /**
         * Whether to reconstruct string switch statements.
         *
         * @return true if string switch reconstruction is enabled
         */
        public boolean isReconstructStringSwitch() {
            return reconstructStringSwitch;
        }


        /**
         * Whether to reconstruct record declarations.
         *
         * @return true if record reconstruction is enabled
         */
        public boolean isReconstructRecord() {
            return reconstructRecord;
        }


        /**
         * Whether to reconstruct text blocks.
         *
         * @return true if text block reconstruction is enabled
         */
        public boolean isReconstructTextBlocks() {
            return reconstructTextBlocks;
        }


        /**
         * Whether to hide the default constructor.
         *
         * @return true if empty default constructors should be hidden
         */
        public boolean isHideDefaultConstructor() {
            return hideDefaultConstructor;
        }


        /**
         * Whether to hide empty super() calls.
         *
         * @return true if empty super() calls should be hidden
         */
        public boolean isHideEmptySuper() {
            return hideEmptySuper;
        }


        /**
         * Whether to show @Override annotations.
         *
         * @return true if @Override annotations should be added
         */
        public boolean isShowOverrideAnnotation() {
            return showOverrideAnnotation;
        }
    }


    /**
     * Holds extra feature toggles that extend beyond the 10-parameter limit
     * of FeatureToggleOptions.
     *
     * @author patrick
     */
    public static final class ExtraFeatureToggles {
        private final boolean reconstructTextBlocks;
        private final boolean hideDefaultConstructor;
        private final boolean hideEmptySuper;
        private final boolean showOverrideAnnotation;


        /**
         * Constructor for ExtraFeatureToggles.
         *
         * @param reconstructTextBlocks whether to reconstruct text blocks
         * @param hideDefaultConstructor whether to hide default constructors
         * @param hideEmptySuper whether to hide empty super() calls
         * @param showOverrideAnnotation whether to show @Override annotations
         */
        public ExtraFeatureToggles(boolean reconstructTextBlocks, boolean hideDefaultConstructor,
                                    boolean hideEmptySuper, boolean showOverrideAnnotation) {
            this.reconstructTextBlocks = reconstructTextBlocks;
            this.hideDefaultConstructor = hideDefaultConstructor;
            this.hideEmptySuper = hideEmptySuper;
            this.showOverrideAnnotation = showOverrideAnnotation;
        }


        /**
         * Whether to reconstruct text blocks.
         *
         * @return true if text block reconstruction is enabled
         */
        public boolean isReconstructTextBlocks() {
            return reconstructTextBlocks;
        }


        /**
         * Whether to hide the default constructor.
         *
         * @return true if empty default constructors should be hidden
         */
        public boolean isHideDefaultConstructor() {
            return hideDefaultConstructor;
        }


        /**
         * Whether to hide empty super() calls.
         *
         * @return true if empty super() calls should be hidden
         */
        public boolean isHideEmptySuper() {
            return hideEmptySuper;
        }


        /**
         * Whether to show @Override annotations.
         *
         * @return true if @Override annotations should be added
         */
        public boolean isShowOverrideAnnotation() {
            return showOverrideAnnotation;
        }
    }


    /**
     * Holds filtering and error handling options for the constructor parameter grouping.
     *
     * @author patrick
     */
    public static final class FilterAndErrorOptions {
        private final String includeClassRegex;
        private final String excludeClassRegex;
        private final boolean ignoreInvalidBytecode;
        private final int maxMethodTimeSeconds;


        /**
         * Constructor for FilterAndErrorOptions.
         *
         * @param includeClassRegex    the include class regex
         * @param excludeClassRegex    the exclude class regex
         * @param ignoreInvalidBytecode whether to ignore invalid bytecode
         * @param maxMethodTimeSeconds  the max method time in seconds
         */
        public FilterAndErrorOptions(String includeClassRegex, String excludeClassRegex,
                                      boolean ignoreInvalidBytecode, int maxMethodTimeSeconds) {
            this.includeClassRegex = includeClassRegex;
            this.excludeClassRegex = excludeClassRegex;
            this.ignoreInvalidBytecode = ignoreInvalidBytecode;
            this.maxMethodTimeSeconds = maxMethodTimeSeconds;
        }


        /**
         * Get the include class regex.
         *
         * @return the include regex
         */
        public String getIncludeClassRegex() {
            return includeClassRegex;
        }


        /**
         * Get the exclude class regex.
         *
         * @return the exclude regex
         */
        public String getExcludeClassRegex() {
            return excludeClassRegex;
        }


        /**
         * Whether to ignore invalid bytecode.
         *
         * @return true if invalid bytecode should be ignored
         */
        public boolean isIgnoreInvalidBytecode() {
            return ignoreInvalidBytecode;
        }


        /**
         * Get the max method time in seconds.
         *
         * @return the max method time in seconds
         */
        public int getMaxMethodTimeSeconds() {
            return maxMethodTimeSeconds;
        }
    }
}
