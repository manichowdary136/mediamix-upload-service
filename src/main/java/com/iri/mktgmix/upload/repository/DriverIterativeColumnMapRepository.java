package com.iri.mktgmix.upload.repository;

import com.iri.mktgmix.upload.domain.DriverIterativeColumnMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverIterativeColumnMapRepository extends JpaRepository<DriverIterativeColumnMap, Long> {
    List<DriverIterativeColumnMap> findByDriverId(Integer driverId);
}

