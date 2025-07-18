package com.project.anesu.ecommerce.ordermanagementservice.unitTests.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.project.anesu.ecommerce.ordermanagementservice.entity.customer.Customer;
import com.project.anesu.ecommerce.ordermanagementservice.service.exception.InvalidCustomerException;
import com.project.anesu.ecommerce.ordermanagementservice.service.util.CustomerValidator;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerValidatorTest {

  private CustomerValidator cut;
  private Customer customer;

  @BeforeEach
  void setUp() {
    customer = new Customer();
    customer.setFirstName("Lisa");
    customer.setLastName("Simpson");
    customer.setEmail("simpsonLi455@gmail.com");
    customer.setPhoneNumber("+49 55 55 55");
    customer.setBirthDate((LocalDate.of(2000, 4, 15)));
    customer.setSavedAddresses(List.of());
    cut = new CustomerValidator();
  }

  @Test
  void shouldThrowExceptionWhen_FirstNameIsNull() {

    // Given
    customer.setId(2L);
    customer.setFirstName(null);
    customer.setLastName("Simpson");

    // When
    InvalidCustomerException exception =
        assertThrows(InvalidCustomerException.class, () -> cut.validate(customer));

    // Then
    assertThat(exception.getMessage()).isEqualTo("First or last name is null!");
  }

  @Test
  void shouldThrowExceptionWhen_LastNameIsNull() {

    // Given
    customer.setId(2L);
    customer.setFirstName("Lisa");
    customer.setLastName(null);

    // When
    InvalidCustomerException exception =
        assertThrows(InvalidCustomerException.class, () -> cut.validate(customer));

    // Then
    assertThat(exception.getMessage()).isEqualTo("First or last name is null!");
  }

  @Test
  void shouldThrowExceptionWhen_EmailIsNull() {

    // Given
    customer.setId(3L);
    customer.setFirstName("Lisa");
    customer.setLastName("Simpson");
    customer.setEmail(null);

    // When
    InvalidCustomerException exception =
        assertThrows(InvalidCustomerException.class, () -> cut.validate(customer));

    // Then
    assertThat(exception.getMessage()).isEqualTo("Valid email is required!");
  }

  @Test
  void shouldThrowExceptionWhen_AddressFieldIsEmpty() {

    // Given
    customer.setId(2L);
    customer.setFirstName("Lisa");
    customer.setLastName("Simpson");
    customer.setSavedAddresses(null);

    // When
    InvalidCustomerException exception =
        assertThrows(InvalidCustomerException.class, () -> cut.validate(customer));

    // Then
    assertThat(exception.getMessage()).isEqualTo("Please provide valid address.");
  }

  @Test
  void shouldThrowExceptionWhen_PhoneNumberIsEmpty() {

    // Given
    customer.setId(2L);
    customer.setFirstName("Lisa");
    customer.setLastName("Simpson");
    customer.setPhoneNumber(null);

    // When

    InvalidCustomerException exception =
        assertThrows(InvalidCustomerException.class, () -> cut.validate(customer));

    // Then
    assertThat(exception.getMessage()).isEqualTo("Please provide valid phone number.");
  }
}
