package com.example.productimport.mapper;

import com.example.productimport.entity.SampleEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis mapper interface for SampleEntity operations.
 *
 * <p>This mapper provides database access methods for SampleEntity table.
 */
@Mapper
public interface SampleMapper {

  /**
   * Selects all sample entities.
   *
   * @return list of all sample entities
   */
  List<SampleEntity> selectAll();

  /**
   * Inserts a new sample entity.
   *
   * @param entity the entity to insert
   * @return number of affected rows
   */
  int insert(SampleEntity entity);

  /**
   * Updates an existing sample entity.
   *
   * @param entity the entity to update
   * @return number of affected rows
   */
  int update(SampleEntity entity);

  /**
   * Selects a sample entity by ID.
   *
   * @param id the entity ID
   * @return the sample entity
   */
  SampleEntity selectById(Long id);
}
