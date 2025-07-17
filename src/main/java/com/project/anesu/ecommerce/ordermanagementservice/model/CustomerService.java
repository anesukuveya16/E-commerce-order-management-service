package com.project.anesu.ecommerce.ordermanagementservice.model;

import com.project.anesu.ecommerce.ordermanagementservice.entity.address.Address;
import com.project.anesu.ecommerce.ordermanagementservice.entity.customer.Customer;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.CustomerNotFoundException;
import java.util.List;

/**
 * Service interface for managing customer-related operations within the Order Management Service.
 *
 * <p>This interface defines the contract for creating, updating, retrieving, and deleting customer
 * records.
 */
public interface CustomerService {

  /**
   * Creates a new customer record.
   *
   * @param customer the customer entity to be created
   * @return the created {@link Customer} object
   */
  Customer createCustomer(Customer customer);

  /**
   * Updates an existing customer record by ID.
   *
   * @param customerId the ID of the customer to update
   * @param updatedCustomer the customer entity containing updated information
   * @return the updated {@link Customer} object
   * @throws CustomerNotFoundException if the customer with the given ID is not found
   */
  Customer updateCustomer(Long customerId, Customer updatedCustomer)
      throws CustomerNotFoundException;

  /**
   * Retrieves a customer by their ID.
   *
   * @param customerId the ID of the customer to retrieve
   * @return the {@link Customer} object with the given ID
   */
  Customer getCustomerById(Long customerId);

  /**
   * Retrieves a list of all customers.
   *
   * @return a list of all {@link Customer} objects
   */
  List<Customer> getAllCustomers();

  /**
   * Deletes a customer by their ID.
   *
   * @param customerId the ID of the customer to delete
   */
  void deleteCustomer(Long customerId);

  /**
   * Links a new delivery address to the specified customer.
   *
   * @param customerId the ID of the customer to link the address to
   * @param address the {@link Address} to be linked
   * @return the updated {@link Customer} with the new address linked
   * @throws CustomerNotFoundException if the customer with the given ID is not found
   */
  Customer linkDeliveryAddressToCustomer(Long customerId, Address address)
      throws CustomerNotFoundException;

  /**
   * Updates an existing delivery address associated with a customer.
   *
   * @param customerId the ID of the customer whose address is to be updated
   * @param addressId the ID of the address to update
   * @param updatedAddress the {@link Address} object containing updated information
   * @return the updated {@link Customer} with the modified address
   * @throws CustomerNotFoundException if the customer with the given ID is not found
   */
  Customer updateDeliveryAddressToCustomer(Long customerId, Long addressId, Address updatedAddress)
      throws CustomerNotFoundException;

  /**
   * Deletes a delivery address from a customer.
   *
   * @param customerId the ID of the customer from whom the address will be removed
   * @param addressId the ID of the address to be removed
   * @param address the {@link Address} object to confirm deletion // * @return the updated {@link
   *     Customer} after address removal
   * @throws CustomerNotFoundException if the customer with the given ID is not found
   */
  void deleteDeliveryAddressFromCustomer(Long customerId, Long addressId, Address address)
      throws CustomerNotFoundException;
}
