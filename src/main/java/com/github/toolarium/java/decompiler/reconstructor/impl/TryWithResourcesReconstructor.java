/*
 * TryWithResourcesReconstructor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reconstructor.impl;

import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodCallExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IVariableExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ICatchClause;
import com.github.toolarium.java.decompiler.model.ast.statement.IExpressionStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IIfStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IThrowStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ITryCatchStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IVariableDeclarationStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.reconstructor.IReconstructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Detects try-with-resources bytecode patterns and transforms them into proper
 * try-with-resources statements. The compiler generates complex exception handling
 * with close() calls in finally blocks for try-with-resources. This reconstructor
 * identifies those patterns and replaces them with clean try-with-resources AST nodes.
 *
 * <p>The compiler translates {@code try (Resource r = expr) { body }} into:
 * <pre>
 * Resource r = expr;
 * try {
 *     body
 * } finally {
 *     r.close();
 * }
 * </pre>
 *
 * <p>Or with exception suppression:
 * <pre>
 * Resource r = expr;
 * Throwable primary = null;
 * try {
 *     body
 * } catch (Throwable t) {
 *     primary = t;
 *     throw t;
 * } finally {
 *     if (primary != null) {
 *         try { r.close(); } catch (Throwable s) { primary.addSuppressed(s); }
 *     } else {
 *         r.close();
 *     }
 * }
 * </pre>
 *
 * @author patrick
 */
public class TryWithResourcesReconstructor implements IReconstructor {
    private static final Logger LOG = LoggerFactory.getLogger(TryWithResourcesReconstructor.class);
    private static final String CLOSE_METHOD = "close";


    /**
     * Constructor for TryWithResourcesReconstructor.
     */
    public TryWithResourcesReconstructor() {
        // default constructor
    }


    /**
     * Get the name of this reconstructor.
     *
     * @return the name "TryWithResourcesReconstructor"
     */
    @Override
    public String getName() {
        return "TryWithResourcesReconstructor";
    }


    /**
     * Check whether this reconstructor applies to the given class.
     * Returns true for all classes since try-with-resources patterns can appear anywhere
     * and detection is performed at the AST level during reconstruction.
     *
     * @param structClass the parsed class structure
     * @return true always
     */
    @Override
    public boolean applies(IStructClass structClass) {
        return true;
    }


    /**
     * Apply try-with-resources reconstruction by detecting the compiler-generated
     * patterns in method bodies and transforming them into try-with-resources statements.
     *
     * @param classDeclaration the original class declaration
     * @param structClass      the parsed class structure for metadata access
     * @return the transformed class declaration
     */
    @Override
    public IClassDeclaration reconstruct(IClassDeclaration classDeclaration, IStructClass structClass) {
        DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();

        List<IMethodDeclaration> transformedMethods = new ArrayList<>();
        boolean changed = false;
        for (IMethodDeclaration method : classDeclaration.getMethods()) {
            IMethodDeclaration transformed = transformMethod(method, declFactory);
            transformedMethods.add(transformed);
            if (transformed != method) {
                changed = true;
            }
        }

        if (!changed) {
            return classDeclaration;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Reconstructed try-with-resources patterns in: " + structClass.getName());
        }

        return declFactory.getClassDeclaration(
                classDeclaration.getAccessFlags(),
                classDeclaration.getName(),
                classDeclaration.getSuperType(),
                classDeclaration.getInterfaceTypes(),
                classDeclaration.getFields(), transformedMethods,
                classDeclaration.getInnerClasses(),
                classDeclaration.getAnnotations(),
                classDeclaration.getPermittedSubclasses(), classDeclaration.isNonSealed());
    }


    /**
     * Transform a method by detecting and replacing try-with-resources patterns in its body.
     *
     * @param method      the original method declaration
     * @param declFactory the declaration model factory
     * @return the transformed method, or the original if no changes were needed
     */
    private IMethodDeclaration transformMethod(IMethodDeclaration method,
                                               DeclarationModelFactory declFactory) {
        if (method.getBody() == null) {
            return method;
        }

        IStatement transformedBody = transformStatement(method.getBody());
        if (transformedBody == method.getBody()) {
            return method;
        }

        return declFactory.getMethodDeclaration(
                method.getAccessFlags(), method.getName(), method.getReturnType(),
                method.getParameters(), method.getThrownTypes(),
                transformedBody, method.isConstructor(), method.getAnnotations());
    }


    /**
     * Transform a statement, recursively processing block and try-catch statements
     * to find and replace try-with-resources patterns.
     *
     * @param statement the statement to transform
     * @return the transformed statement, or the original if no patterns matched
     */
    private IStatement transformStatement(IStatement statement) {
        if (statement instanceof IBlockStatement) {
            return transformBlock((IBlockStatement) statement);
        }
        if (statement instanceof ITryCatchStatement) {
            return transformTryCatch((ITryCatchStatement) statement);
        }
        return statement;
    }


