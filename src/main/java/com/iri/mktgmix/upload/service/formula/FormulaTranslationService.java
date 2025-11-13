package com.iri.mktgmix.upload.service.formula;

import com.iri.mktgmix.upload.service.formula.FormulaParser.Expression;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Facade responsible for translating spreadsheet formulas into SQL fragments.
 * Uses a light-weight interpreter AST internally and delegates function rendering to strategies.
 */
public final class FormulaTranslationService {

    private final SqlFunctionRegistry functionRegistry;

    public FormulaTranslationService() {
        this(SqlFunctionRegistry.withDefaults());
    }

    public FormulaTranslationService(SqlFunctionRegistry functionRegistry) {
        this.functionRegistry = Objects.requireNonNull(functionRegistry, "functionRegistry must not be null");
    }

    /**
     * Translates a list of formulas into SQL fragments.
     * @param formulas formulas keyed by target column
     * @return ordered map with a SQL fragment per target column
     */
    public Map<String, String> translate(List<ColumnFormula> formulas) {
        Objects.requireNonNull(formulas, "formulas must not be null");
        Map<String, String> results = new LinkedHashMap<>();
        SqlExpressionRenderer renderer = new SqlExpressionRenderer(functionRegistry);
        for (ColumnFormula formula : formulas) {
            Expression expression = FormulaParser.parse(formula.getFormula());
            results.put(formula.getTargetColumn(), renderer.render(expression));
        }
        return Collections.unmodifiableMap(results);
    }

    /**
     * Convenience method for translating a single formula.
     */
    public String translate(ColumnFormula formula) {
        return translate(Collections.singletonList(formula)).get(formula.getTargetColumn());
    }
}

