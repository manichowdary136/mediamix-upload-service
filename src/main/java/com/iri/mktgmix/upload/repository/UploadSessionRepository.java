package com.iri.mktgmix.upload.repository;

import com.iri.mktgmix.upload.domain.UploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadSessionRepository extends JpaRepository<UploadSession, Long> {
}

