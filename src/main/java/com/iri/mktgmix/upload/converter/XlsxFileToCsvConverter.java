package com.iri.mktgmix.upload.converter;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class XlsxFileToCsvConverter extends AbstractExcelConverter {

    @Override
    protected Workbook createWorkbook(Path inputFile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(inputFile)) {
            return new XSSFWorkbook(inputStream);
        }
    }
}

