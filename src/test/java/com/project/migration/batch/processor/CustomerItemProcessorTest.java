package com.project.migration.batch.processor;

import com.project.migration.dto.LegacyCustomerDto;
import com.project.migration.entity.Customer;
import com.project.migration.entity.CustomerStatus;
import com.project.migration.exception.DataValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CustomerItemProcessorTest {

    private CustomerItemProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CustomerItemProcessor();
    }

    @Test
    @DisplayName("Should successfully transform a perfectly formatted legacy record")
    void testProcess_ValidData_ReturnsCleanCustomer() throws Exception {
        // Arrange
        LegacyCustomerDto input = LegacyCustomerDto.builder()
                .legacyId("CUST=1001")
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("123-456-7890")
                .dateOfBirth("1990-05-15")
                .status("active")
                .build();

        // Act
        Customer result = processor.process(input);

        // Assert
        assertNotNull(result);
        assertEquals("CUST=1001", result.getLegacyId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getCleanEmail());
        assertEquals("123-456-7890", result.getFormattedPhone());
        assertEquals(LocalDate.of(1990, 5, 15), result.getBirthDate());
        assertEquals(CustomerStatus.ACTIVE, result.getStatus());
    }

    @Test
    @DisplayName("Should throw DataValidationException when email is malformed")
    void testProcess_InvalidEmail_ThrowsException() {
        // Arrange
        LegacyCustomerDto input = LegacyCustomerDto.builder()
                .legacyId("CUST=1002")
                .fullName("John Doe")
                .email("john.doe-no-at-sign.com") // Invalid email
                .phoneNumber("090-0000-0000")
                .dateOfBirth("1990-01-01")
                .status("ACTIVE")
                .build();

        // Act & Assert
        DataValidationException exception = assertThrows(
                DataValidationException.class,
                () -> {processor.process(input);
        });

        assertTrue(exception.getMessage().contains("Invalid email format"), "Message was actually: " + exception.getMessage());
    }

    @Test
    @DisplayName("Should throw DataValidationException when dateOfBirth is malformed")
    void testProcess_InvalidDateOfBirth_ThrowsException() {
        // Arrange
        LegacyCustomerDto input = LegacyCustomerDto.builder()
                .legacyId("CUST=1003")
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("090-0000-0000") // Valid filler
                .dateOfBirth("15-05-1990")    // Wrong format
                .status("ACTIVE")
                .build();

        // Act & Assert
        assertThrows(DataValidationException.class, () -> processor.process(input));
    }
}
