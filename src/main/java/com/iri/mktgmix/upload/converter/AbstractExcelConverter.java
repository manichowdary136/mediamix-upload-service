package com.iri.mktgmix.upload.converter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class AbstractExcelConverter implements FileToCsvConverter {

    @Override
    public Path convert(Path inputFile) throws IOException {
        Path csvPath = replaceExtension(inputFile, ".csv");

        try (Workbook workbook = createWorkbook(inputFile);
             BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8)) {
            DataFormatter formatter = new DataFormatter();
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                writeRow(writer, row, formatter);
            }
        }

        return csvPath;
    }

    protected abstract Workbook createWorkbook(Path inputFile) throws IOException;

    private void writeRow(BufferedWriter writer, Row row, DataFormatter formatter) throws IOException {
        boolean firstCell = true;
        int lastCellNum = row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();

        for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
            if (firstCell) {
                firstCell = false;
            } else {
                writer.write(',');
            }
            Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            String value = formatter.formatCellValue(cell);
            writer.write(escapeForCsv(value));
        }
        writer.newLine();
    }

    private String escapeForCsv(String value) {
        if (value == null) {
            return "";
        }

        boolean requiresQuoting = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");

        if (!requiresQuoting) {
            return value;
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private Path replaceExtension(Path originalFile, String newExtension) {
        String fileName = originalFile.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            fileName = fileName + newExtension;
        } else {
            fileName = fileName.substring(0, dotIndex) + newExtension;
        }
        return originalFile.getParent().resolve(fileName);
    }
}

