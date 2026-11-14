# Architecture: toolarium-java-decompiler

## Overview

A modern Java bytecode decompiler built as an embeddable library with a clean public API.
The design follows an analytical pipeline approach with a focus on modularity, API design,
and documentation.

## Design Goals

1. **API-first**: clean public interfaces for embedding
2. **Modular pipeline**: each decompilation phase is a separate, testable module
3. **Modern Java output**: support Java 21+ constructs (records, sealed classes, pattern matching, switch expressions)
4. **Minimal dependencies**: ASM for bytecode reading, picocli for CLI, SLF4J for logging

## Design Principles

- **Analytical approach**: reconstruct source from bytecode semantics, not heuristic pattern matching
- **Pipeline architecture**: bytecode to control flow graph to data flow to structured to AST to source
- **ASM foundation**: use ObjectWeb ASM for class file parsing (proven, fast, maintained)
- **Clean public API**: `IDecompiler`, `IClassSource`, and `IResultSaver` are first-class contracts
- **Decoupled phases**: each pass lives in its own package with a defined input/output contract
- **Testable passes**: each transformation pass has a typed input and output, enabling unit tests per pass
- **Clean codebase**: Java 11+ idioms, no legacy constraints
- **Documentation**: every public interface and method is documented with Javadoc

## Decompilation Pipeline

```
  .class / .jar input
        |
        v
  +------------------------+
  | 1. Class Reader        |  ASM-based class file parsing into IStructClass model
  +------------------------+
        |
        v
  +------------------------+
  | 2. Bytecode Translator |  JVM bytecode to Java expressions (stack simulation)
  +------------------------+
        |
        v
  +------------------------+
  | 3. Control Flow Builder|  Construct control flow graph with basic blocks and edges
  +------------------------+
        |
        v
  +------------------------+
  | 4. Data Flow Transform |  Convert to data flow form with merge functions at join points
  +------------------------+
        |
        v
  +------------------------+
  | 5. Structurer          |  Recover loops, if/else, switch, try-catch/finally
  +------------------------+
        |
        v
  +------------------------+
  | 6. Reconstructor       |  Recognize records, enums, inner classes, assert, try-with-resources
  +------------------------+
        |
        v
  +------------------------+
  | 7. Simplifier          |  Constant folding, dead code elimination, expression simplification
  +------------------------+
        |
        v
  +------------------------+
  | 8. Source Emitter       |  AST to formatted Java source text
  +------------------------+
        |
        v
    .java output
```

## Package Structure

All packages are under `com.github.toolarium.java.decompiler`.

Each service package follows the same convention: the public interface and singleton factory
live in the parent package, while the implementation lives in the `impl/` subpackage. Model
packages follow the same pattern with a model factory and interfaces in the parent, and
implementations in `impl/`.

