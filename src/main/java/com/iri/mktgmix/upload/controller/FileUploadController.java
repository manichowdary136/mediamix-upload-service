package com.iri.mktgmix.upload.controller;

import com.iri.mktgmix.upload.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
public class FileUploadController {


    private final FileUploadService fileUploadService;

    @PutMapping(
            path = "/{fileId}/chunks",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadChunk(@PathVariable Long fileId,
                            @RequestParam("originalFileName") String originalFileName,
                            @RequestParam("chunkNumber") Integer chunkNumber,
                            @RequestPart("chunk") MultipartFile chunkFile) {
        fileUploadService.mergeChunk(fileId, originalFileName, chunkNumber, chunkFile);
    }
}

