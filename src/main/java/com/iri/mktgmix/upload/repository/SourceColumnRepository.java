package com.iri.mktgmix.upload.repository;

import com.iri.mktgmix.upload.domain.SourceColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SourceColumnRepository extends JpaRepository<SourceColumn, Long> {
    List<SourceColumn> findByFileUploadId(Long fileUploadId);
}

