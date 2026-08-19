package com.project.migration.config;

import com.project.migration.batch.listener.CustomerSkipListener;
import com.project.migration.batch.processor.CustomerItemProcessor;
import com.project.migration.batch.writer.CustomerItemWriter;
import com.project.migration.dto.LegacyCustomerDto;
import com.project.migration.entity.Customer;
import com.project.migration.exception.DataValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.infrastructure.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Core Spring Batch configuration for the legacy data migration pipeline.
 * <p>
 * This configuration orchestrates the ETL (Extract, Transform, Load) process.
 * It wires together the FlatFileItemReader for ingesting legacy CSV data,
 * the ItemProcessor for business validation, and the JpaItemWriter for
 * persisting sanitized records to PostgreSQL.
 * </p>
 */
@Slf4j
@Configuration
public class BatchConfig {

    private static final int CHUNK_SIZE = 1000;
    private static final int SKIP_LIMIT = 10000;
    private static final int CORE_POOL_SIZE = 8;
    private static final int MAX_POOL_SIZE = 16;
    private static final int QUEUE_CAPACITY = 50;

    @Bean(name = "batchTaskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("batch-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean
    public FlatFileItemReader<LegacyCustomerDto> rawCustomerItemReader() {
        return new FlatFileItemReaderBuilder<LegacyCustomerDto>()
                .name("rawCustomerItemReader")
                .resource(new ClassPathResource("data/legacy_customers_1M.csv"))
                .linesToSkip(1)
                .delimited()
                .names("legacyId", "fullName", "email", "phoneNumber", "dateOfBirth", "status")
                .targetType(LegacyCustomerDto.class)
                .build();
    }

    @Bean
    public SynchronizedItemStreamReader<LegacyCustomerDto> customerItemReader(
            FlatFileItemReader<LegacyCustomerDto> rawCustomerItemReader) {
        return new SynchronizedItemStreamReaderBuilder<LegacyCustomerDto>()
                .delegate(rawCustomerItemReader)
                .build();
    }



    @Bean
    public Step processCustomerStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SynchronizedItemStreamReader<LegacyCustomerDto> customerItemReader,
            CustomerItemProcessor customerItemProcessor,
            CustomerItemWriter customerItemWriter,
            CustomerSkipListener customerSkipListener,
            ThreadPoolTaskExecutor batchTaskExecutor) {

        return new StepBuilder("processCustomerStep", jobRepository)
                .<LegacyCustomerDto, Customer>chunk(CHUNK_SIZE)
                .transactionManager(transactionManager)
                .reader(customerItemReader)
                .processor(customerItemProcessor)
                .writer(customerItemWriter)
                .faultTolerant()
                .skipLimit(SKIP_LIMIT)
                .skip(DataValidationException.class)
                .listener(customerSkipListener)
                .taskExecutor(batchTaskExecutor)
                .build();
    }

    /**
     * Customer Migration Job definition.
     */
    @Bean
    public Job customerMigrationJob(JobRepository jobRepository, Step processCustomerStep) {
        return new JobBuilder("customerMigrationJob", jobRepository)
                .start(processCustomerStep)
                .build();
    }
}