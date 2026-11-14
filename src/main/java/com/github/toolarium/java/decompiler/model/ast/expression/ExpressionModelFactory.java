/*
 * ExpressionModelFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.ast.expression;

import com.github.toolarium.java.decompiler.model.ast.expression.impl.ArrayAccessExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.AssignmentExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.BinaryExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.CastExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.ConstantExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.FieldAccessExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.InstanceOfExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.LambdaExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.MethodCallExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.MethodReferenceExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.NewArrayExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.NewExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.SwitchExpressionCaseImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.SwitchExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.UnaryExpressionImpl;
import com.github.toolarium.java.decompiler.model.ast.expression.impl.VariableExpressionImpl;
import com.github.toolarium.java.decompiler.model.type.IType;
import java.util.List;


/**
 * Singleton factory for AST expression model objects.
 *
 * @author patrick
 */
public final class ExpressionModelFactory {

    /**
     * Private constructor.
     */
    private ExpressionModelFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ExpressionModelFactory INSTANCE = new ExpressionModelFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static ExpressionModelFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a constant expression.
     *
     * @param type  the expression type
     * @param value the constant value
     * @return the constant expression
     */
    public IConstantExpression getConstantExpression(IType type, Object value) {
        return new ConstantExpressionImpl(type, value);
    }


    /**
     * Get a variable expression.
     *
     * @param type  the variable type
     * @param name  the variable name
     * @param index the local variable index
     * @return the variable expression
     */
    public IVariableExpression getVariableExpression(IType type, String name, int index) {
        return new VariableExpressionImpl(type, name, index);
    }


    /**
     * Get a binary expression.
     *
     * @param type     the result type
     * @param left     the left operand
     * @param right    the right operand
     * @param operator the binary operator
     * @return the binary expression
     */
    public IBinaryExpression getBinaryExpression(IType type, IExpression left,
                                                 IExpression right, BinaryOperator operator) {
        return new BinaryExpressionImpl(type, left, right, operator);
    }


    /**
     * Get a unary expression.
     *
     * @param type     the result type
     * @param operand  the operand expression
     * @param operator the unary operator
     * @return the unary expression
     */
    public IUnaryExpression getUnaryExpression(IType type, IExpression operand, UnaryOperator operator) {
        return new UnaryExpressionImpl(type, operand, operator);
    }


    /**
     * Get a method call expression.
     *
     * @param type       the return type
     * @param target     the target object expression (null for static calls)
     * @param methodName the method name
     * @param owner      the owner class internal name
     * @param arguments  the method arguments
     * @return the method call expression
     */
    public IMethodCallExpression getMethodCallExpression(IType type, IExpression target,
                                                         String methodName, String owner,
                                                         List<IExpression> arguments) {
        return new MethodCallExpressionImpl(type, target, methodName, owner, arguments);
    }


    /**
     * Get a field access expression.
     *
     * @param type      the field type
     * @param target    the target object expression (null for static access)
     * @param fieldName the field name
     * @param owner     the owner class internal name
     * @return the field access expression
     */
    public IFieldAccessExpression getFieldAccessExpression(IType type, IExpression target,
                                                           String fieldName, String owner) {
        return new FieldAccessExpressionImpl(type, target, fieldName, owner);
    }


    /**
     * Get a cast expression.
     *
     * @param type     the expression result type
     * @param operand  the expression being cast
     * @param castType the target cast type
     * @return the cast expression
     */
    public ICastExpression getCastExpression(IType type, IExpression operand, IType castType) {
        return new CastExpressionImpl(type, operand, castType);
    }


    /**
     * Get an instanceof expression.
     *
     * @param type            the expression result type (boolean)
     * @param operand         the expression being checked
     * @param checkType       the type to check against
     * @param patternVariable the pattern variable name, or null for traditional instanceof
     * @return the instanceof expression
     */
    public IInstanceOfExpression getInstanceOfExpression(IType type, IExpression operand,
                                                         IType checkType, String patternVariable) {
        return new InstanceOfExpressionImpl(type, operand, checkType, patternVariable);
    }


