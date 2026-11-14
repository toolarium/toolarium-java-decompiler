/*
 * EnumReconstructor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reconstructor.impl;

import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IFieldDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IParameterDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IAssignmentExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IFieldAccessExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IExpressionStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.reconstructor.IReconstructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Detects enum classes and removes compiler-generated methods (values(), valueOf())
 * and the synthetic $VALUES field.
 *
 * @author patrick
 */
public class EnumReconstructor implements IReconstructor {
    private static final Logger LOG = LoggerFactory.getLogger(EnumReconstructor.class);
    private static final String VALUES_FIELD = "$VALUES";


    /**
     * Constructor for EnumReconstructor.
     */
    public EnumReconstructor() {
        // default constructor
    }


    /**
     * Get the name of this reconstructor.
     *
     * @return the name "EnumReconstructor"
     */
    @Override
    public String getName() {
        return "EnumReconstructor";
    }


    /**
     * Check whether this reconstructor applies to the given class.
     * Returns true if the class has the enum access flag.
     *
     * @param structClass the parsed class structure
     * @return true if the class is an enum
     */
    @Override
    public boolean applies(IStructClass structClass) {
        return structClass.getAccessFlags().isEnum();
    }


    /**
     * Apply enum reconstruction by removing compiler-generated methods (values(), valueOf())
     * and the synthetic $VALUES field.
     *
     * @param classDeclaration the original class declaration
     * @param structClass      the parsed class structure for metadata access
     * @return the transformed class declaration
     */
    @Override
    public IClassDeclaration reconstruct(IClassDeclaration classDeclaration, IStructClass structClass) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Reconstructing enum: " + structClass.getName());
        }

        DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();

        // Extract constructor arguments from <clinit> before removing it
        Map<String, List<IExpression>> constructorArgs = extractConstructorArgs(classDeclaration);

        // Filter out synthetic methods: values(), valueOf(String)
        // For enum constructors, strip the first two synthetic parameters (name, ordinal)
        List<IMethodDeclaration> filteredMethods = new ArrayList<>();
        for (IMethodDeclaration method : classDeclaration.getMethods()) {
            if (isEnumGeneratedMethod(method)) {
                continue;
            }
            if (method.isConstructor()) {
                filteredMethods.add(stripEnumConstructorParams(method, declFactory));
            } else {
                filteredMethods.add(method);
            }
        }

        // Filter out $VALUES field and attach constructor args to enum constants
        List<IFieldDeclaration> filteredFields = new ArrayList<>();
        for (IFieldDeclaration field : classDeclaration.getFields()) {
            if (VALUES_FIELD.equals(field.getName())) {
                continue;
            }
            List<IExpression> args = constructorArgs.get(field.getName());
            if (args != null && !args.isEmpty()) {
                IExpression initializer = ExpressionModelFactory.getInstance().getNewExpression(field.getFieldType(), field.getFieldType(), args);
                filteredFields.add(declFactory.getFieldDeclaration(field.getAccessFlags(), field.getFieldType(), field.getName(), initializer, field.getAnnotations()));
            } else {
                filteredFields.add(field);
            }
        }

        return declFactory.getClassDeclaration(
                classDeclaration.getAccessFlags(),
                classDeclaration.getName(),
                classDeclaration.getSuperType(),
                classDeclaration.getInterfaceTypes(),
                filteredFields, filteredMethods,
                classDeclaration.getInnerClasses(),
                classDeclaration.getAnnotations(),
                classDeclaration.getPermittedSubclasses(), classDeclaration.isNonSealed());
    }


    /**
     * Check if a method is a compiler-generated enum method.
     *
     * @param method the method
     * @return true if generated
     */
    private boolean isEnumGeneratedMethod(IMethodDeclaration method) {
        String name = method.getName();
        if ("values".equals(name) && method.getParameters().isEmpty()
                && method.getReturnType() != null && method.getReturnType().isArray()) {
            return true;
        }
        if ("valueOf".equals(name) && method.getParameters().size() == 1) {
            return true;
        }
        // Remove <clinit> for enums: it contains enum constant setup and $VALUES init.
        // Limitation: user-defined static initializer blocks inside enums are also removed.
        if ("<clinit>".equals(name)) {
            return true;
        }
        return false;
    }


    /**
     * Strip the first two synthetic parameters (name, ordinal) from an enum constructor.
     * If the constructor has user-defined parameters beyond the synthetic ones,
     * they are preserved so the constructor can be shown in the output.
     *
     * @param method      the enum constructor declaration
     * @param declFactory the declaration model factory
     * @return the constructor with synthetic parameters removed
     */
    private IMethodDeclaration stripEnumConstructorParams(IMethodDeclaration method,
                                                          DeclarationModelFactory declFactory) {
        List<IParameterDeclaration> params = method.getParameters();
        List<IParameterDeclaration> userParams = new ArrayList<>();
        if (params.size() > 2) {
            userParams.addAll(params.subList(2, params.size()));
        }

        return declFactory.getMethodDeclaration(method.getAccessFlags(), method.getName(),
                method.getReturnType(), userParams, method.getThrownTypes(),
                method.getBody(), method.isConstructor(), method.getAnnotations());
    }


    /**
     * Extract constructor arguments from the {@code <clinit>} method body.
     * The static initializer contains assignments like
     * {@code RED = new EnumName("RED", 0, "FF0000")} where the first two arguments
     * are the synthetic name and ordinal. This method returns the user-defined
     * arguments (after skipping the first two).
     *
     * @param classDeclaration the class declaration containing the clinit method
     * @return a map from enum constant name to its user-defined constructor arguments
     */
    private Map<String, List<IExpression>> extractConstructorArgs(IClassDeclaration classDeclaration) {
        Map<String, List<IExpression>> result = new HashMap<>();

        IMethodDeclaration clinit = null;
        for (IMethodDeclaration method : classDeclaration.getMethods()) {
            if ("<clinit>".equals(method.getName())) {
                clinit = method;
                break;
            }
        }

        if (clinit == null || clinit.getBody() == null) {
            return result;
        }

        List<IStatement> statements = getBodyStatements(clinit.getBody());
        for (IStatement stmt : statements) {
            extractArgsFromStatement(stmt, result);
        }

        return result;
    }


    /**
     * Extract constructor arguments from a single statement in the clinit body.
     * Looks for assignment patterns like {@code FIELD = new EnumType(name, ordinal, args...)}.
     *
     * @param statement the statement to examine
     * @param result    the output map to add extracted arguments to
     */
    private void extractArgsFromStatement(IStatement statement, Map<String, List<IExpression>> result) {
        if (!(statement instanceof IExpressionStatement)) {
            return;
        }

        IExpression expr = ((IExpressionStatement) statement).getExpression();
        if (!(expr instanceof IAssignmentExpression)) {
            return;
        }

        IAssignmentExpression assignment = (IAssignmentExpression) expr;
        if (!(assignment.getTarget() instanceof IFieldAccessExpression)) {
            return;
        }
        if (!(assignment.getValue() instanceof INewExpression)) {
            return;
        }

        String fieldName = ((IFieldAccessExpression) assignment.getTarget()).getFieldName();
        INewExpression newExpr = (INewExpression) assignment.getValue();

        // Skip the first two synthetic arguments (name, ordinal)
        List<IExpression> allArgs = newExpr.getArguments();
        if (allArgs.size() > 2) {
            List<IExpression> userArgs = new ArrayList<>(allArgs.subList(2, allArgs.size()));
            result.put(fieldName, userArgs);
        }
    }


    /**
     * Get the statements from a method body, unwrapping block statements.
     *
     * @param body the method body statement
     * @return the list of statements
     */
    private List<IStatement> getBodyStatements(IStatement body) {
        if (body instanceof IBlockStatement) {
            return ((IBlockStatement) body).getStatements();
        }
        List<IStatement> single = new ArrayList<>();
        single.add(body);
        return single;
    }
}
