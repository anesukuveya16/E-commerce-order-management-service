package com.project.anesu.ecommerce.ordermanagementservice.controller;

import com.project.anesu.ecommerce.ordermanagementservice.service.exception.InventoryReturnFailureException;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.ValidationFailedException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
    Map<String, String> errorResponse = Map.of("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(ValidationFailedException.class)
  public ResponseEntity<Map<String, String>> handleValidationFailedException(
      ValidationFailedException ex) {
    Map<String, String> errorResponse = Map.of("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(RestClientException.class)
  public ResponseEntity<Map<String, String>> handleRestClientException(RestClientException ex) {
    Map<String, String> errorResponse = Map.of("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
  }

  @ExceptionHandler(InventoryReturnFailureException.class)
  public ResponseEntity<Map<String, String>> handleRestClientException(
      InventoryReturnFailureException ex) {
    Map<String, String> errorResponse = Map.of("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  // Add other handlers as needed
}