    /**
     * Transform a try-catch statement by recursively processing its body,
     * catch clause bodies, and finally block.
     *
     * @param tryCatch the try-catch statement
     * @return the transformed statement, or the original if no changes
     */
    private IStatement transformTryCatch(ITryCatchStatement tryCatch) {
        IStatement transformedTry = transformStatement(tryCatch.getTryBody());

        List<ICatchClause> transformedCatches = new ArrayList<>();
        boolean catchChanged = false;
        for (ICatchClause clause : tryCatch.getCatchClauses()) {
            IStatement transformedBody = transformStatement(clause.getBody());
            if (transformedBody != clause.getBody()) {
                transformedCatches.add(StatementModelFactory.getInstance().getCatchClause(
                        clause.getExceptionTypes(), clause.getVariableName(), transformedBody));
                catchChanged = true;
            } else {
                transformedCatches.add(clause);
            }
        }

        IStatement transformedFinally = null;
        boolean finallyChanged = false;
        if (tryCatch.getFinallyBody() != null) {
            transformedFinally = transformStatement(tryCatch.getFinallyBody());
            finallyChanged = (transformedFinally != tryCatch.getFinallyBody());
        }

        if (transformedTry == tryCatch.getTryBody() && !catchChanged && !finallyChanged) {
            return tryCatch;
        }

        if (!finallyChanged) {
            transformedFinally = tryCatch.getFinallyBody();
        }

        if (!tryCatch.getResources().isEmpty()) {
            return StatementModelFactory.getInstance().getTryCatchStatement(
                    tryCatch.getLabel(), tryCatch.getResources(),
                    transformedTry, transformedCatches, transformedFinally);
        }
        return StatementModelFactory.getInstance().getTryCatchStatement(
                tryCatch.getLabel(), transformedTry, transformedCatches, transformedFinally);
    }


    /**
     * Transform a block statement by scanning for try-with-resources patterns.
     * The pattern is a resource variable declaration followed by a try-catch
     * whose finally block calls close() on that variable.
     *
     * @param block the block statement
     * @return the transformed block, or the original if no patterns matched
     */
    private IStatement transformBlock(IBlockStatement block) {
        List<IStatement> statements = block.getStatements();
        List<IStatement> result = new ArrayList<>();
        boolean changed = false;

        int i = 0;
        while (i < statements.size()) {
            if (i + 1 < statements.size()
                    && statements.get(i) instanceof IVariableDeclarationStatement) {
                int consumed = tryMatchPattern(statements, i, result);
                if (consumed > 0) {
                    i += consumed;
                    changed = true;
                    continue;
                }
            }

            IStatement transformed = transformStatement(statements.get(i));
            if (transformed != statements.get(i)) {
                changed = true;
            }
            result.add(transformed);
            i++;
        }

        if (!changed) {
            return block;
        }

        return StatementModelFactory.getInstance().getBlockStatement(block.getLabel(), result);
    }


    /**
     * Try to match a try-with-resources pattern starting at the given index.
     * Looks for a resource variable declaration (with initializer) followed by
     * an optional Throwable variable and a try-catch statement whose finally block
     * contains a close() call on the resource variable.
     *
     * @param statements the list of statements to scan
     * @param startIndex the index of the potential resource declaration
     * @param result     the output list to add the transformed statement to
     * @return the number of statements consumed (0 if no match)
     */
    private int tryMatchPattern(List<IStatement> statements, int startIndex, List<IStatement> result) {
        IVariableDeclarationStatement resourceDecl = (IVariableDeclarationStatement) statements.get(startIndex);

        if (resourceDecl.getInitializer() == null) {
            return 0;
        }

        final String resourceVar = resourceDecl.getVariableName();

        int tryIndex = startIndex + 1;
        boolean hasThrowableDecl = false;
        if (tryIndex < statements.size() && statements.get(tryIndex) instanceof IVariableDeclarationStatement) {
            IVariableDeclarationStatement secondDecl = (IVariableDeclarationStatement) statements.get(tryIndex);
            if (isThrowableType(secondDecl.getVariableType().getDisplayName())) {
                hasThrowableDecl = true;
                tryIndex++;
            }
        }

        if (tryIndex >= statements.size()) {
            return 0;
        }
        if (!(statements.get(tryIndex) instanceof ITryCatchStatement)) {
            return 0;
        }
        ITryCatchStatement tryCatch = (ITryCatchStatement) statements.get(tryIndex);

        if (tryCatch.getFinallyBody() == null) {
            return 0;
        }
        if (!containsCloseCall(tryCatch.getFinallyBody(), resourceVar)) {
            return 0;
        }

        List<IVariableDeclarationStatement> resources = Collections.singletonList(resourceDecl);
        IStatement tryBody = transformStatement(tryCatch.getTryBody());
        List<ICatchClause> userCatches = filterUserCatchClauses(tryCatch.getCatchClauses());

        ITryCatchStatement twrStatement = (ITryCatchStatement)
                StatementModelFactory.getInstance().getTryCatchStatement(tryCatch.getLabel(), resources, tryBody, userCatches, null);

        result.add(twrStatement);

        if (hasThrowableDecl) {
            return 3;
        }
        return 2;
    }


