package com.iri.mktgmix.upload.service;

import com.iri.mktgmix.upload.domain.FileUpload;
import com.iri.mktgmix.upload.domain.SourceColumn;
import com.iri.mktgmix.upload.repository.FileUploadRepository;
import com.iri.mktgmix.upload.repository.SourceColumnRepository;
import com.iri.mktgmix.upload.service.dto.FileDataRequest;
import com.iri.mktgmix.upload.service.formula.FormulaTranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UploadedFileDataService {

    private final FileUploadRepository fileUploadRepository;
    private final SourceColumnRepository sourceColumnRepository;
    private final FormulaTranslationService formulaTranslationService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UploadedFileDataService(
            FileUploadRepository fileUploadRepository,
            SourceColumnRepository sourceColumnRepository,
            FormulaTranslationService formulaTranslationService,
            JdbcTemplate jdbcTemplate) {
        this.fileUploadRepository = fileUploadRepository;
        this.sourceColumnRepository = sourceColumnRepository;
        this.formulaTranslationService = formulaTranslationService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Retrieves file data from MonetDB table based on fileId and input columns with formulas.
     * 
     * @param request Request containing fileId and list of ColumnFormula
     * @return List of Map<String, Object> representing rows from the MonetDB table
     */
    public List<Map<String, Object>> getFileData(FileDataRequest request) {
        Long fileId = request.getFileId();
        
        // Step 2: Get name_sanitized and name_original for given fileId
        List<SourceColumn> sourceColumns = sourceColumnRepository.findByFileUploadId(fileId);
        if (sourceColumns.isEmpty()) {
            throw new IllegalArgumentException("No columns found for fileId: " + fileId);
        }
        
        // Build column mapping: name_original -> name_sanitized
        Map<String, String> columnMapping = sourceColumns.stream()
                .collect(Collectors.toMap(
                        SourceColumn::getOriginalName,
                        SourceColumn::getSanitizedName,
                        (existing, replacement) -> existing // Handle duplicates if any
                ));
        
        // Step 3: Get monet_table_name from file_upload
        FileUpload fileUpload = fileUploadRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("FileUpload not found for fileId: " + fileId));
        
        // Step 3: Call FormulaTranslationService.translate with formulas and column mapping
        Map<String, String> translatedExpressions = formulaTranslationService.translate(
                request.getInputColumns(),
                columnMapping
        );
        
        // Step 4: Build SELECT query for all columns
        String selectClause = buildSelectClause(translatedExpressions);
        String sql = "SELECT " + selectClause + " FROM " + fileUpload.getMonetTableName();
        
        // Step 5: Execute against MonetDB using JdbcTemplate
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        
        return results;
    }

    /**
     * Builds the SELECT clause from translated expressions.
     * Format: "expression1 AS targetColumn1, expression2 AS targetColumn2, ..."
     */
    private String buildSelectClause(Map<String, String> translatedExpressions) {
        if (translatedExpressions.isEmpty()) {
            throw new IllegalArgumentException("No translated expressions to build SELECT clause");
        }
        
        return translatedExpressions.entrySet().stream()
                .map(entry -> {
                    String expression = entry.getValue();
                    String targetColumn = entry.getKey();
                    // Escape column names if needed (MonetDB uses double quotes for identifiers)
                    String escapedColumn = escapeIdentifier(targetColumn);
                    return expression + " AS " + escapedColumn;
                })
                .collect(Collectors.joining(", "));
    }

    /**
     * Escapes SQL identifier for MonetDB (uses double quotes).
     */
    private String escapeIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }
        // Replace double quotes with escaped double quotes and wrap in double quotes
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}

