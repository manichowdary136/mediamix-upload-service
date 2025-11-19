package com.iri.mktgmix.upload.service.formula;

import com.iri.mktgmix.upload.service.formula.FormulaParser.BinaryExpression;
import com.iri.mktgmix.upload.service.formula.FormulaParser.ColumnExpression;
import com.iri.mktgmix.upload.service.formula.FormulaParser.Expression;
import com.iri.mktgmix.upload.service.formula.FormulaParser.ExpressionVisitor;
import com.iri.mktgmix.upload.service.formula.FormulaParser.FunctionExpression;
import com.iri.mktgmix.upload.service.formula.FormulaParser.LiteralExpression;
import com.iri.mktgmix.upload.service.formula.FormulaParser.Token;
import com.iri.mktgmix.upload.service.formula.FormulaParser.TokenType;
import com.iri.mktgmix.upload.service.formula.FormulaParser.UnaryExpression;

import java.util.*;

/**
 * AST visitor that renders a parsed formula expression as an equivalent SQL fragment.
 */
final class SqlExpressionRenderer implements ExpressionVisitor<String> {

    private final SqlFunctionRegistry functionRegistry;
    private final Map<String, String> columnMapping;

    SqlExpressionRenderer(SqlFunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
        this.columnMapping = Collections.emptyMap();
    }

    SqlExpressionRenderer(SqlFunctionRegistry functionRegistry, Map<String, String> columnMapping) {
        this.functionRegistry = functionRegistry;
        this.columnMapping = columnMapping != null ? columnMapping : Collections.emptyMap();
    }

    String render(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public String visitBinary(BinaryExpression expression) {
        String left = expression.left().accept(this);
        String right = expression.right().accept(this);
        return "(" + left + " " + operatorLexeme(expression.operator()) + " " + right + ")";
    }

    @Override
    public String visitUnary(UnaryExpression expression) {
        String operand = expression.operand().accept(this);
        return "(" + expression.operator().lexeme() + operand + ")";
    }

    @Override
    public String visitLiteral(LiteralExpression expression) {
        String value = expression.value();
        
        // Handle string literals - convert Excel double quotes to SQL single quotes
        if (value != null && value.length() >= 2) {
            // Check if it's a double-quoted string (Excel format)
            if (value.startsWith("\"") && value.endsWith("\"")) {
                // Extract content between quotes
                String content = value.substring(1, value.length() - 1);
                // Escape single quotes by doubling them (SQL standard)
                content = content.replace("'", "''");
                // Return as SQL string literal with single quotes
                return "'" + content + "'";
            }
            // Check if it's already a single-quoted string
            if (value.startsWith("'") && value.endsWith("'")) {
                // Already in SQL format, but ensure internal quotes are escaped
                String content = value.substring(1, value.length() - 1);
                content = content.replace("'", "''");
                return "'" + content + "'";
            }
        }
        
        // For numbers or other literals, return as-is
        return value;
    }

    @Override
    public String visitColumn(ColumnExpression expression) {
        String nameOriginal = expression.columnReference();
        // If column mapping is provided, map original name to sanitized name
        return Optional.ofNullable(columnMapping.get(nameOriginal))
                .orElseThrow(() -> new IllegalArgumentException(String.format("%s not found in DB column mapping", nameOriginal)));
    }

    @Override
    public String visitFunction(FunctionExpression expression) {
        SqlFunction function = functionRegistry.resolve(expression.functionName());
        List<String> arguments = new ArrayList<>(expression.arguments().size());
        for (Expression argument : expression.arguments()) {
            arguments.add(argument.accept(this));
        }
        return function.toSql(arguments);
    }

    private static String operatorLexeme(Token operator) {
        TokenType type = operator.type();
        switch (type) {
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case STAR:
                return "*";
            case SLASH:
                return "/";
            case GREATER:
                return ">";
            case LESS:
                return "<";
            case GREATER_EQUAL:
                return ">=";
            case LESS_EQUAL:
                return "<=";
            case EQUAL:
                return "=";
            case NOT_EQUAL:
                return "<>";
            default:
                throw new FormulaTranslationException("Unsupported operator: " + operator.lexeme());
        }
    }
}

