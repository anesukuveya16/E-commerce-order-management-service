package com.project.anesu.ecommerce.ordermanagementservice.model;

import com.project.anesu.ecommerce.ordermanagementservice.entity.address.Address;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.Order;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.OrderItem;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.OrderStatus;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.OrderNotFoundException;
import java.util.List;

/**
 * Service interface for managing order-related operations within the Order Management Service.
 *
 * <p>This interface defines the contract for creating, retrieving, updating, and canceling orders.
 */
public interface OrderService {

  /**
   * Creates a new order along with its order items.
   *
   * @param order the {@link Order} entity to be created
   * @param orderItems the list of {@link OrderItem} entities associated with the order
   * @return the created {@link Order} object
   */
  Order createOrder(Order order, List<OrderItem> orderItems);

  /**
   * Process a pending order by changing its status.
   *
   * @param orderId the ID of the order to process
   * @param status the new {@link OrderStatus} to apply
   * @return the updated {@link Order}
   * @throws OrderNotFoundException if the order does not exist
   */
  Order processPendingOrder(Long orderId, OrderStatus status) throws OrderNotFoundException;

  /** Update order status to OUT_FOR_DELIVERY. */
  Order sendOrderOutForDelivery(Long orderId, OrderStatus status) throws OrderNotFoundException;

  /** Marks the order as DELIVERED. */
  Order markAsDeliveredAfterSuccessfulDelivery(Long orderId, OrderStatus status)
      throws OrderNotFoundException;

  /**
   * Retrieves an order by its ID.
   *
   * @param orderId the ID of the order to retrieve
   * @return the {@link Order} if found
   * @throws OrderNotFoundException if no order is found with the given ID
   */
  Order getOrderById(Long orderId) throws OrderNotFoundException;

  /**
   * Retrieves all orders.
   *
   * @return a list of all {@link Order} objects
   */
  List<Order> getAllOrders();

  /**
   * Updates a delivery address for a given order and address ID.
   *
   * @param orderId the ID of the order
   * @param addressId the ID of the address to update
   * @param updatedOrder the updated address details
   * @return the updated {@link Order}
   * @throws OrderNotFoundException if the order is not found
   */
  Order updateDeliveryAddress(Long orderId, Long addressId, Address updatedOrder)
      throws OrderNotFoundException;

  /**
   * Cancels an existing order by its ID.
   *
   * @param orderId the ID of the order to cancel
   * @param cancellationReason reason for cancellation
   * @return the updated {@link Order} with cancellation info
   */
  Order cancelOrder(Long orderId, String cancellationReason);

  /**
   * Adds a new delivery address to an order.
   *
   * @param orderId the ID of the order
   * @param address the new address to be added
   * @return the updated {@link Order}
   * @throws OrderNotFoundException if the order does not exist
   */
  Order addDeliveryAddressToOrder(Long orderId, Address address) throws OrderNotFoundException;
}
