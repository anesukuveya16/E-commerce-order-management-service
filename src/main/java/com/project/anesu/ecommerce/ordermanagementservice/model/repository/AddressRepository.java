package com.project.anesu.ecommerce.ordermanagementservice.model.repository;

import com.project.anesu.ecommerce.ordermanagementservice.entity.address.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {}
