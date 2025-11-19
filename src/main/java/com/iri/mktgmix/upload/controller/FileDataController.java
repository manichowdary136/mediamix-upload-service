package com.iri.mktgmix.upload.controller;

import com.iri.mktgmix.upload.service.UploadedFileDataService;
import com.iri.mktgmix.upload.service.dto.FileDataRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file-data")
public class FileDataController {

    private final UploadedFileDataService uploadedFileDataService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getFileData(@RequestBody FileDataRequest request) {
        try {
            List<Map<String, Object>> results = uploadedFileDataService.getFileData(request);
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid request");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Processing failed");
            error.put("message", e.getMessage());
            // Check if it's a formula or SQL error
            if (e.getMessage() != null && (e.getMessage().contains("formula") || e.getMessage().contains("Formula"))) {
                error.put("type", "FORMULA_ERROR");
            } else if (e.getMessage() != null && (e.getMessage().contains("SQL") || e.getMessage().contains("sql"))) {
                error.put("type", "SQL_ERROR");
            } else {
                error.put("type", "GENERAL_ERROR");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Unexpected error");
        error.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

