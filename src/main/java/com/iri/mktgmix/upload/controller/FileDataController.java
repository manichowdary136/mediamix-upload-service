package com.iri.mktgmix.upload.controller;

import com.iri.mktgmix.upload.controller.dto.ErrorResponse;
import com.iri.mktgmix.upload.service.UploadedFileDataService;
import com.iri.mktgmix.upload.service.dto.FileDataRequest;
import com.iri.mktgmix.upload.service.exception.UploadedFileDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file-data")
public class FileDataController {

    private final UploadedFileDataService uploadedFileDataService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getFileData(@RequestBody FileDataRequest request) {
        return uploadedFileDataService.getFileData(request);
    }

    @ExceptionHandler(UploadedFileDataException.class)
    public ResponseEntity<ErrorResponse> handleUploadedFileDataException(UploadedFileDataException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorType(ex.getErrorType().name())
                .errorMessage(ex.getErrorMessage())
                .errorCode(ex.getErrorCode())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

