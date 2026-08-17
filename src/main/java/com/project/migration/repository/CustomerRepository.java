package com.project.migration.repository;

import com.project.migration.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByCleanEmail(String cleanEmail);
    boolean existsByCleanEmail(String cleanEmail);
}
