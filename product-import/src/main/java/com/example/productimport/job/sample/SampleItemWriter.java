package com.example.productimport.job.sample;

import com.example.productimport.entity.SampleEntity;
import com.example.productimport.mapper.SampleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Sample item writer using MyBatis.
 *
 * <p>This writer updates sample entities in the database using MyBatis mapper.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SampleItemWriter implements ItemWriter<SampleEntity> {

  private final SampleMapper sampleMapper;

  /**
   * Writes a chunk of items to the database.
   *
   * @param chunk the chunk of items to write
   */
  @Override
  public void write(Chunk<? extends SampleEntity> chunk) {
    for (SampleEntity item : chunk) {
      log.debug("Writing item: {}", item);
      int updated = sampleMapper.update(item);
      if (updated == 0) {
        log.warn("No rows updated for item: {}", item);
      }
    }
    log.info("Successfully wrote {} items", chunk.size());
  }
}
