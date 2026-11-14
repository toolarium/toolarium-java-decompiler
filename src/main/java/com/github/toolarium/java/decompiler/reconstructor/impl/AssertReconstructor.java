/*
 * AssertReconstructor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reconstructor.impl;

import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IFieldDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.BinaryOperator;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IBinaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IFieldAccessExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.INewExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IUnaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.UnaryOperator;
import com.github.toolarium.java.decompiler.model.ast.statement.IAssertStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IIfStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IThrowStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import com.github.toolarium.java.decompiler.reconstructor.IReconstructor;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Detects assert statement patterns in decompiled output and transforms them into
 * proper assert statements. Also removes the synthetic $assertionsDisabled field
 * that the compiler generates for assert statements.
 *
 * <p>The compiler translates {@code assert condition : message} into:
 * <pre>
 * if (!$assertionsDisabled) {
 *     if (!condition) {
 *         throw new AssertionError(message);
 *     }
 * }
 * </pre>
 *
 * <p>This reconstructor detects this pattern and transforms it back into
 * {@code assert condition} or {@code assert condition : message}.
 *
 * @author patrick
 */
public class AssertReconstructor implements IReconstructor {
    private static final Logger LOG = LoggerFactory.getLogger(AssertReconstructor.class);
    private static final String ASSERTIONS_DISABLED = "$assertionsDisabled";
    private static final String ASSERTION_ERROR = "AssertionError";
    private static final String JAVA_LANG_ASSERTION_ERROR = "java/lang/AssertionError";


    /**
     * Constructor for AssertReconstructor.
     */
    public AssertReconstructor() {
        // default constructor
    }


    /**
     * Get the name of this reconstructor.
     *
     * @return the name "AssertReconstructor"
     */
    @Override
    public String getName() {
        return "AssertReconstructor";
    }


    /**
     * Check whether this reconstructor applies to the given class.
     * Returns true if the class has the compiler-generated $assertionsDisabled field.
     *
     * @param structClass the parsed class structure
     * @return true if the class uses assert statements
     */
    @Override
    public boolean applies(IStructClass structClass) {
        return structClass.getFields().stream()
                .anyMatch(field -> ASSERTIONS_DISABLED.equals(field.getName()));
    }


