package com.project.migration.repository;

import com.project.migration.entity.MigrationItemErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationItemErrorLogRepository extends JpaRepository<MigrationItemErrorLog, Long> {
}
