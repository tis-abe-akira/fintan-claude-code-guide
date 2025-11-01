package com.example.productimport.job.sample;

import com.example.productimport.entity.SampleEntity;
import com.example.productimport.mapper.SampleMapper;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sample item reader configuration using MyBatis paging.
 *
 * <p>This configuration creates an item reader that fetches sample entities from the database
 * using MyBatis paging mechanism.
 */
@Configuration
@RequiredArgsConstructor
public class SampleItemReaderConfig {

  private final SqlSessionFactory sqlSessionFactory;

  /**
   * Creates a MyBatis paging item reader for sample entities.
   *
   * @return the configured item reader
   */
  @Bean
  public ItemReader<SampleEntity> sampleItemReader() {
    return new MyBatisPagingItemReaderBuilder<SampleEntity>()
        .sqlSessionFactory(sqlSessionFactory)
        .queryId("com.example.productimport.mapper.SampleMapper.selectAll")
        .pageSize(10)
        .parameterValues(new HashMap<>())
        .build();
  }
}
