/*
 * StatementModelFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.statement;

import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.AssertStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.BlockStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.BreakStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.CatchClauseImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.ContinueStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.DoWhileStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.ExpressionStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.ForEachStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.ForStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.IfStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.ReturnStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.SwitchCaseImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.SwitchStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.SynchronizedStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.ThrowStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.TryCatchStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.VariableDeclarationStatementImpl;
import com.github.toolarium.java.decompiler.model.ast.statement.impl.WhileStatementImpl;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.List;


/**
 * Singleton factory for AST statement model objects.
 *
 * @author patrick
 */
public final class StatementModelFactory {

    /**
     * Private constructor.
     */
    private StatementModelFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final StatementModelFactory INSTANCE = new StatementModelFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static StatementModelFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a block statement.
     *
     * @param label      the optional label, or null
     * @param statements the list of statements in this block
     * @return the block statement
     */
    public IBlockStatement getBlockStatement(String label, List<IStatement> statements) {
        return new BlockStatementImpl(label, statements);
    }


    /**
     * Get an expression statement.
     *
     * @param label      the optional label, or null
     * @param expression the expression
     * @return the expression statement
     */
    public IExpressionStatement getExpressionStatement(String label, IExpression expression) {
        return new ExpressionStatementImpl(label, expression);
    }


    /**
     * Get a return statement.
     *
     * @param label the optional label, or null
     * @param value the return expression, or null for void return
     * @return the return statement
     */
    public IReturnStatement getReturnStatement(String label, IExpression value) {
        return new ReturnStatementImpl(label, value);
    }


    /**
     * Get an if statement.
     *
     * @param label      the optional label, or null
     * @param condition  the condition expression
     * @param thenBranch the then branch statement
     * @param elseBranch the else branch statement, or null if no else
     * @return the if statement
     */
    public IIfStatement getIfStatement(String label, IExpression condition, IStatement thenBranch, IStatement elseBranch) {
        return new IfStatementImpl(label, condition, thenBranch, elseBranch);
    }


    /**
     * Get a while statement.
     *
     * @param label     the optional label, or null
     * @param condition the loop condition expression
     * @param body      the loop body statement
     * @return the while statement
     */
    public IWhileStatement getWhileStatement(String label, IExpression condition, IStatement body) {
        return new WhileStatementImpl(label, condition, body);
    }


    /**
     * Get a do-while statement.
     *
     * @param label     the optional label, or null
     * @param condition the loop condition expression
     * @param body      the loop body statement
     * @return the do-while statement
     */
    public IDoWhileStatement getDoWhileStatement(String label, IExpression condition, IStatement body) {
        return new DoWhileStatementImpl(label, condition, body);
    }


    /**
     * Get a for statement.
     *
     * @param label     the optional label, or null
     * @param init      the initialization statement, or null
     * @param condition the loop condition expression, or null
     * @param update    the update expression, or null
     * @param body      the loop body statement
     * @return the for statement
     */
    public IForStatement getForStatement(String label, IStatement init, IExpression condition, IExpression update, IStatement body) {
        return new ForStatementImpl(label, init, condition, update, body);
    }


    /**
     * Get a for-each statement.
     *
     * @param label        the optional label, or null
     * @param variableType the loop variable type
     * @param variableName the loop variable name
     * @param iterable     the iterable expression
     * @param body         the loop body statement
     * @return the for-each statement
     */
    public IForEachStatement getForEachStatement(String label, IType variableType, String variableName,
                                                 IExpression iterable, IStatement body) {
        return new ForEachStatementImpl(label, variableType, variableName, iterable, body);
    }


    /**
     * Get a switch statement.
     *
     * @param label    the optional label, or null
     * @param selector the switch selector expression
     * @param cases    the list of switch cases
     * @return the switch statement
     */
    public ISwitchStatement getSwitchStatement(String label, IExpression selector, List<ISwitchCase> cases) {
        return new SwitchStatementImpl(label, selector, cases);
    }


