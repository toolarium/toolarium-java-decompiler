# toolarium-java-decompiler

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [ 0.9.1 ] - 2026-11-15

## [ 0.9.0 ] - 2026-11-15
### Added
- Archive output: decompile directly into JAR, ZIP, WAR, or EAR archives via `-o output.jar`.
- Parallel batch decompilation using available processors by default.
- CLI `--parallel-threads` / `-t` option for controlling thread count.
- Fluent configuration builder with 20+ options for output formatting, feature toggles, class filtering, and error handling.
- Properties file support for loading decompiler settings.
- CLI `--config` / `-c` option for configuration via properties file.
- Per-feature toggles for all reconstructors (lambda, enum, record, assert, inner class, string switch, try-with-resources, text blocks).
- Optional banner and source file header comments.
- Class include/exclude regex filtering for selective batch decompilation.

## [ 0.8.0 ] - 2026-11-14
### Added
- Sealed class support: `sealed`, `permits`, and `non-sealed` modifiers.
- Text block recovery: multi-line string literals emitted as text blocks (`"""..."""`).
- Anonymous and local class reconstruction.

## [ 0.7.0 ] - 2026-11-13
### Added
- Lambda expression and method reference reconstruction.
- String switch reconstruction.
- Enum reconstruction with custom constructor arguments.
- Assert statement reconstruction.
- Try-with-resources reconstruction.
- For-each loop reconstruction.
- Switch expression reconstruction with arrow syntax.
- Pattern matching instanceof.
- Annotation support for classes, methods, fields, and parameters.
- Inner class nesting in batch decompilation.
- Generic type recovery for parameterized types.
- Expression improvements: compound assignments, increment/decrement, boolean return simplification, else-if chains, precedence-aware parenthesization.
- Type simplification for static field access expressions.

## [ 0.6.0 ] - 2026-11-03
### Added
- Fluent `DecompilerBuilder` for one-line decompilation of files, directories, and JARs.
- Modern Java pattern support: records, enums, inner classes, assert, try-with-resources.
- Control flow reconstruction: do-while, for, for-each, switch, try-catch/finally.
- Expression simplification and type simplification.

## [ 0.5.0 ] - 2026-10-11
### Added
- Java bytecode decompiler library with public API (`IDecompiler`, `IClassSource`, `IResultSaver`).
- Decompilation of `.class` files, directories, and JAR/ZIP archives.
- Command-line interface with output directory, help, and version options.
- Control flow reconstruction: if/else, while.
- In-memory and file-based result savers.
