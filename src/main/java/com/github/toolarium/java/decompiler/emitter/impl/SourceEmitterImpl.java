/*
 * SourceEmitterImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.emitter.impl;

import com.github.toolarium.java.decompiler.emitter.ISourceEmitter;
import com.github.toolarium.java.decompiler.model.ast.IAstVisitor;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IFieldDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IParameterDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.BinaryOperator;
import com.github.toolarium.java.decompiler.model.ast.expression.IArrayAccessExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IAssignmentExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IBinaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ICastExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IConstantExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IFieldAccessExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IInstanceOfExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ILambdaExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodCallExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodReferenceExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewArrayExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ISwitchExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ISwitchExpressionCase;
import com.github.toolarium.java.decompiler.model.ast.expression.IUnaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IVariableExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IAssertStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IBreakStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ICatchClause;
import com.github.toolarium.java.decompiler.model.ast.statement.IContinueStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IDoWhileStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IExpressionStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IForEachStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IForStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IIfStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IReturnStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchCase;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ISynchronizedStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IThrowStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ITryCatchStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IVariableDeclarationStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IWhileStatement;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Implementation of {@link ISourceEmitter} that uses the visitor pattern
 * to walk the AST and produce formatted Java source code.
 *
 * @author patrick
 */
public class SourceEmitterImpl implements ISourceEmitter, IAstVisitor {
    private static final String COMMA_SEPARATOR = ", ";
    private static final String OPEN_BRACE = ") {";
    private static final String JAVA_LANG_PREFIX = "java.lang.";
    private static final String DOT = ".";
    private static final String EQUALS_SPACED = " = ";
    private static final int MAX_COLLECT_DEPTH = 500;
    private static final int MAX_EMISSION_DEPTH = 500;
    private final String indent;
    private final String lineSeparator;
    private final StringBuilder buffer;
    private final java.util.Set<String> importedTypes;
    private String[] indentCache;
    private int indentLevel;
    private int emissionDepth;
    private String currentPackage;


    /**
     * Constructor for SourceEmitterImpl.
     *
     * @param indent        the indentation string (e.g., four spaces)
     * @param lineSeparator the line separator
     */
    public SourceEmitterImpl(String indent, String lineSeparator) {
        this.indent = Objects.requireNonNull(indent);
        this.lineSeparator = Objects.requireNonNull(lineSeparator);
        this.buffer = new StringBuilder();
        this.importedTypes = new java.util.HashSet<>();
        this.indentCache = buildIndentCache(indent, 32);
        this.indentLevel = 0;
        this.emissionDepth = 0;
        this.currentPackage = null;
    }


    /**
     * Emit a class declaration as Java source code.
     *
     * @param classDeclaration the class declaration
     * @return the formatted Java source
     */
    @Override
    public String emitClass(IClassDeclaration classDeclaration) {
        buffer.setLength(0);
        indentLevel = 0;
        classDeclaration.accept(this);
        return buffer.toString();
    }


    /**
     * Emit a statement as Java source code.
     *
     * @param statement the statement
     * @return the formatted Java source
     */
    @Override
    public String emitStatement(IStatement statement) {
        buffer.setLength(0);
        indentLevel = 0;
        currentPackage = null;
        statement.accept(this);
        return buffer.toString();
    }


    /**
     * Emit a class declaration with package and import statements.
     *
     * @param classDeclaration  the class declaration
     * @param fullInternalName  the full internal class name (e.g., "com/example/MyClass")
     * @return the formatted Java source with package and imports
     */
    @Override
    public String emitClassWithPackage(IClassDeclaration classDeclaration, String fullInternalName) {
        buffer.setLength(0);
        indentLevel = 0;

        // Extract package from internal name
        String dotName = fullInternalName.replace('/', '.');
        int lastDot = dotName.lastIndexOf('.');
        if (lastDot > 0) {
            currentPackage = dotName.substring(0, lastDot);
        } else {
            currentPackage = null;
        }

        // Collect type references via lightweight AST walk (no string building)
        importedTypes.clear();
        collectTypes(classDeclaration);

        // Single emission pass with package, imports, and class body
        buffer.setLength(0);
        if (currentPackage != null) {
            buffer.append("package ").append(currentPackage).append(';').append(lineSeparator);
            buffer.append(lineSeparator);
        }

        // Emit import statements (skip java.lang and same-package), sorted for deterministic output
        List<String> sortedImports = new ArrayList<>(importedTypes);
        java.util.Collections.sort(sortedImports);
        boolean hasImports = false;
        for (String importType : sortedImports) {
            if (!importType.startsWith(JAVA_LANG_PREFIX)
                    && (currentPackage == null || !importType.startsWith(currentPackage + DOT))) {
                buffer.append("import ").append(importType).append(';').append(lineSeparator);
                hasImports = true;
            }
        }
        if (hasImports) {
            buffer.append(lineSeparator);
        }

        classDeclaration.accept(this);
        return buffer.toString();
    }


    /**
     * Collect all type references from a class declaration without emitting source.
     * This replaces the previous two-pass approach where the full class body was emitted
     * once to collect types and then again for the final output.
     *
     * @param classDeclaration the class declaration to scan
     */
    private void collectTypes(IClassDeclaration classDeclaration) {
        if (classDeclaration.getSuperType() != null) {
            collectTypeName(classDeclaration.getSuperType());
        }
        for (IType ifaceType : classDeclaration.getInterfaceTypes()) {
            collectTypeName(ifaceType);
        }
        for (IFieldDeclaration field : classDeclaration.getFields()) {
            collectTypeName(field.getFieldType());
            if (field.getInitializer() != null) {
                collectExpressionTypes(field.getInitializer());
            }
        }
        for (IMethodDeclaration method : classDeclaration.getMethods()) {
            collectMethodTypes(method);
        }
        for (IClassDeclaration innerClass : classDeclaration.getInnerClasses()) {
            collectTypes(innerClass);
        }
    }


    /**
     * Collect type references from a method declaration.
     *
     * @param method the method declaration
     */
    private void collectMethodTypes(IMethodDeclaration method) {
        if (method.getReturnType() != null) {
            collectTypeName(method.getReturnType());
        }
        for (IParameterDeclaration param : method.getParameters()) {
            collectTypeName(param.getParameterType());
        }
        for (IType thrownType : method.getThrownTypes()) {
            collectTypeName(thrownType);
        }
        if (method.getBody() != null) {
            collectStatementTypes(method.getBody());
        }
    }


    /**
     * Collect type references from a statement recursively.
     *
     * @param statement the statement
     */
    private void collectStatementTypes(IStatement statement) {
        collectStatementTypes(statement, 0);
    }