    /**
     * Check whether a type name represents java.lang.Throwable.
     *
     * @param typeName the type display name
     * @return true if the type is Throwable
     */
    private boolean isThrowableType(String typeName) {
        if ("java.lang.Throwable".equals(typeName)) {
            return true;
        }
        return "Throwable".equals(typeName);
    }


    /**
     * Check whether a statement or its children contain a close() method call
     * on the specified variable. Recursively checks block statements, if-statements,
     * and nested try-catch bodies.
     *
     * @param statement the statement to check
     * @param varName   the variable name to look for
     * @return true if a close() call on the variable is found
     */
    private boolean containsCloseCall(IStatement statement, String varName) {
        if (statement instanceof IBlockStatement) {
            for (IStatement stmt : ((IBlockStatement) statement).getStatements()) {
                if (containsCloseCall(stmt, varName)) {
                    return true;
                }
            }
            return false;
        }
        if (statement instanceof IExpressionStatement) {
            return isCloseCallOnVariable(((IExpressionStatement) statement).getExpression(), varName);
        }
        if (statement instanceof IIfStatement) {
            IIfStatement ifStmt = (IIfStatement) statement;
            if (containsCloseCall(ifStmt.getThenBranch(), varName)) {
                return true;
            }
            if (ifStmt.getElseBranch() != null) {
                return containsCloseCall(ifStmt.getElseBranch(), varName);
            }
            return false;
        }
        if (statement instanceof ITryCatchStatement) {
            return containsCloseCall(((ITryCatchStatement) statement).getTryBody(), varName);
        }
        return false;
    }


    /**
     * Check whether an expression is a close() method call on the specified variable.
     *
     * @param expression the expression to check
     * @param varName    the variable name
     * @return true if this is varName.close()
     */
    private boolean isCloseCallOnVariable(IExpression expression, String varName) {
        if (!(expression instanceof IMethodCallExpression)) {
            return false;
        }
        IMethodCallExpression call = (IMethodCallExpression) expression;
        if (!CLOSE_METHOD.equals(call.getMethodName())) {
            return false;
        }
        if (call.getTarget() == null) {
            return false;
        }
        if (!(call.getTarget() instanceof IVariableExpression)) {
            return false;
        }
        return varName.equals(((IVariableExpression) call.getTarget()).getName());
    }


    /**
     * Filter catch clauses to remove the synthetic Throwable catch clause that the
     * compiler generates for try-with-resources. The synthetic catch clause catches
     * java.lang.Throwable and contains a re-throw statement.
     *
     * @param catchClauses the original catch clauses
     * @return the list with synthetic catch clauses removed
     */
    private List<ICatchClause> filterUserCatchClauses(List<ICatchClause> catchClauses) {
        List<ICatchClause> userClauses = new ArrayList<>();
        for (ICatchClause clause : catchClauses) {
            if (!isSyntheticThrowableCatch(clause)) {
                userClauses.add(clause);
            }
        }
        return userClauses;
    }


    /**
     * Check whether a catch clause is the synthetic Throwable catch generated
     * for try-with-resources. This catch clause catches Throwable and its body
     * contains a re-throw statement.
     *
     * @param clause the catch clause to check
     * @return true if this is a synthetic TWR catch
     */
    private boolean isSyntheticThrowableCatch(ICatchClause clause) {
        if (clause.getExceptionTypes().size() != 1) {
            return false;
        }
        String exType = clause.getExceptionTypes().get(0).getDisplayName();
        if (!isThrowableType(exType)) {
            return false;
        }

        List<IStatement> bodyStmts = getBodyStatements(clause.getBody());
        for (IStatement stmt : bodyStmts) {
            if (stmt instanceof IThrowStatement) {
                return true;
            }
        }
        return false;
    }


    /**
     * Get the list of statements from a body, unwrapping a block statement if necessary.
     *
     * @param body the body statement
     * @return the list of statements
     */
    private List<IStatement> getBodyStatements(IStatement body) {
        if (body instanceof IBlockStatement) {
            return ((IBlockStatement) body).getStatements();
        }
        return Collections.singletonList(body);
    }
}
