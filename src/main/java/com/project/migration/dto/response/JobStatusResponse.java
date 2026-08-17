package com.project.migration.dto.response;

import java.time.LocalDateTime;

public record JobStatusResponse(
    Long jobId,
    String status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String exitCode
    ) {}
