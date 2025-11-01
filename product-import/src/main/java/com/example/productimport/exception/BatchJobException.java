package com.example.productimport.exception;

/**
 * Custom exception for batch job operations.
 *
 * <p>This exception is thrown when batch job execution or configuration errors occur.
 */
public class BatchJobException extends RuntimeException {

  /**
   * Constructs a new batch job exception with the specified message.
   *
   * @param message the detail message
   */
  public BatchJobException(String message) {
    super(message);
  }

  /**
   * Constructs a new batch job exception with the specified message and cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public BatchJobException(String message, Throwable cause) {
    super(message, cause);
  }
}