```
com.github.toolarium.java.decompiler
|
+-- JavaDecompiler                       CLI entry point (picocli)
|
+-- api/                                 Public API
|   +-- IDecompiler                      Main decompiler interface
|   +-- IDecompilerConfiguration         Configuration interface (output, features, filtering, errors)
|   +-- IResultSaver                     Output destination abstraction
|   +-- DecompilerBuilder                Fluent builder for configuring and running the decompiler
|   +-- DecompilerConfigurationBuilder   Fluent builder for IDecompilerConfiguration
|   +-- DecompilerFactory                Singleton factory for IDecompiler
|   +-- ResultSaverFactory               Singleton factory for IResultSaver
|
+-- source/                              Input source abstraction
|   +-- IClassSource                     Class file input (JAR, directory, single file)
|   +-- ClassSourceFactory               Singleton factory for IClassSource
|   +-- impl/
|       +-- DirectoryClassSource         Reads from a directory of .class files
|       +-- JarClassSource               Reads from JAR/ZIP archives
|       +-- SingleFileClassSource        Reads a single .class file
|
+-- reader/                              Phase 1: Class file reading
|   +-- IClassReader                     Reads .class bytes into IStructClass model
|   +-- ClassReaderFactory               Singleton factory for IClassReader
|   +-- GenericSignatureParser           Parses JVM Signature attributes into generic IType
|   +-- impl/
|       +-- AsmClassReaderImpl           ASM-based implementation
|
+-- translator/                          Phase 2: Bytecode translation
|   +-- IBytecodeTranslator              Translates JVM bytecode to Java expressions
|   +-- BytecodeTranslatorFactory        Singleton factory for IBytecodeTranslator
|   +-- impl/
|       +-- BytecodeTranslatorImpl       Stack simulation and expression generation
|
+-- controlflow/                         Phase 3: Control flow graph construction
|   +-- IControlFlowBuilder              Builds IControlFlowGraph from method bytecode
|   +-- ControlFlowBuilderFactory        Singleton factory for IControlFlowBuilder
|   +-- impl/
|       +-- ControlFlowBuilderImpl
|
+-- dataflow/                            Phase 4: Data flow transformation
|   +-- IDataFlowTransformer             Converts control flow graph to data flow form
|   +-- DataFlowTransformerFactory       Singleton factory for IDataFlowTransformer
|   +-- impl/
|       +-- DataFlowTransformerImpl
|
+-- structurer/                          Phase 5: Control flow structuring
|   +-- IStructurer                      Recovers loops, if/else, switch, try-catch/finally
|   +-- StructurerFactory                Singleton factory for IStructurer
|   +-- impl/
|       +-- StructurerImpl
|
+-- reconstructor/                       Phase 6: Pattern reconstruction
|   +-- IReconstructor                   Recognizes high-level Java patterns
|   +-- ReconstructorFactory             Singleton factory for IReconstructor
|   +-- impl/
|       +-- RecordReconstructor          Reconstructs record declarations
|       +-- EnumReconstructor            Reconstructs enum declarations
|       +-- InnerClassReconstructor      Reconstructs inner class relationships
|       +-- AssertReconstructor          Reconstructs assert statements
|       +-- TryWithResourcesReconstructor Reconstructs try-with-resources
|       +-- LambdaBodyReconstructor      Inlines lambda$ method bodies into lambda expressions
|       +-- SyntheticMemberReconstructor Removes compiler-generated members
|
+-- simplifier/                          Phase 7: Expression simplification
|   +-- ISimplifier                      Constant folding, dead code elimination
|   +-- SimplifierFactory                Singleton factory for ISimplifier
|   +-- impl/
|       +-- SimplifierImpl
|
+-- emitter/                             Phase 8: Source code generation
|   +-- ISourceEmitter                   Emits formatted Java source from AST
|   +-- SourceEmitterFactory             Singleton factory for ISourceEmitter
|   +-- impl/
|       +-- SourceEmitterImpl
|
+-- impl/                               Orchestration
|   +-- DecompilerImpl                   Orchestrates the full pipeline
|   +-- DecompilerConfigurationImpl      Immutable IDecompilerConfiguration implementation
|   +-- output/
|       +-- FileResultSaver              Writes decompiled source to files
|       +-- InMemoryResultSaver          Collects results in memory
|
+-- model/                               Data model (all with equals/hashCode/toString)
    +-- classfile/                        Class file structure
    |   +-- IStructClass, IStructMethod, IStructField, IStructMember, IStructRecordComponent
    |   +-- IClassFileMetadata, IMethodCode
    |   +-- ClassfileModelFactory
    |   +-- impl/
    |
    +-- bytecode/                         Bytecode instructions
    |   +-- IInstruction, IJumpInstruction, ISwitchInstruction
    |   +-- IInstructionSequence, IExceptionHandler, IExceptionTable
    |   +-- BytecodeModelFactory
    |   +-- impl/
    |
    +-- controlflow/                      Control flow graph
    |   +-- IBasicBlock, IControlFlowGraph, IEdge, EdgeType
    |   +-- ControlFlowModelFactory
    |   +-- impl/
    |
    +-- dataflow/                         Data flow form
    |   +-- IDataFlowBlock, IDataFlowVariable, IMergeFunction
    |   +-- DataFlowModelFactory
    |   +-- impl/
    |
    +-- ast/                              Java AST
    |   +-- IAstNode, IAstVisitor
    |   +-- declaration/                  4 declaration types
    |   |   +-- IClassDeclaration, IMethodDeclaration, IFieldDeclaration, IParameterDeclaration
    |   |   +-- DeclarationModelFactory
    |   |   +-- impl/
    |   +-- expression/                   15 expression types
    |   |   +-- IExpression, IBinaryExpression, IUnaryExpression, IConstantExpression, ...
    |   |   +-- ILambdaExpression, IMethodReferenceExpression, ISwitchExpression
    |   |   +-- BinaryOperator, UnaryOperator
    |   |   +-- ExpressionModelFactory
    |   |   +-- impl/
    |   +-- statement/                    16 statement types
    |       +-- IStatement, IIfStatement, IWhileStatement, IForStatement, ISwitchStatement, ...
    |       +-- StatementModelFactory
    |       +-- impl/
    |
    +-- type/                             Type system
    |   +-- IType, IPrimitiveType, IObjectType, IArrayType, IGenericType, IParameterizedType, IWildcardType, IMethodType
    |   +-- TypeModelFactory
    |   +-- impl/
    |
    +-- common/                           Shared model classes
        +-- IAccessFlags, IAnnotation, IAttribute, IConstantPool
        +-- CommonModelFactory
        +-- impl/
```

