package com.iri.mktgmix.upload.service.dto;

import com.iri.mktgmix.upload.service.formula.ColumnFormula;

import java.util.List;
import java.util.Objects;

public class FileDataRequest {
    private Long fileId;
    private List<ColumnFormula> inputColumns;

    public FileDataRequest() {
    }

    public FileDataRequest(Long fileId, List<ColumnFormula> inputColumns) {
        this.fileId = Objects.requireNonNull(fileId, "fileId must not be null");
        this.inputColumns = Objects.requireNonNull(inputColumns, "inputColumns must not be null");
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public List<ColumnFormula> getInputColumns() {
        return inputColumns;
    }

    public void setInputColumns(List<ColumnFormula> inputColumns) {
        this.inputColumns = inputColumns;
    }
}

