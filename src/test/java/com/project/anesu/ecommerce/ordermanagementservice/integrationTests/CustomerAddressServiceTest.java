package com.project.anesu.ecommerce.ordermanagementservice.integrationTests;

import static com.project.anesu.ecommerce.ordermanagementservice.controller.customer.CustomerServiceRestEndpoints.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.project.anesu.ecommerce.ordermanagementservice.entity.customer.Customer;
import com.project.anesu.ecommerce.ordermanagementservice.model.repository.CustomerRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerAddressServiceTest {

  @Autowired CustomerRepository customerRepository;

  private Long customerId;

  @LocalServerPort private int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;

    Customer customer = new Customer();
    customer.setFirstName("Bart");
    customer.setLastName("Simpson");
    customer.setEmail("sim.bart@gmail.com");
    customer.setPhoneNumber("+49(90)555-5555");
    customer.setBirthDate(LocalDate.of(1991, 11, 15));

    Customer savedCustomer = customerRepository.save(customer);
    customerId = savedCustomer.getId();
  }

  @Test
  void shouldLinkCustomerToGivenCustomerAddressSuccessfully() {

    String linkAddressRequestBody =
        """
                            {
                              "streetName": "Elm Street",
                              "streetNumber": "123",
                              "city": "Springfield",
                              "state": "IL",
                              "zipCode": "62704",
                              "orders": []
                           }
                     """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(linkAddressRequestBody)
        .when()
        .post(LANDING_PAGE + LINK_CUSTOMER_DELIVERY_ADDRESSES, customerId)
        .then()
        .statusCode(200)
        .body("savedAddresses", hasSize(1))
        .body("savedAddresses[0].streetName", equalTo("Elm Street"))
        .body("savedAddresses[0].streetNumber", equalTo("123"))
        .body("savedAddresses[0].city", equalTo("Springfield"));
  }

  @Test
  void updateCustomerAddressSuccessfully() {

    String linkAddressRequestBody =
        """
                                {
                                  "streetName": "Moon Street",
                                  "streetNumber": "3",
                                  "city": "Spring Ville",
                                  "state": "Toronto",
                                  "zipCode": "62704",
                                  "orders": []
                               }
                         """;

    Long addressId =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(linkAddressRequestBody)
            .when()
            .post(LANDING_PAGE + LINK_CUSTOMER_DELIVERY_ADDRESSES, customerId)
            .then()
            .statusCode(200)
            .body("savedAddresses", hasSize(1))
            .body("savedAddresses[0].streetName", equalTo("Moon Street"))
            .body("savedAddresses[0].streetNumber", equalTo("3"))
            .body("savedAddresses[0].city", equalTo("Spring Ville"))
            .extract()
            .jsonPath()
            .getLong("id");

    String updateAddressRequestBody =
        """
                                {
                                  "streetName": "Moon-way Street",
                                  "streetNumber": "33",
                                  "city": "Spring Valley",
                                  "state": "Toronto",
                                  "zipCode": "45704",
                                  "orders": []
                               }
                         """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(updateAddressRequestBody)
        .when()
        .put(LANDING_PAGE + UPDATE_CUSTOMER_ADDRESS, customerId, addressId)
        .then()
        .statusCode(200)
        .body("savedAddresses", hasSize(1))
        .body("savedAddresses[0].streetName", equalTo("Moon-way Street"))
        .body("savedAddresses[0].streetNumber", equalTo("33"))
        .body("savedAddresses[0].city", equalTo("Spring Valley"))
        .body("savedAddresses[0].state", equalTo("Toronto"))
        .body("savedAddresses[0].zipCode", equalTo("45704"));
  }

  @Test
  void deleteSelectedCustomerAddressFromDatabase() {

    String linkAddressRequestBody =
        """
                                {
                                  "streetName": "Elm Street",
                                  "streetNumber": "123",
                                  "city": "Springfield",
                                  "state": "IL",
                                  "zipCode": "62704",
                                  "orders": []
                               }
                         """;
    Long addressId =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(linkAddressRequestBody)
            .when()
            .post(LANDING_PAGE + LINK_CUSTOMER_DELIVERY_ADDRESSES, customerId)
            .then()
            .statusCode(200)
            .body("savedAddresses", hasSize(1))
            .body("savedAddresses[0].streetName", equalTo("Elm Street"))
            .body("savedAddresses[0].streetNumber", equalTo("123"))
            .body("savedAddresses[0].city", equalTo("Springfield"))
            .extract()
            .jsonPath()
            .getLong("id");

    String deleteAddressRequestBody =
        """
                                {
                                  "streetName": "Elm Street",
                                  "streetNumber": "123",
                                  "city": "Springfield",
                                  "state": "IL",
                                  "zipCode": "62704",
                                  "orders": []
                               }
                         """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(deleteAddressRequestBody)
        .when()
        .delete(LANDING_PAGE + DELETE_SELECTED_CUSTOMER_ADDRESS, customerId, addressId)
        .then()
        .statusCode(204);
  }
}
