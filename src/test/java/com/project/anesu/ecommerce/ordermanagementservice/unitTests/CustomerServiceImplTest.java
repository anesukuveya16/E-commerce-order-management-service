package com.project.anesu.ecommerce.ordermanagementservice.unitTests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.project.anesu.ecommerce.ordermanagementservice.entity.customer.Customer;
import com.project.anesu.ecommerce.ordermanagementservice.model.repository.CustomerRepository;
import com.project.anesu.ecommerce.ordermanagementservice.service.CustomerServiceImpl;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.CustomerNotFoundException;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.InvalidCustomerException;
import com.project.anesu.ecommerce.ordermanagementservice.service.util.CustomerValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

  @Mock private CustomerRepository customerRepositoryMock;
  @Mock private CustomerValidator customerValidatorMock;

  private CustomerServiceImpl cut;

  @BeforeEach
  void setUp() {
    cut = new CustomerServiceImpl(customerRepositoryMock, customerValidatorMock);
  }

  @Test
  void shouldSuccessfullyCreateAndSAveCustomerIntoTheDatabase() {

    // Given
    Customer addNewCustomer = getNewCustomerDetails();

    doNothing().when(customerValidatorMock).validate(addNewCustomer);
    when(customerRepositoryMock.save(any())).thenReturn(addNewCustomer);

    // When
    Customer createdCustomer = cut.createCustomer(addNewCustomer);

    // Then
    assertNotNull(createdCustomer);

    verify(customerValidatorMock).validate(createdCustomer);
    verify(customerRepositoryMock, times(1)).save(addNewCustomer);
  }

  @Test
  void shouldNotAddNewCustomerIfValidationHasFailed() {

    // Given
    Customer addNewCustomer = getNewCustomerDetails();

    doThrow(InvalidCustomerException.class).when(customerValidatorMock).validate(addNewCustomer);

    // When
    assertThrows(InvalidCustomerException.class, () -> cut.createCustomer(addNewCustomer));

    // Then
    verifyNoMoreInteractions(customerRepositoryMock);
    verify(customerValidatorMock).validate(addNewCustomer);
  }

  @Test
  void shouldRetrieveAllCustomersWithinTheDatabase() {

    // Given
    when(customerRepositoryMock.findAll()).thenReturn(List.of());

    // When
    List<Customer> allCustomers = cut.getAllCustomers();

    // Then
    assertNotNull(allCustomers);

    verify(customerRepositoryMock, times(1)).findAll();
  }

  @Test
  void shouldSuccessfullyRetrieveCustomerByGivenId() {

    // Given
    Long customerId = 15L;
    Customer customer = new Customer();
    customer.setId(customerId);

    when(customerRepositoryMock.findById(customerId)).thenReturn(Optional.of(customer));

    // When
    cut.getCustomerById(customerId);

    // Then
    verify(customerRepositoryMock, times(1)).findById(customerId);
  }

  @Test
  void shouldThrowExceptionWhenCustomerNotFound_ByGivenCustomerId() {

    // Given
    Long customerId = 15L;
    when(customerRepositoryMock.findById(customerId)).thenReturn(Optional.empty());

    // When
    assertThrows(CustomerNotFoundException.class, () -> cut.getCustomerById(customerId));

    // Then
    verify(customerRepositoryMock, times(1)).findById(customerId);
    verifyNoMoreInteractions(customerRepositoryMock);
  }

  @Test
  void shouldDeleteCustomerByGivenCustomerId() {

    // Given
    Long customerId = 150L;
    Customer customer = new Customer();
    customer.setId(customerId);

    when(customerRepositoryMock.existsById(customerId)).thenReturn(true);

    // When
    cut.deleteCustomer(customerId);

    // Then
    verify(customerRepositoryMock, times(1)).deleteById(customerId);
    verifyNoMoreInteractions(customerRepositoryMock);
  }

  @Test
  void shouldThrowExceptionWhen_GivenCustomerIdIsNull() {

    // Given
    Long customerId = 150L;
    when(customerRepositoryMock.existsById(customerId)).thenReturn(false);

    // When
    assertThrows(CustomerNotFoundException.class, () -> cut.deleteCustomer(customerId));

    // Then
    verifyNoMoreInteractions(customerRepositoryMock);
  }

  private Customer getNewCustomerDetails() {
    Customer addNewCustomer = new Customer();
    addNewCustomer.setFirstName("Marge");
    addNewCustomer.setLastName("Simpson");
    addNewCustomer.setPhoneNumber("123456789");
    addNewCustomer.setEmail("margesimpson24@gmail.com");
    addNewCustomer.setSavedAddresses(List.of());
    addNewCustomer.setBirthDate(LocalDate.of(1989, 3, 12));
    return addNewCustomer;
  }
}
