package com.example.productimport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Product Import Batch application.
 *
 * <p>This Spring Boot application provides batch processing capabilities for importing and
 * processing product data.
 */
@SpringBootApplication
public class Application {

  /**
   * Main entry point for the application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
