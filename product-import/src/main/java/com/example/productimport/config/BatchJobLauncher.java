package com.example.productimport.config;

import com.example.productimport.dto.JobExecutionResponse;
import com.example.productimport.exception.BatchJobException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Service component for launching batch jobs.
 *
 * <p>This class provides methods to execute batch jobs with parameters and retrieve execution
 * results.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BatchJobLauncher {

  private final JobLauncher jobLauncher;
  private final JobExplorer jobExplorer;
  private final JobRepository jobRepository;
  private final ApplicationContext applicationContext;

  /**
   * Executes a batch job with the given parameters.
   *
   * @param jobName the name of the job to execute
   * @param parameters the job parameters
   * @return the job execution response
   * @throws BatchJobException if the job execution fails
   */
  public JobExecutionResponse runJob(String jobName, Map<String, Object> parameters) {
    try {
      Job job = applicationContext.getBean(jobName, Job.class);
      JobParameters jobParameters = buildJobParameters(parameters);

      log.info("Starting job: {} with parameters: {}", jobName, parameters);
      JobExecution execution = jobLauncher.run(job, jobParameters);

      return convertToResponse(execution);
    } catch (Exception e) {
      log.error("Failed to execute job: {}", jobName, e);
      throw new BatchJobException("Failed to execute job: " + jobName, e);
    }
  }

  /**
   * Retrieves job execution details by execution ID.
   *
   * @param executionId the job execution ID
   * @return the job execution response
   */
  public JobExecutionResponse getJobExecution(Long executionId) {
    JobExecution execution = jobExplorer.getJobExecution(executionId);
    if (execution == null) {
      throw new BatchJobException("Job execution not found: " + executionId);
    }
    return convertToResponse(execution);
  }

  /**
   * Builds JobParameters from a map of parameters.
   *
   * @param parameters the parameter map
   * @return the JobParameters object
   */
  private JobParameters buildJobParameters(Map<String, Object> parameters) {
    JobParametersBuilder builder = new JobParametersBuilder();

    // Add timestamp to ensure unique job instance
    builder.addLong("timestamp", System.currentTimeMillis());

    if (parameters != null) {
      parameters.forEach(
          (key, value) -> {
            if (value instanceof String) {
              builder.addString(key, (String) value);
            } else if (value instanceof Long) {
              builder.addLong(key, (Long) value);
            } else if (value instanceof Double) {
              builder.addDouble(key, (Double) value);
            } else {
              builder.addString(key, value.toString());
            }
          });
    }

    return builder.toJobParameters();
  }

  /**
   * Converts JobExecution to JobExecutionResponse.
   *
   * @param execution the job execution
   * @return the response DTO
   */
  private JobExecutionResponse convertToResponse(JobExecution execution) {
    JobExecutionResponse response = new JobExecutionResponse();
    response.setExecutionId(execution.getId());
    response.setStatus(execution.getStatus().toString());
    response.setExitCode(execution.getExitStatus().getExitCode());

    if (execution.getStartTime() != null) {
      response.setStartTime(execution.getStartTime());
    }
    if (execution.getEndTime() != null) {
      response.setEndTime(execution.getEndTime());
    }

    return response;
  }
}
