package com.project.anesu.ecommerce.ordermanagementservice.service;

import com.project.anesu.ecommerce.ordermanagementservice.entity.address.Address;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.Order;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.OrderItem;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.OrderStatus;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final RestTemplate restTemplate;
  private final OrderRepository orderRepository;
  private final OrderValidator orderValidator;
  private static final String VALIDATE_ORDER = "http://localhost:9091";

  @Override
  public Order createOrder(Order order, List<OrderItem> orderItems) throws OrderNotFoundException {

    validateNewOrder(order, orderItems);

    List<Map<String, Object>> batchInventoryValidationRequest = new ArrayList<>();
    for (OrderItem orderItem : orderItems) {
      Map<String, Object> orderItemData = new HashMap<>();
      orderItemData.put("productId", orderItem.getProductId());
      orderItemData.put("quantity", orderItem.getQuantity());
      batchInventoryValidationRequest.add(orderItemData);
    }

    String validationUrl = VALIDATE_ORDER + "/api/stock/validate-and-deduct-product";

    try {
      ResponseEntity<String> validationResponse =
          restTemplate.postForEntity(validationUrl, batchInventoryValidationRequest, String.class);

      // the service itself could not process the order entirely
      if (validationResponse.getStatusCode() != HttpStatus.OK) {
        throw new ValidationFailedException(
            "Order validation failed" + validationResponse.getBody());
      }
      // while making the request of validation
    } catch (HttpClientErrorException e) {
      throw new IllegalStateException(
          "Error occurred, could not creat order successfully." + e.getResponseBodyAsString());
    } catch (RestClientException e) {
      throw new RestClientException("Failed to connect to Product Service" + e.getMessage());
    }

    order.setCustomerId(order.getCustomerId());
    order.setOrderDate(LocalDateTime.now());
    order.setOrderStatus(OrderStatus.ORDER_PLACED);
    order.setTotalPrice(order.getTotalPrice());

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
  public Order processPendingOrder(Long orderId, OrderStatus status) throws OrderNotFoundException {

    Order orderRequest = getOrderByIdAndStatus(orderId, status);
    orderRequest.setOrderStatus(OrderStatus.PENDING_TO_PROCESSING);
    return orderRepository.save(orderRequest);
  }

  @Override
  public Order sendOrderOutForDelivery(Long orderId, OrderStatus status)
      throws OrderNotFoundException {

    Order orderRequest = getOrderByIdAndStatus(orderId, status);
    orderRequest.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);

    return orderRepository.save(orderRequest);
  }

  @Override
  public Order markAsDeliveredAfterSuccessfulDelivery(Long orderId, OrderStatus status)
      throws OrderNotFoundException {

    Order orderRequest = getOrderByIdAndStatus(orderId, status);
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
        existingAddressToUpdate.setStreetName(updatedOrderAddress.getStreetName());
        existingAddressToUpdate.setStreetNumber(updatedOrderAddress.getStreetNumber());
        existingAddressToUpdate.setCity(updatedOrderAddress.getCity());
        existingAddressToUpdate.setState(updatedOrderAddress.getState());
        existingAddressToUpdate.setZipCode(updatedOrderAddress.getZipCode());

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

    List<Map<String, Object>> orderBatchRequest = new ArrayList<>();

    for (OrderItem orderItem : orderItems) {
      Map<String, Object> orderItemData = new HashMap<>();
      orderItemData.put("productId", orderItem.getProductId());
      orderItemData.put("quantity", orderItem.getQuantity());
      orderBatchRequest.add(orderItemData);
    }

    String returnInventoryUrl = VALIDATE_ORDER + "/api/stock/add-returned-inventory";

    try {
      ResponseEntity<String> expectedResponse =
          restTemplate.postForEntity(returnInventoryUrl, orderBatchRequest, String.class);

      if (expectedResponse.getStatusCode() != HttpStatus.OK) {
        throw new InventoryReturnFailureException(
            "An error occurred while trying to return inventory.");
      }

    } catch (RestClientException e) {
      throw new RestClientException("Failed to connect to Product Service" + e.getMessage());
    }

    orderRepository.save(orderRequest);
    return orderRequest;
  }

  private void validateNewOrder(Order order, List<OrderItem> orderItems) {
    orderValidator.validateNewOrder(order, orderItems);
  }
}
