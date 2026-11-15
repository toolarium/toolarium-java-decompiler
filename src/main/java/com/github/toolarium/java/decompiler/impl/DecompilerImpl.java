/*
 * DecompilerImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.impl;

import com.github.toolarium.java.decompiler.api.DecompilerConfigurationBuilder;
import com.github.toolarium.java.decompiler.api.IDecompiler;
import com.github.toolarium.java.decompiler.api.IDecompilerConfiguration;
import com.github.toolarium.java.decompiler.api.IResultSaver;
import com.github.toolarium.java.decompiler.controlflow.ControlFlowBuilderFactory;
import com.github.toolarium.java.decompiler.controlflow.IControlFlowBuilder;
import com.github.toolarium.java.decompiler.emitter.ISourceEmitter;
import com.github.toolarium.java.decompiler.emitter.SourceEmitterFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IFieldDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IParameterDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.classfile.IInnerClassEntry;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.model.classfile.IStructField;
import com.github.toolarium.java.decompiler.model.classfile.IStructMethod;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import com.github.toolarium.java.decompiler.reader.ClassReaderFactory;
import com.github.toolarium.java.decompiler.reader.GenericSignatureParser;
import com.github.toolarium.java.decompiler.reader.IClassReader;
import com.github.toolarium.java.decompiler.reconstructor.IReconstructor;
import com.github.toolarium.java.decompiler.reconstructor.ReconstructorFactory;
import com.github.toolarium.java.decompiler.simplifier.ISimplifier;
import com.github.toolarium.java.decompiler.simplifier.SimplifierFactory;
import com.github.toolarium.java.decompiler.source.IClassSource;
import com.github.toolarium.java.decompiler.structurer.IStructurer;
import com.github.toolarium.java.decompiler.structurer.StructurerFactory;
import com.github.toolarium.java.decompiler.translator.BytecodeTranslatorFactory;
import com.github.toolarium.java.decompiler.translator.IBytecodeTranslator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main implementation of {@link IDecompiler} that orchestrates the full decompilation pipeline:
 * class reading, CFG construction, control flow structuring, and source emission.
 *
 * <p>Instances of this class are not thread-safe for concurrent external calls.
 * Each calling thread should use its own instance obtained from
 * {@link com.github.toolarium.java.decompiler.api.DecompilerFactory}.
 * The internal parallel mode ({@code parallelThreads > 1}) is safe because it
 * creates independent pipeline instances per worker thread.
 *
 * @author patrick
 */
public class DecompilerImpl implements IDecompiler {
    private static final Logger LOG = LoggerFactory.getLogger(DecompilerImpl.class);
    private static final long PARALLEL_CLASS_TIMEOUT_SECONDS = 300;
    private static final int MAX_REGEX_LENGTH = 1000;
    private final IDecompilerConfiguration configuration;
    private final IClassReader classReader;
    private final IControlFlowBuilder cfgBuilder;
    private final IStructurer structurer;
    private final ISimplifier simplifier;
    private final List<IReconstructor> reconstructors;
    private final Pattern includePattern;
    private final Pattern excludePattern;


    /**
     * Constructor for DecompilerImpl with default settings.
     */
    public DecompilerImpl() {
        this(DecompilerConfigurationBuilder.builder().build());
    }


    /**
     * Constructor for DecompilerImpl with custom configuration.
     *
     * @param configuration the decompiler configuration
     */
    public DecompilerImpl(IDecompilerConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.classReader = ClassReaderFactory.getInstance().getClassReader();
        this.cfgBuilder = ControlFlowBuilderFactory.getInstance().getCfgBuilder();
        this.structurer = StructurerFactory.getInstance().getStructurer();
        this.simplifier = SimplifierFactory.getInstance().getSimplifier();
        this.reconstructors = buildFilteredReconstructors(this.configuration);

        this.includePattern = compileOptionalPattern(this.configuration.getIncludeClassRegex(), "include");
        this.excludePattern = compileOptionalPattern(this.configuration.getExcludeClassRegex(), "exclude");
    }


    /**
     * Decompile a single class from raw bytecode.
     *
     * @param classBytes the class file bytes
     * @return the decompiled Java source code
     * @throws IOException if the bytes are not a valid class file
     */
    @Override
    public String decompile(byte[] classBytes) throws IOException {
        Objects.requireNonNull(classBytes, "classBytes");

        DecompileResult result = decompileToDeclaration(classBytes);
        ISourceEmitter sourceEmitter = createSourceEmitter();
        String source = sourceEmitter.emitClassWithPackage(result.getDeclaration(), result.getStructClass().getName());
        source = prependHeaderComments(source, result.getStructClass().getName());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Decompiled class: " + result.getStructClass().getName());
        }

