package com.project.anesu.ecommerce.ordermanagementservice.service.exception;

public class InvalidOrderException extends RuntimeException {
  public InvalidOrderException(String message) {
    super(message);
  }
}
