package com.project.migration.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "migration_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MigrationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_records_processed")
    private Integer totalRecordsProcessed;

    @Column(name = "successful_migrations")
    private Integer successfulMigrations;

    @Column(name = "failed_records")
    private Integer failedRecords;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MigrationStatus status;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

}
