package com.example.productimport.dto;

import java.util.Map;
import lombok.Data;

/**
 * Request DTO for batch job execution.
 *
 * <p>This class contains job parameters to be passed when starting a batch job.
 */
@Data
public class JobExecutionRequest {

  private Map<String, Object> jobParameters;
}
