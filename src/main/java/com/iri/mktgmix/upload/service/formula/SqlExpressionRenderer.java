package com.iri.mktgmix.upload.service.formula;

import com.iri.mktgmix.upload.service.formula.FormulaParser.BinaryExpression;
import com.iri.mktgmix.upload.service.formula.FormulaParser.ColumnExpression;
import com.iri.mktgmix.upload.service.formula.FormulaParser.ExpressionVisitor;
import com.iri.mktgmix.upload.service.formula.FormulaParser.FunctionExpression;
import com.iri.mktgmix.upload.service.formula.FormulaParser.LiteralExpression;
import com.iri.mktgmix.upload.service.formula.FormulaParser.UnaryExpression;
import com.iri.mktgmix.upload.service.formula.FormulaParser.Expression;
import com.iri.mktgmix.upload.service.formula.FormulaParser.Token;
import com.iri.mktgmix.upload.service.formula.FormulaParser.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * AST visitor that renders a parsed formula expression as an equivalent SQL fragment.
 */
final class SqlExpressionRenderer implements ExpressionVisitor<String> {

    private final SqlFunctionRegistry functionRegistry;

    SqlExpressionRenderer(SqlFunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
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
        return expression.value();
    }

    @Override
    public String visitColumn(ColumnExpression expression) {
        String key = expression.columnReference();
        return key;
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
        return switch (type) {
            case PLUS -> "+";
            case MINUS -> "-";
            case STAR -> "*";
            case SLASH -> "/";
            default -> throw new FormulaTranslationException("Unsupported operator: " + operator.lexeme());
        };
    }
}

