package com.example.productimport.controller;

import com.example.productimport.config.BatchJobLauncher;
import com.example.productimport.dto.JobExecutionRequest;
import com.example.productimport.dto.JobExecutionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing batch job executions.
 *
 * <p>This controller provides endpoints to trigger batch jobs and retrieve execution status.
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchJobController {

  private final BatchJobLauncher batchJobLauncher;

  /**
   * Triggers a batch job execution.
   *
   * @param jobName the name of the job to execute
   * @param request the job execution request containing parameters
   * @return the job execution response
   */
  @PostMapping("/jobs/{jobName}")
  public ResponseEntity<JobExecutionResponse> runJob(
      @PathVariable String jobName, @Valid @RequestBody JobExecutionRequest request) {
    JobExecutionResponse response = batchJobLauncher.runJob(jobName, request.getJobParameters());
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }

  /**
   * Retrieves job execution details by execution ID.
   *
   * @param executionId the job execution ID
   * @return the job execution response
   */
  @GetMapping("/executions/{executionId}")
  public ResponseEntity<JobExecutionResponse> getJobExecution(@PathVariable Long executionId) {
    JobExecutionResponse response = batchJobLauncher.getJobExecution(executionId);
    return ResponseEntity.ok(response);
  }
}
