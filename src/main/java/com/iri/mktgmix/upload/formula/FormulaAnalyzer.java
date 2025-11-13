package com.iri.mktgmix.upload.formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight helper that extracts column identifiers and arithmetic operators from a formula string.
 */
public final class FormulaAnalyzer {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Pattern OPERATOR = Pattern.compile("[+\\-*/]");

    private FormulaAnalyzer() {
    }

    /**
     * Parses the provided formula string and returns the ordered list of identifiers and operators.
     *
     * @param formula arithmetic expression such as {@code A + B - C}
     * @return parsed components with identifiers and operators preserved in appearance order
     */
    public static FormulaComponents analyze(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            return new FormulaComponents(Collections.emptyList(), Collections.emptyList());
        }

        List<String> columns = new ArrayList<>();
        List<String> operators = new ArrayList<>();

        Matcher identifierMatcher = IDENTIFIER.matcher(formula);
        while (identifierMatcher.find()) {
            columns.add(identifierMatcher.group());
        }

        Matcher operatorMatcher = OPERATOR.matcher(formula);
        while (operatorMatcher.find()) {
            operators.add(operatorMatcher.group());
        }

        return new FormulaComponents(Collections.unmodifiableList(columns), Collections.unmodifiableList(operators));
    }

    /**
     * Parsed representation of a formula.
     *
     * @param columns   ordered list of column identifiers
     * @param operators ordered list of arithmetic operators
     */
    public record FormulaComponents(List<String> columns, List<String> operators) {
    }
}

