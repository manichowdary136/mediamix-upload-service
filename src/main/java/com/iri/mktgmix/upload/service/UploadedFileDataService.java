package com.iri.mktgmix.upload.service;

import com.iri.mktgmix.upload.domain.DriverIterativeColumnMap;
import com.iri.mktgmix.upload.domain.FileUpload;
import com.iri.mktgmix.upload.domain.SourceColumn;
import com.iri.mktgmix.upload.repository.DriverIterativeColumnMapRepository;
import com.iri.mktgmix.upload.repository.FileUploadRepository;
import com.iri.mktgmix.upload.repository.SourceColumnRepository;
import com.iri.mktgmix.upload.service.dto.FileDataRequest;
import com.iri.mktgmix.upload.service.formula.ColumnFormula;
import com.iri.mktgmix.upload.service.formula.FormulaTranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UploadedFileDataService {

    private final FileUploadRepository fileUploadRepository;
    private final SourceColumnRepository sourceColumnRepository;
    private final DriverIterativeColumnMapRepository driverIterativeColumnMapRepository;
    private final FormulaTranslationService formulaTranslationService;
    private final JdbcTemplate monetJdbcTemplate;

    @Autowired
    public UploadedFileDataService(
            FileUploadRepository fileUploadRepository,
            SourceColumnRepository sourceColumnRepository,
            DriverIterativeColumnMapRepository driverIterativeColumnMapRepository,
            FormulaTranslationService formulaTranslationService,
            @Qualifier("monetJdbcTemplate") JdbcTemplate monetJdbcTemplate) {
        this.fileUploadRepository = fileUploadRepository;
        this.sourceColumnRepository = sourceColumnRepository;
        this.driverIterativeColumnMapRepository = driverIterativeColumnMapRepository;
        this.formulaTranslationService = formulaTranslationService;
        this.monetJdbcTemplate = monetJdbcTemplate;
    }

    /**
     * Retrieves file data from MonetDB table based on fileId and input columns with formulas.
     * 
     * @param request Request containing fileId and list of ColumnFormula
     * @return List of Map<String, Object> representing rows from the MonetDB table
     * @throws IllegalArgumentException if fileId is invalid or columns not found
     * @throws RuntimeException if formula translation or SQL execution fails
     */
    public List<Map<String, Object>> getFileData(FileDataRequest request) {
        try {
            Long fileId = request.getFileId();
            
            // Step 1: Get file upload and source columns
            FileUpload fileUpload = fileUploadRepository.findById(fileId)
                    .orElseThrow(() -> new IllegalArgumentException("FileUpload not found for fileId: " + fileId));
            
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
            
            // Also add array index mappings: source_data[index] -> name_sanitized
            // Sort source columns by ordinal to ensure correct index mapping
            List<SourceColumn> sortedColumns = sourceColumns.stream()
                    .sorted(Comparator.comparing(SourceColumn::getOrdinal))
                    .collect(Collectors.toList());
            
            for (int i = 0; i < sortedColumns.size(); i++) {
                SourceColumn col = sortedColumns.get(i);
                columnMapping.put("source_data[" + i + "]", col.getSanitizedName());
            }
            
            // Step 2: Get target column IDs and lookup their names and data types
            List<Long> targetColumnIds = request.getInputColumns().stream()
                    .map(ColumnFormula::getTargetColumnId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            // Build map of target column ID -> (name, dataType)
            Map<Long, TargetColumnInfo> targetColumnInfoMap = new HashMap<>();
            if (!targetColumnIds.isEmpty()) {
                List<DriverIterativeColumnMap> targetColumns = driverIterativeColumnMapRepository.findAllById(targetColumnIds);
                targetColumnInfoMap = targetColumns.stream()
                        .collect(Collectors.toMap(
                                DriverIterativeColumnMap::getId,
                                col -> new TargetColumnInfo(col.getColumnName(), col.getColumnDatatype(), col.getTimeFormat())
                        ));
            }
            
            // Step 3: Translate formulas to SQL
            Map<Object, String> translatedExpressions;
            try {
                translatedExpressions = formulaTranslationService.translate(
                        request.getInputColumns(),
                        columnMapping
                );
            } catch (com.iri.mktgmix.upload.service.formula.FormulaTranslationException e) {
                throw new RuntimeException("Invalid formula: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Formula translation failed: " + e.getMessage(), e);
            }
            
            // Step 4: Build SELECT query with target column names and casting
            String selectClause = buildSelectClause(translatedExpressions, request.getInputColumns(), targetColumnInfoMap);
            String sql = "SELECT " + selectClause + " FROM " + escapeIdentifier(fileUpload.getMonetTableName());
            
            // Step 5: Execute against MonetDB using JdbcTemplate
            try {
                List<Map<String, Object>> results = monetJdbcTemplate.queryForList(sql);
                return results;
            } catch (DataAccessException e) {
                throw new RuntimeException("SQL execution failed: " + e.getMessage() + ". SQL: " + sql, e);
            }
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation errors as-is
        } catch (RuntimeException e) {
            throw e; // Re-throw runtime errors as-is
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while retrieving file data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Internal class to hold target column information.
     */
    private static class TargetColumnInfo {
        final String name;
        final String dataType;
        final String timeFormat;
        
        TargetColumnInfo(String name, String dataType, String timeFormat) {
            this.name = name;
            this.dataType = dataType;
            this.timeFormat = timeFormat;
        }
    }

    /**
     * Builds the SELECT clause from translated expressions with target column names and casting.
     * Format: "CAST(expression1 AS type) AS targetColumnName1, ..."
     */
    private String buildSelectClause(Map<Object, String> translatedExpressions, 
                                     List<ColumnFormula> inputColumns,
                                     Map<Long, TargetColumnInfo> targetColumnInfoMap) {
        if (translatedExpressions.isEmpty()) {
            throw new IllegalArgumentException("No translated expressions to build SELECT clause");
        }
        
        // Create a map from targetColumn (ID or name) to the formula index
        Map<Object, Integer> targetColumnToIndex = new HashMap<>();
        for (int i = 0; i < inputColumns.size(); i++) {
            targetColumnToIndex.put(inputColumns.get(i).getTargetColumn(), i);
        }
        
        return translatedExpressions.entrySet().stream()
                .map(entry -> {
                    String expression = entry.getValue();
                    Object targetColumnKey = entry.getKey();
                    
                    // Get target column name and data type
                    String targetColumnName;
                    String dataType = null;
                    
                    Long targetColumnId = null;
                    if (targetColumnKey instanceof Number) {
                        targetColumnId = ((Number) targetColumnKey).longValue();
                    } else if (targetColumnKey instanceof String) {
                        try {
                            targetColumnId = Long.parseLong((String) targetColumnKey);
                        } catch (NumberFormatException e) {
                            // Not a number, use as name (backward compatibility)
                            targetColumnName = (String) targetColumnKey;
                        }
                    }
                    
                    if (targetColumnId != null) {
                        TargetColumnInfo info = targetColumnInfoMap.get(targetColumnId);
                        if (info != null) {
                            targetColumnName = info.name;
                            dataType = info.dataType;
                        } else {
                            // Fallback: use ID as string if not found
                            targetColumnName = String.valueOf(targetColumnId);
                        }
                    } else {
                        targetColumnName = targetColumnKey != null ? targetColumnKey.toString() : "unknown";
                    }
                    
                    // Apply casting based on data type
                    String castExpression = applyCasting(expression, dataType);
                    
                    // Escape column names (MonetDB uses double quotes for identifiers)
                    String escapedColumn = escapeIdentifier(targetColumnName);
                    return castExpression + " AS " + escapedColumn;
                })
                .collect(Collectors.joining(", "));
    }
    
    /**
     * Applies SQL casting based on target column data type.
     */
    private String applyCasting(String expression, String dataType) {
        if (dataType == null || dataType.trim().isEmpty()) {
            return expression; // No casting if data type is unknown
        }
        
        String normalizedType = dataType.trim().toUpperCase();
        
        switch (normalizedType) {
            case "DECIMAL":
            case "NUMERIC":
            case "FLOAT":
            case "DOUBLE":
            case "INTEGER":
            case "INT":
            case "BIGINT":
                return "CAST(" + expression + " AS DECIMAL)";
            case "DATE":
                // Format date as MM/DD/YYYY
                return "TO_CHAR(CAST(" + expression + " AS DATE), 'MM/DD/YYYY')";
            case "TIMESTAMP":
            case "DATETIME":
                // Format timestamp as MM/DD/YYYY
                return "TO_CHAR(CAST(" + expression + " AS TIMESTAMP), 'MM/DD/YYYY')";
            case "VARCHAR":
            case "TEXT":
            case "STRING":
            default:
                // For string types, cast to VARCHAR
                return "CAST(" + expression + " AS VARCHAR)";
        }
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

