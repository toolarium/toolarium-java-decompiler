[![License](https://img.shields.io/github/license/toolarium/toolarium-java-decompiler)](https://github.com/toolarium/toolarium-java-decompiler/blob/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.toolarium/toolarium-java-decompiler/0.9.0)](https://search.maven.org/artifact/com.github.toolarium/toolarium-java-decompiler/0.9.0/jar)
[![javadoc](https://javadoc.io/badge2/com.github.toolarium/toolarium-java-decompiler/javadoc.svg)](https://javadoc.io/doc/com.github.toolarium/toolarium-java-decompiler)

# toolarium-java-decompiler

A modern, modular Java bytecode decompiler library and CLI tool. Designed API-first for embedding in tools and IDEs, inspired by Vineflower's analytical approach but with a clean, well-documented architecture. Decompiles `.class` files, directories, and JAR/ZIP archives back to readable Java source code.


## Features

- **Public API for embedding**: clean `IDecompiler`, `IClassSource`, and `IResultSaver` interfaces for programmatic use
- **Multiple input sources**: decompile single `.class` files, directories, and JAR/ZIP archives
- **ASM-based bytecode reading**: supports Java 8 through 21+ class files (ASM 9.7.1)
- **Control flow reconstruction**: if/else, while, do-while, for, for-each, switch, try-catch/finally
- **Modern Java patterns**: records, enums, inner classes, assert, try-with-resources, synthetic member removal
- **Annotation reconstruction**: class, method, field, and parameter annotations emitted in source output
- **Inner class nesting**: named inner classes nested inside their enclosing class in batch decompilation
- **Anonymous and local class reconstruction**: anonymous inner classes and method-local class definitions recovered and nested
- **Sealed class support**: `sealed`, `permits`, and `non-sealed` modifiers reconstructed from PermittedSubclasses attribute
- **Text block recovery**: multi-line string literals emitted as text blocks (`"""..."""`)
- **Expression simplification**: constant folding and dead code elimination
- **Data flow analysis**: merge functions at control flow join points for accurate variable recovery
- **Type simplification**: `java.lang.*` and same-package types shown as simple names
- **Rich AST**: visitor-based AST with 12 expression types, 16 statement types, and 4 declaration types
- **Archive output**: decompile directly into JAR, ZIP, WAR, or EAR archives
- **Flexible output**: in-memory, file-based, and archive result savers for embedding and batch use
- **Fastest decompiler**: 3.4x faster than Vineflower and CFR on commons-lang3 (396 classes), 7.7x faster than Procyon; see [docs/performance.md](docs/performance.md)
- **Parallel batch decompilation**: multi-threaded class decompilation using available processors by default
- **Comprehensive configuration**: builder-style API with 20+ options for output formatting, feature toggles, class filtering, and error handling
- **Properties file support**: load configuration from `.properties` files for CLI and programmatic use
- **Command-line interface**: picocli-based CLI with output directory, configuration file, parallel threads, help, and version options


## Comparison with other decompilers

| Feature | toolarium | Vineflower | Fernflower | CFR | Procyon |
|---|:---:|:---:|:---:|:---:|:---:|
| **Embeddable library API** | **Yes** | Limited | Limited | Limited | Limited |
| **Fluent builder API** | **Yes** | No | No | No | No |
| **Documented architecture** | **Yes** | <u>Partial</u> | No | No | <u>Partial</u> |
| **Builder-style configuration** | **Yes** | Properties | Properties | Flags | Flags |
| **Parallel batch decompilation** | **Yes** | No | No | No | No |
| If/else, while, for, switch | **Yes** | **Yes** | **Yes** | **Yes** | **Yes** |
| Try-catch/finally | **Yes** | **Yes** | **Yes** | **Yes** | **Yes** |
| Try-with-resources | **Yes** | **Yes** | <u>Partial</u> | **Yes** | **Yes** |
| Records, enums | **Yes** | **Yes** | **Yes** | **Yes** | **Yes** |
| Lambda expressions | **Yes** | **Yes** | **Yes** | **Yes** | **Yes** |
| Method references | **Yes** | **Yes** | **Yes** | **Yes** | **Yes** |
| Inner class nesting | **Yes** | **Yes** | **Yes** | **Yes** | **Yes** |
| Anonymous/local classes | **Yes** | **Yes** | **Yes** | **Yes** | **Yes** |
| Annotations | **Yes** | **Yes** | <u>Partial</u> | **Yes** | **Yes** |
| Generic type recovery | **Yes** | **Yes** | **Yes** | **Yes** | **Yes** |
| Switch expressions | **Yes** | **Yes** | <u>Partial</u> | **Yes** | <u>Partial</u> |
| Pattern matching instanceof | **Yes** | **Yes** | <u>Partial</u> | **Yes** | <u>Partial</u> |
| Sealed classes, permits | **Yes** | **Yes** | <u>Partial</u> | **Yes** | <u>Partial</u> |
| Text blocks | **Yes** | **Yes** | <u>Partial</u> | **Yes** | <u>Partial</u> |
| Assert reconstruction | **Yes** | **Yes** | <u>Partial</u> | **Yes** | **Yes** |

**Key differentiator**: toolarium-java-decompiler is designed API-first for embedding in tools, IDEs, and build pipelines. While Vineflower, Fernflower, CFR, and Procyon are primarily standalone decompilers with limited integration APIs, toolarium provides clean `IDecompiler`, `IClassSource`, and `IResultSaver` interfaces, a fluent `DecompilerBuilder`, and a fully documented modular pipeline architecture.

See [docs/output-comparison.md](docs/output-comparison.md) for detailed side-by-side output examples.


## Adding the library

**Gradle**
```gradle
implementation 'com.github.toolarium:toolarium-java-decompiler:0.9.0'
```

**Maven**
```xml
<dependency>
    <groupId>com.github.toolarium</groupId>
    <artifactId>toolarium-java-decompiler</artifactId>
    <version>0.9.0</version>
</dependency>
```

The library brings ASM 9.7.1 and `slf4j-api` as transitive dependencies. You must provide an SLF4J binding (e.g., Logback) at runtime.


## Quick start

### CLI usage

```bash
# Decompile a single class file (output to console)
java -jar toolarium-java-decompiler-0.9.0.jar MyClass.class

# Decompile to output directory
java -jar toolarium-java-decompiler-0.9.0.jar -o output/ MyClass.class

# Decompile an entire JAR
java -jar toolarium-java-decompiler-0.9.0.jar -o output/ mylib.jar

# Decompile a directory of class files
java -jar toolarium-java-decompiler-0.9.0.jar -o output/ build/classes/

# Decompile to a JAR archive
java -jar toolarium-java-decompiler-0.9.0.jar -o output.jar mylib.jar

# Decompile to a ZIP archive
java -jar toolarium-java-decompiler-0.9.0.jar -o output.zip build/classes/

# Decompile with a configuration file
java -jar toolarium-java-decompiler-0.9.0.jar -c decompiler.properties -o output/ mylib.jar

# Decompile with explicit thread count
java -jar toolarium-java-decompiler-0.9.0.jar -t 4 -o output/ mylib.jar

# Decompile sequentially (single-threaded)
java -jar toolarium-java-decompiler-0.9.0.jar -t 1 -o output/ mylib.jar

# Show help
java -jar toolarium-java-decompiler-0.9.0.jar --help

# Show version
java -jar toolarium-java-decompiler-0.9.0.jar --version
```

### Library API (builder)

```java
// Decompile a JAR to an output directory
DecompilerBuilder.builder()
    .input(Path.of("mylib.jar"))
    .output(Path.of("output/"))
    .decompile();

// Decompile to memory
Map<String, String> results = DecompilerBuilder.builder()
    .input(Path.of("mylib.jar"))
    .decompileToMemory();

// Decompile a single class to string
String source = DecompilerBuilder.builder()
    .input(Path.of("MyClass.class"))
    .decompileToString();

// With custom configuration (parallel by default)
IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
    .indentString("\t")
    .showBanner(true)
    .parallelThreads(8)
    .build();
DecompilerBuilder.builder()
    .input(Path.of("mylib.jar"))
    .output(Path.of("output/"))
    .configuration(config)
    .decompile();

// Custom result saver
DecompilerBuilder.builder()
    .input(Path.of("mylib.jar"))
    .resultSaver(customSaver)
    .decompile();
```

### Library API (direct)

```java
// Get a decompiler instance
IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler();

// Decompile from raw bytes
String javaSource = decompiler.decompile(classBytes);

// Decompile a single class from a JAR
IClassSource source = ClassSourceFactory.getInstance().getJarClassSource(jarPath);
String javaSource = decompiler.decompile(source, "com/example/MyClass");

// Decompile all classes to memory
IResultSaver saver = ResultSaverFactory.getInstance().getInMemoryResultSaver();
decompiler.decompileAll(source, saver);
Map<String, String> results = saver.getResults();
```


## API

| Class / Interface | Package | Description |
|---|---|---|
| `DecompilerBuilder` | `api` | Fluent builder for configuring input, output, and running the decompiler |
| `IDecompiler` | `api` | Main entry point: decompile from bytes, from a class source, or batch decompile all classes |
| `IDecompilerConfiguration` | `api` | Configuration interface with 20+ options for output, features, filtering, and error handling |
| `DecompilerConfigurationBuilder` | `api` | Fluent builder for creating configuration instances, with properties file support |
| `IClassSource` | `source` | Input abstraction for directories, JAR/ZIP archives, and single class files |
| `IResultSaver` | `api` | Output abstraction: file-based or in-memory collection of decompiled sources |
| `DecompilerFactory` | `api` | Singleton factory for obtaining an `IDecompiler` instance (with optional configuration) |
| `ClassSourceFactory` | `source` | Singleton factory for creating `IClassSource` instances from paths |
| `ResultSaverFactory` | `api` | Singleton factory for creating `IResultSaver` instances |

### IDecompiler

```java
String decompile(byte[] classBytes) throws IOException;
String decompile(IClassSource source, String className) throws IOException;
void decompileAll(IClassSource source, IResultSaver saver) throws IOException;
```

### IClassSource

```java
List<String> listClasses() throws IOException;
byte[] readClass(String className) throws IOException;
boolean hasClass(String className) throws IOException;
```

### IResultSaver

```java
void saveSource(String className, String sourceCode) throws IOException;
int getSavedCount();
Map<String, String> getResults();
```


## Architecture

The decompiler follows a modular pipeline architecture:

```
.class -> Reader -> Translator -> CFG Builder -> Data Flow -> Structurer
       -> Reconstructor -> Simplifier -> Source Emitter -> .java
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for the full design, package structure, and Vineflower comparison.
See [docs/configuration.md](docs/configuration.md) for the full configuration option reference with examples.
See [docs/output-comparison.md](docs/output-comparison.md) for a side-by-side output comparison with Vineflower, Fernflower, CFR, and Procyon.
See [docs/performance.md](docs/performance.md) for performance benchmarks against Vineflower, CFR, and Procyon.


## Built With

* [cb](https://github.com/toolarium/common-build) - The toolarium common build
* [ASM](https://asm.ow2.io/) - Java bytecode manipulation framework (9.7.1)
* [picocli](https://picocli.info/) - Command-line interface framework (4.7.6)


## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/toolarium/toolarium-java-decompiler/tags).


## License

This project is licensed under the MPL-2.0 License, see the [LICENSE](LICENSE) file for details.

| Action | Allowed | Required | Not Allowed |
|---|---|---|---|
| Commercial use | Yes | | |
| Private use | Yes | | |
| Modify source | Yes | | |
| Distribute | Yes | | |
| Mix with proprietary code | Yes | | |
| Share modified MPL files | | Yes | |
| Keep license notices | | Yes | |
| Remove copyright notices | | | Yes |
| Hold authors liable | | | Yes |
| Use authors' trademarks | | | Yes |