    /**
     * Collect type references from a statement recursively with depth tracking.
     *
     * @param statement the statement
     * @param depth     the current recursion depth
     */
    private void collectStatementTypes(IStatement statement, int depth) {
        if (depth > MAX_COLLECT_DEPTH) {
            return;
        }
        int nextDepth = depth + 1;
        if (statement instanceof IBlockStatement) {
            for (IStatement stmt : ((IBlockStatement) statement).getStatements()) {
                collectStatementTypes(stmt, nextDepth);
            }
        } else if (statement instanceof IVariableDeclarationStatement) {
            IVariableDeclarationStatement varDecl = (IVariableDeclarationStatement) statement;
            collectTypeName(varDecl.getVariableType());
            if (varDecl.getInitializer() != null) {
                collectExpressionTypes(varDecl.getInitializer(), nextDepth);
            }
        } else if (statement instanceof IExpressionStatement) {
            collectExpressionTypes(((IExpressionStatement) statement).getExpression(), nextDepth);
        } else if (statement instanceof IReturnStatement) {
            IReturnStatement ret = (IReturnStatement) statement;
            if (ret.getValue() != null) {
                collectExpressionTypes(ret.getValue(), nextDepth);
            }
        } else if (statement instanceof IIfStatement) {
            IIfStatement ifStmt = (IIfStatement) statement;
            collectExpressionTypes(ifStmt.getCondition(), nextDepth);
            collectStatementTypes(ifStmt.getThenBranch(), nextDepth);
            if (ifStmt.getElseBranch() != null) {
                collectStatementTypes(ifStmt.getElseBranch(), nextDepth);
            }
        } else if (statement instanceof IWhileStatement) {
            IWhileStatement whileStmt = (IWhileStatement) statement;
            collectExpressionTypes(whileStmt.getCondition(), nextDepth);
            collectStatementTypes(whileStmt.getBody(), nextDepth);
        } else if (statement instanceof IForStatement) {
            IForStatement forStmt = (IForStatement) statement;
            if (forStmt.getInit() != null) {
                collectStatementTypes(forStmt.getInit(), nextDepth);
            }
            if (forStmt.getCondition() != null) {
                collectExpressionTypes(forStmt.getCondition(), nextDepth);
            }
            if (forStmt.getUpdate() != null) {
                collectExpressionTypes(forStmt.getUpdate(), nextDepth);
            }
            collectStatementTypes(forStmt.getBody(), nextDepth);
        } else if (statement instanceof IThrowStatement) {
            collectExpressionTypes(((IThrowStatement) statement).getExpression(), nextDepth);
        } else if (statement instanceof ITryCatchStatement) {
            ITryCatchStatement tryStmt = (ITryCatchStatement) statement;
            for (IVariableDeclarationStatement res : tryStmt.getResources()) {
                collectTypeName(res.getVariableType());
                if (res.getInitializer() != null) {
                    collectExpressionTypes(res.getInitializer(), nextDepth);
                }
            }
            collectStatementTypes(tryStmt.getTryBody(), nextDepth);
            for (ICatchClause catchClause : tryStmt.getCatchClauses()) {
                for (IType exType : catchClause.getExceptionTypes()) {
                    collectTypeName(exType);
                }
                collectStatementTypes(catchClause.getBody(), nextDepth);
            }
            if (tryStmt.getFinallyBody() != null) {
                collectStatementTypes(tryStmt.getFinallyBody(), nextDepth);
            }
        }
    }


    /**
     * Collect type references from an expression recursively.
     *
     * @param expression the expression
     */
    private void collectExpressionTypes(IExpression expression) {
        collectExpressionTypes(expression, 0);
    }


    /**
     * Collect type references from an expression recursively with depth tracking.
     *
     * @param expression the expression
     * @param depth      the current recursion depth
     */
    private void collectExpressionTypes(IExpression expression, int depth) {
        if (expression == null || depth > MAX_COLLECT_DEPTH) {
            return;
        }
        int nextDepth = depth + 1;
        if (expression instanceof INewExpression) {
            collectTypeName(((INewExpression) expression).getInstantiatedType());
            for (IExpression arg : ((INewExpression) expression).getArguments()) {
                collectExpressionTypes(arg, nextDepth);
            }
        } else if (expression instanceof ICastExpression) {
            collectTypeName(((ICastExpression) expression).getCastType());
            collectExpressionTypes(((ICastExpression) expression).getOperand(), nextDepth);
        } else if (expression instanceof IInstanceOfExpression) {
            collectTypeName(((IInstanceOfExpression) expression).getCheckType());
            collectExpressionTypes(((IInstanceOfExpression) expression).getOperand(), nextDepth);
        } else if (expression instanceof IBinaryExpression) {
            collectExpressionTypes(((IBinaryExpression) expression).getLeft(), nextDepth);
            collectExpressionTypes(((IBinaryExpression) expression).getRight(), nextDepth);
        } else if (expression instanceof IUnaryExpression) {
            collectExpressionTypes(((IUnaryExpression) expression).getOperand(), nextDepth);
        } else if (expression instanceof IMethodCallExpression) {
            IMethodCallExpression call = (IMethodCallExpression) expression;
            if (call.getTarget() != null) {
                collectExpressionTypes(call.getTarget(), nextDepth);
            }
            for (IExpression arg : call.getArguments()) {
                collectExpressionTypes(arg, nextDepth);
            }
        } else if (expression instanceof IAssignmentExpression) {
            collectExpressionTypes(((IAssignmentExpression) expression).getTarget(), nextDepth);
            collectExpressionTypes(((IAssignmentExpression) expression).getValue(), nextDepth);
        } else if (expression instanceof ILambdaExpression) {
            ILambdaExpression lambda = (ILambdaExpression) expression;
            for (IExpression arg : lambda.getCapturedArguments()) {
                collectExpressionTypes(arg, nextDepth);
            }
            if (lambda.getBody() != null) {
                collectExpressionTypes(lambda.getBody(), nextDepth);
            }
        } else if (expression instanceof IMethodReferenceExpression) {
            IMethodReferenceExpression methodRef = (IMethodReferenceExpression) expression;
            if (methodRef.getQualifier() != null) {
                collectExpressionTypes(methodRef.getQualifier(), nextDepth);
            }
        }
    }


    /**
     * Register a type name for import collection without emitting any output.
     *
     * @param type the type to register
     */
    private void collectTypeName(IType type) {
        if (type == null || type.isPrimitive()) {
            return;
        }
        String name = type.getDisplayName();
        if (name.contains(DOT) && !name.startsWith(JAVA_LANG_PREFIX)) {
            importedTypes.add(name);
        }
    }


    /**
     * Visit a class declaration and emit its source representation.
     *
     * @param declaration the class declaration
     */
    @Override
    public void visitClassDeclaration(IClassDeclaration declaration) {
        if (!declaration.getAnnotations().isEmpty()) {
            emitAnnotations(declaration.getAnnotations());
        }
        boolean isEnum = declaration.getAccessFlags().isEnum();
        boolean isSealed = !declaration.getPermittedSubclasses().isEmpty();
        emitAccessModifiers(declaration.getAccessFlags(), isSealed, declaration.isNonSealed());
        if (declaration.getAccessFlags().isInterface()) {
            buffer.append("interface ");
        } else if (isEnum) {
            buffer.append("enum ");
        } else {
            buffer.append("class ");
        }
        buffer.append(declaration.getName());

        if (declaration.getSuperType() != null && !isEnum) {
            buffer.append(" extends ").append(typeName(declaration.getSuperType()));
        }
        if (!declaration.getInterfaceTypes().isEmpty()) {
            if (declaration.getAccessFlags().isInterface()) {
                buffer.append(" extends ");
            } else {
                buffer.append(" implements ");
            }
            emitTypeList(declaration.getInterfaceTypes());
        }

        if (isSealed) {
            buffer.append(" permits ");
            List<String> permitted = declaration.getPermittedSubclasses();
            for (int i = 0; i < permitted.size(); i++) {
                if (i > 0) {
                    buffer.append(COMMA_SEPARATOR);
                }
                buffer.append(permitted.get(i));
            }
        }

        buffer.append(" {").append(lineSeparator);
        indentLevel++;

        if (isEnum) {
            emitEnumBody(declaration);
        } else {
            emitClassBody(declaration);
        }

        indentLevel--;
        emitIndent();
        buffer.append('}').append(lineSeparator);
    }


