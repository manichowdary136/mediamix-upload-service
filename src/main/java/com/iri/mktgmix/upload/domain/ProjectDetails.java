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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "app", name = "project_details")
public class ProjectDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id", nullable = false)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;

    @Column(name = "project_objective", columnDefinition = "text")
    private String projectObjective;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "no_of_weeks", length = 100)
    private String numberOfWeeks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_user_id", length = 100)
    private String createdUserId;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "modified_user_id", length = 100)
    private String modifiedUserId;

    @Column(name = "modified_by", length = 100)
    private String modifiedBy;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "stage", length = 50)
    private String stage;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @Column(name = "modeling_measure", length = 255)
    private String modelingMeasure;

    @Column(name = "is_trade_calculated")
    private Boolean tradeCalculated;

    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "version_id")
    private Integer versionId;

    @Column(name = "display_name", length = 254)
    private String displayName;

    @Column(name = "is_active_version")
    private Boolean activeVersion;

    @Column(name = "project_period", length = 255)
    private String projectPeriod;

    @Column(name = "is_from_base_version")
    private Boolean fromBaseVersion;
}

