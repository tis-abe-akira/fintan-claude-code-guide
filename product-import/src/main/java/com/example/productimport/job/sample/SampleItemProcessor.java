package com.example.productimport.job.sample;

import com.example.productimport.entity.SampleEntity;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Sample item processor for business logic.
 *
 * <p>This processor transforms and validates sample entities during batch processing.
 */
@Component
@Slf4j
public class SampleItemProcessor implements ItemProcessor<SampleEntity, SampleEntity> {

  /**
   * Processes a sample entity by applying business logic.
   *
   * @param item the input item
   * @return the processed item, or null to skip the item
   */
  @Override
  public SampleEntity process(SampleEntity item) {
    log.debug("Processing item: {}", item);

    // Example business logic: update status based on name
    if (item.getName() != null && item.getName().contains("INVALID")) {
      log.warn("Skipping invalid item: {}", item);
      return null; // Skip this item
    }

    // Update status and timestamp
    item.setStatus("PROCESSED");
    item.setUpdatedAt(LocalDateTime.now());

    log.debug("Processed item: {}", item);
    return item;
  }
}
