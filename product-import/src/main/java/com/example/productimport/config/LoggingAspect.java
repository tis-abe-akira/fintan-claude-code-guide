package com.example.productimport.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * AOP logging aspect for all public methods.
 *
 * <p>This aspect logs the start and end of all public methods in the application, along with
 * execution duration.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

  /**
   * Logs method execution start, end, duration, and any exceptions.
   *
   * @param joinPoint the proceeding join point
   * @return the result of the method execution
   * @throws Throwable if the method execution throws an exception
   */
  @Around("execution(public * com.example.productimport..*.*(..))")
  public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().toShortString();
    long startTime = System.currentTimeMillis();

    log.info("Starting method: {}", methodName);

    try {
      Object result = joinPoint.proceed();
      long duration = System.currentTimeMillis() - startTime;
      log.info("Completed method: {}, Duration: {} ms", methodName, duration);
      return result;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Exception in method: {}, Duration: {} ms", methodName, duration, e);
      throw e;
    }
  }
}
