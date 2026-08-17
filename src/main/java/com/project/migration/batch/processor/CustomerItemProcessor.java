package com.project.migration.batch.processor;

import com.project.migration.dto.LegacyCustomerDto;
import com.project.migration.entity.Customer;
import com.project.migration.entity.CustomerStatus;
import com.project.migration.exception.DataValidationException;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@Component
public class CustomerItemProcessor implements ItemProcessor<LegacyCustomerDto, Customer> {

    // Standard RFC 5322 Regex for Email Validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Customer process(LegacyCustomerDto item) throws Exception {
        Customer cleanCustomer = new Customer();

        // Map Direct Strings (Ensuring legacyId isn't lost)
        if (item.getLegacyId() == null || item.getLegacyId().trim().isEmpty()) {
            throw new DataValidationException("Legacy ID is missing.");
        }
        cleanCustomer.setLegacyId(item.getLegacyId().trim());
        cleanCustomer.setFormattedPhone(item.getPhoneNumber() != null ? item.getPhoneNumber().trim() : "");

        // 1. Name Processing
        String rawName = item.getFullName() != null ? item.getFullName().trim() : "";
        if (rawName.isEmpty()) {
            throw new DataValidationException("Full name is missing or empty.");
        }
        String[] nameParts = rawName.split("\\s+", 2);
        cleanCustomer.setFirstName(nameParts[0]);
        cleanCustomer.setLastName(nameParts.length > 1 ? nameParts[1] : "");

        // 2. Email Validation
        String email = item.getEmail() != null ? item.getEmail().trim() : "";
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new DataValidationException("Invalid email format detected: " + email);
        }
        cleanCustomer.setCleanEmail(email);

        // 3. Date Parsing (dateOfBirth -> birthDate)
        try {
            String rawDate = item.getDateOfBirth() != null ? item.getDateOfBirth().trim() : "";
            LocalDate parsedDate = LocalDate.parse(rawDate, DATE_FORMATTER);
            cleanCustomer.setBirthDate(parsedDate);
        } catch (DateTimeParseException e) {
            throw new DataValidationException("Malformed date string: " + item.getDateOfBirth());
        }

        // 4. Status Mapping
        try {
            String rawStatus = item.getStatus() != null ? item.getStatus().trim().toUpperCase() : "";
            cleanCustomer.setStatus(CustomerStatus.valueOf(rawStatus));
        } catch (IllegalArgumentException e) {
            throw new DataValidationException("Unrecognized customer status: " + item.getStatus());
        }

        return cleanCustomer;
    }
}
