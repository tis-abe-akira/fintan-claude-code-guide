package com.example.productimport.job.sample;

import com.example.productimport.entity.SampleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Sample batch job configuration.
 *
 * <p>This configuration defines a sample batch job with a single step that reads, processes, and
 * writes sample entities.
 */
@Configuration
@RequiredArgsConstructor
public class SampleJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  /**
   * Defines the sample batch job.
   *
   * @param sampleStep the step to execute in this job
   * @return the configured job
   */
  @Bean
  public Job sampleJob(Step sampleStep) {
    return new JobBuilder("sampleJob", jobRepository).start(sampleStep).build();
  }

  /**
   * Defines the sample step.
   *
   * @param reader the item reader
   * @param processor the item processor
   * @param writer the item writer
   * @return the configured step
   */
  @Bean
  public Step sampleStep(
      ItemReader<SampleEntity> sampleItemReader,
      ItemProcessor<SampleEntity, SampleEntity> sampleItemProcessor,
      ItemWriter<SampleEntity> sampleItemWriter) {
    return new StepBuilder("sampleStep", jobRepository)
        .<SampleEntity, SampleEntity>chunk(10, transactionManager)
        .reader(sampleItemReader)
        .processor(sampleItemProcessor)
        .writer(sampleItemWriter)
        .build();
  }
}