        return source;
    }


    /**
     * Decompile a single class from a class source.
     *
     * @param source    the class source
     * @param className the internal class name (e.g., "java/lang/String")
     * @return the decompiled Java source code
     * @throws IOException if the class cannot be read or decompiled
     */
    @Override
    public String decompile(IClassSource source, String className) throws IOException {
        byte[] classBytes = source.readClass(className);
        return decompile(classBytes);
    }


    /**
     * Decompile raw bytecode to an intermediate class declaration and struct class.
     * This method performs parsing, AST construction, and reconstruction, but does not emit source.
     *
     * @param classBytes the class file bytes
     * @return the decompile result containing both the declaration and the struct class
     * @throws IOException if the bytes are not a valid class file
     */
    private DecompileResult decompileToDeclaration(byte[] classBytes) throws IOException {
        ClassNode classNode = new ClassNode();
        new ClassReader(classBytes).accept(classNode, 0);

        IStructClass structClass = classReader.readClass(classNode);

        Map<String, MethodNode> methodNodeMap = new HashMap<>(classNode.methods.size() * 4 / 3 + 1);
        for (MethodNode mn : classNode.methods) {
            methodNodeMap.put(mn.name + mn.desc, mn);
        }

        IBytecodeTranslator translator = BytecodeTranslatorFactory.getInstance().getBytecodeTranslator();
        if (configuration.getMaxMethodTimeSeconds() > 0) {
            translator.setMaxTranslationTimeSeconds(configuration.getMaxMethodTimeSeconds());
        }
        IClassDeclaration classDecl = buildClassDeclaration(structClass, classNode, methodNodeMap,
                translator);

        for (IReconstructor reconstructor : reconstructors) {
            if (reconstructor.applies(structClass)) {
                classDecl = reconstructor.reconstruct(classDecl, structClass);
            }
        }

        return new DecompileResult(classDecl, structClass);
    }


    /**
     * Decompile all classes from a source, writing results to the saver.
     * Inner classes are nested inside their enclosing class and not saved separately.
     *
     * @param source the class source
     * @param saver  the result saver
     * @throws IOException if reading or decompiling fails
     */
    @Override
    public void decompileAll(IClassSource source, IResultSaver saver) throws IOException {
        List<String> classNames = source.listClasses();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Decompiling " + classNames.size() + " classes.");
        }

        List<String> includedClasses = new ArrayList<>();
        for (String className : classNames) {
            if (isClassIncluded(className)) {
                includedClasses.add(className);
            }
        }

        // First pass: decompile all classes to intermediate declarations
        Map<String, DecompileResult> results;
        List<String> failedClasses;
        int threads = configuration.getParallelThreads();
        if (threads > 1 && includedClasses.size() > 1) {
            ParallelDecompileOutcome outcome = decompileClassesParallel(source, includedClasses, threads);
            results = outcome.getResults();
            failedClasses = outcome.getFailedClasses();
        } else {
            results = new LinkedHashMap<>();
            failedClasses = new ArrayList<>();
            decompileClassesSequential(source, includedClasses, results, failedClasses);
        }

        // Second pass: identify inner class relationships (named, anonymous, and local)
        Map<String, List<String>> outerToInners = new HashMap<>();
        Set<String> innerClassNames = new LinkedHashSet<>();
        for (Map.Entry<String, DecompileResult> entry : results.entrySet()) {
            IStructClass structClass = entry.getValue().getStructClass();
            for (IInnerClassEntry ice : structClass.getInnerClassEntries()) {
                String innerName = ice.getInternalName();
                if (!innerName.equals(entry.getKey())) {
                    continue;
                }

                String outerName;
                if (ice.getOuterName() != null) {
                    outerName = ice.getOuterName();
                } else {
                    outerName = inferOuterClassName(innerName);
                }

                if (outerName != null && results.containsKey(outerName)) {
                    outerToInners.computeIfAbsent(outerName, k -> new ArrayList<>()).add(innerName);
                    innerClassNames.add(innerName);
                }
            }
        }

        // Sealed/non-sealed pass: detect non-sealed classes whose parent is sealed
        Set<String> sealedClassNames = new LinkedHashSet<>();
        for (Map.Entry<String, DecompileResult> entry : results.entrySet()) {
            if (!entry.getValue().getStructClass().getPermittedSubclasses().isEmpty()) {
                sealedClassNames.add(entry.getKey());
            }
        }
        if (!sealedClassNames.isEmpty()) {
            for (Map.Entry<String, DecompileResult> entry : results.entrySet()) {
                IStructClass sc = entry.getValue().getStructClass();
                boolean parentIsSealed = false;
                if (sc.getSuperName() != null && sealedClassNames.contains(sc.getSuperName())) {
                    parentIsSealed = true;
                }
                if (!parentIsSealed) {
                    for (String ifaceName : sc.getInterfaceNames()) {
                        if (sealedClassNames.contains(ifaceName)) {
                            parentIsSealed = true;
                            break;
                        }
                    }
                }
                if (parentIsSealed && sc.getPermittedSubclasses().isEmpty()
                        && !sc.getAccessFlags().isFinal()) {
                    IClassDeclaration oldDecl = entry.getValue().getDeclaration();
                    IClassDeclaration newDecl = DeclarationModelFactory.getInstance().getClassDeclaration(
                            oldDecl.getAccessFlags(), oldDecl.getName(), oldDecl.getSuperType(),
                            oldDecl.getInterfaceTypes(), oldDecl.getFields(), oldDecl.getMethods(),
                            oldDecl.getInnerClasses(), oldDecl.getAnnotations(),
                            oldDecl.getPermittedSubclasses(), true);
                    results.put(entry.getKey(), new DecompileResult(newDecl, sc));
                }
            }
        }

        // Third pass: nest inner classes and emit source for top-level classes only
        for (String className : classNames) {
            if (innerClassNames.contains(className)) {
                continue;
            }
            DecompileResult result = results.get(className);
            if (result == null) {
                continue;
            }

            IClassDeclaration classDecl = result.getDeclaration();
            List<String> inners = outerToInners.get(className);
            if (inners != null && !inners.isEmpty()) {
                classDecl = nestInnerClasses(classDecl, inners, results);
            }

            ISourceEmitter sourceEmitter = createSourceEmitter();
            String sourceCode = sourceEmitter.emitClassWithPackage(classDecl, className);
            sourceCode = prependHeaderComments(sourceCode, className);
            saver.saveSource(className, sourceCode);
        }

        if (!failedClasses.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to decompile " + failedClasses.size() + " classes: " + failedClasses);
            }
            LOG.warn("Failed to decompile " + failedClasses.size() + " classes: " + failedClasses);
        }
    }


    /**
     * Nest inner class declarations inside an outer class declaration.
     * Each inner class is renamed to its simple name (stripping the "Outer$" prefix).
     *
     * @param outerDecl the outer class declaration
     * @param innerNames the internal names of the inner classes
     * @param results    the decompile results map for looking up inner class declarations
     * @return the outer class declaration with inner classes nested
     */
    private IClassDeclaration nestInnerClasses(IClassDeclaration outerDecl, List<String> innerNames,
                                               Map<String, DecompileResult> results) {
        DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();
        List<IClassDeclaration> innerDecls = new ArrayList<>();

        for (String innerName : innerNames) {
            DecompileResult innerResult = results.get(innerName);
            if (innerResult == null) {
                continue;
            }

            IClassDeclaration innerDecl = innerResult.getDeclaration();

            // Use the simple name from the InnerClasses attribute, or derive from the class name
            String simpleName = null;
            for (IInnerClassEntry ice : innerResult.getStructClass().getInnerClassEntries()) {
                if (ice.getInternalName().equals(innerName)) {
                    if (ice.getSimpleName() != null) {
                        simpleName = ice.getSimpleName();
                    }
                    break;
                }
            }
            if (simpleName == null) {
                // Anonymous or local class: extract from internal name (e.g., "Outer$1" -> "1")
                int dollarIdx = innerName.lastIndexOf('$');
                if (dollarIdx >= 0 && dollarIdx < innerName.length() - 1) {
                    simpleName = innerName.substring(dollarIdx + 1);
                }
            }

            if (simpleName != null && !simpleName.equals(innerDecl.getName())) {
                innerDecl = declFactory.getClassDeclaration(
                        innerDecl.getAccessFlags(), simpleName, innerDecl.getSuperType(),
                        innerDecl.getInterfaceTypes(), innerDecl.getFields(), innerDecl.getMethods(),
                        innerDecl.getInnerClasses(), innerDecl.getAnnotations(),
                        innerDecl.getPermittedSubclasses(), innerDecl.isNonSealed());
            }

            innerDecls.add(innerDecl);
        }

        return declFactory.getClassDeclaration(
                outerDecl.getAccessFlags(), outerDecl.getName(), outerDecl.getSuperType(),
                outerDecl.getInterfaceTypes(), outerDecl.getFields(), outerDecl.getMethods(),
                innerDecls, outerDecl.getAnnotations(),
                outerDecl.getPermittedSubclasses(), outerDecl.isNonSealed());
    }


    /**
     * Build an IClassDeclaration from the parsed class structure and ASM class node.
     *
     * @param structClass   the parsed class structure
     * @param classNode     the ASM class node for bytecode access
     * @param methodNodeMap pre-built map of method name+descriptor to MethodNode
     * @param translator    the bytecode translator for this decompilation
     * @return the class declaration
     */
    private IClassDeclaration buildClassDeclaration(IStructClass structClass, ClassNode classNode,
                                                     Map<String, MethodNode> methodNodeMap,
                                                     IBytecodeTranslator translator) {
        TypeModelFactory typeFactory = TypeModelFactory.getInstance();

        // Determine simple class name
        String fullName = structClass.getName();
        String simpleName = fullName;
        int lastSlash = fullName.lastIndexOf('/');
        if (lastSlash >= 0) {
            simpleName = fullName.substring(lastSlash + 1);
        }

        // Super type and interface types (prefer generic signature when available)
        IType superType = null;
        List<IType> interfaceTypes = new ArrayList<>();

        if (structClass.getSignature() != null) {
            try {
                GenericSignatureParser.ClassSignatureResult classResult =
                        GenericSignatureParser.parseClassSignature(structClass.getSignature(), typeFactory);
                IType parsedSuper = classResult.getSuperclassType();
                if (parsedSuper != null && !"java.lang.Object".equals(parsedSuper.getDisplayName())) {
                    superType = parsedSuper;
                }
                interfaceTypes.addAll(classResult.getInterfaceTypes());
            } catch (Exception ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to parse class signature, falling back to raw types: "
                            + structClass.getSignature(), ex);
                }
                superType = buildRawSuperType(structClass, typeFactory);
                interfaceTypes = buildRawInterfaceTypes(structClass, typeFactory);
            }
        } else {
            superType = buildRawSuperType(structClass, typeFactory);
            interfaceTypes = buildRawInterfaceTypes(structClass, typeFactory);
        }

        // Build field node lookup for O(1) access by name
        Map<String, org.objectweb.asm.tree.FieldNode> fieldNodeMap = new HashMap<>();
        if (classNode != null) {
            for (org.objectweb.asm.tree.FieldNode fn : classNode.fields) {
                fieldNodeMap.put(fn.name, fn);
            }
        }

        // Fields
        List<IFieldDeclaration> fields = new ArrayList<>();
        for (IStructField field : structClass.getFields()) {
            fields.add(buildFieldDeclaration(field, fieldNodeMap));
        }

        // Methods
        List<IMethodDeclaration> methods = new ArrayList<>();
        for (int i = 0; i < structClass.getMethods().size(); i++) {
            IStructMethod method = structClass.getMethods().get(i);
            MethodNode methodNode = methodNodeMap.get(method.getName() + method.getDescriptor());
            methods.add(buildMethodDeclaration(method, methodNode, simpleName, fullName, translator));
        }

        List<String> permittedSubclasses = buildPermittedSubclassNames(structClass);

        return DeclarationModelFactory.getInstance().getClassDeclaration(
                structClass.getAccessFlags(), simpleName, superType,
                interfaceTypes, fields, methods, Collections.emptyList(),
                structClass.getAnnotations(), permittedSubclasses, false);
    }


    /**
     * Infer the outer class name from an anonymous or local class internal name.
     * Anonymous classes follow the pattern "Outer$1", "Outer$2", etc.
     * Local classes follow the pattern "Outer$1LocalName".
     * The outer name is the part before the last "$" followed by a digit.
     *
     * @param innerName the inner class internal name
     * @return the inferred outer class name, or null if the pattern does not match
     */
    private String inferOuterClassName(String innerName) {
        int dollarIdx = innerName.lastIndexOf('$');
        if (dollarIdx <= 0 || dollarIdx >= innerName.length() - 1) {
            return null;
        }
        char afterDollar = innerName.charAt(dollarIdx + 1);
        if (Character.isDigit(afterDollar)) {
            return innerName.substring(0, dollarIdx);
        }
        return null;
    }


    /**
     * Build simple names from the permitted subclasses internal names.
     *
     * @param structClass the struct class
     * @return the list of simple names, empty if not sealed
     */
    private List<String> buildPermittedSubclassNames(IStructClass structClass) {
        List<String> permittedSubclasses = structClass.getPermittedSubclasses();
        if (permittedSubclasses.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> simpleNames = new ArrayList<>();
        for (String internalName : permittedSubclasses) {
            int lastSlash = internalName.lastIndexOf('/');
            if (lastSlash >= 0) {
                simpleNames.add(internalName.substring(lastSlash + 1));
            } else {
                simpleNames.add(internalName);
            }
        }
        return simpleNames;
    }


    /**
     * Build a field declaration, extracting constant initializers when available.
     *
     * @param field        the struct field
     * @param fieldNodeMap the pre-built map from field name to ASM FieldNode
     * @return the field declaration
     */
    private IFieldDeclaration buildFieldDeclaration(IStructField field, Map<String, org.objectweb.asm.tree.FieldNode> fieldNodeMap) {
        IType fieldType = resolveFieldType(field);
        IExpression initializer = null;

        // Look up constant value from ASM FieldNode
        org.objectweb.asm.tree.FieldNode fn = fieldNodeMap.get(field.getName());
        if (fn != null && fn.value != null) {
            Object value = fn.value;
            if ("Z".equals(field.getDescriptor()) && value instanceof Integer) {
                value = ((Integer) value) != 0;
            }
            initializer = ExpressionModelFactory.getInstance().getConstantExpression(fieldType, value);
        }

        return DeclarationModelFactory.getInstance().getFieldDeclaration(
                field.getAccessFlags(), fieldType, field.getName(), initializer,
                field.getAnnotations());
    }


    /**
     * Build a method declaration, including decompiling its body.
     *
     * @param method        the struct method
     * @param methodNode    the ASM method node (for bytecode)
     * @param className     the simple class name (for constructor detection)
     * @param fullClassName the full internal class name (for this reference)
     * @param translator    the bytecode translator
     * @return the method declaration
     */
    private IMethodDeclaration buildMethodDeclaration(IStructMethod method, MethodNode methodNode,
                                                      String className, String fullClassName,
                                                      IBytecodeTranslator translator) {
        boolean isConstructor = "<init>".equals(method.getName());
        String methodName;
        if (isConstructor) {
            methodName = className;
        } else {
            methodName = method.getName();
        }

        // Parse return type and parameter types (prefer generic signature when available)
        IType returnType;
        List<IParameterDeclaration> params;

        if (method.getSignature() != null) {
            try {
                GenericSignatureParser.MethodSignatureResult methodResult =
                        GenericSignatureParser.parseMethodSignature(method.getSignature(),
                                TypeModelFactory.getInstance());
                returnType = methodResult.getReturnType();
                params = buildParametersFromTypes(method, methodResult.getParameterTypes());
            } catch (Exception ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to parse method signature, falling back to descriptor: "
                            + method.getSignature(), ex);
                }
                returnType = parseReturnType(method.getDescriptor());
                params = buildParameters(method);
            }
        } else {
            returnType = parseReturnType(method.getDescriptor());
            params = buildParameters(method);
        }

        // Parse thrown exception types
        List<IType> thrownTypes = new ArrayList<>();
        for (String exType : method.getExceptionTypes()) {
            thrownTypes.add(TypeModelFactory.getInstance().getObjectType(exType));
        }

        // Decompile method body using bytecode translator
        IBlockStatement body = null;
        if (methodNode != null && methodNode.instructions != null && methodNode.instructions.size() > 0) {
            List<IStatement> translated = translator.translate(methodNode, fullClassName);
            body = StatementModelFactory.getInstance().getBlockStatement(null, translated);
            body = simplifier.simplify(body);
        }

        return DeclarationModelFactory.getInstance().getMethodDeclaration(
                method.getAccessFlags(), methodName, returnType,
                params, thrownTypes, body, isConstructor,
                method.getAnnotations());
    }


    /**
     * Build parameter declarations from a method.
     *
     * @param method the struct method
     * @return the parameter declarations
     */
    private List<IParameterDeclaration> buildParameters(IStructMethod method) {
        DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();
        List<IParameterDeclaration> params = new ArrayList<>();
        List<IType> paramTypes = parseParameterTypes(method.getDescriptor());
        List<List<IAnnotation>> paramAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < paramTypes.size(); i++) {
            String paramName;
            if (i < method.getParameterNames().size()) {
                paramName = method.getParameterNames().get(i);
            } else {
                paramName = "arg" + i;
            }
            List<IAnnotation> annotations;
            if (i < paramAnnotations.size()) {
                annotations = paramAnnotations.get(i);
            } else {
                annotations = Collections.emptyList();
            }
            params.add(declFactory.getParameterDeclaration(paramTypes.get(i), paramName, false, annotations));
        }

        return params;
    }


    /**
     * Parse a JVM type descriptor into an IType.
     *
     * @param descriptor the type descriptor
     * @return the type
     */
    private IType parseDescriptorType(String descriptor) {
        TypeModelFactory typeFactory = TypeModelFactory.getInstance();
        if (descriptor.isEmpty()) {
            return typeFactory.getObjectType("java/lang/Object");
        }

        char first = descriptor.charAt(0);
        switch (first) {
            case 'Z': case 'B': case 'C': case 'S':
            case 'I': case 'J': case 'F': case 'D':
            case 'V':
                return typeFactory.getPrimitiveType(first);
            case 'L':
                String className = descriptor.substring(1, descriptor.length() - 1);
                return typeFactory.getObjectType(className);
            case '[':
                IType elementType = parseDescriptorType(descriptor.substring(1));
                return typeFactory.getArrayType(elementType, 1);
            default:
                return typeFactory.getObjectType("java/lang/Object");
        }
    }


    /**
     * Parse the return type from a method descriptor.
     *
     * @param descriptor the method descriptor (e.g., "(II)V")
     * @return the return type
     */
    private IType parseReturnType(String descriptor) {
        int closeParenIdx = descriptor.lastIndexOf(')');
        if (closeParenIdx >= 0) {
            return parseDescriptorType(descriptor.substring(closeParenIdx + 1));
        }
        return TypeModelFactory.getInstance().getPrimitiveType('V');
    }


    /**
     * Resolve the type of a field, preferring the generic signature when available.
     *
     * @param field the struct field
     * @return the resolved field type
     */
    private IType resolveFieldType(IStructField field) {
        if (field.getSignature() != null) {
            try {
                return GenericSignatureParser.parseFieldTypeSignature(
                        field.getSignature(), TypeModelFactory.getInstance());
            } catch (Exception ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to parse field signature, falling back to descriptor: "
                            + field.getSignature(), ex);
                }
            }
        }
        return parseDescriptorType(field.getDescriptor());
    }


    /**
     * Build the raw superclass type from the struct class descriptor (no generics).
     *
     * @param structClass the struct class
     * @param typeFactory the type factory
     * @return the super type, or null if java.lang.Object
     */
    private IType buildRawSuperType(IStructClass structClass, TypeModelFactory typeFactory) {
        if (structClass.getSuperName() != null && !"java/lang/Object".equals(structClass.getSuperName())) {
            return typeFactory.getObjectType(structClass.getSuperName());
        }
        return null;
    }


    /**
     * Build the raw interface types from the struct class descriptor (no generics).
     *
     * @param structClass the struct class
     * @param typeFactory the type factory
     * @return the interface types
     */
    private List<IType> buildRawInterfaceTypes(IStructClass structClass, TypeModelFactory typeFactory) {
        List<IType> types = new ArrayList<>();
        for (String ifaceName : structClass.getInterfaceNames()) {
            types.add(typeFactory.getObjectType(ifaceName));
        }
        return types;
    }


    /**
     * Build parameter declarations from generic parameter types.
     *
     * @param method     the struct method for parameter names
     * @param paramTypes the generic parameter types
     * @return the parameter declarations
     */
    private List<IParameterDeclaration> buildParametersFromTypes(IStructMethod method, List<IType> paramTypes) {
        DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();
        List<IParameterDeclaration> params = new ArrayList<>();
        List<List<IAnnotation>> paramAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < paramTypes.size(); i++) {
            String paramName;
            if (i < method.getParameterNames().size()) {
                paramName = method.getParameterNames().get(i);
            } else {
                paramName = "arg" + i;
            }
            List<IAnnotation> annotations;
            if (i < paramAnnotations.size()) {
                annotations = paramAnnotations.get(i);
            } else {
                annotations = Collections.emptyList();
            }
            params.add(declFactory.getParameterDeclaration(paramTypes.get(i), paramName, false, annotations));
        }

        return params;
    }


    /**
     * Holds the intermediate result of decompiling a class to its AST declaration,
     * before source emission.
     */
    private static final class DecompileResult {
        private final IClassDeclaration declaration;
        private final IStructClass structClass;


        /**
         * Constructor for DecompileResult.
         *
         * @param declaration the class declaration
         * @param structClass the struct class
         */
        DecompileResult(IClassDeclaration declaration, IStructClass structClass) {
            this.declaration = declaration;
            this.structClass = structClass;
        }


        /**
         * Get the class declaration.
         *
         * @return the class declaration
         */
        IClassDeclaration getDeclaration() {
            return declaration;
        }


        /**
         * Get the struct class.
         *
         * @return the struct class
         */
        IStructClass getStructClass() {
            return structClass;
        }
    }


    /**
     * Parse parameter types from a method descriptor.
     *
     * @param descriptor the method descriptor
     * @return the parameter types
     */
    private List<IType> parseParameterTypes(String descriptor) {
        List<IType> types = new ArrayList<>();
        int idx = 1; // skip opening '('
        while (idx < descriptor.length() && descriptor.charAt(idx) != ')') {
            char ch = descriptor.charAt(idx);
            switch (ch) {
                case 'Z': case 'B': case 'C': case 'S':
                case 'I': case 'J': case 'F': case 'D':
                    types.add(TypeModelFactory.getInstance().getPrimitiveType(ch));
                    idx++;
                    break;
                case 'L':
                    int semiIdx = descriptor.indexOf(';', idx);
                    if (semiIdx < 0) {
                        return types;
                    }
                    String className = descriptor.substring(idx + 1, semiIdx);
                    types.add(TypeModelFactory.getInstance().getObjectType(className));
                    idx = semiIdx + 1;
                    break;
                case '[':
                    int arrayStart = idx;
                    while (idx < descriptor.length() && descriptor.charAt(idx) == '[') {
                        idx++;
                    }
                    if (idx >= descriptor.length()) {
                        return types;
                    }
                    int dims = idx - arrayStart;
                    IType baseType;
                    if (descriptor.charAt(idx) == 'L') {
                        int semi = descriptor.indexOf(';', idx);
                        if (semi < 0) {
                            return types;
                        }
                        baseType = TypeModelFactory.getInstance()
                                .getObjectType(descriptor.substring(idx + 1, semi));
                        idx = semi + 1;
                    } else {
                        baseType = TypeModelFactory.getInstance().getPrimitiveType(descriptor.charAt(idx));
                        idx++;
                    }
                    types.add(TypeModelFactory.getInstance().getArrayType(baseType, dims));
                    break;
                default:
                    idx++;
                    break;
            }
        }
        return types;
    }


    /**
     * Create a source emitter configured from the current configuration.
     *
     * @return the source emitter
     */
    private ISourceEmitter createSourceEmitter() {
        return SourceEmitterFactory.getInstance().getSourceEmitter(configuration.getIndentString(), configuration.getLineSeparator());
    }


    /**
     * Prepend banner and source file header comments to the source code if enabled.
     *
     * @param source    the decompiled source code
     * @param className the internal class name
     * @return the source code with header comments prepended
     */
    private String prependHeaderComments(String source, String className) {
        if (!configuration.isShowBanner() && !configuration.isShowSourceFile()) {
            return source;
        }

        String sep = configuration.getLineSeparator();
        StringBuilder header = new StringBuilder();

        if (configuration.isShowBanner()) {
            header.append("// Decompiled by toolarium-java-decompiler").append(sep);
        }

        if (configuration.isShowSourceFile()) {
            String simpleName = className;
            int lastSlash = className.lastIndexOf('/');
            if (lastSlash >= 0) {
                simpleName = className.substring(lastSlash + 1);
            }
            header.append("// Source: ").append(simpleName).append(".java").append(sep);
        }

        if (header.length() > 0) {
            header.append(sep);
        }

        return header.append(source).toString();
    }


    /**
     * Check whether a class should be included based on the configured include/exclude patterns.
     *
     * @param className the internal class name
     * @return true if the class should be decompiled
     */
    private boolean isClassIncluded(String className) {
        String dotName = className.replace('/', '.');

        if (includePattern != null && !includePattern.matcher(dotName).matches()) {
            return false;
        }

        if (excludePattern != null && excludePattern.matcher(dotName).matches()) {
            return false;
        }

        return true;
    }


    /**
     * Compile an optional regex pattern, returning null if the regex is null or invalid.
     * Invalid patterns are logged as warnings so the decompiler continues with no filter
     * rather than failing at construction time.
     *
     * @param regex the regex string, or null
     * @param label a label for log messages (e.g., "include" or "exclude")
     * @return the compiled pattern, or null if regex is null or invalid
     */
    private static Pattern compileOptionalPattern(String regex, String label) {
        if (regex == null) {
            return null;
        }

        if (regex.length() > MAX_REGEX_LENGTH) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Regex for " + label + " class filter exceeds maximum length ("
                        + regex.length() + " > " + MAX_REGEX_LENGTH + "), filter will be ignored.");
            }
            LOG.warn("Regex for " + label + " class filter exceeds maximum length, filter will be ignored.");
            return null;
        }

        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Invalid " + label + " class regex, filter will be ignored: " + regex, ex);
            }
            LOG.warn("Invalid " + label + " class regex, filter will be ignored: " + regex);
            return null;
        }
    }


    /**
     * Build a filtered list of reconstructors based on the configuration.
     * Reconstructors whose corresponding toggle is disabled are excluded.
     *
     * @param config the decompiler configuration
     * @return the filtered reconstructor list
     */
    private static List<IReconstructor> buildFilteredReconstructors(IDecompilerConfiguration config) {
        List<IReconstructor> all = ReconstructorFactory.getInstance().getReconstructors();
        List<IReconstructor> filtered = new ArrayList<>();

        for (IReconstructor reconstructor : all) {
            if (isReconstructorEnabled(reconstructor, config)) {
                filtered.add(reconstructor);
            }
        }

        return Collections.unmodifiableList(filtered);
    }


    /**
     * Check whether a reconstructor is enabled according to the configuration.
     *
     * @param reconstructor the reconstructor
     * @param config        the configuration
     * @return true if the reconstructor should be applied
     */
    private static boolean isReconstructorEnabled(IReconstructor reconstructor, IDecompilerConfiguration config) {
        String name = reconstructor.getName();

        if ("LambdaBodyReconstructor".equals(name)) {
            return config.isReconstructLambda();
        }
        if ("SyntheticMemberReconstructor".equals(name)) {
            return config.isRemoveSyntheticMembers() || config.isRemoveBridgeMethods();
        }
        if ("InnerClassReconstructor".equals(name)) {
            return config.isReconstructInnerClasses();
        }
        if ("EnumReconstructor".equals(name)) {
            return config.isReconstructEnum();
        }
        if ("RecordReconstructor".equals(name)) {
            return config.isReconstructRecord();
        }
        if ("AssertReconstructor".equals(name)) {
            return config.isReconstructAssert();
        }
        if ("StringSwitchReconstructor".equals(name)) {
            return config.isReconstructStringSwitch();
        }
        if ("TryWithResourcesReconstructor".equals(name)) {
            return config.isReconstructTryWithResources();
        }

        return true;
    }


    /**
     * Decompile classes sequentially, collecting results and failures.
     *
     * @param source         the class source
     * @param classNames     the class names to decompile
     * @param results        the map to populate with decompile results
     * @param failedClasses  the list to populate with failed class names
     */
    private void decompileClassesSequential(IClassSource source, List<String> classNames,
                                             Map<String, DecompileResult> results,
                                             List<String> failedClasses) {
        for (String className : classNames) {
            try {
                byte[] classBytes = source.readClass(className);
                results.put(className, decompileToDeclaration(classBytes));
            } catch (Exception ex) {
                failedClasses.add(className);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to decompile class: " + className, ex);
                }
            }
        }
    }


    /**
     * Decompile classes in parallel using a work-stealing thread pool.
     * Each class is decompiled independently, and results are collected after all tasks complete.
     * Per-class results are retrieved with a timeout to prevent hanging on pathological input.
     *
     * @param source       the class source
     * @param classNames   the class names to decompile
     * @param threadCount  the number of threads in the pool
     * @return the parallel decompile outcome containing results and failures
     */
    private ParallelDecompileOutcome decompileClassesParallel(IClassSource source,
                                                               List<String> classNames,
                                                               int threadCount) {
        int poolSize = Math.min(threadCount, classNames.size());
        ExecutorService executor = Executors.newWorkStealingPool(poolSize);
        Map<String, Future<DecompileResult>> futures = new LinkedHashMap<>();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Parallel decompilation with " + poolSize + " threads for "
                    + classNames.size() + " classes.");
        }

        // Pre-read all class bytes on a single thread to avoid contention on the source
        Map<String, byte[]> classBytesMap = new LinkedHashMap<>();
        List<String> failedClasses = new ArrayList<>();
        for (String className : classNames) {
            try {
                classBytesMap.put(className, source.readClass(className));
            } catch (IOException ex) {
                failedClasses.add(className);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to read class: " + className, ex);
                }
            }
        }

        for (Map.Entry<String, byte[]> entry : classBytesMap.entrySet()) {
            futures.put(entry.getKey(), executor.submit(() -> decompileToDeclaration(entry.getValue())));
        }

        executor.shutdown();

        Map<String, DecompileResult> results = new LinkedHashMap<>();

        try {
            for (Entry<String, Future<DecompileResult>> entry : futures.entrySet()) {
                String className = entry.getKey();
                try {
                    results.put(className, entry.getValue().get(PARALLEL_CLASS_TIMEOUT_SECONDS, TimeUnit.SECONDS));
                } catch (TimeoutException tex) {
                    entry.getValue().cancel(true);
                    failedClasses.add(className);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Decompilation timed out for class: " + className, tex);
                    }
                } catch (InterruptedException iex) {
                    Thread.currentThread().interrupt();
                    failedClasses.add(className);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Decompilation interrupted for class: " + className, iex);
                    }
                } catch (Exception ex) {
                    failedClasses.add(className);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Failed to decompile class: " + className, ex);
                    }
                }
            }
        } finally {
            executor.shutdownNow();
        }

        return new ParallelDecompileOutcome(results, failedClasses);
    }


    /**
     * Holds the outcome of a parallel decompilation run.
     *
     * @author patrick
     */
    private static final class ParallelDecompileOutcome {
        private final Map<String, DecompileResult> results;
        private final List<String> failedClasses;


        /**
         * Constructor for ParallelDecompileOutcome.
         *
         * @param results       the decompile results keyed by class name
         * @param failedClasses the list of class names that failed
         */
        ParallelDecompileOutcome(Map<String, DecompileResult> results, List<String> failedClasses) {
            this.results = results;
            this.failedClasses = failedClasses;
        }


        /**
         * Get the decompile results.
         *
         * @return the results map
         */
        Map<String, DecompileResult> getResults() {
            return results;
        }


        /**
         * Get the failed class names.
         *
         * @return the failed class list
         */
        List<String> getFailedClasses() {
            return failedClasses;
        }
    }
}
