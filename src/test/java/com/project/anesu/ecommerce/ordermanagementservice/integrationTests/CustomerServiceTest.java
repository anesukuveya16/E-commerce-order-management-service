package com.project.anesu.ecommerce.ordermanagementservice.integrationTests;

import static com.project.anesu.ecommerce.ordermanagementservice.controller.customer.CustomerServiceRestEndpoints.*;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerServiceTest {

  @LocalServerPort private int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @Test
  void shouldSuccessfullyCreateAndSaveNewCustomerToDatabase() {

    String newCustomerRequestBody =
        """
                            {
                              "firstName": "Lisa",
                              "lastName": "Simpson",
                              "email": "simps.lisa@gmail.com",
                              "phoneNumber": "123-456-7890",
                              "birthDate": "1990-07-23",
                              "addresses": []
                             }
                        """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(newCustomerRequestBody)
        .when()
        .post(LANDING_PAGE + CREATE_CUSTOMER)
        .then()
        .statusCode(200)
        .body("firstName", equalTo("Lisa"))
        .body("lastName", equalTo("Simpson"));
  }

  @Test
  void shouldRetrieveCustomerFromDatabaseByGivenCustomerId() {

    String newCustomerRequestBody =
        """
                            {
                              "firstName": "Lisa",
                              "lastName": "Simpson",
                              "email": "simps.lisa@gmail.com",
                              "phoneNumber": "123-456-7890",
                              "birthDate": "1990-07-23",
                              "addresses": []
                             }
                        """;

    Long customerId =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(newCustomerRequestBody)
            .when()
            .post(LANDING_PAGE + CREATE_CUSTOMER)
            .then()
            .statusCode(200)
            .body("firstName", equalTo("Lisa"))
            .body("lastName", equalTo("Simpson"))
            .extract()
            .jsonPath()
            .getLong("id");

    RestAssured.given()
        .contentType(ContentType.JSON)
        .when()
        .get(LANDING_PAGE + GET_CUSTOMER_BY_ID, customerId)
        .then()
        .statusCode(200);
  }

  @Test
  void shouldUpdateCustomerFromDatabaseByGivenCustomerId() {

    String newCustomerRequestBody =
        """
                                {
                                  "firstName": "Lisa",
                                  "lastName": "Simpson",
                                  "email": "simps.lisa@gmail.com",
                                  "phoneNumber": "122-555-7890",
                                  "birthDate": "1980-07-26",
                                  "addresses": []
                                 }
                            """;

    Long customerId =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(newCustomerRequestBody)
            .when()
            .post(LANDING_PAGE + CREATE_CUSTOMER)
            .then()
            .statusCode(200)
            .body("firstName", equalTo("Lisa"))
            .body("lastName", equalTo("Simpson"))
            .extract()
            .jsonPath()
            .getLong("id");

    String updateCustomerRequestBody =
        """
                                {
                                  "firstName": "Lisa",
                                  "lastName": "Simpson",
                                  "email": "simps.lisa@gmail.com",
                                  "phoneNumber": "122-577-7878",
                                  "birthDate": "1980-07-26",
                                  "addresses": []
                                 }
                            """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(updateCustomerRequestBody)
        .when()
        .put(LANDING_PAGE + UPDATE_CUSTOMER, customerId)
        .then()
        .statusCode(200)
        .body("firstName", equalTo("Lisa"))
        .body("lastName", equalTo("Simpson"))
        .body("phoneNumber", equalTo("122-577-7878"));
  }

  @Test
  void shouldSuccessfullyRetrieveAllCustomersFromDatabase() {

    String newCustomerRequestBodyOne =
        """
                            {
                              "firstName": "Lisa",
                              "lastName": "Simpson",
                              "email": "simps.lisa@gmail.com",
                              "phoneNumber": "123-456-7890",
                              "birthDate": "1990-07-23",
                              "addresses": []
                             }
                        """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(newCustomerRequestBodyOne)
        .when()
        .post(LANDING_PAGE + CREATE_CUSTOMER)
        .then()
        .statusCode(200)
        .body("firstName", equalTo("Lisa"))
        .body("lastName", equalTo("Simpson"));

    String newCustomerRequestBodyTwo =
        """
                            {
                              "firstName": "Marge",
                              "lastName": "Simpson",
                              "email": "simps.marge@gmail.com",
                              "phoneNumber": "123-456-7890",
                              "birthDate": "1990-07-23",
                              "addresses": []
                             }
                        """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(newCustomerRequestBodyTwo)
        .when()
        .post(LANDING_PAGE + CREATE_CUSTOMER)
        .then()
        .statusCode(200)
        .body("firstName", equalTo("Marge"))
        .body("lastName", equalTo("Simpson"));

    RestAssured.given()
        .contentType(ContentType.JSON)
        .when()
        .get(LANDING_PAGE + GET_ALL_CUSTOMERS)
        .then()
        .statusCode(200);
  }

  @Test
  void shouldSuccessfullyDeleteCustomerFromDatabase() {

    String newCustomerRequestBody =
        """
                                {
                                  "firstName": "Lisa",
                                  "lastName": "Simpson",
                                  "email": "simps.lisa@gmail.com",
                                  "phoneNumber": "122-555-7890",
                                  "birthDate": "1980-07-26",
                                  "addresses": []
                                 }
                            """;

    Long customerId =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(newCustomerRequestBody)
            .when()
            .post(LANDING_PAGE + CREATE_CUSTOMER)
            .then()
            .statusCode(200)
            .body("firstName", equalTo("Lisa"))
            .body("lastName", equalTo("Simpson"))
            .extract()
            .jsonPath()
            .getLong("id");

    RestAssured.given()
        .contentType(ContentType.JSON)
        .when()
        .delete(LANDING_PAGE + DELETE_CUSTOMER, customerId)
        .then()
        .statusCode(204);
  }
}
