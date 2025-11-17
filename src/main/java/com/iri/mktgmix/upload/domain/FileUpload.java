package com.iri.mktgmix.upload.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(schema = "app", name = "file_upload")
public class FileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "bytes_received", nullable = false)
    private Long bytesReceived;

    @Column(name = "upload_etag", length = 64)
    private String uploadEtag;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    @Column(name = "checksum_source_sha256", length = 64)
    private String checksumSourceSha256;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false)
    private FileUploadStatus uploadStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "ingest_status")
    private FileIngestStatus ingestStatus;

    @Column(name = "csv_path", length = 1024)
    private String csvPath;

    @Column(name = "monet_table_name", length = 128)
    private String monetTableName;

    @Column(name = "row_count", nullable = false)
    private Long rowCount = 0L;

    @Column(name = "ingested_at")
    private LocalDateTime ingestedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_user_id", length = 100)
    private String createdById;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "modified_user_id", length = 100)
    private String modifiedById;

    @Column(name = "modified_by", length = 100)
    private String modifiedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_details_id", nullable = false)
    private DriverDetails driverDetails;

}

