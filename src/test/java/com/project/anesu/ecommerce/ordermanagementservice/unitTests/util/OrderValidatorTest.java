package com.project.anesu.ecommerce.ordermanagementservice.unitTests.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.project.anesu.ecommerce.ordermanagementservice.entity.order.Order;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.OrderItem;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.InvalidOrderException;
import com.project.anesu.ecommerce.ordermanagementservice.service.util.OrderValidator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderValidatorTest {

  private OrderValidator cut;

  @BeforeEach
  void setUp() {
    cut = new OrderValidator();
  }

  @Test
  void shouldThrowExceptionWhen_CustomerIdIsNull() {

    // Given
    Order order = new Order();
    order.setCustomerId(null);
    order.setOrderItem(List.of());

    // When
    InvalidOrderException exception =
        assertThrows(
            InvalidOrderException.class, () -> cut.validateNewOrder(order, order.getOrderItem()));

    // Then
    assertEquals("Customer ID cannot be null.", exception.getMessage());
  }

  @Test
  void shouldThrowException_WhenOrderItemListQuantityIsEmpty() {

    // Given
    Long customerId = 1L;

    Order order = new Order();
    order.setCustomerId(customerId);

    List<OrderItem> orderItems = List.of();

    // When
    InvalidOrderException exception =
        assertThrows(InvalidOrderException.class, () -> cut.validateNewOrder(order, orderItems));

    // Then
    assertEquals("Order must contain at least one item.", exception.getMessage());
  }

  @Test
  void shouldThrowException_WhenProductIdIsNull_ForOrderItem() {

    // Given
    Long customerId = 1L;
    Order order = new Order();
    order.setCustomerId(customerId);

    OrderItem orderItem = new OrderItem();
    orderItem.setQuantity(3);
    orderItem.setProductId(null);

    List<OrderItem> orderItems = List.of(orderItem);

    // When
    InvalidOrderException exception =
        assertThrows(InvalidOrderException.class, () -> cut.validateNewOrder(order, orderItems));

    // Then
    assertEquals("Product ID cannot be null.", exception.getMessage());
  }

  @Test
  void shouldThrowException_WhenOrderItemQuantityIsLessThanZero_ForOrder() {

    // Given
    Order order = new Order();
    order.setCustomerId(2L);

    OrderItem orderItem = new OrderItem();
    orderItem.setProductId(2L);
    orderItem.setQuantity(0);

    List<OrderItem> orderItems = new ArrayList<>();
    orderItems.add(null);

    // When
    InvalidOrderException exception =
        assertThrows(InvalidOrderException.class, () -> cut.validateNewOrder(order, orderItems));

    // Then
    assertEquals("Order item cannot be null.", exception.getMessage());
  }
}
