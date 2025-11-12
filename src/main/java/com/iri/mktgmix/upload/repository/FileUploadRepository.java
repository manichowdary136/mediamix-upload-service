package com.iri.mktgmix.upload.repository;

import com.iri.mktgmix.upload.domain.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {
}

