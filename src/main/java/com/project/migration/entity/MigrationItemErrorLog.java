package com.project.migration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity representing the quarantine log for failed migration records.
 * <p>
 * When the batch processor encounters malformed legacy data that cannot be
 * safely transformed, the raw payload and the corresponding exception stack trace
 * are captured and persisted here. This allows the job to continue processing
 * while providing an audit trail for the Data Operations team to manually review.
 * </p>
 */
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
