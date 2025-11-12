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
public class FileMappingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "file_upload_id")
    private Long fileUploadId;

    @Column(name = "required_mapped_count")
    private Integer requiredMappedCount;

    @Column(name = "total_required_count")
    private Integer totalRequiredCount;

    @Column(name = "total_mapped_count")
    private Integer totalMappedCount;

    @Column(name = "is_marked_as_mapped")
    private Boolean markedAsMapped;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}

