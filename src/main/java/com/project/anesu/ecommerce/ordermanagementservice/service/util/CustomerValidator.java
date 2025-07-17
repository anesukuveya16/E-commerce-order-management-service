package com.project.anesu.ecommerce.ordermanagementservice.service.util;

import com.project.anesu.ecommerce.ordermanagementservice.entity.customer.Customer;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.InvalidCustomerException;
import org.springframework.stereotype.Component;

@Component
public class CustomerValidator {

  public void validate(Customer customer) {

    validateCustomerInfo(customer);
    validateCustomerEmail(customer);
  }

  private void validateCustomerInfo(Customer customer) throws InvalidCustomerException {

    if (customer.getFirstName() == null || customer.getLastName() == null) {
      throw new InvalidCustomerException("First or last name is null!");
    }

    if (customer.getSavedAddresses() == null) {
      throw new InvalidCustomerException("Please provide valid address.");
    }

    if (customer.getPhoneNumber() == null) {
      throw new InvalidCustomerException("Please provide valid phone number.");
    }
  }

  private void validateCustomerEmail(Customer customer) throws InvalidCustomerException {

    if (customer.getEmail() == null) {
      throw new InvalidCustomerException("Valid email is required!");
    }
  }
}
