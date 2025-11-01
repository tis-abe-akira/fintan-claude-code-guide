package com.example.productimport.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sample entity representing a record in the sample_entity table.
 *
 * <p>This entity is used as an example for batch processing operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleEntity {

  private Long id;

  @NotBlank(message = "Name must not be blank")
  @Size(max = 100, message = "Name must not exceed 100 characters")
  private String name;

  @Size(max = 50, message = "Status must not exceed 50 characters")
  private String status;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}