    /**
     * Get a switch case.
     *
     * @param values     the case value expressions (empty for default case)
     * @param isDefault  true if this is the default case
     * @param statements the list of statements in this case
     * @return the switch case
     */
    public ISwitchCase getSwitchCase(List<IExpression> values, boolean isDefault, List<IStatement> statements) {
        return new SwitchCaseImpl(values, isDefault, statements);
    }


    /**
     * Get a try-catch statement.
     *
     * @param label        the optional label, or null
     * @param tryBody      the try block statement
     * @param catchClauses the list of catch clauses
     * @param finallyBody  the finally block statement, or null if no finally
     * @return the try-catch statement
     */
    public ITryCatchStatement getTryCatchStatement(String label, IStatement tryBody,
                                                   List<ICatchClause> catchClauses, IStatement finallyBody) {
        return new TryCatchStatementImpl(label, tryBody, catchClauses, finallyBody);
    }


    /**
     * Get a try-catch statement with resource declarations for try-with-resources.
     *
     * @param label        the optional label, or null
     * @param resources    the resource declarations
     * @param tryBody      the try block statement
     * @param catchClauses the list of catch clauses
     * @param finallyBody  the finally block statement, or null if no finally
     * @return the try-catch statement with resources
     */
    public ITryCatchStatement getTryCatchStatement(String label, List<IVariableDeclarationStatement> resources,
                                                   IStatement tryBody, List<ICatchClause> catchClauses,
                                                   IStatement finallyBody) {
        return new TryCatchStatementImpl(label, resources, tryBody, catchClauses, finallyBody);
    }


    /**
     * Get a catch clause.
     *
     * @param exceptionTypes the caught exception types
     * @param variableName   the exception variable name
     * @param body           the catch body statement
     * @return the catch clause
     */
    public ICatchClause getCatchClause(List<IType> exceptionTypes, String variableName, IStatement body) {
        return new CatchClauseImpl(exceptionTypes, variableName, body);
    }


    /**
     * Get a throw statement.
     *
     * @param label      the optional label, or null
     * @param expression the expression being thrown
     * @return the throw statement
     */
    public IThrowStatement getThrowStatement(String label, IExpression expression) {
        return new ThrowStatementImpl(label, expression);
    }


    /**
     * Get a break statement.
     *
     * @param label       the optional label, or null
     * @param targetLabel the target label for the break, or null for unlabeled
     * @return the break statement
     */
    public IBreakStatement getBreakStatement(String label, String targetLabel) {
        return new BreakStatementImpl(label, targetLabel);
    }


    /**
     * Get a continue statement.
     *
     * @param label       the optional label, or null
     * @param targetLabel the target label for the continue, or null for unlabeled
     * @return the continue statement
     */
    public IContinueStatement getContinueStatement(String label, String targetLabel) {
        return new ContinueStatementImpl(label, targetLabel);
    }


    /**
     * Get a variable declaration statement.
     *
     * @param label        the optional label, or null
     * @param variableType the variable type
     * @param variableName the variable name
     * @param initializer  the initializer expression, or null if uninitialized
     * @param isFinal      true if the variable is declared final
     * @return the variable declaration statement
     */
    public IVariableDeclarationStatement getVariableDeclarationStatement(String label, IType variableType,
                                                                        String variableName, IExpression initializer,
                                                                        boolean isFinal) {
        return new VariableDeclarationStatementImpl(label, variableType, variableName, initializer, isFinal);
    }


    /**
     * Get a synchronized statement.
     *
     * @param label the optional label, or null
     * @param lock  the lock expression
     * @param body  the synchronized body statement
     * @return the synchronized statement
     */
    public ISynchronizedStatement getSynchronizedStatement(String label, IExpression lock, IStatement body) {
        return new SynchronizedStatementImpl(label, lock, body);
    }


    /**
     * Get an assert statement.
     *
     * @param label     the optional label, or null
     * @param condition the condition expression
     * @param message   the optional message expression, or null
     * @return the assert statement
     */
    public IAssertStatement getAssertStatement(String label, IExpression condition, IExpression message) {
        return new AssertStatementImpl(label, condition, message);
    }
}
