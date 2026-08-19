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

        LegacyCustomerDto item = customerItemReader.read();

        assertNotNull(item);
        assertEquals("LEGACY_1", item.getLegacyId()); // Updated
        assertEquals("Test User 1", item.getFullName()); // Updated
        assertEquals("user1@enterprise.com", item.getEmail()); // Updated
        assertEquals("090-1234-5678", item.getPhoneNumber());
        assertEquals("1985-05-15", item.getDateOfBirth());
        assertEquals("ACTIVE", item.getStatus());

        customerItemReader.close();
    }
}
