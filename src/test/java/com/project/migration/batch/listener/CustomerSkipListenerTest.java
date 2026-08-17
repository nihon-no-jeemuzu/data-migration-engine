package com.project.migration.batch.listener;

import com.project.migration.dto.LegacyCustomerDto;
import com.project.migration.entity.MigrationItemErrorLog;
import com.project.migration.exception.DataValidationException;
import com.project.migration.repository.MigrationItemErrorLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerSkipListenerTest {

    @Mock
    private MigrationItemErrorLogRepository errorLogRepository;

    @InjectMocks
    private CustomerSkipListener skipListener;

    @Test
    @DisplayName("Should save to ErrorLogRepository when a processing error is skipped")
    void testOnSkipInProcess_SavesErrorLog() {
        // Arrange
        LegacyCustomerDto badItem = LegacyCustomerDto.builder()
                .legacyId("BAD-999")
                .fullName("Corrupt Data")
                .build();

        Throwable exception = new DataValidationException("Invalid email format");

        // Act
        skipListener.onSkipInProcess(badItem, exception);

        // Assert
        ArgumentCaptor<MigrationItemErrorLog> logCaptor = ArgumentCaptor.forClass(MigrationItemErrorLog.class);

        verify(errorLogRepository, times(1)).save(logCaptor.capture());

        MigrationItemErrorLog savedLog = logCaptor.getValue();
        assertEquals("BAD-999", savedLog.getLegacyId());
        assertEquals("Invalid email format", savedLog.getErrorReason());
        assertNotNull(savedLog.getFailedAt());
    }
}
