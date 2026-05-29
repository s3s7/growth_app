package com.sg.GrowthApp.repository;

import com.sg.GrowthApp.entity.GrowthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface GrowthRecordRepository extends JpaRepository<GrowthRecord, Long> {
    List<GrowthRecord> findAll(Sort sort);
}