    /**
     * Apply assert reconstruction by detecting the assert bytecode pattern in method bodies
     * and replacing it with proper assert statements. Also removes the synthetic
     * $assertionsDisabled field.
     *
     * @param classDeclaration the original class declaration
     * @param structClass      the parsed class structure for metadata access
     * @return the transformed class declaration
     */
    @Override
    public IClassDeclaration reconstruct(IClassDeclaration classDeclaration, IStructClass structClass) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Reconstructing assert patterns: " + structClass.getName());
        }

        DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();

        // Remove the $assertionsDisabled field
        List<IFieldDeclaration> filteredFields = new ArrayList<>();
        for (IFieldDeclaration field : classDeclaration.getFields()) {
            if (!ASSERTIONS_DISABLED.equals(field.getName())) {
                filteredFields.add(field);
            }
        }

        // Transform method bodies to replace assert patterns
        List<IMethodDeclaration> transformedMethods = new ArrayList<>();
        for (IMethodDeclaration method : classDeclaration.getMethods()) {
            transformedMethods.add(transformMethod(method, declFactory));
        }

        return declFactory.getClassDeclaration(
                classDeclaration.getAccessFlags(),
                classDeclaration.getName(),
                classDeclaration.getSuperType(),
                classDeclaration.getInterfaceTypes(),
                filteredFields, transformedMethods,
                classDeclaration.getInnerClasses(),
                classDeclaration.getAnnotations(),
                classDeclaration.getPermittedSubclasses(), classDeclaration.isNonSealed());
    }


    /**
     * Transform a method by replacing assert patterns in its body.
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
     * Transform a statement, recursively processing block statements to find and replace
     * assert patterns.
     *
     * @param statement the statement to transform
     * @return the transformed statement, or the original if no patterns matched
     */
    private IStatement transformStatement(IStatement statement) {
        if (statement instanceof IBlockStatement) {
            return transformBlock((IBlockStatement) statement);
        }
        return statement;
    }


    /**
     * Transform a block statement by scanning for assert patterns and replacing them
     * with assert statements.
     *
     * @param block the block statement
     * @return the transformed block, or the original if no patterns matched
     */
    private IStatement transformBlock(IBlockStatement block) {
        List<IStatement> statements = block.getStatements();
        List<IStatement> transformed = new ArrayList<>();
        boolean changed = false;

        for (IStatement stmt : statements) {
            IAssertStatement assertStmt = tryMatchAssertPattern(stmt);
            if (assertStmt != null) {
                transformed.add(assertStmt);
                changed = true;
            } else {
                transformed.add(stmt);
            }
        }

        if (!changed) {
            return block;
        }

        return StatementModelFactory.getInstance().getBlockStatement(block.getLabel(), transformed);
    }


    /**
     * Try to match the assert bytecode pattern in a statement. The pattern is:
     * {@code if ($assertionsDisabled == 0) { if (condition) { throw new AssertionError(...) } }}
     *
     * @param statement the statement to check
     * @return an IAssertStatement if the pattern matches, or null otherwise
     */
    private IAssertStatement tryMatchAssertPattern(IStatement statement) {
        if (!(statement instanceof IIfStatement)) {
            return null;
        }
        IIfStatement outerIf = (IIfStatement) statement;

        // Check outer condition: $assertionsDisabled == 0
        if (!isAssertionsDisabledCheck(outerIf.getCondition())) {
            return null;
        }

        // The then-branch should contain a single inner if with a throw
        IStatement innerStmt = unwrapBlock(outerIf.getThenBranch());
        if (!(innerStmt instanceof IIfStatement)) {
            return null;
        }
        IIfStatement innerIf = (IIfStatement) innerStmt;

        // The inner if's then-branch should contain a throw of AssertionError
        IStatement throwStmt = unwrapBlock(innerIf.getThenBranch());
        if (!(throwStmt instanceof IThrowStatement)) {
            return null;
        }
        IThrowStatement throwStatement = (IThrowStatement) throwStmt;

        // Check that it throws AssertionError
        IExpression thrownExpr = throwStatement.getExpression();
        if (!(thrownExpr instanceof INewExpression)) {
            return null;
        }
        INewExpression newExpr = (INewExpression) thrownExpr;
        String typeName = newExpr.getInstantiatedType().getDisplayName();
        if (!typeName.endsWith(ASSERTION_ERROR) && !typeName.endsWith("AssertionError")) {
            return null;
        }

        // The assert condition is the negation of the inner if's condition.
        // The bytecode tests the trigger condition (e.g., b == 0) and throws when true,
        // but the assert should express the positive invariant (e.g., b != 0).
        IExpression assertCondition = negateCondition(innerIf.getCondition());

        // Extract optional message from AssertionError constructor
        IExpression message = null;
        if (!newExpr.getArguments().isEmpty()) {
            message = newExpr.getArguments().get(0);
        }

        return StatementModelFactory.getInstance().getAssertStatement(
                null, assertCondition, message);
    }


    /**
     * Check if an expression is the assert-disabled guard check. The pattern is
     * either {@code $assertionsDisabled == 0} or a reference to the $assertionsDisabled field.
     *
     * @param expression the expression to check
     * @return true if this is the assertions disabled check
     */
    private boolean isAssertionsDisabledCheck(IExpression expression) {
        // Direct field access: if ($assertionsDisabled)
        if (expression instanceof IFieldAccessExpression) {
            return ASSERTIONS_DISABLED.equals(((IFieldAccessExpression) expression).getFieldName());
        }

        // Binary comparison: $assertionsDisabled == 0
        if (expression instanceof IBinaryExpression) {
            IBinaryExpression binary = (IBinaryExpression) expression;
            if (binary.getLeft() instanceof IFieldAccessExpression) {
                String fieldName = ((IFieldAccessExpression) binary.getLeft()).getFieldName();
                return ASSERTIONS_DISABLED.equals(fieldName);
            }
            if (binary.getRight() instanceof IFieldAccessExpression) {
                String fieldName = ((IFieldAccessExpression) binary.getRight()).getFieldName();
                return ASSERTIONS_DISABLED.equals(fieldName);
            }
        }

        return false;
    }


    /**
     * Negate a boolean condition expression. For binary comparisons, flips the
     * operator (e.g., EQ to NE, LT to GE). For unary NOT, unwraps it.
     * For other expressions, wraps in logical NOT.
     *
     * @param condition the condition to negate
     * @return the negated condition
     */
    private IExpression negateCondition(IExpression condition) {
        if (condition instanceof IBinaryExpression) {
            IBinaryExpression binExpr = (IBinaryExpression) condition;
            BinaryOperator negated = negateOperator(binExpr.getOperator());
            if (negated != null) {
                return ExpressionModelFactory.getInstance().getBinaryExpression(
                        condition.getType(), binExpr.getLeft(), binExpr.getRight(), negated);
            }
        }

        if (condition instanceof IUnaryExpression) {
            IUnaryExpression unaryExpr = (IUnaryExpression) condition;
            if (unaryExpr.getOperator() == UnaryOperator.LOGICAL_NOT) {
                return unaryExpr.getOperand();
            }
        }

        return ExpressionModelFactory.getInstance().getUnaryExpression(
                TypeModelFactory.getInstance().getPrimitiveType('Z'),
                condition, UnaryOperator.LOGICAL_NOT);
    }


    /**
     * Get the negated comparison operator for a binary operator.
     *
     * @param op the operator
     * @return the negated operator, or null if not a comparison
     */
    private BinaryOperator negateOperator(BinaryOperator op) {
        switch (op) {
            case EQ: return BinaryOperator.NE;
            case NE: return BinaryOperator.EQ;
            case LT: return BinaryOperator.GE;
            case GE: return BinaryOperator.LT;
            case GT: return BinaryOperator.LE;
            case LE: return BinaryOperator.GT;
            default: return null;
        }
    }


    /**
     * Unwrap a block statement containing a single statement. Returns the inner statement
     * if the block has exactly one child, otherwise returns the original statement.
     *
     * @param statement the statement to unwrap
     * @return the unwrapped statement
     */
    private IStatement unwrapBlock(IStatement statement) {
        if (statement instanceof IBlockStatement) {
            List<IStatement> stmts = ((IBlockStatement) statement).getStatements();
            if (stmts.size() == 1) {
                return stmts.get(0);
            }
        }
        return statement;
    }
}
