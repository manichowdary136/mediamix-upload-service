package com.iri.mktgmix.upload.service.formula;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class FormulaTranslationServiceTest {

    private final FormulaTranslationService service = new FormulaTranslationService();

    @Test
    void translatesConcatFunction() {
        ColumnFormula formula = ColumnFormula.of("target_sub_brand", "CONCAT(source_a, source_b)");
        String sql = service.translate(formula);
        assertThat(sql).isEqualTo("CONCAT(source_a, source_b)");
    }

    @Test
    void translatesArithmeticExpressionWithPrecedence() {
        ColumnFormula formula = ColumnFormula.of("net_spend", "source_spend + source_tax * 0.1");
        String sql = service.translate(formula);
        assertThat(sql).isEqualTo("(source_spend + (source_tax * 0.1))");
    }

    @Test
    void translatesNestedFunctions() {
        ColumnFormula formula = ColumnFormula.of("clean_name", "TRIM(UPPER(source_name))");
        String sql = service.translate(formula);
        assertThat(sql).isEqualTo("TRIM(UPPER(source_name))");
    }

    @Test
    void translatesMultipleFormulasPreservingOrder() {
        List<ColumnFormula> formulas = Arrays.asList(
                ColumnFormula.of("colA", "source_a"),
                ColumnFormula.of("colB", "source_b")
        );
        Map<String, String> translated = service.translate(formulas);
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("colA", "source_a");
        expected.put("colB", "source_b");
        assertThat(translated).containsExactlyEntriesOf(expected);
    }
}

