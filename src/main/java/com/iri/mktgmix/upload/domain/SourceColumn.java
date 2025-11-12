package com.iri.mktgmix.upload.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "app", name = "source_column")
public class SourceColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "file_upload_id", nullable = false)
    private Long fileUploadId;

    @Column(name = "name_original", nullable = false, length = 255)
    private String originalName;

    @Column(name = "name_sanitized", nullable = false, length = 255)
    private String sanitizedName;

    @Column(name = "data_type", length = 50)
    private String dataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status", nullable = false, length = 16)
    private SourceColumnMappingStatus mappingStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_user_id", length = 100)
    private String createdUserId;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "modified_user_id", length = 100)
    private String modifiedUserId;

    @Column(name = "modified_by", length = 100)
    private String modifiedBy;
}

