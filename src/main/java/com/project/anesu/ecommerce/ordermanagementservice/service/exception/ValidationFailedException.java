package com.project.anesu.ecommerce.ordermanagementservice.service.exception;

public class ValidationFailedException extends RuntimeException {
  public ValidationFailedException(String message) {
    super(message);
  }
}
