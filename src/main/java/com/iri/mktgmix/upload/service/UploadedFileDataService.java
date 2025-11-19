package com.iri.mktgmix.upload.service;

import com.iri.mktgmix.upload.domain.FileUpload;
import com.iri.mktgmix.upload.domain.SourceColumn;
import com.iri.mktgmix.upload.repository.FileUploadRepository;
import com.iri.mktgmix.upload.repository.SourceColumnRepository;
import com.iri.mktgmix.upload.service.dto.FileDataRequest;
import com.iri.mktgmix.upload.service.exception.ErrorType;
import com.iri.mktgmix.upload.service.exception.UploadedFileDataException;
import com.iri.mktgmix.upload.service.formula.FormulaTranslationException;
import com.iri.mktgmix.upload.service.formula.FormulaTranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UploadedFileDataService {

    private final FileUploadRepository fileUploadRepository;
    private final SourceColumnRepository sourceColumnRepository;
    private final FormulaTranslationService formulaTranslationService;
    private final JdbcTemplate monetJdbcTemplate;

    @Autowired
    public UploadedFileDataService(
            FileUploadRepository fileUploadRepository,
            SourceColumnRepository sourceColumnRepository,
            FormulaTranslationService formulaTranslationService,
            @Qualifier("monetJdbcTemplate") JdbcTemplate monetJdbcTemplate) {
        this.fileUploadRepository = fileUploadRepository;
        this.sourceColumnRepository = sourceColumnRepository;
        this.formulaTranslationService = formulaTranslationService;
        this.monetJdbcTemplate = monetJdbcTemplate;
    }

    public List<Map<String, Object>> getFileData(FileDataRequest request) {
        try {
            Long fileId = request.getFileId();
            
            List<SourceColumn> sourceColumns = sourceColumnRepository.findByFileUploadId(fileId);
            if (sourceColumns.isEmpty()) {
                throw new UploadedFileDataException(
                    ErrorType.OTHER,
                    "No columns found for fileId: " + fileId
                );
            }
            
            Map<String, String> columnMapping = sourceColumns.stream()
                    .collect(Collectors.toMap(
                            SourceColumn::getOriginalName,
                            SourceColumn::getSanitizedName,
                            (existing, replacement) -> existing
                    ));
            
            FileUpload fileUpload = fileUploadRepository.findById(fileId)
                    .orElseThrow(() -> new UploadedFileDataException(
                        ErrorType.OTHER,
                        "FileUpload not found for fileId: " + fileId
                    ));
            
            Map<String, String> translatedExpressions = formulaTranslationService.translate(
                    request.getInputColumns(),
                    columnMapping
            );
            
            String selectClause = buildSelectClause(translatedExpressions);
            String sql = "SELECT " + selectClause + " FROM " + fileUpload.getMonetTableName();
            
            List<Map<String, Object>> results = monetJdbcTemplate.queryForList(sql);
            
            return results;
        } catch (FormulaTranslationException e) {
            throw new UploadedFileDataException(
                ErrorType.INVALID_INPUT_FORMULA_ERROR,
                getExceptionMessage(e, "Formula translation error occurred"),
                e
            );
        } catch (DataAccessException e) {
            throw new UploadedFileDataException(
                ErrorType.DB_TRANSLATION_ERROR,
                getExceptionMessage(e, "Database error occurred"),
                e
            );
        } catch (UploadedFileDataException e) {
            throw e;
        } catch (Exception e) {
            throw new UploadedFileDataException(
                ErrorType.OTHER,
                getExceptionMessage(e, "An unexpected error occurred"),
                e
            );
        }
    }

    private String buildSelectClause(Map<String, String> translatedExpressions) {
        if (translatedExpressions.isEmpty()) {
            throw new UploadedFileDataException(
                ErrorType.OTHER,
                "No translated expressions to build SELECT clause"
            );
        }
        
        return translatedExpressions.entrySet().stream()
                .map(entry -> {
                    String expression = entry.getValue();
                    String targetColumn = entry.getKey();
                    String escapedColumn = escapeIdentifier(targetColumn);
                    return expression + " AS " + escapedColumn;
                })
                .collect(Collectors.joining(", "));
    }

    private String escapeIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private String getExceptionMessage(Exception exception, String defaultMessage) {
        if (exception == null) {
            return defaultMessage;
        }
        
        if (exception.getMessage() != null && !exception.getMessage().trim().isEmpty()) {
            return exception.getMessage();
        }
        
        if (exception instanceof DataAccessException) {
            DataAccessException dae = (DataAccessException) exception;
            Throwable rootCause = dae.getRootCause();
            if (rootCause != null && rootCause.getMessage() != null && !rootCause.getMessage().trim().isEmpty()) {
                return rootCause.getMessage();
            }
        }
        
        Throwable cause = exception.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().trim().isEmpty()) {
            return cause.getMessage();
        }
        
        return defaultMessage;
    }
}