    /**
     * Emit the body of an enum class, separating enum constants from regular fields and methods.
     *
     * @param declaration the enum class declaration
     */
    private void emitEnumBody(IClassDeclaration declaration) {
        List<IFieldDeclaration> enumConstants = new ArrayList<>();
        List<IFieldDeclaration> regularFields = new ArrayList<>();
        for (IFieldDeclaration field : declaration.getFields()) {
            if (isEnumConstant(field, declaration.getName())) {
                enumConstants.add(field);
            } else {
                regularFields.add(field);
            }
        }

        // Emit enum constants
        for (int i = 0; i < enumConstants.size(); i++) {
            emitIndent();
            buffer.append(enumConstants.get(i).getName());
            emitEnumConstantArgs(enumConstants.get(i));
            if (i < enumConstants.size() - 1) {
                buffer.append(',');
            } else {
                buffer.append(';');
            }
            buffer.append(lineSeparator);
        }
        if (!enumConstants.isEmpty()
                && (!regularFields.isEmpty() || !declaration.getMethods().isEmpty())) {
            buffer.append(lineSeparator);
        }

        // Emit regular fields
        for (IFieldDeclaration field : regularFields) {
            field.accept(this);
            newLine();
        }
        if (!regularFields.isEmpty() && !declaration.getMethods().isEmpty()) {
            newLine();
        }

        // Emit methods, showing user-defined constructors with custom parameters
        boolean firstMethod = true;
        for (IMethodDeclaration method : declaration.getMethods()) {
            if (method.isConstructor() && method.getParameters().isEmpty()) {
                continue;
            }
            if (!firstMethod) {
                newLine();
            }
            method.accept(this);
            newLine();
            firstMethod = false;
        }
        emitInnerClasses(declaration);
    }


    /**
     * Emit the body of a regular (non-enum) class.
     *
     * @param declaration the class declaration
     */
    private void emitClassBody(IClassDeclaration declaration) {
        for (IFieldDeclaration field : declaration.getFields()) {
            field.accept(this);
            newLine();
        }
        if (!declaration.getFields().isEmpty() && !declaration.getMethods().isEmpty()) {
            newLine();
        }
        for (int i = 0; i < declaration.getMethods().size(); i++) {
            if (i > 0) {
                newLine();
            }
            declaration.getMethods().get(i).accept(this);
            newLine();
        }
        emitInnerClasses(declaration);
    }


    /**
     * Emit inner class declarations nested inside the given class.
     *
     * @param declaration the outer class declaration
     */
    private void emitInnerClasses(IClassDeclaration declaration) {
        for (IClassDeclaration innerClass : declaration.getInnerClasses()) {
            buffer.append(lineSeparator);
            innerClass.accept(this);
        }
    }


    /**
     * Check whether a field declaration represents an enum constant.
     *
     * @param field     the field declaration
     * @param className the enum class name
     * @return true if the field is a public static final field whose type matches the enum class
     */
    private boolean isEnumConstant(IFieldDeclaration field, String className) {
        if (!field.getAccessFlags().isStatic() || !field.getAccessFlags().isFinal()) {
            return false;
        }
        if (!field.getAccessFlags().isPublic()) {
            return false;
        }
        String typeName = field.getFieldType().getDisplayName();
        int lastDot = typeName.lastIndexOf('.');
        if (lastDot >= 0) {
            typeName = typeName.substring(lastDot + 1);
        }
        return className.equals(typeName);
    }


    /**
     * Emit constructor arguments for an enum constant if present. The field's initializer
     * is a {@code INewExpression} whose arguments are the user-defined constructor arguments.
     *
     * @param field the enum constant field declaration
     */
    private void emitEnumConstantArgs(IFieldDeclaration field) {
        if (field.getInitializer() == null) {
            return;
        }
        if (!(field.getInitializer() instanceof INewExpression)) {
            return;
        }
        INewExpression newExpr = (INewExpression) field.getInitializer();
        List<IExpression> args = newExpr.getArguments();
        if (args.isEmpty()) {
            return;
        }
        buffer.append('(');
        for (int ai = 0; ai < args.size(); ai++) {
            if (ai > 0) {
                buffer.append(COMMA_SEPARATOR);
            }
            emitExpression(args.get(ai));
        }
        buffer.append(')');
    }


