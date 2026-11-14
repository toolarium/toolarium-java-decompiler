/*
 * LambdaBodyReconstructor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reconstructor.impl;

import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IArrayAccessExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IAssignmentExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IBinaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ICastExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IFieldAccessExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IInstanceOfExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ILambdaExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodCallExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewArrayExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IUnaryExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ICatchClause;
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
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.reconstructor.IReconstructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Inlines compiler-generated lambda body methods into lambda expressions.
 * When javac compiles a lambda, it generates a synthetic {@code lambda$} method containing
 * the body and an {@code invokedynamic} call site that references it. This reconstructor
 * finds those lambda expressions with null bodies, looks up the corresponding
 * {@code lambda$} method declaration, extracts its body, and inlines it. After inlining,
 * the {@code lambda$} methods are removed from the class declaration.
 *
 * @author patrick
 */
public class LambdaBodyReconstructor implements IReconstructor {
    private static final Logger LOG = LoggerFactory.getLogger(LambdaBodyReconstructor.class);
    private static final String LAMBDA_METHOD_PREFIX = "lambda$";


    /**
     * Constructor for LambdaBodyReconstructor.
     */
    public LambdaBodyReconstructor() {
        // default constructor
    }


    /**
     * Get the name of this reconstructor.
     *
     * @return the name "LambdaBodyReconstructor"
     */
    @Override
    public String getName() {
        return "LambdaBodyReconstructor";
    }


    /**
     * Check whether this reconstructor applies to the given class.
     * Returns true if the class has any methods whose name starts with "lambda$".
     *
     * @param structClass the parsed class structure
     * @return true if the class contains lambda methods
     */
    @Override
    public boolean applies(IStructClass structClass) {
        return structClass.getMethods().stream()
                .anyMatch(m -> m.getName().startsWith(LAMBDA_METHOD_PREFIX));
    }