    /**
     * Get a new (object creation) expression.
     *
     * @param type             the expression result type
     * @param instantiatedType the type being instantiated
     * @param arguments        the constructor arguments
     * @return the new expression
     */
    public INewExpression getNewExpression(IType type, IType instantiatedType, List<IExpression> arguments) {
        return new NewExpressionImpl(type, instantiatedType, arguments);
    }


    /**
     * Get a new array expression.
     *
     * @param type           the expression result type
     * @param elementType    the array element type
     * @param dimensionSizes the dimension size expressions
     * @param initializers   the initializer expressions
     * @return the new array expression
     */
    public INewArrayExpression getNewArrayExpression(IType type, IType elementType,
                                                     List<IExpression> dimensionSizes,
                                                     List<IExpression> initializers) {
        return new NewArrayExpressionImpl(type, elementType, dimensionSizes, initializers);
    }


    /**
     * Get an assignment expression.
     *
     * @param type   the expression result type
     * @param target the left-hand side (assignment target)
     * @param value  the right-hand side (value being assigned)
     * @return the assignment expression
     */
    public IAssignmentExpression getAssignmentExpression(IType type, IExpression target, IExpression value) {
        return new AssignmentExpressionImpl(type, target, value);
    }


    /**
     * Get an array access expression.
     *
     * @param type  the element type
     * @param array the array expression
     * @param index the index expression
     * @return the array access expression
     */
    public IArrayAccessExpression getArrayAccessExpression(IType type, IExpression array, IExpression index) {
        return new ArrayAccessExpressionImpl(type, array, index);
    }


    /**
     * Get a lambda expression.
     *
     * @param type                     the functional interface type
     * @param functionalMethodName     the functional interface method name
     * @param parameterNames           the lambda parameter names
     * @param parameterTypeDescriptors the lambda parameter type descriptors
     * @param capturedArguments        the captured arguments
     * @param body                     the body expression (may be null)
     * @param implementationMethodName the implementation method name
     * @param implementationOwner      the implementation owner class
     * @return the lambda expression
     */
    public ILambdaExpression getLambdaExpression(IType type, String functionalMethodName,
                                                 List<String> parameterNames,
                                                 List<String> parameterTypeDescriptors,
                                                 List<IExpression> capturedArguments,
                                                 IExpression body,
                                                 String implementationMethodName,
                                                 String implementationOwner) {
        return new LambdaExpressionImpl(type, functionalMethodName, parameterNames,
                parameterTypeDescriptors, capturedArguments, body,
                implementationMethodName, implementationOwner);
    }


    /**
     * Get a method reference expression.
     *
     * @param type                 the functional interface type
     * @param qualifier            the qualifier expression (may be null)
     * @param owner                the owner class internal name
     * @param methodName           the referenced method name
     * @param constructorReference true if this is a constructor reference
     * @return the method reference expression
     */
    public IMethodReferenceExpression getMethodReferenceExpression(IType type, IExpression qualifier,
                                                                   String owner, String methodName,
                                                                   boolean constructorReference) {
        return new MethodReferenceExpressionImpl(type, qualifier, owner, methodName, constructorReference);
    }


    /**
     * Get a switch expression.
     *
     * @param type     the result type of the switch expression
     * @param selector the selector expression
     * @param cases    the switch expression cases
     * @return the switch expression
     */
    public ISwitchExpression getSwitchExpression(IType type, IExpression selector, List<ISwitchExpressionCase> cases) {
        return new SwitchExpressionImpl(type, selector, cases);
    }


    /**
     * Get a switch expression case.
     *
     * @param values           the case value expressions (empty for default case)
     * @param isDefault        true if this is the default case
     * @param resultExpression the result expression for this case
     * @return the switch expression case
     */
    public ISwitchExpressionCase getSwitchExpressionCase(List<IExpression> values, boolean isDefault, IExpression resultExpression) {
        return new SwitchExpressionCaseImpl(values, isDefault, resultExpression);
    }
}
