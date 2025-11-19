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
            String targetColumn = formula.getTargetColumn();
            try {
                Expression expression = FormulaParser.parse(formula.getFormula());
                results.put(targetColumn, renderer.render(expression));
            } catch (FormulaTranslationException e) {
                if (e.getTargetColumn() == null) {
                    throw FormulaTranslationException.withTargetColumn(e.getMessage(), targetColumn, e);
                }
                throw e;
            } catch (RuntimeException e) {
                throw FormulaTranslationException.withTargetColumn(
                    e.getMessage() != null ? e.getMessage() : "Unexpected error during formula translation",
                    targetColumn,
                    e
                );
            }
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
     * @return Map of targetColumn -> SQL expression
     */
    public Map<String, String> translate(List<ColumnFormula> formulas, Map<String, String> columnMapping) {
        Objects.requireNonNull(formulas, "formulas must not be null");
        Objects.requireNonNull(columnMapping, "columnMapping must not be null");
        
        Map<String, String> results = new LinkedHashMap<>();
        SqlExpressionRenderer renderer = new SqlExpressionRenderer(functionRegistry, columnMapping);
        
        for (ColumnFormula formula : formulas) {
            String formulaValue = formula.getFormula();
            String targetColumn = formula.getTargetColumn();
            String sanitizedName = Optional.ofNullable(columnMapping.get(targetColumn))
                .orElse(targetColumn);
            
            try {
                String sqlExpression = Optional.ofNullable(formulaValue)
                    .filter(s -> !s.trim().isEmpty())
                    .map(f -> {
                        try {
                            Expression expression = FormulaParser.parse(f);
                            return renderer.render(expression);
                        } catch (FormulaTranslationException e) {
                            if (e.getTargetColumn() == null) {
                                throw FormulaTranslationException.withTargetColumn(e.getMessage(), targetColumn, e);
                            }
                            throw e;
                        } catch (RuntimeException e) {
                            throw FormulaTranslationException.withTargetColumn(
                                e.getMessage() != null ? e.getMessage() : "Unexpected error during formula translation",
                                targetColumn,
                                e
                            );
                        }
                    })
                    .orElse(sanitizedName);
                
                results.put(targetColumn, sqlExpression);
            } catch (FormulaTranslationException e) {
                throw e;
            } catch (RuntimeException e) {
                throw FormulaTranslationException.withTargetColumn(
                    e.getMessage() != null ? e.getMessage() : "Unexpected error during formula translation",
                    targetColumn,
                    e
                );
            }
        }
        
        return Collections.unmodifiableMap(results);
    }
}

