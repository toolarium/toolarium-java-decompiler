/*
 * SimplifierImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.simplifier.impl;

import com.github.toolarium.java.decompiler.model.ast.expression.BinaryOperator;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IAssignmentExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IBinaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ICastExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IConstantExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IInstanceOfExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodCallExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ISwitchExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.ISwitchExpressionCase;
import com.github.toolarium.java.decompiler.model.ast.expression.IUnaryExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IVariableExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.UnaryOperator;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IBreakStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IContinueStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IExpressionStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IIfStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IReturnStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchCase;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IThrowStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IVariableDeclarationStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IWhileStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import com.github.toolarium.java.decompiler.simplifier.ISimplifier;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link ISimplifier} that applies multiple simplification passes
 * to the AST: constant folding, boolean simplification, redundant cast removal,
 * dead code elimination, and empty block removal.
 *
 * @author patrick
 */
public class SimplifierImpl implements ISimplifier {
    private static final Logger LOG = LoggerFactory.getLogger(SimplifierImpl.class);
    private static final int MAX_SIMPLIFICATION_DEPTH = 500;


    /**
     * Constructor for SimplifierImpl.
     */
    public SimplifierImpl() {
        // default constructor
    }


    /**
     * Apply simplification passes to a block statement: constant folding, boolean
     * simplification, redundant cast removal, dead code elimination, and empty block removal.
     *
     * @param block the block statement to simplify
     * @return the simplified block statement
     */
    @Override
    public IBlockStatement simplify(IBlockStatement block) {
        List<IStatement> simplified = simplifyStatements(block.getStatements(), 0);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Simplified " + block.getStatements().size() + " statements to " + simplified.size());
        }

