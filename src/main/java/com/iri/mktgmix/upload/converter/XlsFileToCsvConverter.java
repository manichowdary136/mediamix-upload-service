package com.iri.mktgmix.upload.converter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class XlsFileToCsvConverter extends AbstractExcelConverter {

    @Override
    protected Workbook createWorkbook(Path inputFile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(inputFile)) {
            return new HSSFWorkbook(inputStream);
        }
    }
}