    /**
     * Visit a method declaration and emit its source representation.
     *
     * @param declaration the method declaration
     */
    @Override
    public void visitMethodDeclaration(IMethodDeclaration declaration) {
        if (!declaration.getAnnotations().isEmpty()) {
            emitAnnotations(declaration.getAnnotations());
        }
        emitIndent();
        emitAccessModifiers(declaration.getAccessFlags());

        if (declaration.isConstructor()) {
            // Constructor: no return type, name is class name
            buffer.append(declaration.getName());
        } else {
            if (declaration.getReturnType() != null) {
                buffer.append(typeName(declaration.getReturnType())).append(' ');
            } else {
                buffer.append("void ");
            }
            buffer.append(declaration.getName());
        }

        buffer.append('(');
        List<IParameterDeclaration> params = declaration.getParameters();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                buffer.append(COMMA_SEPARATOR);
            }
            IParameterDeclaration param = params.get(i);
            if (!param.getAnnotations().isEmpty()) {
                emitInlineAnnotations(param.getAnnotations());
            }
            if (param.isFinal()) {
                buffer.append("final ");
            }
            buffer.append(typeName(param.getParameterType()));
            buffer.append(' ').append(param.getName());
        }
        buffer.append(')');

        if (!declaration.getThrownTypes().isEmpty()) {
            buffer.append(" throws ");
            emitTypeList(declaration.getThrownTypes());
        }

        if (declaration.getBody() != null) {
            buffer.append(" {").append(lineSeparator);
            indentLevel++;
            emitStatementBody(declaration.getBody());
            indentLevel--;
            emitIndent();
            buffer.append('}').append(lineSeparator);
        } else {
            buffer.append(';').append(lineSeparator);
        }
    }


    /**
     * Visit a field declaration and emit its source representation.
     *
     * @param declaration the field declaration
     */
    @Override
    public void visitFieldDeclaration(IFieldDeclaration declaration) {
        if (!declaration.getAnnotations().isEmpty()) {
            emitAnnotations(declaration.getAnnotations());
        }
        emitIndent();
        emitAccessModifiers(declaration.getAccessFlags());
        buffer.append(typeName(declaration.getFieldType()));
        buffer.append(' ').append(declaration.getName());
        if (declaration.getInitializer() != null) {
            buffer.append(EQUALS_SPACED);
            emitExpression(declaration.getInitializer());
        }
        buffer.append(';').append(lineSeparator);
    }


    /**
     * Visit a block statement and emit each contained statement.
     *
     * @param statement the block statement
     */
    @Override
    public void visitBlockStatement(IBlockStatement statement) {
        emissionDepth++;
        if (emissionDepth > MAX_EMISSION_DEPTH) {
            emitIndent();
            buffer.append("// emission depth limit reached").append(lineSeparator);
            emissionDepth--;
            return;
        }
        for (IStatement stmt : statement.getStatements()) {
            stmt.accept(this);
        }
        emissionDepth--;
    }


    /**
     * Visit an expression statement and emit the expression followed by a semicolon.
     *
     * @param statement the expression statement
     */
    @Override
    public void visitExpressionStatement(IExpressionStatement statement) {
        emitIndent();
        emitExpression(statement.getExpression());
        buffer.append(';').append(lineSeparator);
    }


    /**
     * Visit a return statement and emit the return keyword with optional value.
     *
     * @param statement the return statement
     */
    @Override
    public void visitReturnStatement(IReturnStatement statement) {
        emitIndent();
        buffer.append("return");
        if (statement.getValue() != null) {
            buffer.append(' ');
            emitExpression(statement.getValue());
        }
        buffer.append(';').append(lineSeparator);
    }


    /**
     * Visit an if statement and emit the condition, then branch, and optional else branch.
     *
     * @param statement the if statement
     */
    @Override
    public void visitIfStatement(IIfStatement statement) {
        emitIfStatement(statement, false);
    }


    /**
     * Emit an if statement, handling else-if chains by detecting when the else branch
     * contains a single if statement.
     *
     * @param statement the if statement
     * @param isElseIf  true if this is part of an else-if chain (suppress leading indent)
     */
    private void emitIfStatement(IIfStatement statement, boolean isElseIf) {
        if (!isElseIf) {
            emitIndent();
        }
        buffer.append("if (");
        emitExpression(statement.getCondition());
        buffer.append(OPEN_BRACE).append(lineSeparator);
        indentLevel++;
        emitStatementBody(statement.getThenBranch());
        indentLevel--;
        if (statement.getElseBranch() != null) {
            IStatement unwrapped = unwrapSingleStatement(statement.getElseBranch());
            if (unwrapped instanceof IIfStatement) {
                emitIndent();
                buffer.append("} else ");
                emitIfStatement((IIfStatement) unwrapped, true);
                return;
            }
            emitIndent();
            buffer.append("} else {").append(lineSeparator);
            indentLevel++;
            emitStatementBody(statement.getElseBranch());
            indentLevel--;
        }
        emitIndent();
        buffer.append('}').append(lineSeparator);
    }


    /**
     * Unwrap a block statement containing a single statement, returning the inner statement.
     * Returns the original statement if it is not a single-statement block.
     *
     * @param statement the statement to unwrap
     * @return the inner statement or the original
     */
    private IStatement unwrapSingleStatement(IStatement statement) {
        if (statement instanceof IBlockStatement) {
            List<IStatement> stmts = ((IBlockStatement) statement).getStatements();
            if (stmts.size() == 1) {
                return stmts.get(0);
            }
        }
        return statement;
    }


    /**
     * Visit a while statement and emit the loop condition and body.
     *
     * @param statement the while statement
     */
    @Override
    public void visitWhileStatement(IWhileStatement statement) {
        emitIndent();
        buffer.append("while (");
        emitExpression(statement.getCondition());
        buffer.append(OPEN_BRACE).append(lineSeparator);
        indentLevel++;
        emitStatementBody(statement.getBody());
        indentLevel--;
        emitIndent();
        buffer.append('}').append(lineSeparator);
    }


    /**
     * Visit a do-while statement and emit the body followed by the loop condition.
     *
     * @param statement the do-while statement
     */
    @Override
    public void visitDoWhileStatement(IDoWhileStatement statement) {
        emitIndent();
        buffer.append("do {").append(lineSeparator);
        indentLevel++;
        emitStatementBody(statement.getBody());
        indentLevel--;
        emitIndent();
        buffer.append("} while (");
        emitExpression(statement.getCondition());
        buffer.append(");").append(lineSeparator);
    }


    /**
     * Visit a for statement and emit the init, condition, update, and body.
     *
     * @param statement the for statement
     */
    @Override
    public void visitForStatement(IForStatement statement) {
        emitIndent();
        buffer.append("for (");
        if (statement.getInit() != null) {
            emitInlineStatement(statement.getInit());
        }
        buffer.append("; ");
        if (statement.getCondition() != null) {
            emitExpression(statement.getCondition());
        }
        buffer.append("; ");
        if (statement.getUpdate() != null) {
            emitExpression(statement.getUpdate());
        }
        buffer.append(OPEN_BRACE).append(lineSeparator);
        indentLevel++;
        emitStatementBody(statement.getBody());
        indentLevel--;
        emitIndent();
        buffer.append('}').append(lineSeparator);
    }


    /**
     * Visit a for-each statement and emit the element variable, iterable, and body.
     *
     * @param statement the for-each statement
     */
    @Override
    public void visitForEachStatement(IForEachStatement statement) {
        emitIndent();
        buffer.append("for (");
        buffer.append(typeName(statement.getVariableType()));
        buffer.append(' ').append(statement.getVariableName()).append(" : ");
        emitExpression(statement.getIterable());
        buffer.append(OPEN_BRACE).append(lineSeparator);
        indentLevel++;
        emitStatementBody(statement.getBody());
        indentLevel--;
        emitIndent();
        buffer.append('}').append(lineSeparator);
    }


    /**
     * Visit a switch statement and emit the selector and case branches.
     *
     * @param statement the switch statement
     */
    @Override
    public void visitSwitchStatement(ISwitchStatement statement) {
        emitIndent();
        buffer.append("switch (");
        emitExpression(statement.getSelector());
        buffer.append(OPEN_BRACE).append(lineSeparator);
        indentLevel++;
        for (ISwitchCase switchCase : statement.getCases()) {
            emitSwitchCase(switchCase);
        }
        indentLevel--;
        emitIndent();
        buffer.append('}').append(lineSeparator);
    }


    /**
     * Visit a try-catch statement and emit the try body, catch clauses, and optional finally.
     *
     * @param statement the try-catch statement
     */
    @Override
    public void visitTryCatchStatement(ITryCatchStatement statement) {
        emitIndent();
        if (statement.getResources().isEmpty()) {
            buffer.append("try {").append(lineSeparator);
        } else {
            buffer.append("try (");
            List<IVariableDeclarationStatement> resources = statement.getResources();
            for (int i = 0; i < resources.size(); i++) {
                if (i > 0) {
                    buffer.append("; ");
                }
                IVariableDeclarationStatement res = resources.get(i);
                buffer.append(typeName(res.getVariableType()));
                buffer.append(' ').append(res.getVariableName());
                if (res.getInitializer() != null) {
                    buffer.append(EQUALS_SPACED);
                    emitExpression(res.getInitializer());
                }
            }
            buffer.append(OPEN_BRACE).append(lineSeparator);
        }
        indentLevel++;
        emitStatementBody(statement.getTryBody());
        indentLevel--;

        for (ICatchClause catchClause : statement.getCatchClauses()) {
            emitIndent();
            buffer.append("} catch (");
            emitTypeList(catchClause.getExceptionTypes());
            buffer.append(' ').append(catchClause.getVariableName());
            buffer.append(OPEN_BRACE).append(lineSeparator);
            indentLevel++;
            emitStatementBody(catchClause.getBody());
            indentLevel--;
        }

        if (statement.getFinallyBody() != null) {
            emitIndent();
            buffer.append("} finally {").append(lineSeparator);
            indentLevel++;
            emitStatementBody(statement.getFinallyBody());
            indentLevel--;
        }

        emitIndent();
        buffer.append('}').append(lineSeparator);
    }


    /**
     * Visit a throw statement and emit the throw keyword with the exception expression.
     *
     * @param statement the throw statement
     */
    @Override
    public void visitThrowStatement(IThrowStatement statement) {
        emitIndent();
        buffer.append("throw ");
        emitExpression(statement.getExpression());
        buffer.append(';').append(lineSeparator);
    }


    /**
     * Visit an assert statement and emit the assert keyword with condition and optional message.
     *
     * @param statement the assert statement
     */
    @Override
    public void visitAssertStatement(IAssertStatement statement) {
        emitIndent();
        buffer.append("assert ");
        emitExpression(statement.getCondition());
        if (statement.getMessage() != null) {
            buffer.append(" : ");
            emitExpression(statement.getMessage());
        }
        buffer.append(';').append(lineSeparator);
    }


    /**
     * Visit a break statement and emit the break keyword with optional target label.
     *
     * @param statement the break statement
     */
    @Override
    public void visitBreakStatement(IBreakStatement statement) {
        emitIndent();
        buffer.append("break");
        if (statement.getTargetLabel() != null) {
            buffer.append(' ').append(statement.getTargetLabel());
        }
        buffer.append(';').append(lineSeparator);
    }


    /**
     * Visit a continue statement and emit the continue keyword with optional target label.
     *
     * @param statement the continue statement
     */
    @Override
    public void visitContinueStatement(IContinueStatement statement) {
        emitIndent();
        buffer.append("continue");
        if (statement.getTargetLabel() != null) {
            buffer.append(' ').append(statement.getTargetLabel());
        }
        buffer.append(';').append(lineSeparator);
    }


    /**
     * Visit a variable declaration statement and emit type, name, and optional initializer.
     *
     * @param statement the variable declaration statement
     */
    @Override
    public void visitVariableDeclarationStatement(IVariableDeclarationStatement statement) {
        emitIndent();
        if (statement.isFinal()) {
            buffer.append("final ");
        }
        buffer.append(typeName(statement.getVariableType()));
        buffer.append(' ').append(statement.getVariableName());
        if (statement.getInitializer() != null) {
            buffer.append(EQUALS_SPACED);
            emitExpression(statement.getInitializer());
        }
        buffer.append(';').append(lineSeparator);
    }


    /**
     * Visit a synchronized statement and emit the lock expression and body.
     *
     * @param statement the synchronized statement
     */
    @Override
    public void visitSynchronizedStatement(ISynchronizedStatement statement) {
        emitIndent();
        buffer.append("synchronized (");
        emitExpression(statement.getLock());
        buffer.append(OPEN_BRACE).append(lineSeparator);
        indentLevel++;
        emitStatementBody(statement.getBody());
        indentLevel--;
        emitIndent();
        buffer.append('}').append(lineSeparator);
    }


    /**
     * Visit a constant expression and emit its literal value.
     *
     * @param expression the constant expression
     */
    @Override
    public void visitConstantExpression(IConstantExpression expression) {
        Object value = expression.getValue();
        if (value == null) {
            buffer.append("null");
        } else if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.contains("\n")) {
                emitTextBlock(strValue);
            } else {
                buffer.append('"').append(escapeString(strValue)).append('"');
            }
        } else if (value instanceof Character) {
            buffer.append('\'').append(escapeChar((Character) value)).append('\'');
        } else if (value instanceof Long) {
            buffer.append(value).append('L');
        } else if (value instanceof Float) {
            buffer.append(value).append('f');
        } else if (value instanceof Double) {
            buffer.append(value).append('d');
        } else if (value instanceof Boolean) {
            buffer.append(value);
        } else {
            buffer.append(value);
        }
    }


    /**
     * Visit a variable expression and emit its sanitized name.
     *
     * @param expression the variable expression
     */
    @Override
    public void visitVariableExpression(IVariableExpression expression) {
        buffer.append(sanitizeIdentifier(expression.getName()));
    }


    /**
     * Visit a binary expression and emit the left operand, operator, and right operand.
     *
     * @param expression the binary expression
     */
    @Override
    public void visitBinaryExpression(IBinaryExpression expression) {
        emitBinaryChild(expression.getLeft(), expression.getOperator(), true);
        buffer.append(' ').append(expression.getOperator().getSymbol()).append(' ');
        emitBinaryChild(expression.getRight(), expression.getOperator(), false);
    }


    /**
     * Visit a unary expression and emit the operator and operand in the correct order.
     *
     * @param expression the unary expression
     */
    @Override
    public void visitUnaryExpression(IUnaryExpression expression) {
        String symbol = expression.getOperator().getSymbol();
        String opName = expression.getOperator().name();
        if (opName.startsWith("POST")) {
            emitExpression(expression.getOperand());
            buffer.append(symbol);
        } else {
            buffer.append(symbol);
            emitExpression(expression.getOperand());
        }
    }


    /**
     * Visit a method call expression and emit the target, method name, and arguments.
     *
     * @param expression the method call expression
     */
    @Override
    public void visitMethodCallExpression(IMethodCallExpression expression) {
        if (expression.getTarget() != null) {
            emitExpression(expression.getTarget());
            buffer.append('.');
        }
        buffer.append(expression.getMethodName()).append('(');
        List<IExpression> args = expression.getArguments();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                buffer.append(COMMA_SEPARATOR);
            }
            emitExpression(args.get(i));
        }
        buffer.append(')');
    }


    /**
     * Visit a field access expression and emit the target or owner followed by the field name.
     *
     * @param expression the field access expression
     */
    @Override
    public void visitFieldAccessExpression(IFieldAccessExpression expression) {
        if (expression.getTarget() != null) {
            emitExpression(expression.getTarget());
            buffer.append('.');
        } else {
            String ownerName = expression.getOwner().replace('/', '.');
            ownerName = simplifyClassName(ownerName);
            buffer.append(ownerName).append('.');
        }
        buffer.append(sanitizeIdentifier(expression.getFieldName()));
    }


    /**
     * Visit a cast expression and emit the target type in parentheses followed by the operand.
     *
     * @param expression the cast expression
     */
    @Override
    public void visitCastExpression(ICastExpression expression) {
        buffer.append('(');
        buffer.append(typeName(expression.getCastType()));
        buffer.append(") ");
        emitExpression(expression.getOperand());
    }


    /**
     * Visit an instanceof expression and emit the operand, check type, and optional pattern variable.
     *
     * @param expression the instanceof expression
     */
    @Override
    public void visitInstanceOfExpression(IInstanceOfExpression expression) {
        emitExpression(expression.getOperand());
        buffer.append(" instanceof ");
        buffer.append(typeName(expression.getCheckType()));
        if (expression.getPatternVariable() != null) {
            buffer.append(' ').append(expression.getPatternVariable());
        }
    }


    /**
     * Visit a new expression and emit the object creation with constructor arguments.
     *
     * @param expression the new expression
     */
    @Override
    public void visitNewExpression(INewExpression expression) {
        buffer.append("new ").append(typeName(expression.getInstantiatedType())).append('(');
        List<IExpression> args = expression.getArguments();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                buffer.append(COMMA_SEPARATOR);
            }
            emitExpression(args.get(i));
        }
        buffer.append(')');
    }


    /**
     * Visit a new array expression and emit the array creation with dimensions or initializers.
     *
     * @param expression the new array expression
     */
    @Override
    public void visitNewArrayExpression(INewArrayExpression expression) {
        buffer.append("new ").append(typeName(expression.getElementType()));
        if (!expression.getDimensionSizes().isEmpty()) {
            for (IExpression dim : expression.getDimensionSizes()) {
                buffer.append('[');
                emitExpression(dim);
                buffer.append(']');
            }
        } else if (!expression.getInitializers().isEmpty()) {
            buffer.append("[]{");
            List<IExpression> inits = expression.getInitializers();
            for (int i = 0; i < inits.size(); i++) {
                if (i > 0) {
                    buffer.append(COMMA_SEPARATOR);
                }
                emitExpression(inits.get(i));
            }
            buffer.append('}');
        }
    }


    /**
     * Visit an assignment expression and emit the target, equals sign, and value.
     *
     * @param expression the assignment expression
     */
    @Override
    public void visitAssignmentExpression(IAssignmentExpression expression) {
        if (expression.getValue() instanceof IBinaryExpression) {
            IBinaryExpression binary = (IBinaryExpression) expression.getValue();
            if (isCompoundAssignmentOperator(binary.getOperator())
                    && expression.getTarget().equals(binary.getLeft())) {
                if (isIncrementPattern(binary)) {
                    emitExpression(expression.getTarget());
                    buffer.append("++");
                    return;
                }
                if (isDecrementPattern(binary)) {
                    emitExpression(expression.getTarget());
                    buffer.append("--");
                    return;
                }
                emitExpression(expression.getTarget());
                buffer.append(' ').append(binary.getOperator().getSymbol()).append("= ");
                emitExpression(binary.getRight());
                return;
            }
        }
        emitExpression(expression.getTarget());
        buffer.append(EQUALS_SPACED);
        emitExpression(expression.getValue());
    }


    /**
     * Check whether a binary operator supports compound assignment form.
     *
     * @param op the binary operator
     * @return true if the operator has a compound assignment variant
     */
    private boolean isCompoundAssignmentOperator(BinaryOperator op) {
        if (op == BinaryOperator.ADD || op == BinaryOperator.SUB
                || op == BinaryOperator.MUL || op == BinaryOperator.DIV
                || op == BinaryOperator.REM) {
            return true;
        }
        if (op == BinaryOperator.AND || op == BinaryOperator.OR
                || op == BinaryOperator.XOR) {
            return true;
        }
        if (op == BinaryOperator.SHL || op == BinaryOperator.SHR
                || op == BinaryOperator.USHR) {
            return true;
        }
        return false;
    }


    /**
     * Check whether a binary expression represents an increment pattern (x + 1).
     *
     * @param binary the binary expression
     * @return true if the right operand is the integer constant 1 and the operator is ADD
     */
    private boolean isIncrementPattern(IBinaryExpression binary) {
        if (binary.getOperator() != BinaryOperator.ADD) {
            return false;
        }
        return isIntConstant(binary.getRight(), 1);
    }


    /**
     * Check whether a binary expression represents a decrement pattern (x - 1).
     *
     * @param binary the binary expression
     * @return true if the right operand is the integer constant 1 and the operator is SUB
     */
    private boolean isDecrementPattern(IBinaryExpression binary) {
        if (binary.getOperator() != BinaryOperator.SUB) {
            return false;
        }
        return isIntConstant(binary.getRight(), 1);
    }


    /**
     * Check whether an expression is an integer constant with the given value.
     *
     * @param expr  the expression
     * @param value the expected integer value
     * @return true if the expression is a constant with the given value
     */
    private boolean isIntConstant(IExpression expr, int value) {
        if (expr instanceof IConstantExpression) {
            Object val = ((IConstantExpression) expr).getValue();
            return Integer.valueOf(value).equals(val);
        }
        return false;
    }


    /**
     * Visit an array access expression and emit the array followed by the index in brackets.
     *
     * @param expression the array access expression
     */
    @Override
    public void visitArrayAccessExpression(IArrayAccessExpression expression) {
        emitExpression(expression.getArray());
        buffer.append('[');
        emitExpression(expression.getIndex());
        buffer.append(']');
    }


    /**
     * Visit a lambda expression and emit it in the form {@code (args) -> body} or
     * {@code (args) -> { ... }}.
     *
     * @param expression the lambda expression
     */
    @Override
    public void visitLambdaExpression(ILambdaExpression expression) {
        List<String> paramNames = expression.getParameterNames();
        if (paramNames.size() == 1) {
            buffer.append(paramNames.get(0));
        } else {
            buffer.append('(');
            for (int i = 0; i < paramNames.size(); i++) {
                if (i > 0) {
                    buffer.append(COMMA_SEPARATOR);
                }
                buffer.append(paramNames.get(i));
            }
            buffer.append(')');
        }
        buffer.append(" -> ");
        if (expression.getBody() != null) {
            emitExpression(expression.getBody());
        } else {
            buffer.append("{ /* ").append(expression.getImplementationMethodName()).append(" */ }");
        }
    }


    /**
     * Visit a method reference expression and emit it in the form {@code Owner::method}
     * or {@code qualifier::method}.
     *
     * @param expression the method reference expression
     */
    @Override
    public void visitMethodReferenceExpression(IMethodReferenceExpression expression) {
        if (expression.getQualifier() != null) {
            emitExpression(expression.getQualifier());
        } else {
            String ownerName = expression.getOwner().replace('/', '.');
            ownerName = simplifyClassName(ownerName);
            buffer.append(ownerName);
        }
        buffer.append("::");
        if (expression.isConstructorReference()) {
            buffer.append("new");
        } else {
            buffer.append(expression.getMethodName());
        }
    }


    /**
     * Visit a switch expression and emit it with arrow syntax
     * (e.g., switch (x) { case 1 -> "one"; default -> "other"; }).
     *
     * @param expression the switch expression
     */
    @Override
    public void visitSwitchExpression(ISwitchExpression expression) {
        buffer.append("switch (");
        emitExpression(expression.getSelector());
        buffer.append(") {").append(lineSeparator);
        indentLevel++;
        for (ISwitchExpressionCase switchCase : expression.getCases()) {
            emitIndent();
            if (switchCase.isDefault()) {
                buffer.append("default -> ");
            } else {
                buffer.append("case ");
                List<IExpression> values = switchCase.getValues();
                for (int i = 0; i < values.size(); i++) {
                    if (i > 0) {
                        buffer.append(", ");
                    }
                    emitExpression(values.get(i));
                }
                buffer.append(" -> ");
            }
            emitExpression(switchCase.getResultExpression());
            buffer.append(';').append(lineSeparator);
        }
        indentLevel--;
        emitIndent();
        buffer.append('}');
    }


    /**
     * Emit an expression by dispatching to the visitor.
     *
     * @param expression the expression to emit
     */
    private void emitExpression(IExpression expression) {
        if (expression == null) {
            buffer.append("/* null */");
            return;
        }
        expression.accept(this);
    }


    /**
     * Emit a child of a binary expression, adding parentheses only when needed
     * for correct operator precedence.
     *
     * @param child    the child expression
     * @param parentOp the parent binary operator
     * @param isLeft   true if this is the left child
     */
    private void emitBinaryChild(IExpression child, BinaryOperator parentOp, boolean isLeft) {
        if (child instanceof IBinaryExpression) {
            IBinaryExpression childBin = (IBinaryExpression) child;
            if (needsParentheses(childBin.getOperator(), parentOp, isLeft)) {
                buffer.append('(');
                emitExpression(child);
                buffer.append(')');
                return;
            }
        }
        emitExpression(child);
    }


    /**
     * Determine whether a child binary expression needs parentheses inside
     * a parent binary expression based on Java operator precedence.
     *
     * @param childOp  the child operator
     * @param parentOp the parent operator
     * @param isLeft   true if the child is on the left side
     * @return true if parentheses are needed
     */
    private boolean needsParentheses(BinaryOperator childOp, BinaryOperator parentOp, boolean isLeft) {
        int childPrec = getOperatorPrecedence(childOp);
        int parentPrec = getOperatorPrecedence(parentOp);
        if (childPrec < parentPrec) {
            return true;
        }
        if (childPrec == parentPrec && !isLeft) {
            return isNonAssociative(parentOp);
        }
        return false;
    }


    /**
     * Check whether a binary operator is non-associative, meaning the right
     * child at the same precedence level needs parentheses.
     *
     * @param op the operator
     * @return true if non-associative
     */
    private boolean isNonAssociative(BinaryOperator op) {
        switch (op) {
            case SUB:
            case DIV:
            case REM:
            case SHL:
            case SHR:
            case USHR:
                return true;
            default:
                return false;
        }
    }


    /**
     * Get the Java operator precedence for a binary operator.
     * Higher values bind tighter.
     *
     * @param op the operator
     * @return the precedence level
     */
    private int getOperatorPrecedence(BinaryOperator op) {
        switch (op) {
            case LOGICAL_OR:
                return 1;
            case LOGICAL_AND:
                return 2;
            case OR:
                return 3;
            case XOR:
                return 4;
            case AND:
                return 5;
            case EQ:
            case NE:
                return 6;
            case LT:
            case GT:
            case LE:
            case GE:
                return 7;
            case SHL:
            case SHR:
            case USHR:
                return 8;
            case ADD:
            case SUB:
                return 9;
            case MUL:
            case DIV:
            case REM:
                return 10;
            default:
                return 0;
        }
    }


    /**
     * Emit a statement body (handles block statements transparently).
     *
     * @param statement the body statement
     */
    private void emitStatementBody(IStatement statement) {
        if (statement instanceof IBlockStatement) {
            for (IStatement stmt : ((IBlockStatement) statement).getStatements()) {
                stmt.accept(this);
            }
        } else {
            statement.accept(this);
        }
    }


    /**
     * Emit a statement inline (without newline, for for-loop init).
     *
     * @param statement the statement
     */
    private void emitInlineStatement(IStatement statement) {
        if (statement instanceof IVariableDeclarationStatement) {
            IVariableDeclarationStatement varDecl = (IVariableDeclarationStatement) statement;
            if (varDecl.isFinal()) {
                buffer.append("final ");
            }
            buffer.append(typeName(varDecl.getVariableType()));
            buffer.append(' ').append(varDecl.getVariableName());
            if (varDecl.getInitializer() != null) {
                buffer.append(EQUALS_SPACED);
                emitExpression(varDecl.getInitializer());
            }
        } else if (statement instanceof IExpressionStatement) {
            emitExpression(((IExpressionStatement) statement).getExpression());
        }
    }


    /**
     * Emit a switch case.
     *
     * @param switchCase the switch case
     */
    private void emitSwitchCase(ISwitchCase switchCase) {
        emitIndent();
        if (switchCase.isDefault()) {
            buffer.append("default:").append(lineSeparator);
        } else {
            for (IExpression value : switchCase.getValues()) {
                buffer.append("case ");
                emitExpression(value);
                buffer.append(':').append(lineSeparator);
            }
        }
        indentLevel++;
        for (IStatement stmt : switchCase.getStatements()) {
            stmt.accept(this);
        }
        indentLevel--;
    }


    /**
     * Emit access modifiers for fields and methods (not classes).
     *
     * @param flags the access flags
     */
    private void emitAccessModifiers(IAccessFlags flags) {
        emitAccessModifiers(flags, false, false);
    }


    /**
     * Emit access modifiers with sealed/non-sealed class support.
     *
     * @param flags     the access flags
     * @param isSealed  true if the class is sealed (has permitted subclasses)
     * @param nonSealed true if the class is non-sealed
     */
    private void emitAccessModifiers(IAccessFlags flags, boolean isSealed, boolean nonSealed) {
        if (flags.isPublic()) {
            buffer.append("public ");
        } else if (flags.isProtected()) {
            buffer.append("protected ");
        } else if (flags.isPrivate()) {
            buffer.append("private ");
        }
        if (flags.isStatic() && !flags.isEnum()) {
            buffer.append("static ");
        }
        if (flags.isAbstract() && !flags.isInterface() && !flags.isEnum() && !isSealed) {
            buffer.append("abstract ");
        }
        if (isSealed) {
            buffer.append("sealed ");
        } else if (nonSealed) {
            buffer.append("non-sealed ");
        }
        if (flags.isFinal() && !flags.isEnum() && !isSealed) {
            buffer.append("final ");
        }
    }


    /**
     * Emit annotations on separate lines, each preceded by the current indent.
     *
     * @param annotations the annotations to emit
     */
    private void emitAnnotations(List<IAnnotation> annotations) {
        for (IAnnotation annotation : annotations) {
            emitIndent();
            emitAnnotation(annotation);
            buffer.append(lineSeparator);
        }
    }


    /**
     * Emit annotations inline (for parameters), separated by spaces.
     *
     * @param annotations the annotations to emit
     */
    private void emitInlineAnnotations(List<IAnnotation> annotations) {
        for (IAnnotation annotation : annotations) {
            emitAnnotation(annotation);
            buffer.append(' ');
        }
    }


    /**
     * Emit a single annotation with its element-value pairs.
     *
     * @param annotation the annotation to emit
     */
    private void emitAnnotation(IAnnotation annotation) {
        buffer.append('@');
        buffer.append(annotationTypeName(annotation.getDescriptor()));
        Map<String, Object> values = annotation.getValues();
        if (values != null && !values.isEmpty()) {
            buffer.append('(');
            if (values.size() == 1 && values.containsKey("value")) {
                emitAnnotationValue(values.get("value"));
            } else {
                boolean first = true;
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    if (!first) {
                        buffer.append(COMMA_SEPARATOR);
                    }
                    first = false;
                    buffer.append(entry.getKey()).append(EQUALS_SPACED);
                    emitAnnotationValue(entry.getValue());
                }
            }
            buffer.append(')');
        }
    }


    /**
     * Emit an annotation element value.
     *
     * @param value the value to emit
     */
    @SuppressWarnings("unchecked")
    private void emitAnnotationValue(Object value) {
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.contains(DOT) && strValue.equals(strValue.toUpperCase())) {
                buffer.append(strValue);
            } else {
                buffer.append('"').append(escapeString(strValue)).append('"');
            }
        } else if (value instanceof Character) {
            buffer.append('\'').append(value).append('\'');
        } else if (value instanceof Long) {
            buffer.append(value).append('L');
        } else if (value instanceof Float) {
            buffer.append(value).append('f');
        } else if (value instanceof Double) {
            buffer.append(value).append('d');
        } else if (value instanceof Boolean || value instanceof Integer
                || value instanceof Short || value instanceof Byte) {
            buffer.append(value);
        } else if (value instanceof IAnnotation) {
            emitAnnotation((IAnnotation) value);
        } else if (value instanceof List) {
            buffer.append('{');
            List<Object> list = (List<Object>) value;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    buffer.append(COMMA_SEPARATOR);
                }
                emitAnnotationValue(list.get(i));
            }
            buffer.append('}');
        } else if (value != null) {
            buffer.append(value);
        }
    }


    /**
     * Convert an annotation type descriptor to its simple display name.
     * For example, "Ljava/lang/Override;" becomes "Override".
     *
     * @param descriptor the annotation type descriptor
     * @return the simple annotation name
     */
    private String annotationTypeName(String descriptor) {
        String internalName = descriptor;
        if (internalName.startsWith("L") && internalName.endsWith(";")) {
            internalName = internalName.substring(1, internalName.length() - 1);
        }
        String dotName = internalName.replace('/', '.');

        if (dotName.contains(DOT) && !dotName.startsWith(JAVA_LANG_PREFIX)) {
            importedTypes.add(dotName);
        }

        if (dotName.startsWith("java.lang.annotation.") || dotName.startsWith(JAVA_LANG_PREFIX)) {
            int lastDot = dotName.lastIndexOf('.');
            return dotName.substring(lastDot + 1);
        }

        if (currentPackage != null && dotName.startsWith(currentPackage + DOT)) {
            String simplified = dotName.substring(currentPackage.length() + 1);
            if (simplified.indexOf('.') < 0) {
                return simplified;
            }
        }

        int lastDot = dotName.lastIndexOf('.');
        if (lastDot >= 0) {
            return dotName.substring(lastDot + 1);
        }
        return dotName;
    }


    /**
     * Emit a comma-separated list of types.
     *
     * @param types the types
     */
    private void emitTypeList(List<IType> types) {
        for (int i = 0; i < types.size(); i++) {
            if (i > 0) {
                buffer.append(COMMA_SEPARATOR);
            }
            buffer.append(typeName(types.get(i)));
        }
    }


    /**
     * Get the simplified type name. Strips java.lang. prefix and current package prefix.
     *
     * @param type the type
     * @return the simplified name
     */
    private String typeName(IType type) {
        if (type == null) {
            return "Object";
        }
        String name = type.getDisplayName();

        // Don't process primitive or array-of-primitive types
        if (type.isPrimitive() || name.indexOf('.') < 0) {
            return name;
        }

        // Collect for imports (full qualified names with dots)
        if (name.contains(DOT) && !name.startsWith(JAVA_LANG_PREFIX)) {
            importedTypes.add(name);
        }

        // Strip java.lang. prefix (always implicit)
        if (name.startsWith(JAVA_LANG_PREFIX) && name.indexOf('.', JAVA_LANG_PREFIX.length()) < 0) {
            return name.substring(JAVA_LANG_PREFIX.length());
        }

        // Strip current package prefix
        if (currentPackage != null && name.startsWith(currentPackage + DOT)) {
            String simplified = name.substring(currentPackage.length() + 1);
            if (simplified.indexOf('.') < 0) {
                return simplified;
            }
        }

        // For imported types, use simple name
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0 && importedTypes.contains(name)) {
            return name.substring(lastDot + 1);
        }

        return name;
    }


    /**
     * Simplify a fully qualified class name by stripping the java.lang. prefix
     * and applying import simplification.
     *
     * @param name the fully qualified class name (dot-separated)
     * @return the simplified name
     */
    private String simplifyClassName(String name) {
        if (name.startsWith(JAVA_LANG_PREFIX) && name.indexOf('.', JAVA_LANG_PREFIX.length()) < 0) {
            return name.substring(JAVA_LANG_PREFIX.length());
        }
        if (currentPackage != null && name.startsWith(currentPackage + DOT)) {
            String simplified = name.substring(currentPackage.length() + 1);
            if (simplified.indexOf('.') < 0) {
                return simplified;
            }
        }
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0 && importedTypes.contains(name)) {
            return name.substring(lastDot + 1);
        }
        return name;
    }


    /**
     * Emit the current indentation.
     */
    private void emitIndent() {
        if (indentLevel < indentCache.length) {
            buffer.append(indentCache[indentLevel]);
        } else {
            if (indentLevel >= indentCache.length) {
                indentCache = buildIndentCache(indent, indentLevel + 16);
            }
            buffer.append(indentCache[indentLevel]);
        }
    }


    /**
     * Build a cache of pre-computed indent strings for each depth level.
     *
     * @param indentStr the single-level indent string
     * @param size      the number of levels to cache
     * @return the indent cache array
     */
    private static String[] buildIndentCache(String indentStr, int size) {
        String[] cache = new String[size];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            cache[i] = sb.toString();
            sb.append(indentStr);
        }
        return cache;
    }


    /**
     * Emit a newline.
     */
    private void newLine() {
        // line separator already appended by statements
    }


    /**
     * Sanitize an identifier for Java source output. JVM bytecode allows characters that are
     * not valid in Java source identifiers. This method prefixes invalid identifiers with
     * a comment marker so the output remains readable.
     *
     * @param name the identifier name
     * @return the sanitized identifier
     */
    private String sanitizeIdentifier(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return "/* non-standard */ _" + name;
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                return "/* non-standard */ " + name;
            }
        }
        return name;
    }


    /**
     * Escape a string literal.
     *
     * @param str the string to escape
     * @return the escaped string
     */
    private String escapeString(String str) {
        boolean needsEscape = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '\\' || ch == '"' || ch == '\'' || ch < 0x20) {
                needsEscape = true;
                break;
            }
        }
        if (!needsEscape) {
            return str;
        }

        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            sb.append(escapeChar(ch));
        }
        return sb.toString();
    }


    /**
     * Escape a single character for a string or char literal.
     *
     * @param ch the character
     * @return the escaped representation
     */
    private String escapeChar(char ch) {
        switch (ch) {
            case '\\': return "\\\\";
            case '"': return "\\\"";
            case '\'': return "\\'";
            case '\n': return "\\n";
            case '\r': return "\\r";
            case '\t': return "\\t";
            case '\b': return "\\b";
            case '\f': return "\\f";
            default:
                if (ch < 0x20) {
                    return "\\u" + String.format("%04X", (int) ch);
                }
                return String.valueOf(ch);
        }
    }


    /**
     * Emit a multi-line string as a Java text block (triple-quoted string).
     * The text block uses the current indentation level for alignment.
     *
     * @param str the string containing newlines
     */
    private void emitTextBlock(String str) {
        buffer.append("\"\"\"").append(lineSeparator);
        String[] lines = str.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            emitIndent();
            String line = lines[i];
            for (int ci = 0; ci < line.length(); ci++) {
                char ch = line.charAt(ci);
                if (ch == '\\') {
                    buffer.append("\\\\");
                } else if (ch == '\r') {
                    buffer.append("\\r");
                } else if (ch == '\t') {
                    buffer.append('\t');
                } else if (ch < 0x20 && ch != '\t') {
                    buffer.append("\\u").append(String.format("%04X", (int) ch));
                } else {
                    buffer.append(ch);
                }
            }
            if (i < lines.length - 1) {
                buffer.append(lineSeparator);
            }
        }
        buffer.append(lineSeparator);
        emitIndent();
        buffer.append("\"\"\"");
    }
}
