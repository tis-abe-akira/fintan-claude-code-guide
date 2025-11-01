package com.example.productimport.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * Response DTO for batch job execution.
 *
 * <p>This class contains job execution details including status and timing information.
 */
@Data
public class JobExecutionResponse {

  private Long executionId;

  private String status;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String exitCode;
}
