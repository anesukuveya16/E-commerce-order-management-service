package com.project.anesu.ecommerce.ordermanagementservice.unitTests;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.project.anesu.ecommerce.ordermanagementservice.entity.address.Address;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.Order;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.OrderItem;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.OrderStatus;
import com.project.anesu.ecommerce.ordermanagementservice.model.repository.OrderRepository;
import com.project.anesu.ecommerce.ordermanagementservice.service.OrderServiceImpl;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.InvalidOrderException;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.OrderNotFoundException;
import com.project.anesu.ecommerce.ordermanagementservice.service.util.OrderValidator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplServiceTest {

  @Mock private RestTemplate restTemplateMock;
  @Mock private OrderRepository orderRepositoryMock;
  @Mock private OrderValidator orderValidatorMock;

  private OrderServiceImpl cut;

  @BeforeEach
  void setUp() {
    cut = new OrderServiceImpl(restTemplateMock, orderRepositoryMock, orderValidatorMock);
  }

  @Test
  void shouldSuccessfullyCreateOrder() {

    // Given
    OrderItem orderItem = new OrderItem();
    orderItem.setProductId(101L);
    orderItem.setQuantity(2);

    List<OrderItem> orderItems = List.of(orderItem);

    Order order = new Order();
    order.setCustomerId(1L);
    order.setTotalPrice(99.99);

    doNothing().when(orderValidatorMock).validateNewOrder(any(Order.class), anyList());

    RestCallToInventoryService("http://localhost:9091/api/stock/validate-and-deduct-product");

    Order newOrder = new Order();
    newOrder.setId(1L);
    newOrder.setCustomerId(order.getCustomerId());
    newOrder.setOrderStatus(OrderStatus.ORDER_PLACED);
    newOrder.setTotalPrice(order.getTotalPrice());
    newOrder.setOrderItem(orderItems);

    when(orderRepositoryMock.save(Mockito.any(Order.class))).thenReturn(newOrder);

    // When
    Order newlyCreatedOrder = cut.createOrder(order, orderItems);

    // Then
    assertThat(newlyCreatedOrder).isNotNull();
    assertEquals(OrderStatus.ORDER_PLACED, newlyCreatedOrder.getOrderStatus());

    verify(orderValidatorMock).validateNewOrder(any(Order.class), anyList());
    verify(orderRepositoryMock, times(1)).save(Mockito.any(Order.class));
  }

  @Test
  void shouldNotCreateOrderWhen_ValidationHasFailed() {

    // Given
    OrderItem orderItem = new OrderItem();
    orderItem.setProductId(101L);
    orderItem.setQuantity(2);

    List<OrderItem> orderItems = List.of(orderItem);

    Order order = new Order();
    order.setCustomerId(1L);
    order.setTotalPrice(99.99);

    doThrow(InvalidOrderException.class)
        .when(orderValidatorMock)
        .validateNewOrder(order, orderItems);

    // When
    assertThrows(InvalidOrderException.class, () -> cut.createOrder(order, orderItems));

    // Then
    verify(orderValidatorMock).validateNewOrder(order, orderItems);
    verifyNoMoreInteractions(orderRepositoryMock);
  }

  @Test
  void shouldSuccessfullyRetrieveRequestedOrder_ByGivenOrderId() {

    // Given
    Long orderId = 1L;
    Order order = new Order();
    order.setId(orderId);

    when(orderRepositoryMock.findById(orderId)).thenReturn(Optional.of(order));

    // When
    cut.getOrderById(orderId);

    // Then
    verify(orderRepositoryMock, times(1)).findById(orderId);
  }

  @Test
  void shouldThrowExceptionWhenOrderNotIsFound_ByGivenOrderId() {

    // Given
    Long orderId = 1L;
    when(orderRepositoryMock.findById(orderId)).thenReturn(Optional.empty());

    // When
    assertThrows(OrderNotFoundException.class, () -> cut.getOrderById(orderId));

    // Then
    verify(orderRepositoryMock, times(1)).findById(orderId);
    verifyNoMoreInteractions(orderRepositoryMock);
  }

  @Test
  void shouldRetrieveAllOrdersSuccessfully() {

    // Given
    when(orderRepositoryMock.findAll()).thenReturn(List.of());

    // When
    cut.getAllOrders();

    // Then
    verify(orderRepositoryMock, times(1)).findAll();
  }

  @Test
  void shouldRetrieveOrderByIdAndStatusSuccessfully() {

    // Given
    Long orderId = 15L;
    Order order = new Order();
    order.setId(orderId);
    order.setOrderStatus(OrderStatus.ORDER_PLACED);

    when(orderRepositoryMock.findByIdAndOrderStatus(orderId, order.getOrderStatus()))
        .thenReturn(Optional.of(order));

    // When
    Order retrievedOrder = cut.getOrderByIdAndStatus(orderId, order.getOrderStatus());

    // Then
    assertThat(retrievedOrder.getOrderStatus()).isEqualTo(order.getOrderStatus());

    verify(orderRepositoryMock, times(1)).findByIdAndOrderStatus(orderId, order.getOrderStatus());
  }

  @Test
  void processPendingOrder_shouldProcessNewCreatedOrderSuccessfully() {

    // Given
    Long orderId = 1L;
    Order order = new Order();
    order.setId(orderId);
    order.setOrderStatus(OrderStatus.ORDER_PLACED);

    when(orderRepositoryMock.findByIdAndOrderStatus(orderId, order.getOrderStatus()))
        .thenReturn(Optional.of(order));
    when(orderRepositoryMock.save(any(Order.class))).thenReturn(order);

    // When
    Order processOrder = cut.processPendingOrder(orderId, order.getOrderStatus());
    // Then
    assertThat(processOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING_TO_PROCESSING);

    verify(orderRepositoryMock, times(1)).save(order);
  }

  @Test
  void processPendingOrder_shouldThrowExceptionWhenOrderIsNotFound_ByGivenOrderId() {

    // Given
    Long orderId = 1L;
    OrderStatus currentOrderStatus = OrderStatus.ORDER_PLACED;

    when(orderRepositoryMock.findByIdAndOrderStatus(orderId, currentOrderStatus))
        .thenReturn(Optional.empty());

    // When
    assertThrows(
        OrderNotFoundException.class, () -> cut.processPendingOrder(orderId, currentOrderStatus));

    // Then
    verify(orderRepositoryMock, times(1)).findByIdAndOrderStatus(orderId, currentOrderStatus);
    verifyNoMoreInteractions(orderRepositoryMock);
  }

  @Test
  void sendOrderOutForDelivery_ShouldUpdateOrderStatusSuccessfully() {

    // Given
    Long orderId = 1L;
    Order order = new Order();
    order.setId(orderId);
    order.setOrderStatus(OrderStatus.PENDING_TO_PROCESSING);

    when(orderRepositoryMock.findByIdAndOrderStatus(orderId, order.getOrderStatus()))
        .thenReturn(Optional.of(order));
    when(orderRepositoryMock.save(any(Order.class))).thenReturn(order);

    // When
    Order sendOrder = cut.sendOrderOutForDelivery(orderId, order.getOrderStatus());

    // Then
    assertThat(sendOrder.getOrderStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);

    verify(orderRepositoryMock, times(1)).save(sendOrder);
  }

  @Test
  void sendOrderOutForDelivery_shouldThrowExceptionWhenOrderIsNotFound_ByGivenOrderId() {

    // Given
    Long orderId = 1L;
    OrderStatus currentOrderStatus = OrderStatus.ORDER_PLACED;

    when(orderRepositoryMock.findByIdAndOrderStatus(orderId, currentOrderStatus))
        .thenReturn(Optional.empty());

    // When
    assertThrows(
        OrderNotFoundException.class,
        () -> cut.sendOrderOutForDelivery(orderId, currentOrderStatus));

    // Then

    verify(orderRepositoryMock, times(1)).findByIdAndOrderStatus(orderId, currentOrderStatus);
    verifyNoMoreInteractions(orderRepositoryMock);
  }

  @Test
  void markOrderAsDelivered_ShouldUpdateNewStatusOfSuccessfullyAfterSuccessfulDelivery() {

    // Given
    Long orderId = 1L;
    Order order = new Order();
    order.setId(orderId);
    order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);

    when(orderRepositoryMock.findByIdAndOrderStatus(orderId, order.getOrderStatus()))
        .thenReturn(Optional.of(order));
    when(orderRepositoryMock.save(any(Order.class))).thenReturn(order);

    // When
    Order deliverOrder =
        cut.markAsDeliveredAfterSuccessfulDelivery(orderId, order.getOrderStatus());

    // Then
    assertThat(deliverOrder.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);

    verify(orderRepositoryMock, times(1)).save(order);
  }

  @Test
  void
      markOrderAsDelivered_shouldThrowExceptionWhenRequestedOrderIsNotFound_ByGivenIdAfterSuccessfulDelivery() {

    // Given
    Long orderId = 1L;
    OrderStatus orderStatus = OrderStatus.OUT_FOR_DELIVERY;

    when(orderRepositoryMock.findByIdAndOrderStatus(orderId, orderStatus))
        .thenReturn(Optional.empty());

    // When
    assertThrows(
        OrderNotFoundException.class,
        () -> cut.markAsDeliveredAfterSuccessfulDelivery(orderId, orderStatus));

    // Then
    verify(orderRepositoryMock, times(1)).findByIdAndOrderStatus(orderId, orderStatus);
    verifyNoMoreInteractions(orderRepositoryMock);
  }

  @Test
  void shouldAllowCustomerToUpdateTheirDeliveryAddress() {

    // Given
    Long orderId = 1L;
    Order order = new Order();
    order.setId(orderId);
    order.setOrderStatus(OrderStatus.ORDER_PLACED);

    Address existingAddress = getExistingCustomerAddress("Boch str.", "400", "10243");
    order.setDeliveryAddress(List.of(existingAddress));

    Address updatedAddress = getExistingCustomerAddress("Alvis str.", "2", "10200");

    when(orderRepositoryMock.findById(orderId)).thenReturn(Optional.of(order));
    when(orderRepositoryMock.save(any(Order.class))).thenReturn(order);

    // When
    Order updateExistingAddress =
        cut.updateDeliveryAddress(orderId, updatedAddress.getId(), updatedAddress);

    // Then
    List<Address> addresses = updateExistingAddress.getDeliveryAddress();
    assertEquals(1, addresses.size());

    Address updateAddress = addresses.getFirst();
    assertThat(updateAddress.getStreetName()).isEqualTo("Alvis str.");
    assertThat(updateAddress.getStreetNumber()).isEqualTo("2");
    assertThat(updateAddress.getZipCode()).isEqualTo("10200");

    verify(orderRepositoryMock, times(1)).save(order);
  }

  @Test
  void shouldThrowExceptionWhenOrderForCancellationIsNotFound_ByGivenId() {

    // Given
    Order order = new Order();
    Long orderId = 1L;
    order.setId(orderId);
    order.setCancellationReason("Reason.");

    when(orderRepositoryMock.findById(orderId)).thenReturn(Optional.empty());

    // When
    assertThrows(
        OrderNotFoundException.class,
        () -> cut.cancelOrder(orderId, order.getCancellationReason()));

    // Then
    verify(orderRepositoryMock, times(1)).findById(orderId);
    verifyNoMoreInteractions(orderRepositoryMock);
  }

  @Test
  void shouldSuccessfullyCancelAndRevaluateReturnedOrder() {

    // Given
    Long orderId = 1L;

    OrderItem returningOrderItems = new OrderItem();
    returningOrderItems.setProductId(101L);
    returningOrderItems.setQuantity(2);

    List<OrderItem> orderItems = List.of(returningOrderItems);

    Order order = new Order();
    order.setId(orderId);
    order.setOrderStatus(OrderStatus.CANCELLED);
    order.setCancellationReason("Reason.");
    order.setOrderItem(orderItems);

    RestCallToInventoryService("http://localhost:9091/api/stock/add-returned-inventory");

    when(orderRepositoryMock.findById(orderId)).thenReturn(Optional.of(order));
    when(orderRepositoryMock.save(Mockito.any(Order.class))).thenReturn(order);

    // When
    cut.cancelOrder(orderId, order.getCancellationReason());

    // Then
    assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());

    verify(orderRepositoryMock, times(1)).save(order);
  }

  private Address getExistingCustomerAddress(String streetName, String number, String number1) {
    Address existingAddress = new Address();
    existingAddress.setId(2L);
    existingAddress.setStreetName(streetName);
    existingAddress.setStreetNumber(number);
    existingAddress.setCity("Berlin");
    existingAddress.setState("Berlin");
    existingAddress.setZipCode(number1);
    return existingAddress;
  }

  private void RestCallToInventoryService(String url) {
    ResponseEntity<String> validationResponse =
        new ResponseEntity<>("OK", HttpStatusCode.valueOf(200));
    when(restTemplateMock.postForEntity(
            Mockito.eq(url), Mockito.anyList(), Mockito.eq(String.class)))
        .thenReturn(validationResponse);
  }
}
