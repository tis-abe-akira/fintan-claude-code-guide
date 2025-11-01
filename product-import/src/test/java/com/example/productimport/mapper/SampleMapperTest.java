package com.example.productimport.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.productimport.entity.SampleEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

/**
 * Test class for SampleMapper.
 *
 * <p>This class tests MyBatis mapper operations for SampleEntity.
 */
@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class SampleMapperTest {

  @Autowired private SampleMapper sampleMapper;

  /**
   * Tests selecting all sample entities.
   */
  @Test
  @Sql("/test-data.sql")
  void testSelectAll() {
    // When
    List<SampleEntity> entities = sampleMapper.selectAll();

    // Then
    assertThat(entities).isNotEmpty();
  }

  /**
   * Tests inserting a new sample entity.
   */
  @Test
  void testInsert() {
    // Given
    SampleEntity entity = new SampleEntity();
    entity.setName("Test Entity");
    entity.setStatus("NEW");
    entity.setCreatedAt(LocalDateTime.now());
    entity.setUpdatedAt(LocalDateTime.now());

    // When
    int result = sampleMapper.insert(entity);

    // Then
    assertThat(result).isEqualTo(1);
    assertThat(entity.getId()).isNotNull();
  }

  /**
   * Tests updating a sample entity.
   */
  @Test
  @Sql("/test-data.sql")
  void testUpdate() {
    // Given
    SampleEntity entity = sampleMapper.selectAll().get(0);
    entity.setStatus("UPDATED");
    entity.setUpdatedAt(LocalDateTime.now());

    // When
    int result = sampleMapper.update(entity);

    // Then
    assertThat(result).isEqualTo(1);

    SampleEntity updated = sampleMapper.selectById(entity.getId());
    assertThat(updated.getStatus()).isEqualTo("UPDATED");
  }
}
