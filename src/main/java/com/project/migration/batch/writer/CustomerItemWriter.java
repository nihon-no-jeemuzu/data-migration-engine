package com.project.migration.batch.writer;

import com.project.migration.entity.Customer;
import com.project.migration.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerItemWriter implements ItemWriter<Customer> {

    private final CustomerRepository customerRepository;

    @Override
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        log.info("Persisting chunk of {} sanitized customers to the database.", chunk.size());

        //chunk.getItems() extracts the underlying List from the Spring Batch Chunk object
        customerRepository.saveAll(chunk.getItems());

        log.debug("Successfully committed {} records.", chunk.size());
    }
}
