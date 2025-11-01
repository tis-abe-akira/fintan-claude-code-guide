package com.example.productimport.job;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test class for SampleJobConfig.
 *
 * <p>This class tests the sample batch job execution and verifies its behavior.
 */
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class SampleJobConfigTest {

  @Autowired private JobLauncherTestUtils jobLauncherTestUtils;

  /**
   * Tests that the sample job executes successfully.
   *
   * @throws Exception if job execution fails
   */
  @Test
  void testSampleJob() throws Exception {
    // Given
    JobParameters jobParameters =
        new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters();

    // When
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    // Then
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
  }
}
