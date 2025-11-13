package com.iri.mktgmix.upload.formula;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormulaAnalyzerTest {

    @Test
    void extractsColumnsAndOperators() {
        FormulaAnalyzer.FormulaComponents result = FormulaAnalyzer.analyze("A + B - C");

        assertEquals(List.of("A", "B", "C"), result.columns());
        assertEquals(List.of("+", "-"), result.operators());
    }

    @Test
    void ignoresWhitespaceAndSupportsUnderscores() {
        FormulaAnalyzer.FormulaComponents result = FormulaAnalyzer.analyze("col_1*col2 / col3");

        assertEquals(List.of("col_1", "col2", "col3"), result.columns());
        assertEquals(List.of("*", "/"), result.operators());
    }

    @Test
    void handlesEmptyInput() {
        FormulaAnalyzer.FormulaComponents result = FormulaAnalyzer.analyze("   ");

        assertEquals(List.of(), result.columns());
        assertEquals(List.of(), result.operators());
    }
}

