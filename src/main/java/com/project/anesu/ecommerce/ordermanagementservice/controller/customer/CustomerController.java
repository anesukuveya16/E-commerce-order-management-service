package com.project.anesu.ecommerce.ordermanagementservice.controller.customer;

import static com.project.anesu.ecommerce.ordermanagementservice.controller.customer.CustomerServiceRestEndpoints.*;

import com.project.anesu.ecommerce.ordermanagementservice.entity.address.Address;
import com.project.anesu.ecommerce.ordermanagementservice.entity.customer.Customer;
import com.project.anesu.ecommerce.ordermanagementservice.model.CustomerService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(LANDING_PAGE)
public class CustomerController {

  private final CustomerService customerService;

  @PostMapping(CREATE_CUSTOMER)
  public Customer createCustomer(@RequestBody Customer customer) {

    return customerService.createCustomer(customer);
  }

  @GetMapping(GET_CUSTOMER_BY_ID)
  public Customer getCustomerById(@PathVariable Long customerId) {

    return customerService.getCustomerById(customerId);
  }

  @PutMapping(UPDATE_CUSTOMER)
  public ResponseEntity<Customer> updateCustomer(
      @PathVariable Long customerId, @RequestBody Customer customer) {

    Customer updated = customerService.updateCustomer(customerId, customer);
    if (updated != null) {
      return ResponseEntity.ok(updated);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping(GET_ALL_CUSTOMERS)
  public List<Customer> getAllCustomers() {

    return customerService.getAllCustomers();
  }

  @DeleteMapping(DELETE_CUSTOMER)
  public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {

    customerService.deleteCustomer(customerId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(LINK_CUSTOMER_DELIVERY_ADDRESSES)
  public Customer linkDeliveryAddressToCustomer(
      @PathVariable Long customerId, @RequestBody Address address) {

    return customerService.linkDeliveryAddressToCustomer(customerId, address);
  }

  @PutMapping(UPDATE_CUSTOMER_ADDRESS)
  public ResponseEntity<Customer> updateCustomerDeliveryAddress(
      @PathVariable Long customerId,
      @PathVariable Long addressId,
      @RequestBody Address updatedAddress) {

    Customer updatedCustomerAddress =
        customerService.updateDeliveryAddressToCustomer(customerId, addressId, updatedAddress);
    if (updatedAddress != null) {
      return ResponseEntity.ok(updatedCustomerAddress);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping(DELETE_SELECTED_CUSTOMER_ADDRESS)
  public ResponseEntity<Void> deleteCustomerAddress(
      @PathVariable Long customerId, @PathVariable Long addressId, @RequestBody Address address) {

    customerService.deleteDeliveryAddressFromCustomer(customerId, addressId, address);
    return ResponseEntity.noContent().build();
  }
}
