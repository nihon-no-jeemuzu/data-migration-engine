package com.project.migration;

import com.project.migration.dto.LegacyCustomerDto;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BatchReaderIntegrationTest {

    @Autowired
    private FlatFileItemReader<LegacyCustomerDto> customerItemReader;

    @Test
    public void testReaderParsesCsvCorrectly() throws Exception {

        ExecutionContext executionContext = new ExecutionContext();

        customerItemReader.open(executionContext);

        LegacyCustomerDto firstRecord = customerItemReader.read();

        assertNotNull(firstRecord, "The reader should have returned a record, but it returned null.");

        assertEquals("LEGACY_1001", firstRecord.getLegacyId());
        assertEquals("Taro Yamada", firstRecord.getFullName());
        assertEquals("taro.yamada@example.com", firstRecord.getEmail());
        assertEquals("090-1234-5678", firstRecord.getPhoneNumber());
        assertEquals("1985-05-15", firstRecord.getDateOfBirth());
        assertEquals("ACTIVE", firstRecord.getStatus());

        customerItemReader.close();
    }
}
