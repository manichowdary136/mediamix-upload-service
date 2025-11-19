package com.iri.mktgmix.upload.service.exception;

import org.springframework.dao.DataAccessException;

import java.sql.SQLException;

/**
 * Runtime exception raised when an error occurs in UploadedFileDataService.
 * Contains error type, error message, and error code.
 */
public class UploadedFileDataException extends RuntimeException {

    private final ErrorType errorType;
    private final String errorMessage;
    private final Integer errorCode;

    public UploadedFileDataException(ErrorType errorType, String errorMessage) {
        super(errorMessage);
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.errorCode = errorType.getErrorCode();
    }

    public UploadedFileDataException(ErrorType errorType, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.errorCode = errorType.getErrorCode();
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * Helper method to extract SQL error code from DataAccessException for logging/debugging.
     * 
     * @param ex DataAccessException
     * @return SQL error code if available, null otherwise
     */
    public static Integer extractSqlErrorCode(DataAccessException ex) {
        Throwable cause = ex.getRootCause();
        if (cause instanceof SQLException) {
            return ((SQLException) cause).getErrorCode();
        }
        return null;
    }
}

