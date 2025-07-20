package com.project.anesu.ecommerce.ordermanagementservice.controller.order;

import static com.project.anesu.ecommerce.ordermanagementservice.controller.order.OrderServiceRestEndpoints.*;

import com.project.anesu.ecommerce.ordermanagementservice.entity.address.Address;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.Order;
import com.project.anesu.ecommerce.ordermanagementservice.model.OrderService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(LANDING_PAGE)
public class OrderController {

  private final OrderService orderService;

  @PostMapping(CREATE_ORDER)
  public ResponseEntity<Order> createOrder(@RequestBody Order order) {

    Order createdOrder = orderService.createOrder(order, order.getOrderItem());

    if (createdOrder != null) {
      return ResponseEntity.ok().body(createdOrder);
    } else {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping(ADD_DELIVERY_ADDRESS)
  public Order linkDeliveryAddressToCreatedOrder(
      @PathVariable Long orderId, @RequestBody Address address) {

    return orderService.addDeliveryAddressToOrder(orderId, address);
  }

  @GetMapping(GET_ALL_ORDERS)
  public List<Order> getAllOrders() {

    return orderService.getAllOrders();
  }

  @GetMapping(GET_ORDER_BY_ID)
  public Order getOrderById(@PathVariable Long orderId) {

    return orderService.getOrderById(orderId);
  }

  @PutMapping(PROCESS_ORDER)
  public Order processPendingOrder(@PathVariable Long orderId) {

    return orderService.processPendingOrder(orderId);
  }

  @PutMapping(SEND_ORDER_FOR_DELIVERY)
  public Order sendOutOrderToDeliverToCustomer(@PathVariable Long orderId) {

    return orderService.sendOrderOutForDelivery(orderId);
  }

  @PutMapping(MARK_AS_DELIVERED)
  public Order markOrderAsDelivered(@PathVariable Long orderId) {

    return orderService.markAsDeliveredAfterSuccessfulDelivery(orderId);
  }

  @PutMapping(CANCEL_ORDER)
  public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId, @RequestBody String reason) {

    Order cancel = orderService.cancelOrder(orderId, reason);
    return ResponseEntity.status(HttpStatus.OK).body(cancel);
  }

  @PutMapping(UPDATE_DELIVERY_ADDRESS)
  public ResponseEntity<Order> updateDeliveryAddress(
      @PathVariable Long orderId,
      @PathVariable Long addressId,
      @RequestBody Address updatedAddress) {

    Order update = orderService.updateDeliveryAddress(orderId, addressId, updatedAddress);
    if (update != null) {
      return ResponseEntity.ok(update);
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