## Key Interfaces

### IDecompiler (main entry point)
```java
public interface IDecompiler {
    String decompile(byte[] classBytes) throws IOException;
    String decompile(IClassSource source, String className) throws IOException;
    void decompileAll(IClassSource source, IResultSaver saver) throws IOException;
}
```

### IClassSource (input abstraction)
```java
public interface IClassSource extends AutoCloseable {
    List<String> listClasses() throws IOException;
    byte[] readClass(String className) throws IOException;
    boolean hasClass(String className) throws IOException;
}
```

### IResultSaver (output abstraction)
```java
public interface IResultSaver extends AutoCloseable {
    void saveSource(String className, String sourceCode) throws IOException;
    int getSavedCount();
    Map<String, String> getResults();
}
```

## Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| org.ow2.asm:asm | 9.7.1 | Bytecode reading and class file parsing |
| org.ow2.asm:asm-tree | 9.7.1 | Tree API for class/method/field nodes |
| org.ow2.asm:asm-util | 9.7.1 | Bytecode utility classes |
| info.picocli:picocli | 4.7.6 | Command-line interface |
| org.slf4j:slf4j-api | (managed) | Logging facade |
| ch.qos.logback:logback-classic | (managed) | Logging implementation |

## Factory Pattern

Every service and model package uses the toolarium HOLDER singleton pattern:

```java
public class ExampleFactory {
    private ExampleFactory() {
    }

    /**
     * Private class, the only instance of the singelton which will be created
     * by accessing the holder class.
     */
    private static final class HOLDER {
        static final ExampleFactory INSTANCE = new ExampleFactory();
    }

    public static ExampleFactory getInstance() {
        return HOLDER.INSTANCE;
    }

    public IExample getExample() {
        return new ExampleImpl();
    }
}
```

Factory classes live in the parent package next to the interfaces they serve, not inside `impl/`.

## Testing Strategy

1. **Unit tests per pass**: each pipeline phase has its own tests with crafted bytecode input
2. **Round-trip tests**: compile Java source, decompile, verify output matches expected
3. **Model tests**: equality, hash code, and toString for all model classes
4. **Integration tests**: end-to-end decompilation of real class files

## Further Documentation

See [docs/configuration.md](docs/configuration.md) for the full configuration option reference with
builder API examples, properties file format, and CLI usage.

See [docs/output-comparison.md](docs/output-comparison.md) for a side-by-side output comparison on
representative Java patterns.
