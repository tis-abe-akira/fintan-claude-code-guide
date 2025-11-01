package com.example.productimport.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch configuration class.
 *
 * <p>This class enables Spring Batch processing and configures batch-related beans.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {
  // Spring Boot auto-configuration handles JobRepository, JobLauncher, and TransactionManager
}
