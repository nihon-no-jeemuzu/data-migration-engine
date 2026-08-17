package com.project.migration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "migration_item_error_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigrationItemErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "legacy_id")
    private String legacyId;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayLoad;

    @Column(name = "error_reason", columnDefinition = "TEXT")
    private String errorReason;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;
}
