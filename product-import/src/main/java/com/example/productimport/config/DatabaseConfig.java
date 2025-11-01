package com.example.productimport.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Database configuration class for MyBatis.
 *
 * <p>This class configures MyBatis mapper scanning and database-related settings.
 */
@Configuration
@MapperScan("com.example.productimport.mapper")
public class DatabaseConfig {
  // Spring Boot auto-configuration handles DataSource, SqlSessionFactory, and TransactionManager
}
