package com.iri.mktgmix.upload.service.formula;

import com.iri.mktgmix.upload.service.formula.FormulaParser.Expression;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Service layer responsible for translating spreadsheet formulas into SQL fragments.
 */
@Service
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

    /**
     * Translates a list of formulas into SQL fragments using column mapping.
     * Column mapping: name_original -> name_sanitized
     * 
     * @param formulas List of column formulas to translate
     * @param columnMapping Map from original column names to sanitized column names
     * @return Map of targetColumn (Object - can be ID or name) -> SQL expression
     */
    public Map<Object, String> translate(List<ColumnFormula> formulas, Map<String, String> columnMapping) {
        Objects.requireNonNull(formulas, "formulas must not be null");
        Objects.requireNonNull(columnMapping, "columnMapping must not be null");
        
        Map<Object, String> results = new LinkedHashMap<>();
        SqlExpressionRenderer renderer = new SqlExpressionRenderer(functionRegistry, columnMapping);
        
        for (ColumnFormula formula : formulas) {
            String formulaValue = formula.getFormula();
            Object targetColumn = formula.getTargetColumn();
            
            String sqlExpression = Optional.ofNullable(formulaValue)
                    .filter(s -> !s.trim().isEmpty())
                    .map(FormulaParser::parse)
                    .map(renderer::render)
                    .orElse(null); // Will be handled below
            
            // If formula is empty/null, we need to determine the sanitized name
            // This should not happen in practice, but handle it gracefully
            if (sqlExpression == null) {
                String targetColumnName = formula.getTargetColumnName();
                if (targetColumnName != null) {
                    sqlExpression = Optional.ofNullable(columnMapping.get(targetColumnName))
                            .orElse(targetColumnName);
                } else {
                    // Fallback: use targetColumn as string
                    sqlExpression = targetColumn != null ? targetColumn.toString() : "";
                }
            }
            
            results.put(targetColumn, sqlExpression);
        }
        
        return Collections.unmodifiableMap(results);
    }
}

