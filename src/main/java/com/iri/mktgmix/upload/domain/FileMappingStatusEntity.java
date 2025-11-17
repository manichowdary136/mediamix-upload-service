package com.iri.mktgmix.upload.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(schema = "app", name = "file_mapping_status")
public class FileMappingStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "file_upload_id", nullable = false)
    private Long fileUploadId;

    @Column(name = "total_required_count", nullable = false)
    private Integer totalRequiredCount = 0;

    @Column(name = "required_mapped_count", nullable = false)
    private Integer requiredMappedCount = 0;

    @Column(name = "total_mapped_count", nullable = false)
    private Integer totalMappedCount = 0;

    @Column(name = "is_marked_as_mapped", nullable = false)
    private Boolean isMarkedAsMapped = false;

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

