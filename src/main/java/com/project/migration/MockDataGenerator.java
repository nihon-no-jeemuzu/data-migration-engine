package com.project.migration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class MockDataGenerator {

    public static void main(String[] args) {
        // Path where the new massive CSV will be created
        String filePath = "src/main/resources/data/legacy_customers_1M.csv";
        int totalRecords = 1_000_000;

        System.out.println("Starting generation of " + totalRecords + " records...");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write CSV Header
            writer.write("legacyId,fullName,email,phoneNumber,dateOfBirth,status\n");

            for (int i = 1; i <= totalRecords; i++) {
                // Inject a corrupted record every 100,000 rows to test our Quarantine/Skip logic
                if (i % 100_000 == 0) {
                    writer.write("BAD-" + i + ",Corrupted User " + i + ",invalid-email-format,000-000-0000,BAD-DATE,UNKNOWN\n");
                } else {
                    // Valid records
                    writer.write("LEGACY_" + i + ",Test User " + i + ",user" + i + "@enterprise.com,090-1234-5678,1985-05-15,ACTIVE\n");
                }

                if (i % 250_000 == 0) {
                    System.out.println("Generated " + i + " records...");
                }
            }
            System.out.println("✅ Successfully generated 1 Million records at: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to write mock data: " + e.getMessage());
        }
    }
}