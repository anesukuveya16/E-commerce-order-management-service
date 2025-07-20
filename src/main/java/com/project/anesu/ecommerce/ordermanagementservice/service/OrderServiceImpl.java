package com.project.anesu.ecommerce.ordermanagementservice.service;

import com.project.anesu.ecommerce.ordermanagementservice.entity.address.Address;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.Order;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.OrderItem;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.OrderStatus;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.OrderValidationEndpoints;
import com.project.anesu.ecommerce.ordermanagementservice.model.OrderService;
import com.project.anesu.ecommerce.ordermanagementservice.model.repository.OrderRepository;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.AddressNotFoundException;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.InventoryReturnFailureException;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.OrderNotFoundException;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.ValidationFailedException;
import com.project.anesu.ecommerce.ordermanagementservice.service.util.OrderValidator;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final RestTemplate restTemplate;
  private final OrderRepository orderRepository;
  private final OrderValidator orderValidator;

  @Override
  public Order createOrder(Order order, List<OrderItem> orderItems) throws OrderNotFoundException {

    validateNewOrder(order, orderItems);

    List<Map<String, Object>> batchOrderRequest = getBatchOrderRequest(orderItems);

    String validationUrl = OrderValidationEndpoints.VALIDATE_AND_DEDUCT_PRODUCT.getUrl();

    ResponseEntity<String> validationResponse =
        restTemplate.postForEntity(validationUrl, batchOrderRequest, String.class);

    if (validationResponse.getStatusCode() != HttpStatus.OK) {
      throw new ValidationFailedException("Order validation failed" + validationResponse.getBody());
    }

    setAdditionalOrderDetails(order);

    for (OrderItem orderItem : orderItems) {
      orderItem.setOrder(order);
    }
    order.setOrderItem(orderItems);

    return orderRepository.save(order);
  }

  @Override
  public Order addDeliveryAddressToOrder(Long orderId, Address address)
      throws OrderNotFoundException {

    Order order = getOrderById(orderId);

    address.setOrder(order);
    order.getDeliveryAddress().add(address);

    return orderRepository.save(order);
  }

  @Override
  public Order processPendingOrder(Long orderId) throws OrderNotFoundException {

    Order orderRequest = getOrderByIdAndStatus(orderId, OrderStatus.ORDER_PLACED);

    orderRequest.setOrderStatus(OrderStatus.PROCESSING);
    return orderRepository.save(orderRequest);
  }

  @Override
  public Order sendOrderOutForDelivery(Long orderId) throws OrderNotFoundException {

    Order orderRequest = getOrderByIdAndStatus(orderId, OrderStatus.PROCESSING);

    orderRequest.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
    return orderRepository.save(orderRequest);
  }

  @Override
  public Order markAsDeliveredAfterSuccessfulDelivery(Long orderId) throws OrderNotFoundException {

    Order orderRequest = getOrderByIdAndStatus(orderId, OrderStatus.OUT_FOR_DELIVERY);

    orderRequest.setOrderStatus(OrderStatus.DELIVERED);
    return orderRepository.save(orderRequest);
  }

  @Override
  public Order getOrderById(Long orderId) {

    return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
  }

  public Order getOrderByIdAndStatus(Long orderId, OrderStatus status)
      throws OrderNotFoundException {

    return orderRepository
        .findByIdAndOrderStatus(orderId, status)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
  }

  @Override
  public List<Order> getAllOrders() {

    return orderRepository.findAll();
  }

  @Transactional
  @Override
  public Order updateDeliveryAddress(Long orderId, Long addressId, Address updatedOrderAddress)
      throws OrderNotFoundException {

    Order order = getOrderById(orderId);
    List<Address> savedAddresses = order.getDeliveryAddress();

    boolean updated = false;
    for (Address existingAddressToUpdate : savedAddresses) {
      if (existingAddressToUpdate.getId().equals(addressId)) {
        updateExistingDeliveryAddress(updatedOrderAddress, existingAddressToUpdate);

        existingAddressToUpdate.setOrder(order);
        updated = true;
        break;
      }
    }

    if (!updated) {
      throw new AddressNotFoundException("No address found with ID: " + addressId);
    }
    return orderRepository.save(order);
  }

  @Override
  public Order cancelOrder(Long orderId, String cancellationReason) throws OrderNotFoundException {

    Order orderRequest = getOrderById(orderId);

    orderRequest.setOrderStatus(OrderStatus.CANCELLED);
    orderRequest.setCancellationReason(cancellationReason);

    List<OrderItem> orderItems = orderRequest.getOrderItem();

    List<Map<String, Object>> batchOrderRequest = getBatchOrderRequest(orderItems);

    String returnInventoryUrl = OrderValidationEndpoints.ADD_RETURNED_INVENTORY.getUrl();

    ResponseEntity<String> expectedResponse =
        restTemplate.postForEntity(returnInventoryUrl, batchOrderRequest, String.class);

    if (expectedResponse.getStatusCode() != HttpStatus.OK) {
      throw new InventoryReturnFailureException(
          "An error occurred while trying to return inventory.");
    }

    orderRepository.save(orderRequest);
    return orderRequest;
  }

  private void validateNewOrder(Order order, List<OrderItem> orderItems) {
    orderValidator.validateNewOrder(order, orderItems);
  }

  private List<Map<String, Object>> getBatchOrderRequest(List<OrderItem> orderItems) {
    List<Map<String, Object>> batchOrderRequest = new ArrayList<>();

    for (OrderItem orderItem : orderItems) {
      Map<String, Object> orderItemData = new HashMap<>();
      orderItemData.put("productId", orderItem.getProductId());
      orderItemData.put("quantity", orderItem.getQuantity());
      batchOrderRequest.add(orderItemData);
    }
    return batchOrderRequest;
  }

  private void updateExistingDeliveryAddress(
      Address updatedOrderAddress, Address existingAddressToUpdate) {
    existingAddressToUpdate.setStreetName(updatedOrderAddress.getStreetName());
    existingAddressToUpdate.setStreetNumber(updatedOrderAddress.getStreetNumber());
    existingAddressToUpdate.setCity(updatedOrderAddress.getCity());
    existingAddressToUpdate.setState(updatedOrderAddress.getState());
    existingAddressToUpdate.setZipCode(updatedOrderAddress.getZipCode());
  }

  private void setAdditionalOrderDetails(Order order) {
    order.setCustomerId(order.getCustomerId());
    order.setOrderDate(LocalDateTime.now());
    order.setOrderStatus(OrderStatus.ORDER_PLACED);
    order.setTotalPrice(order.getTotalPrice());
  }
}
