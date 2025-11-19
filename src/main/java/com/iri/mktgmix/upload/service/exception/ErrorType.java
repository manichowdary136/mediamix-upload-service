package com.iri.mktgmix.upload.service.exception;

/**
 * Enum representing different types of errors that can occur in UploadedFileDataService.
 */
public enum ErrorType {
    /**
     * Error related to invalid input formula (FormulaTranslationException).
     */
    INVALID_INPUT_FORMULA_ERROR(1000),
    
    /**
     * Error related to database operations (DataAccessException).
     */
    DB_TRANSLATION_ERROR(3000),
    
    /**
     * Other types of errors.
     */
    OTHER(5000);

    private final Integer errorCode;

    ErrorType(Integer errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Returns the numeric error code for this error type.
     * 
     * @return Integer error code
     */
    public Integer getErrorCode() {
        return errorCode;
    }
}

