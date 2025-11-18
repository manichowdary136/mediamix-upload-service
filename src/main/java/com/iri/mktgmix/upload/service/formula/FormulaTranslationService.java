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
            
            String sqlExpression = Optional.ofNullable(formulaValue)
            .map(FormulaParser::parse).map(renderer::render)
            .orElse(sanitizedName);
        
                results.put(targetColumn, sqlExpression);
            

        }
        
        return Collections.unmodifiableMap(results);
    }
}

