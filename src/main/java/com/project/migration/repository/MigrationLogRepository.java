package com.project.migration.repository;

import com.project.migration.entity.MigrationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MigrationLogRepository extends JpaRepository<MigrationLog, Long> {
    Optional<MigrationLog> findByJobId(Long jobId);
}
