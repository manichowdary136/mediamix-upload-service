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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "meta", name = "driver_details")
public class DriverDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "driver_type_id")
    private Integer driverTypeId;

    @Column(name = "driver_name", nullable = false, length = 255)
    private String driverName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "custom_name", length = 255)
    private String customName;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Column(name = "relevance_order")
    private Integer relevanceOrder;
}

