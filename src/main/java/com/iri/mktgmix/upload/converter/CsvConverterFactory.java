package com.iri.mktgmix.upload.converter;

import java.util.Locale;

public final class CsvConverterFactory {

    private CsvConverterFactory() {
    }

    public static FileToCsvConverter forFileName(String originalFileName) {
        String extension = extractExtension(originalFileName);

        switch (extension) {
            case "xls":
                return new XlsFileToCsvConverter();
            case "xlsx":
                return new XlsxFileToCsvConverter();
            default:
                throw new IllegalArgumentException("Unsupported spreadsheet type: " + extension);
        }
    }

    private static String extractExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new IllegalArgumentException("Original filename must include extension");
        }
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.') + 1);
        return extension.toLowerCase(Locale.ROOT);
    }
}

