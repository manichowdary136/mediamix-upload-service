package com.iri.mktgmix.upload.service.formula;

import java.util.Objects;

/**
 * Value object representing the mapping between a target column and its source-side formula.
 * targetColumn can be either an ID (Long) or a name (String) for backward compatibility.
 */
public class ColumnFormula {

    private Object targetColumn; // Can be Long (ID) or String (name for backward compatibility)
    private String formula;

    public ColumnFormula() {
        // Default constructor for JSON deserialization
    }

    private ColumnFormula(Object targetColumn, String formula) {
        this.targetColumn = targetColumn;
        this.formula = formula;
    }

    public static ColumnFormula of(Object targetColumn, String formula) {
        return new ColumnFormula(
                Objects.requireNonNull(targetColumn, "targetColumn must not be null"),
                Objects.requireNonNull(formula, "formula must not be null"));
    }

    public Object getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(Object targetColumn) {
        this.targetColumn = targetColumn;
    }

    /**
     * Gets target column as ID (Long) if it's a number, otherwise returns null.
     */
    public Long getTargetColumnId() {
        if (targetColumn instanceof Number) {
            return ((Number) targetColumn).longValue();
        }
        if (targetColumn instanceof String) {
            try {
                return Long.parseLong((String) targetColumn);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets target column as String name (for backward compatibility).
     */
    public String getTargetColumnName() {
        if (targetColumn instanceof String) {
            return (String) targetColumn;
        }
        return null;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }
}

