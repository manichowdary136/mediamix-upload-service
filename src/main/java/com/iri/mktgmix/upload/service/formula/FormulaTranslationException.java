package com.iri.mktgmix.upload.service.formula;

/**
 * Runtime exception raised when a formula cannot be parsed or translated into SQL.
 */
public class FormulaTranslationException extends RuntimeException {

    private final String targetColumn;

    public FormulaTranslationException(String message) {
        super(message);
        this.targetColumn = null;
    }

    public FormulaTranslationException(String message, Throwable cause) {
        super(message, cause);
        this.targetColumn = null;
    }

    private FormulaTranslationException(String message, String targetColumn) {
        super(formatMessage(message, targetColumn));
        this.targetColumn = targetColumn;
    }

    private FormulaTranslationException(String message, String targetColumn, Throwable cause) {
        super(formatMessage(message, targetColumn), cause);
        this.targetColumn = targetColumn;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public static FormulaTranslationException withTargetColumn(String message, String targetColumn) {
        return new FormulaTranslationException(message, targetColumn);
    }

    public static FormulaTranslationException withTargetColumn(String message, String targetColumn, Throwable cause) {
        return new FormulaTranslationException(message, targetColumn, cause);
    }

    private static String formatMessage(String message, String targetColumn) {
        if (targetColumn != null && !targetColumn.trim().isEmpty()) {
            return "Error in targetColumn '" + targetColumn + "': " + message;
        }
        return message;
    }
}

