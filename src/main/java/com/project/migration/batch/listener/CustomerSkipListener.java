package com.project.migration.batch.listener;

import com.project.migration.dto.LegacyCustomerDto;
import com.project.migration.entity.Customer;
import com.project.migration.entity.MigrationItemErrorLog;
import com.project.migration.repository.MigrationItemErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerSkipListener implements SkipListener<LegacyCustomerDto, Customer> {

    private final MigrationItemErrorLogRepository errorLogRepository;

    private final Long CURRENT_JOB_ID = 1L;

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("Error skipping during read phase: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(LegacyCustomerDto item, Throwable t) {
        log.warn("Quarantining record due to processing failure. Legacy ID: {}", item.getLegacyId());

        MigrationItemErrorLog errorLog = MigrationItemErrorLog.builder()
                .jobId(CURRENT_JOB_ID)
                .legacyId(item.getLegacyId())
                .rawPayLoad(item.toString())
                .errorReason(t.getMessage())
                .failedAt(LocalDateTime.now())
                .build();

        errorLogRepository.save(errorLog);
    }

    @Override
    public void onSkipInWrite(Customer item, Throwable t) {
        log.error("Error skipping during write phase for Customer ID {}: {}", item.getLegacyId(), t.getMessage());
    }

}
