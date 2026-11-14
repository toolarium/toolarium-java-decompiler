/*
 * IAstVisitor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast;

import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IFieldDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.IArrayAccessExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IAssignmentExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IBinaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ICastExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IConstantExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IFieldAccessExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IInstanceOfExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ILambdaExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodCallExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodReferenceExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewArrayExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ISwitchExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IUnaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IVariableExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IAssertStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IBreakStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IContinueStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IDoWhileStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IExpressionStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IForEachStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IForStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IIfStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IReturnStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ISynchronizedStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IThrowStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ITryCatchStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IVariableDeclarationStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IWhileStatement;


/**
 * Visitor interface for the AST.
 *
 * @author patrick
 */
public interface IAstVisitor {

    /**
     * Visit a constant expression.
     *
     * @param expression the expression
     */
    void visitConstantExpression(IConstantExpression expression);


    /**
     * Visit a variable expression.
     *
     * @param expression the expression
     */
    void visitVariableExpression(IVariableExpression expression);


    /**
     * Visit a binary expression.
     *
     * @param expression the expression
     */
    void visitBinaryExpression(IBinaryExpression expression);


    /**
     * Visit a unary expression.
     *
     * @param expression the expression
     */
    void visitUnaryExpression(IUnaryExpression expression);


    /**
     * Visit a method call expression.
     *
     * @param expression the expression
     */
    void visitMethodCallExpression(IMethodCallExpression expression);


    /**
     * Visit a field access expression.
     *
     * @param expression the expression
     */
    void visitFieldAccessExpression(IFieldAccessExpression expression);


    /**
     * Visit a cast expression.
     *
     * @param expression the expression
     */
    void visitCastExpression(ICastExpression expression);


    /**
     * Visit an instanceof expression.
     *
     * @param expression the expression
     */
    void visitInstanceOfExpression(IInstanceOfExpression expression);


    /**
     * Visit a new (object creation) expression.
     *
     * @param expression the expression
     */
    void visitNewExpression(INewExpression expression);


    /**
     * Visit a new array expression.
     *
     * @param expression the expression
     */
    void visitNewArrayExpression(INewArrayExpression expression);


    /**
     * Visit an assignment expression.
     *
     * @param expression the expression
     */
    void visitAssignmentExpression(IAssignmentExpression expression);


    /**
     * Visit an array access expression.
     *
     * @param expression the expression
     */
    void visitArrayAccessExpression(IArrayAccessExpression expression);


    /**
     * Visit a lambda expression.
     *
     * @param expression the expression
     */
    void visitLambdaExpression(ILambdaExpression expression);


    /**
     * Visit a method reference expression.
     *
     * @param expression the expression
     */
    void visitMethodReferenceExpression(IMethodReferenceExpression expression);


    /**
     * Visit a switch expression.
     *
     * @param expression the expression
     */
    void visitSwitchExpression(ISwitchExpression expression);


    /**
     * Visit a block statement.
     *
     * @param statement the statement
     */
    void visitBlockStatement(IBlockStatement statement);


    /**
     * Visit an expression statement.
     *
     * @param statement the statement
     */
    void visitExpressionStatement(IExpressionStatement statement);


    /**
     * Visit a return statement.
     *
     * @param statement the statement
     */
    void visitReturnStatement(IReturnStatement statement);


    /**
     * Visit an if statement.
     *
     * @param statement the statement
     */
    void visitIfStatement(IIfStatement statement);


    /**
     * Visit a while statement.
     *
     * @param statement the statement
     */
    void visitWhileStatement(IWhileStatement statement);


    /**
     * Visit a do-while statement.
     *
     * @param statement the statement
     */
    void visitDoWhileStatement(IDoWhileStatement statement);


    /**
     * Visit a for statement.
     *
     * @param statement the statement
     */
    void visitForStatement(IForStatement statement);


    /**
     * Visit a for-each statement.
     *
     * @param statement the statement
     */
    void visitForEachStatement(IForEachStatement statement);


    /**
     * Visit a switch statement.
     *
     * @param statement the statement
     */
    void visitSwitchStatement(ISwitchStatement statement);


    /**
     * Visit a try-catch statement.
     *
     * @param statement the statement
     */
    void visitTryCatchStatement(ITryCatchStatement statement);


    /**
     * Visit a throw statement.
     *
     * @param statement the statement
     */
    void visitThrowStatement(IThrowStatement statement);


    /**
     * Visit a break statement.
     *
     * @param statement the statement
     */
    void visitBreakStatement(IBreakStatement statement);


    /**
     * Visit a continue statement.
     *
     * @param statement the statement
     */
    void visitContinueStatement(IContinueStatement statement);


    /**
     * Visit a variable declaration statement.
     *
     * @param statement the statement
     */
    void visitVariableDeclarationStatement(IVariableDeclarationStatement statement);


    /**
     * Visit a synchronized statement.
     *
     * @param statement the statement
     */
    void visitSynchronizedStatement(ISynchronizedStatement statement);


    /**
     * Visit an assert statement.
     *
     * @param statement the statement
     */
    void visitAssertStatement(IAssertStatement statement);


    /**
     * Visit a class declaration.
     *
     * @param declaration the declaration
     */
    void visitClassDeclaration(IClassDeclaration declaration);


    /**
     * Visit a method declaration.
     *
     * @param declaration the declaration
     */
    void visitMethodDeclaration(IMethodDeclaration declaration);


    /**
     * Visit a field declaration.
     *
     * @param declaration the declaration
     */
    void visitFieldDeclaration(IFieldDeclaration declaration);
}
