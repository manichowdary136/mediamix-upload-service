package com.iri.mktgmix.upload.service.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Simple recursive descent parser that turns spreadsheet-like formulas into an abstract syntax tree (AST).
 * The resulting AST can subsequently be rendered as SQL.
 */
final class FormulaParser {

    private final String formula;
    private final List<Token> tokens;
    private int current;

    private FormulaParser(String formula) {
        this.formula = formula;
        this.tokens = tokenize(formula);
        this.current = 0;
    }

    static Expression parse(String formula) {
        FormulaParser parser = new FormulaParser(formula);
        Expression expression = parser.parseExpression();
        parser.consume(TokenType.EOF, "Unexpected trailing characters in formula");
        return expression;
    }

    private Expression parseExpression() {
        return parseAddition();
    }

    private Expression parseAddition() {
        Expression expression = parseMultiplication();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = parseMultiplication();
            expression = new BinaryExpression(expression, operator, right);
        }
        return expression;
    }

    private Expression parseMultiplication() {
        Expression expression = parseUnary();
        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expression right = parseUnary();
            expression = new BinaryExpression(expression, operator, right);
        }
        return expression;
    }

    private Expression parseUnary() {
        if (match(TokenType.MINUS)) {
            Token operator = previous();
            Expression right = parseUnary();
            return new UnaryExpression(operator, right);
        }
        return parsePrimary();
    }

    private Expression parsePrimary() {
        if (match(TokenType.NUMBER)) {
            return new LiteralExpression(previous().lexeme());
        }
        if (match(TokenType.STRING)) {
            return new LiteralExpression(previous().lexeme());
        }
        if (match(TokenType.IDENTIFIER)) {
            Token identifier = previous();
            if (match(TokenType.LEFT_PAREN)) {
                List<Expression> arguments = parseArguments();
                consume(TokenType.RIGHT_PAREN, "Expected ')' after function arguments");
                return new FunctionExpression(identifier.lexeme(), arguments);
            }
            return new ColumnExpression(identifier.lexeme());
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expression nested = parseExpression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression");
            return nested;
        }
        throw error(peek(), "Unexpected token: " + peek().lexeme());
    }

    private List<Expression> parseArguments() {
        List<Expression> arguments = new ArrayList<>();
        if (check(TokenType.RIGHT_PAREN)) {
            return arguments;
        }
        do {
            arguments.add(parseExpression());
        } while (match(TokenType.COMMA));
        return arguments;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        return !isAtEnd() && peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private static FormulaTranslationException error(Token token, String message) {
        return new FormulaTranslationException("Error parsing formula near '" + token.lexeme() + "': " + message);
    }

    private static List<Token> tokenize(String formula) {
        List<Token> tokens = new ArrayList<>();
        if (formula == null || formula.isBlank()) {
            return List.of(new Token(TokenType.EOF, ""));
        }
        String input = formula.trim();
        if (input.startsWith("=")) {
            input = input.substring(1);
        }
        int length = input.length();
        int index = 0;
        while (index < length) {
            char current = input.charAt(index);
            if (Character.isWhitespace(current)) {
                index++;
                continue;
            }
            switch (current) {
                case '+' -> {
                    tokens.add(new Token(TokenType.PLUS, "+"));
                    index++;
                }
                case '-' -> {
                    tokens.add(new Token(TokenType.MINUS, "-"));
                    index++;
                }
                case '*' -> {
                    tokens.add(new Token(TokenType.STAR, "*"));
                    index++;
                }
                case '/' -> {
                    tokens.add(new Token(TokenType.SLASH, "/"));
                    index++;
                }
                case '(' -> {
                    tokens.add(new Token(TokenType.LEFT_PAREN, "("));
                    index++;
                }
                case ')' -> {
                    tokens.add(new Token(TokenType.RIGHT_PAREN, ")"));
                    index++;
                }
                case ',' -> {
                    tokens.add(new Token(TokenType.COMMA, ","));
                    index++;
                }
                case '"' -> {
                    int closing = readStringLiteral(input, index + 1, '"');
                    String literal = input.substring(index, closing + 1);
                    tokens.add(new Token(TokenType.STRING, literal));
                    index = closing + 1;
                }
                case '\'' -> {
                    int closing = readStringLiteral(input, index + 1, '\'');
                    String literal = input.substring(index, closing + 1);
                    tokens.add(new Token(TokenType.STRING, literal));
                    index = closing + 1;
                }
                default -> {
                    if (Character.isDigit(current) || current == '.') {
                        int start = index;
                        index = readNumber(input, index);
                        tokens.add(new Token(TokenType.NUMBER, input.substring(start, index)));
                    } else if (isIdentifierStart(current)) {
                        int start = index;
                        index = readIdentifier(input, index);
                        tokens.add(new Token(TokenType.IDENTIFIER, input.substring(start, index)));
                    } else {
                        throw new FormulaTranslationException("Unsupported character in formula: " + current);
                    }
                }
            }
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private static int readIdentifier(String input, int index) {
        int cursor = index;
        int length = input.length();
        while (cursor < length && isIdentifierPart(input.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    private static boolean isIdentifierStart(char character) {
        return Character.isLetter(character) || character == '_' || character == '$';
    }

    private static boolean isIdentifierPart(char character) {
        return Character.isLetterOrDigit(character) || character == '_' || character == '$';
    }

    private static int readNumber(String input, int index) {
        int cursor = index;
        int length = input.length();
        boolean hasDecimal = false;
        while (cursor < length) {
            char character = input.charAt(cursor);
            if (character == '.') {
                if (hasDecimal) {
                    break;
                }
                hasDecimal = true;
            } else if (!Character.isDigit(character)) {
                break;
            }
            cursor++;
        }
        return cursor;
    }

    private static int readStringLiteral(String input, int start, char terminator) {
        int index = start;
        int length = input.length();
        while (index < length) {
            char current = input.charAt(index);
            if (current == terminator) {
                return index;
            }
            if (current == '\\') {
                index++;
            }
            index++;
        }
        throw new FormulaTranslationException("Unterminated string literal in formula");
    }

    enum TokenType {
        IDENTIFIER,
        NUMBER,
        STRING,
        PLUS,
        MINUS,
        STAR,
        SLASH,
        LEFT_PAREN,
        RIGHT_PAREN,
        COMMA,
        EOF
    }

    record Token(TokenType type, String lexeme) {
    }

    interface Expression {
        <T> T accept(ExpressionVisitor<T> visitor);
    }

    interface ExpressionVisitor<T> {
        T visitBinary(BinaryExpression expression);

        T visitUnary(UnaryExpression expression);

        T visitLiteral(LiteralExpression expression);

        T visitColumn(ColumnExpression expression);

        T visitFunction(FunctionExpression expression);
    }

    static final class BinaryExpression implements Expression {
        private final Expression left;
        private final Token operator;
        private final Expression right;

        BinaryExpression(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public Expression left() {
            return left;
        }

        public Token operator() {
            return operator;
        }

        public Expression right() {
            return right;
        }

        @Override
        public <T> T accept(ExpressionVisitor<T> visitor) {
            return visitor.visitBinary(this);
        }
    }

    static final class UnaryExpression implements Expression {
        private final Token operator;
        private final Expression operand;

        UnaryExpression(Token operator, Expression operand) {
            this.operator = operator;
            this.operand = operand;
        }

        public Token operator() {
            return operator;
        }

        public Expression operand() {
            return operand;
        }

        @Override
        public <T> T accept(ExpressionVisitor<T> visitor) {
            return visitor.visitUnary(this);
        }
    }

    static final class LiteralExpression implements Expression {
        private final String value;

        LiteralExpression(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        public <T> T accept(ExpressionVisitor<T> visitor) {
            return visitor.visitLiteral(this);
        }
    }

    static final class ColumnExpression implements Expression {
        private final String columnReference;

        ColumnExpression(String columnReference) {
            this.columnReference = columnReference;
        }

        public String columnReference() {
            return columnReference;
        }

        @Override
        public <T> T accept(ExpressionVisitor<T> visitor) {
            return visitor.visitColumn(this);
        }
    }

    static final class FunctionExpression implements Expression {
        private final String functionName;
        private final List<Expression> arguments;

        FunctionExpression(String functionName, List<Expression> arguments) {
            this.functionName = functionName;
            this.arguments = arguments;
        }

        public String functionName() {
            return functionName;
        }

        public List<Expression> arguments() {
            return arguments;
        }

        @Override
        public <T> T accept(ExpressionVisitor<T> visitor) {
            return visitor.visitFunction(this);
        }
    }
}

