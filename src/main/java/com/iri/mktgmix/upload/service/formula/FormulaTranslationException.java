package com.iri.mktgmix.upload.service.formula;

/**
 * Runtime exception raised when a formula cannot be parsed or translated into SQL.
 */
public class FormulaTranslationException extends RuntimeException {

    public FormulaTranslationException(String message) {
        super(message);
    }

    public FormulaTranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}

