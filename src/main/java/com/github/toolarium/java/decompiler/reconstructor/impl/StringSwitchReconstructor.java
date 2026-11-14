/*
 * StringSwitchReconstructor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reconstructor.impl;

import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.expression.ExpressionModelFactory;
import com.github.toolarium.java.decompiler.model.ast.expression.IAssignmentExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IConstantExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IMethodCallExpression;
import com.github.toolarium.java.decompiler.model.ast.expression.IVariableExpression;
import com.github.toolarium.java.decompiler.model.ast.statement.IBlockStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IExpressionStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IIfStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchCase;
import com.github.toolarium.java.decompiler.model.ast.statement.ISwitchStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.IVariableDeclarationStatement;
import com.github.toolarium.java.decompiler.model.ast.statement.StatementModelFactory;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import com.github.toolarium.java.decompiler.reconstructor.IReconstructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Detects the two-phase String switch bytecode pattern and merges it into a single
 * switch on the original String expression.
 *
 * <p>The compiler translates {@code switch(str)} into two switches:
 * <ol>
 *   <li>A lookupswitch on {@code str.hashCode()} with {@code equals()} checks
 *       that assign an index variable</li>
 *   <li>A tableswitch on the index variable with the actual case bodies</li>
 * </ol>
 *
 * <p>This reconstructor merges both phases back into a single
 * {@code switch(str)} with String case values.
 *
 * @author patrick
 */
public class StringSwitchReconstructor implements IReconstructor {
    private static final Logger LOG = LoggerFactory.getLogger(StringSwitchReconstructor.class);


    /**
     * Constructor for StringSwitchReconstructor.
     */
    public StringSwitchReconstructor() {
        // default constructor
    }


    /**
     * Get the name of this reconstructor.
     *
     * @return the name "StringSwitchReconstructor"
     */
    @Override
    public String getName() {
        return "StringSwitchReconstructor";
    }


    /**
     * Check whether this reconstructor applies to the given class.
     * Returns true for all classes since any class may contain a String switch.
     *
     * @param structClass the parsed class structure
     * @return true always
     */
    @Override
    public boolean applies(IStructClass structClass) {
        return true;
    }


