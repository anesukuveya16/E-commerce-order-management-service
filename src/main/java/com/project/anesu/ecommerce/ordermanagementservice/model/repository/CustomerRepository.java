package com.project.anesu.ecommerce.ordermanagementservice.model.repository;

import com.project.anesu.ecommerce.ordermanagementservice.entity.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {}
