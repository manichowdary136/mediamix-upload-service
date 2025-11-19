package com.iri.mktgmix.upload.service.formula;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple recursive descent parser that turns spreadsheet-like formulas
 * The result of parser can subsequently be rendered as SQL.
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
        return parseComparison();
    }

    private Expression parseComparison() {
        Expression expression = parseAddition();
        while (match(TokenType.GREATER, TokenType.LESS, TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL, TokenType.EQUAL, TokenType.NOT_EQUAL)) {
            Token operator = previous();
            Expression right = parseAddition();
            expression = new BinaryExpression(expression, operator, right);
        }
        return expression;
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
        return peek().type() == type;
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
        if (formula == null) {
            tokens.add(new Token(TokenType.EOF, ""));
            return tokens;
        }
        String input = formula.trim();
        if (input.isEmpty()) {
            tokens.add(new Token(TokenType.EOF, ""));
            return tokens;
        }
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
                case '+':
                    tokens.add(new Token(TokenType.PLUS, "+"));
                    index++;
                    break;
                case '-':
                    tokens.add(new Token(TokenType.MINUS, "-"));
                    index++;
                    break;
                case '*':
                    tokens.add(new Token(TokenType.STAR, "*"));
                    index++;
                    break;
                case '/':
                    tokens.add(new Token(TokenType.SLASH, "/"));
                    index++;
                    break;
                case '(':
                    tokens.add(new Token(TokenType.LEFT_PAREN, "("));
                    index++;
                    break;
                case ')':
                    tokens.add(new Token(TokenType.RIGHT_PAREN, ")"));
                    index++;
                    break;
                case ',':
                    tokens.add(new Token(TokenType.COMMA, ","));
                    index++;
                    break;
                case '>':
                    if (index + 1 < length && input.charAt(index + 1) == '=') {
                        tokens.add(new Token(TokenType.GREATER_EQUAL, ">="));
                        index += 2;
                    } else {
                        tokens.add(new Token(TokenType.GREATER, ">"));
                        index++;
                    }
                    break;
                case '<':
                    if (index + 1 < length && input.charAt(index + 1) == '=') {
                        tokens.add(new Token(TokenType.LESS_EQUAL, "<="));
                        index += 2;
                    } else if (index + 1 < length && input.charAt(index + 1) == '>') {
                        tokens.add(new Token(TokenType.NOT_EQUAL, "<>"));
                        index += 2;
                    } else {
                        tokens.add(new Token(TokenType.LESS, "<"));
                        index++;
                    }
                    break;
                case '=':
                    tokens.add(new Token(TokenType.EQUAL, "="));
                    index++;
                    break;
                case '!':
                    if (index + 1 < length && input.charAt(index + 1) == '=') {
                        tokens.add(new Token(TokenType.NOT_EQUAL, "!="));
                        index += 2;
                    } else {
                        throw new FormulaTranslationException("Unsupported character in formula: " + current);
                    }
                    break;
                case '"':
                    int closingDouble = readStringLiteral(input, index + 1, '"');
                    String literalDouble = input.substring(index, closingDouble + 1);
                    tokens.add(new Token(TokenType.STRING, literalDouble));
                    index = closingDouble + 1;
                    break;
                case '\'':
                    int closingSingle = readStringLiteral(input, index + 1, '\'');
                    String literalSingle = input.substring(index, closingSingle + 1);
                    tokens.add(new Token(TokenType.STRING, literalSingle));
                    index = closingSingle + 1;
                    break;
                case '[':
                    throw new FormulaTranslationException(
                        "Column references must follow SourceData[@Column] or SourceData[@[space column]] format. " +
                        "Standalone bracket notation is not supported."
                    );
                default:
                    if (Character.isDigit(current) || current == '.') {
                        int start = index;
                        index = readNumber(input, index);
                        tokens.add(new Token(TokenType.NUMBER, input.substring(start, index)));
                    } else if (isIdentifierStart(current)) {
                        int start = index;
                        index = readIdentifier(input, index);
                        String identifier = input.substring(start, index);
                        
                        if ("SourceData".equals(identifier)) {
                            int savedIndex = index;
                            while (savedIndex < length && Character.isWhitespace(input.charAt(savedIndex))) {
                                savedIndex++;
                            }
                            if (savedIndex + 1 < length && 
                                input.charAt(savedIndex) == '[' && 
                                input.charAt(savedIndex + 1) == '@') {
                                
                                if (savedIndex + 2 < length && input.charAt(savedIndex + 2) == '[') {
                                    int columnStart = savedIndex + 3;
                                    int columnEnd = readSourceDataColumnReference(input, columnStart);
                                    String columnName = input.substring(columnStart, columnEnd).trim();
                                    tokens.add(new Token(TokenType.IDENTIFIER, columnName));
                                    index = columnEnd + 2;
                                } else {
                                    int columnStart = savedIndex + 2;
                                    int columnEnd = readSourceDataSimpleColumnReference(input, columnStart);
                                    String columnName = input.substring(columnStart, columnEnd).trim();
                                    tokens.add(new Token(TokenType.IDENTIFIER, columnName));
                                    index = columnEnd + 1;
                                }
                            } else {
                                tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                            }
                        } else {
                            tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                        }
                    } else {
                        throw new FormulaTranslationException("Unsupported character in formula: " + current);
                    }
                    break;
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

    private static int readBracketedIdentifier(String input, int start) {
        int index = start;
        int length = input.length();
        while (index < length) {
            char current = input.charAt(index);
            if (current == ']') {
                return index;
            }
            if (current == '\\') {
                index++; // Skip escaped character
            }
            index++;
        }
        throw new FormulaTranslationException("Unclosed bracket in column name");
    }

    private static int readSourceDataColumnReference(String input, int start) {
        int index = start;
        int length = input.length();
        while (index < length) {
            char current = input.charAt(index);
            if (current == ']' && index + 1 < length && input.charAt(index + 1) == ']') {
                return index;
            }
            if (current == '\\') {
                index++;
            }
            index++;
        }
        throw new FormulaTranslationException("Unclosed bracket in SourceData column reference");
    }

    private static int readSourceDataSimpleColumnReference(String input, int start) {
        int index = start;
        int length = input.length();
        while (index < length) {
            char current = input.charAt(index);
            if (current == ']') {
                return index;
            }
            if (!isIdentifierPart(current)) {
                throw new FormulaTranslationException(
                    "Invalid character in SourceData[@Column] format. Column names without spaces should contain only alphanumeric characters, underscores, and dollar signs. " +
                    "For column names with spaces, use SourceData[@[space column]] format."
                );
            }
            index++;
        }
        throw new FormulaTranslationException("Unclosed bracket in SourceData column reference");
    }

    enum TokenType {
        IDENTIFIER,
        NUMBER,
        STRING,
        PLUS,
        MINUS,
        STAR,
        SLASH,
        GREATER,
        LESS,
        GREATER_EQUAL,
        LESS_EQUAL,
        EQUAL,
        NOT_EQUAL,
        LEFT_PAREN,
        RIGHT_PAREN,
        COMMA,
        EOF
    }

    static final class Token {
        private final TokenType type;
        private final String lexeme;

        Token(TokenType type, String lexeme) {
            this.type = type;
            this.lexeme = lexeme;
        }

        TokenType type() {
            return type;
        }

        String lexeme() {
            return lexeme;
        }
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

