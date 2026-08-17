package com.project.migration.config;

import com.project.migration.batch.listener.CustomerSkipListener;
import com.project.migration.batch.processor.CustomerItemProcessor;
import com.project.migration.batch.writer.CustomerItemWriter;
import com.project.migration.dto.LegacyCustomerDto;
import com.project.migration.entity.Customer;
import com.project.migration.exception.DataValidationException;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Bean
    public FlatFileItemReader<LegacyCustomerDto> customerItemReader() {
        return new FlatFileItemReaderBuilder<LegacyCustomerDto>()
                .name("customerItemReader")
                .resource(new ClassPathResource("data/legacy_customers_valid.csv"))
                .linesToSkip(1)
                .delimited()
                .names("legacyId", "fullName", "email", "phoneNumber", "dateOfBirth", "status")
                .targetType(LegacyCustomerDto.class)
                .build();
    }



    @Bean
    public Step processCustomerStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    ItemReader<LegacyCustomerDto> reader,
                                    CustomerItemProcessor processor,
                                    CustomerItemWriter writer,
                                    CustomerSkipListener skipListener) {

        return new StepBuilder("processCustomerStep", jobRepository)
                .<LegacyCustomerDto, Customer>chunk(1000)
                .transactionManager(transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(1000)
                .skip(DataValidationException.class)
                .listener(skipListener)
                .build();
    }


    @Bean
    public Job customerMigrationJob(JobRepository jobRepository, Step processCustomerStep) {
        return new JobBuilder("customerMigrationJob", jobRepository)
                .start(processCustomerStep)
                .build();
    }
}