    /**
     * Apply lambda body inlining by extracting bodies from lambda$ method declarations
     * and replacing the null-body lambda expressions with inlined versions.
     *
     * @param classDeclaration the original class declaration
     * @param structClass      the parsed class structure for metadata access
     * @return the transformed class declaration with inlined lambda bodies
     */
    @Override
    public IClassDeclaration reconstruct(IClassDeclaration classDeclaration, IStructClass structClass) {
        Map<String, IMethodDeclaration> lambdaMethods = new HashMap<>();
        for (IMethodDeclaration method : classDeclaration.getMethods()) {
            if (method.getName().startsWith(LAMBDA_METHOD_PREFIX)) {
                lambdaMethods.put(method.getName(), method);
            }
        }

        if (lambdaMethods.isEmpty()) {
            return classDeclaration;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Found " + lambdaMethods.size() + " lambda methods to inline.");
        }

        Set<String> inlinedMethods = new HashSet<>();
        List<IMethodDeclaration> updatedMethods = new ArrayList<>();

        for (IMethodDeclaration method : classDeclaration.getMethods()) {
            if (method.getName().startsWith(LAMBDA_METHOD_PREFIX)) {
                continue;
            }

            IStatement body = method.getBody();
            if (body == null) {
                updatedMethods.add(method);
                continue;
            }

            IStatement newBody = rewriteStatement(body, lambdaMethods, inlinedMethods);
            if (newBody != body) {
                updatedMethods.add(DeclarationModelFactory.getInstance().getMethodDeclaration(
                        method.getAccessFlags(), method.getName(), method.getReturnType(),
                        method.getParameters(), method.getThrownTypes(), newBody,
                        method.isConstructor(), method.getAnnotations()));
            } else {
                updatedMethods.add(method);
            }
        }

        for (IMethodDeclaration method : classDeclaration.getMethods()) {
            if (method.getName().startsWith(LAMBDA_METHOD_PREFIX)
                    && !inlinedMethods.contains(method.getName())) {
                updatedMethods.add(method);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Inlined " + inlinedMethods.size() + " lambda method bodies.");
        }

        return DeclarationModelFactory.getInstance().getClassDeclaration(
                classDeclaration.getAccessFlags(),
                classDeclaration.getName(),
                classDeclaration.getSuperType(),
                classDeclaration.getInterfaceTypes(),
                classDeclaration.getFields(),
                updatedMethods,
                classDeclaration.getInnerClasses(),
                classDeclaration.getAnnotations(),
                classDeclaration.getPermittedSubclasses(), classDeclaration.isNonSealed());
    }


    /**
     * Rewrite a statement by replacing lambda expressions with inlined bodies.
     * Returns the original statement if no changes are needed.
     *
     * @param statement      the statement to rewrite
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names (updated in place)
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteStatement(IStatement statement,
                                        Map<String, IMethodDeclaration> lambdaMethods,
                                        Set<String> inlinedMethods) {
        if (statement instanceof IBlockStatement) {
            return rewriteBlock((IBlockStatement) statement, lambdaMethods, inlinedMethods);
        }
        if (statement instanceof IExpressionStatement) {
            return rewriteExpressionStatement((IExpressionStatement) statement, lambdaMethods, inlinedMethods);
        }
        if (statement instanceof IReturnStatement) {
            return rewriteReturnStatement((IReturnStatement) statement, lambdaMethods, inlinedMethods);
        }
        if (statement instanceof IIfStatement) {
            return rewriteIfStatement((IIfStatement) statement, lambdaMethods, inlinedMethods);
        }
        if (statement instanceof IWhileStatement) {
            return rewriteWhileStatement((IWhileStatement) statement, lambdaMethods, inlinedMethods);
        }
        if (statement instanceof IDoWhileStatement) {
            return rewriteDoWhileStatement((IDoWhileStatement) statement, lambdaMethods, inlinedMethods);
        }
        if (statement instanceof IForStatement) {
            return rewriteForStatement((IForStatement) statement, lambdaMethods, inlinedMethods);
        }
        if (statement instanceof IForEachStatement) {
            return rewriteForEachStatement((IForEachStatement) statement, lambdaMethods, inlinedMethods);
        }
        if (statement instanceof ISwitchStatement) {
            return rewriteSwitchStatement((ISwitchStatement) statement, lambdaMethods, inlinedMethods);
        }
        if (statement instanceof ITryCatchStatement) {
            return rewriteTryCatchStatement((ITryCatchStatement) statement, lambdaMethods, inlinedMethods);
        }
        if (statement instanceof ISynchronizedStatement) {
            return rewriteSynchronizedStatement((ISynchronizedStatement) statement, lambdaMethods, inlinedMethods);
        }
        if (statement instanceof IThrowStatement) {
            return rewriteThrowStatement((IThrowStatement) statement, lambdaMethods, inlinedMethods);
        }
        if (statement instanceof IVariableDeclarationStatement) {
            return rewriteVariableDeclaration(
                    (IVariableDeclarationStatement) statement, lambdaMethods, inlinedMethods);
        }
        return statement;
    }


    /**
     * Rewrite a block statement by processing each child statement.
     *
     * @param block          the block statement
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten block, or the original if unchanged
     */
    private IStatement rewriteBlock(IBlockStatement block,
                                    Map<String, IMethodDeclaration> lambdaMethods,
                                    Set<String> inlinedMethods) {
        List<IStatement> originalStatements = block.getStatements();
        List<IStatement> newStatements = null;

        for (int i = 0; i < originalStatements.size(); i++) {
            IStatement original = originalStatements.get(i);
            IStatement rewritten = rewriteStatement(original, lambdaMethods, inlinedMethods);
            if (rewritten != original && newStatements == null) {
                newStatements = new ArrayList<>(originalStatements.subList(0, i));
            }
            if (newStatements != null) {
                newStatements.add(rewritten);
            }
        }

        if (newStatements == null) {
            return block;
        }
        return StatementModelFactory.getInstance().getBlockStatement(block.getLabel(), newStatements);
    }


    /**
     * Rewrite an expression statement by processing its expression.
     *
     * @param statement      the expression statement
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteExpressionStatement(IExpressionStatement statement,
                                                  Map<String, IMethodDeclaration> lambdaMethods,
                                                  Set<String> inlinedMethods) {
        IExpression original = statement.getExpression();
        IExpression rewritten = rewriteExpression(original, lambdaMethods, inlinedMethods);
        if (rewritten != original) {
            return StatementModelFactory.getInstance().getExpressionStatement(statement.getLabel(), rewritten);
        }
        return statement;
    }


    /**
     * Rewrite a return statement by processing its value expression.
     *
     * @param statement      the return statement
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteReturnStatement(IReturnStatement statement,
                                              Map<String, IMethodDeclaration> lambdaMethods,
                                              Set<String> inlinedMethods) {
        if (statement.getValue() == null) {
            return statement;
        }
        IExpression original = statement.getValue();
        IExpression rewritten = rewriteExpression(original, lambdaMethods, inlinedMethods);
        if (rewritten != original) {
            return StatementModelFactory.getInstance().getReturnStatement(statement.getLabel(), rewritten);
        }
        return statement;
    }


    /**
     * Rewrite an if statement by processing its condition, then branch, and else branch.
     *
     * @param statement      the if statement
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteIfStatement(IIfStatement statement,
                                          Map<String, IMethodDeclaration> lambdaMethods,
                                          Set<String> inlinedMethods) {
        IExpression newCondition = rewriteExpression(statement.getCondition(), lambdaMethods, inlinedMethods);
        IStatement newThen = rewriteStatement(statement.getThenBranch(), lambdaMethods, inlinedMethods);
        IStatement newElse = statement.getElseBranch();
        if (newElse != null) {
            newElse = rewriteStatement(newElse, lambdaMethods, inlinedMethods);
        }
        if (newCondition != statement.getCondition() || newThen != statement.getThenBranch()
                || newElse != statement.getElseBranch()) {
            return StatementModelFactory.getInstance().getIfStatement(
                    statement.getLabel(), newCondition, newThen, newElse);
        }
        return statement;
    }


    /**
     * Rewrite a while statement by processing its condition and body.
     *
     * @param statement      the while statement
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteWhileStatement(IWhileStatement statement,
                                             Map<String, IMethodDeclaration> lambdaMethods,
                                             Set<String> inlinedMethods) {
        IExpression newCondition = rewriteExpression(statement.getCondition(), lambdaMethods, inlinedMethods);
        IStatement newBody = rewriteStatement(statement.getBody(), lambdaMethods, inlinedMethods);
        if (newCondition != statement.getCondition() || newBody != statement.getBody()) {
            return StatementModelFactory.getInstance().getWhileStatement(
                    statement.getLabel(), newCondition, newBody);
        }
        return statement;
    }


    /**
     * Rewrite a do-while statement by processing its condition and body.
     *
     * @param statement      the do-while statement
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteDoWhileStatement(IDoWhileStatement statement,
                                               Map<String, IMethodDeclaration> lambdaMethods,
                                               Set<String> inlinedMethods) {
        IExpression newCondition = rewriteExpression(statement.getCondition(), lambdaMethods, inlinedMethods);
        IStatement newBody = rewriteStatement(statement.getBody(), lambdaMethods, inlinedMethods);
        if (newCondition != statement.getCondition() || newBody != statement.getBody()) {
            return StatementModelFactory.getInstance().getDoWhileStatement(
                    statement.getLabel(), newCondition, newBody);
        }
        return statement;
    }


    /**
     * Rewrite a for statement by processing its init, condition, update, and body.
     *
     * @param statement      the for statement
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteForStatement(IForStatement statement,
                                           Map<String, IMethodDeclaration> lambdaMethods,
                                           Set<String> inlinedMethods) {
        IStatement newInit = statement.getInit();
        if (newInit != null) {
            newInit = rewriteStatement(newInit, lambdaMethods, inlinedMethods);
        }
        IExpression newCondition = statement.getCondition();
        if (newCondition != null) {
            newCondition = rewriteExpression(newCondition, lambdaMethods, inlinedMethods);
        }
        IExpression newUpdate = statement.getUpdate();
        if (newUpdate != null) {
            newUpdate = rewriteExpression(newUpdate, lambdaMethods, inlinedMethods);
        }
        IStatement newBody = rewriteStatement(statement.getBody(), lambdaMethods, inlinedMethods);
        if (newInit != statement.getInit() || newCondition != statement.getCondition()
                || newUpdate != statement.getUpdate() || newBody != statement.getBody()) {
            return StatementModelFactory.getInstance().getForStatement(
                    statement.getLabel(), newInit, newCondition, newUpdate, newBody);
        }
        return statement;
    }


    /**
     * Rewrite a for-each statement by processing its iterable and body.
     *
     * @param statement      the for-each statement
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteForEachStatement(IForEachStatement statement,
                                               Map<String, IMethodDeclaration> lambdaMethods,
                                               Set<String> inlinedMethods) {
        IExpression newIterable = rewriteExpression(statement.getIterable(), lambdaMethods, inlinedMethods);
        IStatement newBody = rewriteStatement(statement.getBody(), lambdaMethods, inlinedMethods);
        if (newIterable != statement.getIterable() || newBody != statement.getBody()) {
            return StatementModelFactory.getInstance().getForEachStatement(
                    statement.getLabel(), statement.getVariableType(),
                    statement.getVariableName(), newIterable, newBody);
        }
        return statement;
    }


    /**
     * Rewrite a switch statement by processing its selector and cases.
     *
     * @param statement      the switch statement
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteSwitchStatement(ISwitchStatement statement,
                                              Map<String, IMethodDeclaration> lambdaMethods,
                                              Set<String> inlinedMethods) {
        IExpression newSelector = rewriteExpression(statement.getSelector(), lambdaMethods, inlinedMethods);
        boolean casesChanged = false;
        List<ISwitchCase> newCases = new ArrayList<>();
        for (ISwitchCase switchCase : statement.getCases()) {
            List<IStatement> caseStatements = switchCase.getStatements();
            List<IStatement> newCaseStatements = rewriteStatementList(caseStatements, lambdaMethods, inlinedMethods);
            if (newCaseStatements != caseStatements) {
                casesChanged = true;
                newCases.add(StatementModelFactory.getInstance().getSwitchCase(
                        switchCase.getValues(), switchCase.isDefault(), newCaseStatements));
            } else {
                newCases.add(switchCase);
            }
        }
        if (newSelector != statement.getSelector() || casesChanged) {
            return StatementModelFactory.getInstance().getSwitchStatement(
                    statement.getLabel(), newSelector, newCases);
        }
        return statement;
    }


    /**
     * Rewrite a try-catch statement by processing the try body, catch clauses, and finally body.
     *
     * @param statement      the try-catch statement
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteTryCatchStatement(ITryCatchStatement statement,
                                                Map<String, IMethodDeclaration> lambdaMethods,
                                                Set<String> inlinedMethods) {
        IStatement newTryBody = rewriteStatement(statement.getTryBody(), lambdaMethods, inlinedMethods);
        boolean catchChanged = false;
        List<ICatchClause> newCatchClauses = new ArrayList<>();
        for (ICatchClause clause : statement.getCatchClauses()) {
            IStatement newCatchBody = rewriteStatement(clause.getBody(), lambdaMethods, inlinedMethods);
            if (newCatchBody != clause.getBody()) {
                catchChanged = true;
                newCatchClauses.add(StatementModelFactory.getInstance().getCatchClause(
                        clause.getExceptionTypes(), clause.getVariableName(), newCatchBody));
            } else {
                newCatchClauses.add(clause);
            }
        }
        IStatement newFinally = statement.getFinallyBody();
        if (newFinally != null) {
            newFinally = rewriteStatement(newFinally, lambdaMethods, inlinedMethods);
        }
        if (newTryBody != statement.getTryBody() || catchChanged || newFinally != statement.getFinallyBody()) {
            return StatementModelFactory.getInstance().getTryCatchStatement(
                    statement.getLabel(), newTryBody, newCatchClauses, newFinally);
        }
        return statement;
    }


    /**
     * Rewrite a synchronized statement by processing its lock and body.
     *
     * @param statement      the synchronized statement
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteSynchronizedStatement(ISynchronizedStatement statement,
                                                    Map<String, IMethodDeclaration> lambdaMethods,
                                                    Set<String> inlinedMethods) {
        IExpression newLock = rewriteExpression(statement.getLock(), lambdaMethods, inlinedMethods);
        IStatement newBody = rewriteStatement(statement.getBody(), lambdaMethods, inlinedMethods);
        if (newLock != statement.getLock() || newBody != statement.getBody()) {
            return StatementModelFactory.getInstance().getSynchronizedStatement(
                    statement.getLabel(), newLock, newBody);
        }
        return statement;
    }


    /**
     * Rewrite a throw statement by processing its expression.
     *
     * @param statement      the throw statement
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteThrowStatement(IThrowStatement statement,
                                             Map<String, IMethodDeclaration> lambdaMethods,
                                             Set<String> inlinedMethods) {
        IExpression original = statement.getExpression();
        IExpression rewritten = rewriteExpression(original, lambdaMethods, inlinedMethods);
        if (rewritten != original) {
            return StatementModelFactory.getInstance().getThrowStatement(statement.getLabel(), rewritten);
        }
        return statement;
    }


    /**
     * Rewrite a variable declaration statement by processing its initializer.
     *
     * @param statement      the variable declaration
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten statement, or the original if unchanged
     */
    private IStatement rewriteVariableDeclaration(IVariableDeclarationStatement statement,
                                                  Map<String, IMethodDeclaration> lambdaMethods,
                                                  Set<String> inlinedMethods) {
        if (statement.getInitializer() == null) {
            return statement;
        }
        IExpression original = statement.getInitializer();
        IExpression rewritten = rewriteExpression(original, lambdaMethods, inlinedMethods);
        if (rewritten != original) {
            return StatementModelFactory.getInstance().getVariableDeclarationStatement(
                    statement.getLabel(), statement.getVariableType(),
                    statement.getVariableName(), rewritten, statement.isFinal());
        }
        return statement;
    }


    /**
     * Rewrite a list of statements. Returns the original list reference if nothing changed.
     *
     * @param statements     the statement list
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten list, or the original if unchanged
     */
    private List<IStatement> rewriteStatementList(List<IStatement> statements,
                                                  Map<String, IMethodDeclaration> lambdaMethods,
                                                  Set<String> inlinedMethods) {
        List<IStatement> result = null;
        for (int i = 0; i < statements.size(); i++) {
            IStatement original = statements.get(i);
            IStatement rewritten = rewriteStatement(original, lambdaMethods, inlinedMethods);
            if (rewritten != original && result == null) {
                result = new ArrayList<>(statements.subList(0, i));
            }
            if (result != null) {
                result.add(rewritten);
            }
        }
        if (result == null) {
            return statements;
        }
        return result;
    }


    /**
     * Rewrite an expression by replacing lambda expressions with inlined versions.
     * Recursively processes sub-expressions. Returns the original if unchanged.
     *
     * @param expression     the expression to rewrite
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten expression, or the original if unchanged
     */
    private IExpression rewriteExpression(IExpression expression,
                                          Map<String, IMethodDeclaration> lambdaMethods,
                                          Set<String> inlinedMethods) {
        if (expression instanceof ILambdaExpression) {
            return rewriteLambdaExpression((ILambdaExpression) expression, lambdaMethods, inlinedMethods);
        }
        if (expression instanceof IMethodCallExpression) {
            return rewriteMethodCallExpression((IMethodCallExpression) expression, lambdaMethods, inlinedMethods);
        }
        if (expression instanceof IAssignmentExpression) {
            return rewriteAssignmentExpression((IAssignmentExpression) expression, lambdaMethods, inlinedMethods);
        }
        if (expression instanceof IBinaryExpression) {
            return rewriteBinaryExpression((IBinaryExpression) expression, lambdaMethods, inlinedMethods);
        }
        if (expression instanceof IUnaryExpression) {
            return rewriteUnaryExpression((IUnaryExpression) expression, lambdaMethods, inlinedMethods);
        }
        if (expression instanceof ICastExpression) {
            return rewriteCastExpression((ICastExpression) expression, lambdaMethods, inlinedMethods);
        }
        if (expression instanceof INewExpression) {
            return rewriteNewExpression((INewExpression) expression, lambdaMethods, inlinedMethods);
        }
        if (expression instanceof INewArrayExpression) {
            return rewriteNewArrayExpression((INewArrayExpression) expression, lambdaMethods, inlinedMethods);
        }
        if (expression instanceof IArrayAccessExpression) {
            return rewriteArrayAccessExpression(
                    (IArrayAccessExpression) expression, lambdaMethods, inlinedMethods);
        }
        if (expression instanceof IFieldAccessExpression) {
            return rewriteFieldAccessExpression(
                    (IFieldAccessExpression) expression, lambdaMethods, inlinedMethods);
        }
        if (expression instanceof IInstanceOfExpression) {
            return rewriteInstanceOfExpression(
                    (IInstanceOfExpression) expression, lambdaMethods, inlinedMethods);
        }
        return expression;
    }


    /**
     * Rewrite a lambda expression by inlining the body from the corresponding lambda method.
     *
     * @param lambda         the lambda expression with null body
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the lambda with inlined body, or the original if inlining is not possible
     */
    private IExpression rewriteLambdaExpression(ILambdaExpression lambda,
                                                Map<String, IMethodDeclaration> lambdaMethods,
                                                Set<String> inlinedMethods) {
        if (lambda.getBody() != null) {
            return lambda;
        }

        String methodName = lambda.getImplementationMethodName();
        if (methodName == null || !methodName.startsWith(LAMBDA_METHOD_PREFIX)) {
            return lambda;
        }

        IMethodDeclaration lambdaMethod = lambdaMethods.get(methodName);
        if (lambdaMethod == null) {
            return lambda;
        }

        IExpression bodyExpression = extractBodyExpression(lambdaMethod);
        if (bodyExpression == null) {
            return lambda;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Inlining lambda body from method " + methodName);
        }

        inlinedMethods.add(methodName);
        return ExpressionModelFactory.getInstance().getLambdaExpression(
                lambda.getType(), lambda.getFunctionalMethodName(),
                lambda.getParameterNames(), lambda.getParameterTypeDescriptors(),
                lambda.getCapturedArguments(), bodyExpression,
                lambda.getImplementationMethodName(), lambda.getImplementationOwner());
    }


    /**
     * Extract a single expression from a lambda method body. Handles two cases:
     * a single return statement with a value, and a single void expression statement.
     *
     * @param method the lambda method declaration
     * @return the extracted expression, or null if the body is too complex
     */
    private IExpression extractBodyExpression(IMethodDeclaration method) {
        IStatement body = method.getBody();
        if (body == null) {
            return null;
        }

        if (body instanceof IBlockStatement) {
            List<IStatement> statements = ((IBlockStatement) body).getStatements();
            if (statements.size() != 1) {
                return null;
            }
            return extractSingleStatementBody(statements.get(0));
        }

        return extractSingleStatementBody(body);
    }


    /**
     * Extract the expression from a single statement, either a return or expression statement.
     *
     * @param statement the single statement
     * @return the expression, or null if not extractable
     */
    private IExpression extractSingleStatementBody(IStatement statement) {
        if (statement instanceof IReturnStatement) {
            return ((IReturnStatement) statement).getValue();
        }
        if (statement instanceof IExpressionStatement) {
            return ((IExpressionStatement) statement).getExpression();
        }
        return null;
    }


    /**
     * Rewrite a method call expression by processing its target and arguments.
     *
     * @param expression     the method call expression
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten expression, or the original if unchanged
     */
    private IExpression rewriteMethodCallExpression(IMethodCallExpression expression,
                                                    Map<String, IMethodDeclaration> lambdaMethods,
                                                    Set<String> inlinedMethods) {
        IExpression newTarget = expression.getTarget();
        if (newTarget != null) {
            newTarget = rewriteExpression(newTarget, lambdaMethods, inlinedMethods);
        }
        List<IExpression> newArgs = rewriteExpressionList(expression.getArguments(), lambdaMethods, inlinedMethods);
        if (newTarget != expression.getTarget() || newArgs != expression.getArguments()) {
            return ExpressionModelFactory.getInstance().getMethodCallExpression(
                    expression.getType(), newTarget, expression.getMethodName(),
                    expression.getOwner(), newArgs);
        }
        return expression;
    }


    /**
     * Rewrite an assignment expression by processing its target and value.
     *
     * @param expression     the assignment expression
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten expression, or the original if unchanged
     */
    private IExpression rewriteAssignmentExpression(IAssignmentExpression expression,
                                                    Map<String, IMethodDeclaration> lambdaMethods,
                                                    Set<String> inlinedMethods) {
        IExpression newTarget = rewriteExpression(expression.getTarget(), lambdaMethods, inlinedMethods);
        IExpression newValue = rewriteExpression(expression.getValue(), lambdaMethods, inlinedMethods);
        if (newTarget != expression.getTarget() || newValue != expression.getValue()) {
            return ExpressionModelFactory.getInstance().getAssignmentExpression(
                    expression.getType(), newTarget, newValue);
        }
        return expression;
    }


    /**
     * Rewrite a binary expression by processing its operands.
     *
     * @param expression     the binary expression
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten expression, or the original if unchanged
     */
    private IExpression rewriteBinaryExpression(IBinaryExpression expression,
                                                Map<String, IMethodDeclaration> lambdaMethods,
                                                Set<String> inlinedMethods) {
        IExpression newLeft = rewriteExpression(expression.getLeft(), lambdaMethods, inlinedMethods);
        IExpression newRight = rewriteExpression(expression.getRight(), lambdaMethods, inlinedMethods);
        if (newLeft != expression.getLeft() || newRight != expression.getRight()) {
            return ExpressionModelFactory.getInstance().getBinaryExpression(
                    expression.getType(), newLeft, newRight, expression.getOperator());
        }
        return expression;
    }


    /**
     * Rewrite a unary expression by processing its operand.
     *
     * @param expression     the unary expression
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten expression, or the original if unchanged
     */
    private IExpression rewriteUnaryExpression(IUnaryExpression expression,
                                               Map<String, IMethodDeclaration> lambdaMethods,
                                               Set<String> inlinedMethods) {
        IExpression newOperand = rewriteExpression(expression.getOperand(), lambdaMethods, inlinedMethods);
        if (newOperand != expression.getOperand()) {
            return ExpressionModelFactory.getInstance().getUnaryExpression(
                    expression.getType(), newOperand, expression.getOperator());
        }
        return expression;
    }


    /**
     * Rewrite a cast expression by processing its operand.
     *
     * @param expression     the cast expression
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten expression, or the original if unchanged
     */
    private IExpression rewriteCastExpression(ICastExpression expression,
                                              Map<String, IMethodDeclaration> lambdaMethods,
                                              Set<String> inlinedMethods) {
        IExpression newOperand = rewriteExpression(expression.getOperand(), lambdaMethods, inlinedMethods);
        if (newOperand != expression.getOperand()) {
            return ExpressionModelFactory.getInstance().getCastExpression(
                    expression.getType(), newOperand, expression.getCastType());
        }
        return expression;
    }


    /**
     * Rewrite a new expression by processing its arguments.
     *
     * @param expression     the new expression
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten expression, or the original if unchanged
     */
    private IExpression rewriteNewExpression(INewExpression expression,
                                             Map<String, IMethodDeclaration> lambdaMethods,
                                             Set<String> inlinedMethods) {
        List<IExpression> newArgs = rewriteExpressionList(expression.getArguments(), lambdaMethods, inlinedMethods);
        if (newArgs != expression.getArguments()) {
            return ExpressionModelFactory.getInstance().getNewExpression(
                    expression.getType(), expression.getInstantiatedType(), newArgs);
        }
        return expression;
    }


    /**
     * Rewrite a new array expression by processing its dimension sizes and initializers.
     *
     * @param expression     the new array expression
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten expression, or the original if unchanged
     */
    private IExpression rewriteNewArrayExpression(INewArrayExpression expression,
                                                  Map<String, IMethodDeclaration> lambdaMethods,
                                                  Set<String> inlinedMethods) {
        List<IExpression> newDims = rewriteExpressionList(
                expression.getDimensionSizes(), lambdaMethods, inlinedMethods);
        List<IExpression> newInits = rewriteExpressionList(
                expression.getInitializers(), lambdaMethods, inlinedMethods);
        if (newDims != expression.getDimensionSizes() || newInits != expression.getInitializers()) {
            return ExpressionModelFactory.getInstance().getNewArrayExpression(
                    expression.getType(), expression.getElementType(), newDims, newInits);
        }
        return expression;
    }


    /**
     * Rewrite an array access expression by processing its array and index sub-expressions.
     *
     * @param expression     the array access expression
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten expression, or the original if unchanged
     */
    private IExpression rewriteArrayAccessExpression(IArrayAccessExpression expression,
                                                     Map<String, IMethodDeclaration> lambdaMethods,
                                                     Set<String> inlinedMethods) {
        IExpression newArray = rewriteExpression(expression.getArray(), lambdaMethods, inlinedMethods);
        IExpression newIndex = rewriteExpression(expression.getIndex(), lambdaMethods, inlinedMethods);
        if (newArray != expression.getArray() || newIndex != expression.getIndex()) {
            return ExpressionModelFactory.getInstance().getArrayAccessExpression(
                    expression.getType(), newArray, newIndex);
        }
        return expression;
    }


    /**
     * Rewrite a field access expression by processing its target sub-expression.
     *
     * @param expression     the field access expression
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten expression, or the original if unchanged
     */
    private IExpression rewriteFieldAccessExpression(IFieldAccessExpression expression,
                                                     Map<String, IMethodDeclaration> lambdaMethods,
                                                     Set<String> inlinedMethods) {
        if (expression.getTarget() == null) {
            return expression;
        }
        IExpression newTarget = rewriteExpression(expression.getTarget(), lambdaMethods, inlinedMethods);
        if (newTarget != expression.getTarget()) {
            return ExpressionModelFactory.getInstance().getFieldAccessExpression(
                    expression.getType(), newTarget, expression.getFieldName(), expression.getOwner());
        }
        return expression;
    }


    /**
     * Rewrite an instanceof expression by processing its operand.
     *
     * @param expression     the instanceof expression
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten expression, or the original if unchanged
     */
    private IExpression rewriteInstanceOfExpression(IInstanceOfExpression expression,
                                                    Map<String, IMethodDeclaration> lambdaMethods,
                                                    Set<String> inlinedMethods) {
        IExpression newOperand = rewriteExpression(expression.getOperand(), lambdaMethods, inlinedMethods);
        if (newOperand != expression.getOperand()) {
            return ExpressionModelFactory.getInstance().getInstanceOfExpression(
                    expression.getType(), newOperand, expression.getCheckType(),
                    expression.getPatternVariable());
        }
        return expression;
    }


    /**
     * Rewrite a list of expressions. Returns the original list reference if nothing changed.
     *
     * @param expressions    the expression list
     * @param lambdaMethods  the map of lambda method names to their declarations
     * @param inlinedMethods the set of successfully inlined method names
     * @return the rewritten list, or the original if unchanged
     */
    private List<IExpression> rewriteExpressionList(List<IExpression> expressions,
                                                    Map<String, IMethodDeclaration> lambdaMethods,
                                                    Set<String> inlinedMethods) {
        List<IExpression> result = null;
        for (int i = 0; i < expressions.size(); i++) {
            IExpression original = expressions.get(i);
            IExpression rewritten = rewriteExpression(original, lambdaMethods, inlinedMethods);
            if (rewritten != original && result == null) {
                result = new ArrayList<>(expressions.subList(0, i));
            }
            if (result != null) {
                result.add(rewritten);
            }
        }
        if (result == null) {
            return expressions;
        }
        return result;
    }
}
