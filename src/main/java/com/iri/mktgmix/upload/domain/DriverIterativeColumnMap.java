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
@Table(schema = "meta", name = "driver_iterative_column_map")
public class DriverIterativeColumnMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "driver_id", nullable = false)
    private Integer driverId;

    @Column(name = "column_name", nullable = false, length = 255)
    private String columnName;

    @Column(name = "column_datatype", nullable = false, length = 255)
    private String columnDatatype;

    @Column(name = "time_format", length = 255)
    private String timeFormat;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = true;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "column_value", length = 255)
    private String columnValue;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "tooltip", columnDefinition = "text")
    private String tooltip;
}

