package com.project.anesu.ecommerce.ordermanagementservice.service;

import com.project.anesu.ecommerce.ordermanagementservice.entity.address.Address;
import com.project.anesu.ecommerce.ordermanagementservice.entity.customer.Customer;
import com.project.anesu.ecommerce.ordermanagementservice.model.CustomerService;
import com.project.anesu.ecommerce.ordermanagementservice.model.repository.CustomerRepository;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.AddressNotFoundException;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.CustomerNotFoundException;
import com.project.anesu.ecommerce.ordermanagementservice.service.util.CustomerValidator;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository customerRepository;
  private final CustomerValidator customerValidator;

  @Override
  public Customer createCustomer(Customer customer) {

    customerValidator.validate(customer);
    return customerRepository.save(customer);
  }

  @Override
  public Customer updateCustomer(Long customerId, Customer updatedCustomer)
      throws CustomerNotFoundException {

    Customer existingCustomer = getCustomerById(customerId);
    Customer updatedExistingCustomer = updateExistingCustomer(updatedCustomer, existingCustomer);

    customerValidator.validate(updatedExistingCustomer);

    return customerRepository.save(updatedExistingCustomer);
  }

  @Override
  public Customer getCustomerById(Long customerId) throws CustomerNotFoundException {

    return customerRepository
        .findById(customerId)
        .orElseThrow(() -> new CustomerNotFoundException(customerId));
  }

  @Override
  public List<Customer> getAllCustomers() {
    return customerRepository.findAll();
  }

  @Override
  public void deleteCustomer(Long customerId) {
    if (!customerRepository.existsById(customerId)) {
      throw new CustomerNotFoundException(customerId);
    }
    customerRepository.deleteById(customerId);
  }

  @Override
  public Customer linkDeliveryAddressToCustomer(Long customerId, Address address)
      throws CustomerNotFoundException {

    Customer customerAddress = getCustomerById(customerId);

    address.setCustomer(customerAddress);
    customerAddress.getSavedAddresses().add(address);

    return customerRepository.save(customerAddress);
  }

  @Override
  public Customer updateDeliveryAddressToCustomer(
      Long customerId, Long addressId, Address updatedAddress) throws CustomerNotFoundException {

    Customer customer = getCustomerById(customerId);

    List<Address> savedAddresses = customer.getSavedAddresses();
    for (Address existingAddress : savedAddresses) {
      if (existingAddress.getId().equals(addressId)) {
        existingCustomerDeliveryAddress(updatedAddress, existingAddress);

        break;
      }
    }

    return customerRepository.save(customer);
  }

  @Override
  public void deleteDeliveryAddressFromCustomer(Long customerId, Long addressId, Address address)
      throws CustomerNotFoundException {

    Customer customer = getCustomerById(customerId);
    Address addressToDelete = null;

    for (Address existingAddressToDelete : customer.getSavedAddresses()) {
      if (existingAddressToDelete.getId().equals(addressId)) {
        addressToDelete = existingAddressToDelete;
        break;
      }
    }

    if (addressToDelete != null) {
      customer.getSavedAddresses().remove(addressToDelete);
    } else {
      throw new AddressNotFoundException("Address ID " + addressId + " not found!");
    }

    customerRepository.save(customer);
  }

  private Customer updateExistingCustomer(Customer updatedCustomer, Customer existingCustomer) {
    existingCustomer.setFirstName(updatedCustomer.getFirstName());
    existingCustomer.setLastName(updatedCustomer.getLastName());
    existingCustomer.setEmail(updatedCustomer.getEmail());
    existingCustomer.setBirthDate(updatedCustomer.getBirthDate());
    existingCustomer.setPhoneNumber(updatedCustomer.getPhoneNumber());
    return existingCustomer;
  }

  private void existingCustomerDeliveryAddress(Address updatedAddress, Address existingAddress) {
    existingAddress.setStreetName(updatedAddress.getStreetName());
    existingAddress.setStreetNumber(updatedAddress.getStreetNumber());
    existingAddress.setState(updatedAddress.getState());
    existingAddress.setZipCode(updatedAddress.getZipCode());
    existingAddress.setCity(updatedAddress.getCity());
  }
}
