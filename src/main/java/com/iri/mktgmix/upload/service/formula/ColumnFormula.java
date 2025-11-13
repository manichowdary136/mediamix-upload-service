package com.iri.mktgmix.upload.service.formula;

import java.util.Objects;

/**
 * Value object representing the mapping between a target column and its source-side formula.
 */
public final class ColumnFormula {

    private final String targetColumn;
    private final String formula;

    private ColumnFormula(String targetColumn, String formula) {
        this.targetColumn = targetColumn;
        this.formula = formula;
    }

    public static ColumnFormula of(String targetColumn, String formula) {
        return new ColumnFormula(
                Objects.requireNonNull(targetColumn, "targetColumn must not be null"),
                Objects.requireNonNull(formula, "formula must not be null"));
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public String getFormula() {
        return formula;
    }
}

