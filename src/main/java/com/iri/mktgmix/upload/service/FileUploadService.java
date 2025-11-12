package com.iri.mktgmix.upload.service;

import com.iri.mktgmix.upload.converter.FileToCsvConverter;
import com.iri.mktgmix.upload.converter.CsvConverterFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class FileUploadService {

    @Value("${mediamix.upload.storage.base-path:}")
    private String storageBasePath;

    public Path mergeChunk(Long uploadSessionId,
                           String originalFileName,
                           int chunkNumber,
                           MultipartFile chunkFile) {

        Path targetPath = resolveTargetPath(uploadSessionId, originalFileName);
        writeChunk(chunkFile, targetPath, chunkNumber == 1);
        try {
            FileToCsvConverter converter = CsvConverterFactory.forFileName(originalFileName);
            return converter.convert(targetPath);
        } catch (IOException ioException) {
            throw new IllegalStateException("Failed to convert spreadsheet to CSV", ioException);
        }
    }

    private void writeChunk(MultipartFile chunkFile, Path targetPath, boolean isFirstChunk) {
        try {
            Files.createDirectories(targetPath.getParent());
            if (isFirstChunk && Files.exists(targetPath)) {
                Files.delete(targetPath);
            }
            try (InputStream inputStream = chunkFile.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(targetPath,
                         StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException ioException) {
            throw new IllegalStateException("Failed to persist file chunk", ioException);
        }
    }

    private Path resolveTargetPath(Long uploadSessionId, String originalFileName) {
        Path basePath;
        if (storageBasePath != null && !storageBasePath.trim().isEmpty()) {
            basePath = Paths.get(storageBasePath);
        } else {
            basePath = Paths.get(System.getProperty("java.io.tmpdir"), "mediamix-uploads");
        }
        return basePath.resolve(String.valueOf(uploadSessionId)).resolve(originalFileName);
    }
}

