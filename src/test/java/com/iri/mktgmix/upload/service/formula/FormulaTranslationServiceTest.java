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
        ColumnFormula formula = ColumnFormula.of("target_AB", "TRIM(CONCAT(UPPER(B3), \" \", LOWER(C3), \" \", \"_\"))");
        String sql = service.translate(formula);
        assertThat(sql).isEqualTo("TRIM(UPPER(B3) || ' ' || LOWER(C3) || ' ' || '_')");
    }

    @Test
    void translatesArithmeticExpressionWithPrecedence() {
        ColumnFormula formula = ColumnFormula.of("target_col", "A + B * 0.1");
        String sql = service.translate(formula);
        assertThat(sql).isEqualTo("(A + (B * 0.1))");
    }

    @Test
    void translatesComparisonOperators() {
        // Excel format with double quotes
        ColumnFormula formula = ColumnFormula.of("result", "IF(A > B, \"Small\", \"Large\")");
        String sql = service.translate(formula);
        assertThat(sql).isEqualTo("CASE WHEN (A > B) THEN 'Small' ELSE 'Large' END");
    }

    @Test
    void translatesAllComparisonOperators() {
        ColumnFormula formula1 = ColumnFormula.of("gt", "A > B");
        assertThat(service.translate(formula1)).isEqualTo("(A > B)");

        ColumnFormula formula2 = ColumnFormula.of("lt", "A < B");
        assertThat(service.translate(formula2)).isEqualTo("(A < B)");

        ColumnFormula formula3 = ColumnFormula.of("gte", "A >= B");
        assertThat(service.translate(formula3)).isEqualTo("(A >= B)");

        ColumnFormula formula4 = ColumnFormula.of("lte", "A <= B");
        assertThat(service.translate(formula4)).isEqualTo("(A <= B)");

        ColumnFormula formula5 = ColumnFormula.of("eq", "A = B");
        assertThat(service.translate(formula5)).isEqualTo("(A = B)");

        ColumnFormula formula6 = ColumnFormula.of("ne", "A != B");
        assertThat(service.translate(formula6)).isEqualTo("(A <> B)");

        ColumnFormula formula7 = ColumnFormula.of("ne2", "A <> B");
        assertThat(service.translate(formula7)).isEqualTo("(A <> B)");
    }

}

