package com.project.anesu.ecommerce.ordermanagementservice.service.exception;

public class CustomerNotFoundException extends RuntimeException {

  private static final String CUSTOMER_NOT_FOUND_MESSAGE = "Customer not found with id: %s.";

  public CustomerNotFoundException(Long message) {
    super(CUSTOMER_NOT_FOUND_MESSAGE.formatted(message));
  }
}