        return StatementModelFactory.getInstance().getBlockStatement(block.getLabel(), simplified);
    }


    /**
     * Apply simplification passes to a single expression.
     *
     * @param expression the expression to simplify
     * @return the simplified expression
     */
    @Override
    public IExpression simplifyExpression(IExpression expression) {
        return simplifyExpr(expression, 0);
    }


    /**
     * Simplify a list of statements.
     *
     * @param statements the statements to simplify
     * @param depth      current recursion depth
     * @return the simplified statements
     */
    private List<IStatement> simplifyStatements(List<IStatement> statements, int depth) {
        if (depth >= MAX_SIMPLIFICATION_DEPTH) {
            return statements;
        }

        List<IStatement> result = new ArrayList<>();
        boolean hasWhile = false;
        boolean hasIf = false;
        boolean hasSwitch = false;

        for (IStatement stmt : statements) {
            IStatement simplified = simplifyStatement(stmt, depth);

            // Dead code elimination: skip empty blocks
            if (simplified instanceof IBlockStatement) {
                IBlockStatement block = (IBlockStatement) simplified;
                if (block.getStatements().isEmpty()) {
                    continue;
                }
            }

            if (simplified instanceof IWhileStatement) {
                hasWhile = true;
            } else if (simplified instanceof IIfStatement) {
                hasIf = true;
            } else if (simplified instanceof ISwitchStatement) {
                hasSwitch = true;
            }

            result.add(simplified);
        }

        if (hasWhile) {
            result = detectForEachLoops(result);
            result = detectForLoops(result);
        }
        if (hasIf) {
            result = detectElseIfChains(result);
            result = detectPatternInstanceof(result);
        }
        if (hasSwitch) {
            result = detectSwitchExpressions(result);
        }

        return result;
    }


    /**
     * Simplify a single statement.
     *
     * @param statement the statement to simplify
     * @param depth     current recursion depth
     * @return the simplified statement
     */
    private IStatement simplifyStatement(IStatement statement, int depth) {
        if (depth >= MAX_SIMPLIFICATION_DEPTH) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Simplification depth limit reached at " + MAX_SIMPLIFICATION_DEPTH);
            }
            return statement;
        }

        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();

        if (statement instanceof IBlockStatement) {
            IBlockStatement block = (IBlockStatement) statement;
            List<IStatement> simplified = simplifyStatements(block.getStatements(), depth + 1);
            return stmtFactory.getBlockStatement(block.getLabel(), simplified);
        }

        if (statement instanceof IExpressionStatement) {
            IExpressionStatement exprStmt = (IExpressionStatement) statement;
            IExpression simplified = simplifyExpr(exprStmt.getExpression(), depth + 1);
            return stmtFactory.getExpressionStatement(exprStmt.getLabel(), simplified);
        }

        if (statement instanceof IReturnStatement) {
            IReturnStatement retStmt = (IReturnStatement) statement;
            if (retStmt.getValue() != null) {
                IExpression simplified = simplifyExpr(retStmt.getValue(), depth + 1);
                return stmtFactory.getReturnStatement(retStmt.getLabel(), simplified);
            }
            return statement;
        }

        if (statement instanceof IIfStatement) {
            return simplifyIf((IIfStatement) statement, depth + 1);
        }

        if (statement instanceof IWhileStatement) {
            return simplifyWhile((IWhileStatement) statement, depth + 1);
        }

        if (statement instanceof IVariableDeclarationStatement) {
            return simplifyVarDecl((IVariableDeclarationStatement) statement, depth + 1);
        }

        return statement;
    }


    /**
     * Simplify an if statement. If the condition is a constant boolean,
     * eliminate the branch entirely.
     *
     * @param ifStmt the if statement
     * @param depth  current recursion depth
     * @return the simplified statement
     */
    private IStatement simplifyIf(IIfStatement ifStmt, int depth) {
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        IExpression condition = simplifyExpr(ifStmt.getCondition(), depth);
        IStatement thenBranch = simplifyStatement(ifStmt.getThenBranch(), depth);
        IStatement elseBranch = ifStmt.getElseBranch();
        if (elseBranch != null) {
            elseBranch = simplifyStatement(elseBranch, depth);
        }

        // Constant condition elimination
        if (condition instanceof IConstantExpression) {
            Object value = ((IConstantExpression) condition).getValue();
            if (Boolean.TRUE.equals(value)) {
                return thenBranch;
            }
            if (Boolean.FALSE.equals(value)) {
                if (elseBranch != null) {
                    return elseBranch;
                }
                return stmtFactory.getBlockStatement(null, new ArrayList<>());
            }
        }

        // Remove empty else branch
        if (elseBranch instanceof IBlockStatement) {
            IBlockStatement elseBlock = (IBlockStatement) elseBranch;
            if (elseBlock.getStatements().isEmpty()) {
                elseBranch = null;
            }
        }

        // Boolean return simplification: if (cond) { return true; } else { return false; }
        if (elseBranch != null) {
            IStatement simplified = trySimplifyBooleanReturn(condition, thenBranch, elseBranch);
            if (simplified != null) {
                return simplified;
            }
        }

        return stmtFactory.getIfStatement(ifStmt.getLabel(), condition, thenBranch, elseBranch);
    }


    /**
     * Try to simplify a boolean return pattern. Detects if (cond) return true else return false
     * and transforms to return cond (or return !cond for the inverse).
     *
     * @param condition  the if condition
     * @param thenBranch the then branch
     * @param elseBranch the else branch
     * @return a simplified return statement, or null if the pattern does not match
     */
    private IStatement trySimplifyBooleanReturn(IExpression condition, IStatement thenBranch, IStatement elseBranch) {
        Boolean thenValue = extractBooleanReturn(thenBranch);
        Boolean elseValue = extractBooleanReturn(elseBranch);

        if (thenValue == null || elseValue == null) {
            return null;
        }

        // if (cond) return true; else return false; -> return cond;
        if (thenValue && !elseValue) {
            return StatementModelFactory.getInstance().getReturnStatement(null, condition);
        }

        // if (cond) return false; else return true; -> return !cond;
        if (!thenValue && elseValue) {
            IExpression negated = ExpressionModelFactory.getInstance().getUnaryExpression(
                    condition.getType(), condition, UnaryOperator.LOGICAL_NOT);
            return StatementModelFactory.getInstance().getReturnStatement(null, negated);
        }

        return null;
    }


    /**
     * Extract a boolean constant from a return statement inside a branch.
     * Handles both direct return statements and blocks containing a single return.
     *
     * @param branch the branch statement
     * @return Boolean.TRUE, Boolean.FALSE, or null if not a boolean return
     */
    private Boolean extractBooleanReturn(IStatement branch) {
        IStatement stmt = branch;
        if (stmt instanceof IBlockStatement) {
            List<IStatement> stmts = ((IBlockStatement) stmt).getStatements();
            if (stmts.size() != 1) {
                return null;
            }
            stmt = stmts.get(0);
        }

        if (!(stmt instanceof IReturnStatement)) {
            return null;
        }
        IReturnStatement ret = (IReturnStatement) stmt;
        if (ret.getValue() == null) {
            return null;
        }

        if (ret.getValue() instanceof IConstantExpression) {
            Object value = ((IConstantExpression) ret.getValue()).getValue();
            if (Boolean.TRUE.equals(value) || Integer.valueOf(1).equals(value)) {
                return Boolean.TRUE;
            }
            if (Boolean.FALSE.equals(value) || Integer.valueOf(0).equals(value)) {
                return Boolean.FALSE;
            }
        }
        return null;
    }


    /**
     * Simplify a while statement. If the condition is constant false, eliminate.
     *
     * @param whileStmt the while statement
     * @param depth     current recursion depth
     * @return the simplified statement
     */
    private IStatement simplifyWhile(IWhileStatement whileStmt, int depth) {
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        IExpression condition = simplifyExpr(whileStmt.getCondition(), depth);
        IStatement body = simplifyStatement(whileStmt.getBody(), depth);

        // while(false) is dead code
        if (condition instanceof IConstantExpression) {
            Object value = ((IConstantExpression) condition).getValue();
            if (Boolean.FALSE.equals(value)) {
                return stmtFactory.getBlockStatement(null, new ArrayList<>());
            }
        }

        return stmtFactory.getWhileStatement(whileStmt.getLabel(), condition, body);
    }


    /**
     * Simplify a variable declaration by simplifying its initializer.
     *
     * @param varDecl the variable declaration
     * @param depth   current recursion depth
     * @return the simplified statement
     */
    private IStatement simplifyVarDecl(IVariableDeclarationStatement varDecl, int depth) {
        if (varDecl.getInitializer() != null) {
            IExpression simplified = simplifyExpr(varDecl.getInitializer(), depth);
            return StatementModelFactory.getInstance().getVariableDeclarationStatement(
                    varDecl.getLabel(), varDecl.getVariableType(), varDecl.getVariableName(),
                    simplified, varDecl.isFinal());
        }
        return varDecl;
    }


    /**
     * Simplify an expression by applying all expression passes.
     *
     * @param expression the expression
     * @param depth      current recursion depth
     * @return the simplified expression
     */
    private IExpression simplifyExpr(IExpression expression, int depth) {
        if (depth >= MAX_SIMPLIFICATION_DEPTH) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Expression simplification depth limit reached at " + MAX_SIMPLIFICATION_DEPTH);
            }
            return expression;
        }

        if (expression instanceof IBinaryExpression) {
            return simplifyBinary((IBinaryExpression) expression, depth + 1);
        }
        if (expression instanceof IUnaryExpression) {
            return simplifyUnary((IUnaryExpression) expression, depth + 1);
        }
        if (expression instanceof ICastExpression) {
            return simplifyCast((ICastExpression) expression, depth + 1);
        }
        if (expression instanceof IAssignmentExpression) {
            return simplifyAssignment((IAssignmentExpression) expression, depth + 1);
        }
        return expression;
    }


    /**
     * Simplify a binary expression. Performs constant folding for arithmetic
     * and boolean operations.
     *
     * @param binary the binary expression
     * @param depth  current recursion depth
     * @return the simplified expression
     */
    private IExpression simplifyBinary(IBinaryExpression binary, int depth) {
        ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();
        IExpression left = simplifyExpr(binary.getLeft(), depth);
        IExpression right = simplifyExpr(binary.getRight(), depth);
        BinaryOperator op = binary.getOperator();

        // Constant folding: both operands are constants
        if (left instanceof IConstantExpression && right instanceof IConstantExpression) {
            Object leftVal = ((IConstantExpression) left).getValue();
            Object rightVal = ((IConstantExpression) right).getValue();

            Object result = foldConstants(leftVal, rightVal, op);
            if (result != null) {
                return exprFactory.getConstantExpression(binary.getType(), result);
            }
        }

        // Boolean simplification: x && true -> x, x || false -> x
        IExpression boolResult = simplifyBooleanOps(left, right, op);
        if (boolResult != null) {
            return boolResult;
        }

        // Arithmetic identity: x + 0 -> x, x * 1 -> x
        IExpression identityResult = simplifyArithmeticIdentity(left, right, op);
        if (identityResult != null) {
            return identityResult;
        }

        // If children changed, create new node
        if (left != binary.getLeft() || right != binary.getRight()) {
            return exprFactory.getBinaryExpression(binary.getType(), left, right, op);
        }

        return binary;
    }


    /**
     * Simplify a unary expression. Handles double negation and constant folding.
     *
     * @param unary the unary expression
     * @param depth current recursion depth
     * @return the simplified expression
     */
    private IExpression simplifyUnary(IUnaryExpression unary, int depth) {
        ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();
        IExpression operand = simplifyExpr(unary.getOperand(), depth);
        UnaryOperator op = unary.getOperator();

        // Double negation: !!x -> x
        if (op == UnaryOperator.LOGICAL_NOT && operand instanceof IUnaryExpression) {
            IUnaryExpression inner = (IUnaryExpression) operand;
            if (inner.getOperator() == UnaryOperator.LOGICAL_NOT) {
                return inner.getOperand();
            }
        }

        // Constant folding: !true -> false, !false -> true
        if (op == UnaryOperator.LOGICAL_NOT && operand instanceof IConstantExpression) {
            Object value = ((IConstantExpression) operand).getValue();
            if (Boolean.TRUE.equals(value)) {
                return exprFactory.getConstantExpression(
                        TypeModelFactory.getInstance().getPrimitiveType('Z'), Boolean.FALSE);
            }
            if (Boolean.FALSE.equals(value)) {
                return exprFactory.getConstantExpression(
                        TypeModelFactory.getInstance().getPrimitiveType('Z'), Boolean.TRUE);
            }
        }

        // Negate constant: -(5) -> -5
        if (op == UnaryOperator.NEG && operand instanceof IConstantExpression) {
            Object value = ((IConstantExpression) operand).getValue();
            Object negated = negateConstant(value);
            if (negated != null) {
                return exprFactory.getConstantExpression(unary.getType(), negated);
            }
        }

        if (operand != unary.getOperand()) {
            return exprFactory.getUnaryExpression(unary.getType(), operand, op);
        }

        return unary;
    }


    /**
     * Simplify a cast expression. Remove redundant casts where the type already matches.
     *
     * @param cast  the cast expression
     * @param depth current recursion depth
     * @return the simplified expression
     */
    private IExpression simplifyCast(ICastExpression cast, int depth) {
        IExpression operand = simplifyExpr(cast.getOperand(), depth);

        // Remove redundant cast: (int) intExpr -> intExpr
        if (operand.getType() != null && cast.getCastType() != null) {
            if (operand.getType().equals(cast.getCastType())) {
                return operand;
            }
        }

        if (operand != cast.getOperand()) {
            return ExpressionModelFactory.getInstance().getCastExpression(
                    cast.getType(), operand, cast.getCastType());
        }

        return cast;
    }


    /**
     * Simplify an assignment by simplifying the value expression.
     *
     * @param assignment the assignment expression
     * @param depth      current recursion depth
     * @return the simplified expression
     */
    private IExpression simplifyAssignment(IAssignmentExpression assignment, int depth) {
        IExpression target = simplifyExpr(assignment.getTarget(), depth);
        IExpression value = simplifyExpr(assignment.getValue(), depth);

        if (target != assignment.getTarget() || value != assignment.getValue()) {
            return ExpressionModelFactory.getInstance().getAssignmentExpression(
                    assignment.getType(), target, value);
        }

        return assignment;
    }


    /**
     * Fold two constant values with the given operator.
     *
     * @param left  the left value
     * @param right the right value
     * @param op    the operator
     * @return the folded result, or null if not foldable
     */
    private Object foldConstants(Object left, Object right, BinaryOperator op) {
        // Integer constant folding
        if (left instanceof Integer && right instanceof Integer) {
            return foldIntegers((Integer) left, (Integer) right, op);
        }

        // Boolean constant folding
        if (left instanceof Boolean && right instanceof Boolean) {
            return foldBooleans((Boolean) left, (Boolean) right, op);
        }

        // String concatenation: "a" + "b" -> "ab"
        if (op == BinaryOperator.ADD && left instanceof String && right instanceof String) {
            return (String) left + (String) right;
        }

        return null;
    }


    /**
     * Fold two integer constants.
     *
     * @param left  the left value
     * @param right the right value
     * @param op    the operator
     * @return the result, or null
     */
    private Object foldIntegers(int left, int right, BinaryOperator op) {
        switch (op) {
            case ADD: return left + right;
            case SUB: return left - right;
            case MUL: return left * right;
            case AND: return left & right;
            case OR: return left | right;
            case XOR: return left ^ right;
            case SHL: return left << right;
            case SHR: return left >> right;
            case USHR: return left >>> right;
            case EQ: return left == right;
            case NE: return left != right;
            case LT: return left < right;
            case GT: return left > right;
            case LE: return left <= right;
            case GE: return left >= right;
            default: break;
        }
        // Avoid division by zero
        if (right != 0) {
            if (op == BinaryOperator.DIV) {
                return left / right;
            }
            if (op == BinaryOperator.REM) {
                return left % right;
            }
        }
        return null;
    }


    /**
     * Fold two boolean constants.
     *
     * @param left  the left value
     * @param right the right value
     * @param op    the operator
     * @return the result, or null
     */
    private Object foldBooleans(boolean left, boolean right, BinaryOperator op) {
        switch (op) {
            case LOGICAL_AND: return left && right;
            case LOGICAL_OR: return left || right;
            case EQ: return left == right;
            case NE: return left != right;
            default: return null;
        }
    }


    /**
     * Simplify boolean operations with identity/annihilator values.
     *
     * @param left  the left operand
     * @param right the right operand
     * @param op    the operator
     * @return the simplified expression, or null
     */
    private IExpression simplifyBooleanOps(IExpression left, IExpression right, BinaryOperator op) {
        // x && true -> x
        if (op == BinaryOperator.LOGICAL_AND && isConstantBoolean(right, true)) {
            return left;
        }
        // true && x -> x
        if (op == BinaryOperator.LOGICAL_AND && isConstantBoolean(left, true)) {
            return right;
        }
        // x && false -> false
        if (op == BinaryOperator.LOGICAL_AND && isConstantBoolean(right, false)) {
            return right;
        }
        // x || false -> x
        if (op == BinaryOperator.LOGICAL_OR && isConstantBoolean(right, false)) {
            return left;
        }
        // false || x -> x
        if (op == BinaryOperator.LOGICAL_OR && isConstantBoolean(left, false)) {
            return right;
        }
        // x || true -> true
        if (op == BinaryOperator.LOGICAL_OR && isConstantBoolean(right, true)) {
            return right;
        }
        return null;
    }


    /**
     * Simplify arithmetic operations with identity values.
     *
     * @param left  the left operand
     * @param right the right operand
     * @param op    the operator
     * @return the simplified expression, or null
     */
    private IExpression simplifyArithmeticIdentity(IExpression left, IExpression right, BinaryOperator op) {
        // x + 0 -> x, x - 0 -> x
        if ((op == BinaryOperator.ADD || op == BinaryOperator.SUB) && isConstantInt(right, 0)) {
            return left;
        }
        // 0 + x -> x
        if (op == BinaryOperator.ADD && isConstantInt(left, 0)) {
            return right;
        }
        // x * 1 -> x
        if (op == BinaryOperator.MUL && isConstantInt(right, 1)) {
            return left;
        }
        // 1 * x -> x
        if (op == BinaryOperator.MUL && isConstantInt(left, 1)) {
            return right;
        }
        // x * 0 -> 0
        if (op == BinaryOperator.MUL && isConstantInt(right, 0)) {
            return right;
        }
        // x / 1 -> x
        if (op == BinaryOperator.DIV && isConstantInt(right, 1)) {
            return left;
        }
        return null;
    }


    /**
     * Negate a numeric constant.
     *
     * @param value the value
     * @return the negated value, or null
     */
    private Object negateConstant(Object value) {
        if (value instanceof Integer) {
            return -(Integer) value;
        }
        if (value instanceof Long) {
            return -(Long) value;
        }
        if (value instanceof Float) {
            return -(Float) value;
        }
        if (value instanceof Double) {
            return -(Double) value;
        }
        return null;
    }


    /**
     * Check if an expression is a constant boolean with the given value.
     *
     * @param expr  the expression
     * @param value the expected value
     * @return true if it matches
     */
    private boolean isConstantBoolean(IExpression expr, boolean value) {
        if (expr instanceof IConstantExpression) {
            Object val = ((IConstantExpression) expr).getValue();
            return Boolean.valueOf(value).equals(val);
        }
        return false;
    }


    /**
     * Check if an expression is a constant integer with the given value.
     *
     * @param expr  the expression
     * @param value the expected value
     * @return true if it matches
     */
    private boolean isConstantInt(IExpression expr, int value) {
        if (expr instanceof IConstantExpression) {
            Object val = ((IConstantExpression) expr).getValue();
            return Integer.valueOf(value).equals(val);
        }
        return false;
    }


    /**
     * Detect for-each loop patterns from the Iterator idiom:
     * {@code Iterator var = expr.iterator(); while (var.hasNext()) { Type elem = (Type) var.next(); ... }}
     * is transformed into {@code for (Type elem : expr) { ... }}.
     *
     * @param statements the statements to scan
     * @return statements with for-each loops where detected
     */
    private List<IStatement> detectForEachLoops(List<IStatement> statements) {
        List<IStatement> result = new ArrayList<>();
        boolean skipNext = false;

        for (int idx = 0; idx < statements.size(); idx++) {
            if (skipNext) {
                skipNext = false;
                continue;
            }

            if (idx + 1 < statements.size()) {
                IStatement forEach = tryMatchForEach(statements.get(idx), statements.get(idx + 1));
                if (forEach != null) {
                    result.add(forEach);
                    skipNext = true;
                    continue;
                }
            }
            result.add(statements.get(idx));
        }

        return result;
    }


    /**
     * Try to match a for-each pattern from two consecutive statements.
     * The first must be an Iterator variable declaration with an iterator() call,
     * the second a while loop with hasNext() condition and next() as the first body statement.
     *
     * @param first  the candidate Iterator declaration
     * @param second the candidate while loop
     * @return a for-each statement, or null if the pattern does not match
     */
    private IStatement tryMatchForEach(IStatement first, IStatement second) {
        if (!(first instanceof IVariableDeclarationStatement)) {
            return null;
        }
        if (!(second instanceof IWhileStatement)) {
            return null;
        }

        IVariableDeclarationStatement iterDecl = (IVariableDeclarationStatement) first;
        final IWhileStatement whileStmt = (IWhileStatement) second;

        // Check: init is a call to .iterator()
        if (!(iterDecl.getInitializer() instanceof IMethodCallExpression)) {
            return null;
        }
        IMethodCallExpression iterCall = (IMethodCallExpression) iterDecl.getInitializer();
        if (!"iterator".equals(iterCall.getMethodName())) {
            return null;
        }
        IExpression iterable = iterCall.getTarget();
        if (iterable == null) {
            return null;
        }
        String iterVarName = iterDecl.getVariableName();

        // Check: while condition is iterVar.hasNext()
        if (!isHasNextCall(whileStmt.getCondition(), iterVarName)) {
            return null;
        }

        // Check: first body statement is Type elem = (Type) iterVar.next()
        List<IStatement> bodyStmts = getBlockStatements(whileStmt.getBody());
        if (bodyStmts.isEmpty()) {
            return null;
        }
        IStatement firstBody = bodyStmts.get(0);
        if (!(firstBody instanceof IVariableDeclarationStatement)) {
            return null;
        }
        IVariableDeclarationStatement elemDecl = (IVariableDeclarationStatement) firstBody;
        if (!isNextCall(elemDecl.getInitializer(), iterVarName)) {
            return null;
        }

        // Build the for-each body (all body statements except the first next() assignment)
        List<IStatement> forBody = new ArrayList<>(bodyStmts.subList(1, bodyStmts.size()));
        IStatement body = StatementModelFactory.getInstance().getBlockStatement(null, forBody);

        return StatementModelFactory.getInstance().getForEachStatement(
                null, elemDecl.getVariableType(), elemDecl.getVariableName(), iterable, body);
    }


    /**
     * Check if an expression is a call to hasNext() on the given iterator variable.
     *
     * @param expr        the expression
     * @param iterVarName the iterator variable name
     * @return true if this is iterVar.hasNext()
     */
    private boolean isHasNextCall(IExpression expr, String iterVarName) {
        if (!(expr instanceof IMethodCallExpression)) {
            return false;
        }
        IMethodCallExpression call = (IMethodCallExpression) expr;
        if (!"hasNext".equals(call.getMethodName())) {
            return false;
        }
        if (call.getTarget() instanceof IVariableExpression) {
            return iterVarName.equals(((IVariableExpression) call.getTarget()).getName());
        }
        return false;
    }


    /**
     * Check if an expression is a call to next() on the given iterator variable,
     * optionally wrapped in a cast.
     *
     * @param expr        the expression
     * @param iterVarName the iterator variable name
     * @return true if this is (Type) iterVar.next() or iterVar.next()
     */
    private boolean isNextCall(IExpression expr, String iterVarName) {
        IExpression unwrapped = expr;
        if (unwrapped instanceof ICastExpression) {
            unwrapped = ((ICastExpression) unwrapped).getOperand();
        }
        if (!(unwrapped instanceof IMethodCallExpression)) {
            return false;
        }
        IMethodCallExpression call = (IMethodCallExpression) unwrapped;
        if (!"next".equals(call.getMethodName())) {
            return false;
        }
        if (call.getTarget() instanceof IVariableExpression) {
            return iterVarName.equals(((IVariableExpression) call.getTarget()).getName());
        }
        return false;
    }


    /**
     * Get the statements from a block or wrap a single statement in a list.
     *
     * @param statement the statement
     * @return the list of statements
     */
    private List<IStatement> getBlockStatements(IStatement statement) {
        if (statement instanceof IBlockStatement) {
            return ((IBlockStatement) statement).getStatements();
        }
        List<IStatement> single = new ArrayList<>();
        single.add(statement);
        return single;
    }


    /**
     * Detect for-loop patterns: init; while(cond) { body; update; } to for(init; cond; update) { body; }.
     * Also detects while(cond) { body; update; } to for(; cond; update) { body; } when there is
     * no preceding init statement but the body ends with a valid loop update.
     *
     * @param statements the statements to scan
     * @return statements with for-loops where detected
     */
    private List<IStatement> detectForLoops(List<IStatement> statements) {
        List<IStatement> result = new ArrayList<>();

        boolean skipNext = false;
        for (int idx = 0; idx < statements.size(); idx++) {
            if (skipNext) {
                skipNext = false;
                continue;
            }

            if (idx + 1 < statements.size() && isForLoopInit(statements.get(idx))
                    && statements.get(idx + 1) instanceof IWhileStatement) {
                IWhileStatement whileStmt = (IWhileStatement) statements.get(idx + 1);
                IStatement initStmt = statements.get(idx);
                IExpression updateExpr = extractLoopUpdate(whileStmt);

                if (updateExpr != null) {
                    IStatement forBody = removeLastStatement(whileStmt.getBody());
                    result.add(StatementModelFactory.getInstance().getForStatement(
                            null, initStmt, whileStmt.getCondition(), updateExpr, forBody));
                    skipNext = true;
                    continue;
                }
            }

            if (statements.get(idx) instanceof IWhileStatement) {
                IWhileStatement whileStmt = (IWhileStatement) statements.get(idx);
                IExpression updateExpr = extractLoopUpdate(whileStmt);

                if (updateExpr != null) {
                    IStatement forBody = removeLastStatement(whileStmt.getBody());
                    result.add(StatementModelFactory.getInstance().getForStatement(
                            null, null, whileStmt.getCondition(), updateExpr, forBody));
                    continue;
                }
            }

            result.add(statements.get(idx));
        }

        return result;
    }


    /**
     * Check if a statement can be a for-loop initializer.
     * Accepts variable declarations and expression statements containing assignments.
     *
     * @param stmt the statement
     * @return true if it is a variable declaration or an assignment expression statement
     */
    private boolean isForLoopInit(IStatement stmt) {
        if (stmt instanceof IVariableDeclarationStatement) {
            return true;
        }
        if (stmt instanceof IExpressionStatement) {
            IExpression expr = ((IExpressionStatement) stmt).getExpression();
            return expr instanceof IAssignmentExpression;
        }
        return false;
    }


    /**
     * Extract the update expression from the last statement of a while body.
     * Only returns an expression that is a valid loop update: increment, decrement,
     * assignment, or compound assignment.
     *
     * @param whileStmt the while statement
     * @return the update expression, or null if the last statement is not a valid update
     */
    private IExpression extractLoopUpdate(IWhileStatement whileStmt) {
        IStatement body = whileStmt.getBody();
        List<IStatement> bodyStmts;
        if (body instanceof IBlockStatement) {
            bodyStmts = ((IBlockStatement) body).getStatements();
        } else {
            return null;
        }

        if (bodyStmts.isEmpty()) {
            return null;
        }

        IStatement lastStmt = bodyStmts.get(bodyStmts.size() - 1);
        if (lastStmt instanceof IExpressionStatement) {
            IExpression expr = ((IExpressionStatement) lastStmt).getExpression();
            if (isForLoopUpdate(expr)) {
                return expr;
            }
        }
        return null;
    }


    /**
     * Check if an expression is a valid for-loop update.
     * Valid updates are: unary increment/decrement, assignment, compound assignment,
     * and method call expressions (e.g., iterator.next(), cursor.moveToNext()).
     *
     * @param expr the expression to check
     * @return true if the expression is a valid loop update
     */
    private boolean isForLoopUpdate(IExpression expr) {
        if (expr instanceof IUnaryExpression) {
            UnaryOperator op = ((IUnaryExpression) expr).getOperator();
            if (op == UnaryOperator.POST_INC || op == UnaryOperator.POST_DEC
                    || op == UnaryOperator.PRE_INC || op == UnaryOperator.PRE_DEC) {
                return true;
            }
        }
        if (expr instanceof IAssignmentExpression) {
            return true;
        }
        if (expr instanceof IMethodCallExpression) {
            return true;
        }
        return false;
    }


    /**
     * Remove the last statement from a body (the update expression).
     *
     * @param body the loop body
     * @return the body without the last statement
     */
    private IStatement removeLastStatement(IStatement body) {
        if (body instanceof IBlockStatement) {
            List<IStatement> stmts = ((IBlockStatement) body).getStatements();
            if (stmts.size() > 1) {
                return StatementModelFactory.getInstance().getBlockStatement(
                        null, stmts.subList(0, stmts.size() - 1));
            }
            return StatementModelFactory.getInstance().getBlockStatement(null, new ArrayList<>());
        }
        return body;
    }


    /**
     * Detect else-if chain patterns: consecutive if statements where each then-branch
     * ends with a return, throw, break, or continue, followed by a trailing statement.
     * Transforms them into proper if/else-if/else chains.
     *
     * @param statements the statements to scan
     * @return statements with else-if chains where detected
     */
    private List<IStatement> detectElseIfChains(List<IStatement> statements) {
        if (statements.size() < 2) {
            return statements;
        }

        // Find a run of consecutive if-with-exit statements at the end
        int chainStart = -1;
        for (int i = statements.size() - 2; i >= 0; i--) {
            if (statements.get(i) instanceof IIfStatement) {
                IIfStatement ifStmt = (IIfStatement) statements.get(i);
                if (ifStmt.getElseBranch() == null && branchExits(ifStmt.getThenBranch())) {
                    chainStart = i;
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        if (chainStart < 0 || chainStart >= statements.size() - 1) {
            return statements;
        }

        // Build the else-if chain from the end backwards
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        List<IStatement> trailing = new ArrayList<>(
                statements.subList(chainStart + 1, statements.size()));

        // Start with the trailing statements as the final else
        // Only wrap in else if there are statements to include
        // But don't include trailing statements that are just "return;" at the end of a void method
        IStatement elseBlock = null;
        if (!trailing.isEmpty()) {
            elseBlock = stmtFactory.getBlockStatement(null, trailing);
        }

        // Chain if statements from the last in the run to the first
        for (int i = chainStart + findChainEnd(statements, chainStart) - 1; i >= chainStart; i--) {
            IIfStatement ifStmt = (IIfStatement) statements.get(i);
            elseBlock = stmtFactory.getIfStatement(
                    ifStmt.getLabel(), ifStmt.getCondition(), ifStmt.getThenBranch(), elseBlock);
        }

        // Build result: statements before chain + the chained if/else-if/else
        List<IStatement> result = new ArrayList<>(statements.subList(0, chainStart));
        result.add(elseBlock);
        return result;
    }


    /**
     * Find how many consecutive if-with-exit statements form a chain starting at the given index.
     *
     * @param statements the statement list
     * @param start      the starting index
     * @return the count of consecutive if-with-exit statements
     */
    private int findChainEnd(List<IStatement> statements, int start) {
        int count = 0;
        for (int i = start; i < statements.size(); i++) {
            if (statements.get(i) instanceof IIfStatement) {
                IIfStatement ifStmt = (IIfStatement) statements.get(i);
                if (ifStmt.getElseBranch() == null && branchExits(ifStmt.getThenBranch())) {
                    count++;
                    continue;
                }
            }
            break;
        }
        return count;
    }


    /**
     * Check whether a branch exits its enclosing scope (return, throw, break, continue).
     *
     * @param branch the branch statement
     * @return true if the branch always exits
     */
    private boolean branchExits(IStatement branch) {
        IStatement stmt = branch;
        if (stmt instanceof IBlockStatement) {
            List<IStatement> stmts = ((IBlockStatement) stmt).getStatements();
            if (stmts.isEmpty()) {
                return false;
            }
            stmt = stmts.get(stmts.size() - 1);
        }
        return stmt instanceof IReturnStatement
                || stmt instanceof IThrowStatement
                || stmt instanceof IBreakStatement
                || stmt instanceof IContinueStatement;
    }


    /**
     * Detect pattern matching instanceof patterns. When an if-statement has an instanceof
     * condition and the then-branch starts with a cast of the same operand to the same type,
     * merge them into a pattern matching instanceof with a bound variable.
     *
     * @param statements the statements to scan
     * @return statements with pattern matching instanceof where detected
     */
    private List<IStatement> detectPatternInstanceof(List<IStatement> statements) {
        List<IStatement> result = new ArrayList<>();
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();

        for (IStatement stmt : statements) {
            if (!(stmt instanceof IIfStatement)) {
                result.add(stmt);
                continue;
            }

            IIfStatement ifStmt = (IIfStatement) stmt;
            if (!(ifStmt.getCondition() instanceof IInstanceOfExpression)) {
                result.add(stmt);
                continue;
            }

            IInstanceOfExpression instanceOf = (IInstanceOfExpression) ifStmt.getCondition();
            if (instanceOf.getPatternVariable() != null) {
                result.add(stmt);
                continue;
            }

            List<IStatement> thenStmts = getBlockStatements(ifStmt.getThenBranch());
            if (thenStmts.isEmpty()) {
                result.add(stmt);
                continue;
            }

            IStatement firstThen = thenStmts.get(0);
            if (!(firstThen instanceof IVariableDeclarationStatement)) {
                result.add(stmt);
                continue;
            }

            IVariableDeclarationStatement varDecl = (IVariableDeclarationStatement) firstThen;
            if (!matchesInstanceofCast(instanceOf, varDecl)) {
                result.add(stmt);
                continue;
            }

            IInstanceOfExpression patternInstanceof = exprFactory.getInstanceOfExpression(
                    instanceOf.getType(), instanceOf.getOperand(),
                    instanceOf.getCheckType(), varDecl.getVariableName());

            List<IStatement> newThenStmts = new ArrayList<>(thenStmts.subList(1, thenStmts.size()));
            IStatement newThenBranch = stmtFactory.getBlockStatement(null, newThenStmts);

            result.add(stmtFactory.getIfStatement(
                    ifStmt.getLabel(), patternInstanceof, newThenBranch, ifStmt.getElseBranch()));

            if (LOG.isDebugEnabled()) {
                LOG.debug("Detected pattern matching instanceof for variable: " + varDecl.getVariableName());
            }
        }

        return result;
    }


    /**
     * Check whether a variable declaration matches an instanceof cast pattern.
     * The declaration initializer must be a cast of the same operand to the same type.
     *
     * @param instanceOf the instanceof expression
     * @param varDecl    the variable declaration
     * @return true if the declaration is a cast matching the instanceof
     */
    private boolean matchesInstanceofCast(IInstanceOfExpression instanceOf,
                                           IVariableDeclarationStatement varDecl) {
        if (varDecl.getInitializer() == null) {
            return false;
        }
        if (!(varDecl.getInitializer() instanceof ICastExpression)) {
            return false;
        }

        ICastExpression cast = (ICastExpression) varDecl.getInitializer();
        if (!cast.getCastType().equals(instanceOf.getCheckType())) {
            return false;
        }

        return expressionsMatch(cast.getOperand(), instanceOf.getOperand());
    }


    /**
     * Check whether two expressions reference the same variable.
     *
     * @param first  the first expression
     * @param second the second expression
     * @return true if both are variable expressions with the same name
     */
    private boolean expressionsMatch(IExpression first, IExpression second) {
        if (first instanceof IVariableExpression && second instanceof IVariableExpression) {
            return ((IVariableExpression) first).getName()
                    .equals(((IVariableExpression) second).getName());
        }
        return first.equals(second);
    }


    /**
     * Detect switch expression patterns. When a variable declaration is followed by a switch
     * statement where every case assigns to the same variable and breaks, convert to a switch
     * expression assignment.
     *
     * @param statements the statements to scan
     * @return statements with switch expressions where detected
     */
    private List<IStatement> detectSwitchExpressions(List<IStatement> statements) {
        List<IStatement> result = new ArrayList<>();
        boolean skipNext = false;

        for (int idx = 0; idx < statements.size(); idx++) {
            if (skipNext) {
                skipNext = false;
                continue;
            }

            if (idx + 1 < statements.size()
                    && statements.get(idx) instanceof IVariableDeclarationStatement
                    && statements.get(idx + 1) instanceof ISwitchStatement) {
                IVariableDeclarationStatement varDecl =
                        (IVariableDeclarationStatement) statements.get(idx);
                ISwitchStatement switchStmt = (ISwitchStatement) statements.get(idx + 1);

                ISwitchExpression switchExpr = tryBuildSwitchExpression(
                        varDecl.getVariableName(), switchStmt, varDecl.getVariableType());
                if (switchExpr != null) {
                    result.add(StatementModelFactory.getInstance().getVariableDeclarationStatement(
                            varDecl.getLabel(), varDecl.getVariableType(),
                            varDecl.getVariableName(), switchExpr, varDecl.isFinal()));
                    skipNext = true;

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Detected switch expression for variable: " + varDecl.getVariableName());
                    }
                    continue;
                }
            }

            result.add(statements.get(idx));
        }

        return result;
    }


    /**
     * Try to build a switch expression from a switch statement where every case assigns
     * to the given variable and then breaks.
     *
     * @param targetVariable the variable name that each case assigns to
     * @param switchStmt     the switch statement
     * @param resultType     the result type of the switch expression
     * @return a switch expression, or null if the pattern does not match
     */
    private ISwitchExpression tryBuildSwitchExpression(String targetVariable,
                                                       ISwitchStatement switchStmt,
                                                       IType resultType) {
        if (switchStmt.getCases().isEmpty()) {
            return null;
        }

        ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();
        List<ISwitchExpressionCase> expressionCases = new ArrayList<>();

        for (ISwitchCase switchCase : switchStmt.getCases()) {
            IExpression assignedValue = extractCaseAssignment(switchCase, targetVariable);
            if (assignedValue == null) {
                return null;
            }
            expressionCases.add(exprFactory.getSwitchExpressionCase(
                    switchCase.getValues(), switchCase.isDefault(), assignedValue));
        }

        return exprFactory.getSwitchExpression(resultType, switchStmt.getSelector(), expressionCases);
    }


    /**
     * Extract the assigned value from a switch case that assigns to the target variable
     * and then breaks. The case must contain exactly an assignment followed by a break,
     * or a single return of the assigned value.
     *
     * @param switchCase     the switch case
     * @param targetVariable the variable name being assigned to
     * @return the assigned value expression, or null if the pattern does not match
     */
    private IExpression extractCaseAssignment(ISwitchCase switchCase, String targetVariable) {
        List<IStatement> stmts = switchCase.getStatements();
        if (stmts.size() != 2) {
            return null;
        }

        IStatement firstStmt = stmts.get(0);
        IStatement secondStmt = stmts.get(1);

        if (!(secondStmt instanceof IBreakStatement)) {
            return null;
        }

        if (!(firstStmt instanceof IExpressionStatement)) {
            return null;
        }

        IExpression expr = ((IExpressionStatement) firstStmt).getExpression();
        if (!(expr instanceof IAssignmentExpression)) {
            return null;
        }

        IAssignmentExpression assignment = (IAssignmentExpression) expr;
        if (!(assignment.getTarget() instanceof IVariableExpression)) {
            return null;
        }

        String assignedVar = ((IVariableExpression) assignment.getTarget()).getName();
        if (!targetVariable.equals(assignedVar)) {
            return null;
        }

        return assignment.getValue();
    }
}