    /**
     * Apply String switch reconstruction by detecting the two-phase pattern in method
     * bodies and merging them into a single switch statement.
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
            LOG.debug("Reconstructed String switch patterns in: " + structClass.getName());
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
     * Transform a method by merging String switch patterns in its body.
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
     * Transform a statement, recursively processing block statements to find and merge
     * String switch patterns.
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
     * Transform a block statement by scanning for the two-phase String switch pattern
     * and merging matches into a single switch.
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
            // Try to match the full pattern: [stringDecl] [indexDecl] hashSwitch indexSwitch
            int consumed = tryMergeStringSwitchAt(statements, i, result);
            if (consumed > 0) {
                i += consumed;
                changed = true;
            } else {
                // Recursively transform nested blocks
                IStatement transformed = transformStatement(statements.get(i));
                if (transformed != statements.get(i)) {
                    changed = true;
                }
                result.add(transformed);
                i++;
            }
        }

        if (!changed) {
            return block;
        }

        return StatementModelFactory.getInstance().getBlockStatement(block.getLabel(), result);
    }


    /**
     * Try to match and merge a String switch pattern starting at the given index.
     * The pattern is: optional String temp variable, int index variable initialized to -1,
     * a switch on hashCode(), and a switch on the index variable.
     *
     * @param statements the statement list
     * @param start      the starting index
     * @param result     the output list to add the merged switch to
     * @return the number of statements consumed, or 0 if no match
     */
    private int tryMergeStringSwitchAt(List<IStatement> statements, int start, List<IStatement> result) {
        int idx = start;

        // Optional: String tempVar = stringExpr;
        String stringVarName = null;
        IExpression originalStringExpr = null;
        if (idx < statements.size() && statements.get(idx) instanceof IVariableDeclarationStatement) {
            IVariableDeclarationStatement varDecl = (IVariableDeclarationStatement) statements.get(idx);
            if (isStringType(varDecl.getVariableType()) && varDecl.getInitializer() != null) {
                stringVarName = varDecl.getVariableName();
                originalStringExpr = varDecl.getInitializer();
                idx++;
            }
        }

        // Required: int indexVar = -1;
        if (idx >= statements.size() || !(statements.get(idx) instanceof IVariableDeclarationStatement)) {
            return 0;
        }
        IVariableDeclarationStatement indexDecl = (IVariableDeclarationStatement) statements.get(idx);
        if (!isIntType(indexDecl.getVariableType()) || !isConstantInt(indexDecl.getInitializer(), -1)) {
            return 0;
        }
        final String indexVarName = indexDecl.getVariableName();
        idx++;

        // Required: switch on hashCode()
        if (idx >= statements.size() || !(statements.get(idx) instanceof ISwitchStatement)) {
            return 0;
        }
        ISwitchStatement hashSwitch = (ISwitchStatement) statements.get(idx);
        if (!isHashCodeSwitch(hashSwitch, stringVarName)) {
            return 0;
        }
        idx++;

        // Required: switch on indexVar
        if (idx >= statements.size() || !(statements.get(idx) instanceof ISwitchStatement)) {
            return 0;
        }
        ISwitchStatement indexSwitch = (ISwitchStatement) statements.get(idx);
        if (!isIndexVarSwitch(indexSwitch, indexVarName)) {
            return 0;
        }
        idx++;

        // Extract the string-to-index mapping from the hashCode switch
        Map<Integer, String> indexToString = extractIndexToStringMapping(hashSwitch, stringVarName, indexVarName);
        if (indexToString.isEmpty()) {
            return 0;
        }

        // Determine the original string expression to use as the selector
        IExpression selector = originalStringExpr;
        if (selector == null) {
            selector = extractHashCodeTarget(hashSwitch);
        }
        if (selector == null) {
            return 0;
        }

        // Build the merged switch
        ISwitchStatement merged = buildMergedSwitch(selector, indexSwitch, indexToString);
        result.add(merged);

        int consumed = idx - start;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Merged String switch pattern consuming " + consumed + " statements.");
        }
        return consumed;
    }


    /**
     * Check if a switch statement's selector is a hashCode() call, optionally on a
     * known string variable.
     *
     * @param switchStmt    the switch statement
     * @param stringVarName the expected string variable name, or null to accept any
     * @return true if the selector is a hashCode() method call
     */
    private boolean isHashCodeSwitch(ISwitchStatement switchStmt, String stringVarName) {
        IExpression selector = switchStmt.getSelector();
        if (!(selector instanceof IMethodCallExpression)) {
            return false;
        }
        IMethodCallExpression call = (IMethodCallExpression) selector;
        if (!"hashCode".equals(call.getMethodName())) {
            return false;
        }
        if (stringVarName != null && call.getTarget() instanceof IVariableExpression) {
            return stringVarName.equals(((IVariableExpression) call.getTarget()).getName());
        }
        return true;
    }


    /**
     * Check if a switch statement's selector is the index variable.
     *
     * @param switchStmt   the switch statement
     * @param indexVarName the expected index variable name
     * @return true if the selector is the index variable
     */
    private boolean isIndexVarSwitch(ISwitchStatement switchStmt, String indexVarName) {
        IExpression selector = switchStmt.getSelector();
        if (selector instanceof IVariableExpression) {
            return indexVarName.equals(((IVariableExpression) selector).getName());
        }
        return false;
    }


    /**
     * Extract the target of the hashCode() call from the switch selector.
     *
     * @param hashSwitch the hashCode switch statement
     * @return the expression that hashCode() is called on
     */
    private IExpression extractHashCodeTarget(ISwitchStatement hashSwitch) {
        IExpression selector = hashSwitch.getSelector();
        if (selector instanceof IMethodCallExpression) {
            return ((IMethodCallExpression) selector).getTarget();
        }
        return null;
    }


    /**
     * Extract the mapping from index values to String literals from the hashCode switch.
     * Each case in the hashCode switch contains if(var.equals("literal")) { indexVar = N; }
     * patterns.
     *
     * @param hashSwitch    the hashCode switch
     * @param stringVarName the string variable name
     * @param indexVarName  the index variable name
     * @return a map from index values to the corresponding String literals
     */
    private Map<Integer, String> extractIndexToStringMapping(ISwitchStatement hashSwitch,
                                                              String stringVarName,
                                                              String indexVarName) {
        Map<Integer, String> mapping = new HashMap<>();

        for (ISwitchCase switchCase : hashSwitch.getCases()) {
            for (IStatement stmt : switchCase.getStatements()) {
                extractMappingsFromStatement(stmt, stringVarName, indexVarName, mapping);
            }
        }

        return mapping;
    }


    /**
     * Extract index-to-string mappings from a single statement. Handles if statements
     * with equals() checks and assignment bodies.
     *
     * @param statement     the statement to examine
     * @param stringVarName the string variable name
     * @param indexVarName  the index variable name
     * @param mapping       the output mapping to add to
     */
    private void extractMappingsFromStatement(IStatement statement, String stringVarName,
                                               String indexVarName, Map<Integer, String> mapping) {
        if (statement instanceof IIfStatement) {
            IIfStatement ifStmt = (IIfStatement) statement;
            String literal = extractEqualsLiteral(ifStmt.getCondition(), stringVarName);
            if (literal != null) {
                Integer index = extractIndexAssignment(ifStmt.getThenBranch(), indexVarName);
                if (index != null) {
                    mapping.put(index, literal);
                }
            }
        }

        // Handle blocks that contain if statements
        if (statement instanceof IBlockStatement) {
            for (IStatement inner : ((IBlockStatement) statement).getStatements()) {
                extractMappingsFromStatement(inner, stringVarName, indexVarName, mapping);
            }
        }
    }


    /**
     * Extract the String literal from an equals() call condition.
     * Matches patterns like: tempVar.equals("literal") or "literal".equals(tempVar).
     *
     * @param condition     the condition expression
     * @param stringVarName the expected string variable name, or null
     * @return the String literal, or null if the pattern does not match
     */
    private String extractEqualsLiteral(IExpression condition, String stringVarName) {
        if (!(condition instanceof IMethodCallExpression)) {
            return null;
        }
        IMethodCallExpression call = (IMethodCallExpression) condition;
        if (!"equals".equals(call.getMethodName())) {
            return null;
        }
        if (call.getArguments().size() != 1) {
            return null;
        }

        // Pattern: tempVar.equals("literal")
        if (call.getArguments().get(0) instanceof IConstantExpression) {
            Object value = ((IConstantExpression) call.getArguments().get(0)).getValue();
            if (value instanceof String) {
                return (String) value;
            }
        }

        // Pattern: "literal".equals(tempVar)
        if (call.getTarget() instanceof IConstantExpression) {
            Object value = ((IConstantExpression) call.getTarget()).getValue();
            if (value instanceof String && call.getArguments().get(0) instanceof IVariableExpression) {
                return (String) value;
            }
        }

        return null;
    }


    /**
     * Extract the index value from an assignment statement in an if branch.
     * Matches patterns like: indexVar = N.
     *
     * @param branch       the then-branch of an if statement
     * @param indexVarName the expected index variable name
     * @return the index value, or null if the pattern does not match
     */
    private Integer extractIndexAssignment(IStatement branch, String indexVarName) {
        IStatement stmt = branch;
        if (stmt instanceof IBlockStatement) {
            List<IStatement> stmts = ((IBlockStatement) stmt).getStatements();
            if (stmts.isEmpty()) {
                return null;
            }
            stmt = stmts.get(0);
        }

        if (!(stmt instanceof IExpressionStatement)) {
            return null;
        }
        IExpression expr = ((IExpressionStatement) stmt).getExpression();
        if (!(expr instanceof IAssignmentExpression)) {
            return null;
        }
        IAssignmentExpression assignment = (IAssignmentExpression) expr;

        if (assignment.getTarget() instanceof IVariableExpression) {
            String name = ((IVariableExpression) assignment.getTarget()).getName();
            if (indexVarName.equals(name) && assignment.getValue() instanceof IConstantExpression) {
                Object value = ((IConstantExpression) assignment.getValue()).getValue();
                if (value instanceof Integer) {
                    return (Integer) value;
                }
            }
        }

        return null;
    }


    /**
     * Build the merged switch statement using String literal case values from the
     * hashCode switch and case bodies from the index switch.
     *
     * @param selector      the original String expression to switch on
     * @param indexSwitch   the index switch with the actual case bodies
     * @param indexToString mapping from index values to String literals
     * @return the merged switch statement
     */
    private ISwitchStatement buildMergedSwitch(IExpression selector, ISwitchStatement indexSwitch,
                                                Map<Integer, String> indexToString) {
        StatementModelFactory stmtFactory = StatementModelFactory.getInstance();
        ExpressionModelFactory exprFactory = ExpressionModelFactory.getInstance();
        List<ISwitchCase> mergedCases = new ArrayList<>();

        for (ISwitchCase indexCase : indexSwitch.getCases()) {
            if (indexCase.isDefault()) {
                mergedCases.add(stmtFactory.getSwitchCase(indexCase.getValues(), true, indexCase.getStatements()));
                continue;
            }

            // Replace integer case values with the corresponding String literals
            List<IExpression> stringValues = new ArrayList<>();
            for (IExpression caseValue : indexCase.getValues()) {
                if (caseValue instanceof IConstantExpression) {
                    Object val = ((IConstantExpression) caseValue).getValue();
                    if (val instanceof Integer) {
                        String literal = indexToString.get(val);
                        if (literal != null) {
                            stringValues.add(exprFactory.getConstantExpression(
                                    TypeModelFactory.getInstance().getObjectType("java/lang/String"),
                                    literal));
                            continue;
                        }
                    }
                }
                stringValues.add(caseValue);
            }

            mergedCases.add(stmtFactory.getSwitchCase(stringValues, false, indexCase.getStatements()));
        }

        return stmtFactory.getSwitchStatement(null, selector, mergedCases);
    }


    /**
     * Check if a type represents java.lang.String.
     *
     * @param type the type to check
     * @return true if it is a String type
     */
    private boolean isStringType(com.github.toolarium.java.decompiler.model.type.IType type) {
        if (type == null) {
            return false;
        }
        String name = type.getDisplayName();
        return "String".equals(name) || "java.lang.String".equals(name);
    }


    /**
     * Check if a type represents an int.
     *
     * @param type the type to check
     * @return true if it is an int type
     */
    private boolean isIntType(com.github.toolarium.java.decompiler.model.type.IType type) {
        if (type == null) {
            return false;
        }
        return "int".equals(type.getDisplayName());
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
}
