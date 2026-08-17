package com.project.migration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  Represents a single raw record straight from the legacy CSV extract.
 *  All fields are Strings because the raw data is un-sanitized.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegacyCustomerDto {
    private String legacyId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String dateOfBirth;
    private String status;
}
