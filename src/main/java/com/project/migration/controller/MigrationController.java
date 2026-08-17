package com.project.migration.controller;

import com.project.migration.dto.response.JobStatusResponse;
import com.project.migration.dto.response.JobTriggerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/migrations")
@RequiredArgsConstructor
public class MigrationController {

    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final Job customerMigrationJob;

    @PostMapping("/trigger")
    public ResponseEntity<JobTriggerResponse> triggerMigration() {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .addString("invokedBy", "REST_API")
                    .toJobParameters();

            log.info("Attempting to trigger customerMigrationJob...");

            JobExecution execution = jobOperator.start(customerMigrationJob, parameters);

            return ResponseEntity.accepted().body(new JobTriggerResponse(
                    execution.getId(),
                    execution.getStatus().name(),
                    "Migration job successfully initiated."
            ));

        } catch (Exception e) {
            log.error("Failed to start migration job", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new JobTriggerResponse(null, "FAILED", e.getMessage()));
        }
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable Long jobId) {
        JobExecution execution = jobRepository.getJobExecution(jobId);

        if (execution == null) {
            return ResponseEntity.notFound().build();
        }

        JobStatusResponse response= new JobStatusResponse(
                execution.getId(),
                execution.getStatus().name(),
                execution.getStartTime() != null ? execution.getStartTime() : null,
                execution.getEndTime() != null ? execution.getEndTime() : null,
                execution.getExitStatus().getExitCode()
        );

        return ResponseEntity.ok(response);
    }
}
