package com.iri.mktgmix.upload.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMappingRequest {
    private Long targetColumnId;
    private String mappingType;
    private String advancedFormula;
}

