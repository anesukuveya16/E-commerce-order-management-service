package com.project.anesu.ecommerce.ordermanagementservice.model.repository;

import com.project.anesu.ecommerce.ordermanagementservice.entity.order.Order;
import com.project.anesu.ecommerce.ordermanagementservice.entity.order.OrderStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  Optional<Order> findByIdAndOrderStatus(Long orderId, OrderStatus status);
}
