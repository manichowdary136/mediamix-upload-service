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
        assertThat(sql).isEqualTo("TRIM(APPEND(UPPER(B3), \" \", LOWER(C3), \" \", \"_\"))");
    }

    @Test
    void translatesArithmeticExpressionWithPrecedence() {
        ColumnFormula formula = ColumnFormula.of("target_col", "A + B * 0.1");
        String sql = service.translate(formula);
        assertThat(sql).isEqualTo("(A + (B * 0.1))");
    }


}

