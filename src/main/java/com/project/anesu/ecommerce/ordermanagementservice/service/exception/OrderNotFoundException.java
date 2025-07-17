package com.project.anesu.ecommerce.ordermanagementservice.service.exception;

public class OrderNotFoundException extends RuntimeException {

  private static final String ORDER_NOT_FOUND_EXCEPTION_MESSAGE = "Order not found with id: %s";

  public OrderNotFoundException(Long message) {
    super(ORDER_NOT_FOUND_EXCEPTION_MESSAGE.formatted(message));
  }
}
