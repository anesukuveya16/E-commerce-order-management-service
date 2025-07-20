package com.project.anesu.ecommerce.ordermanagementservice.integrationTests;

import static com.project.anesu.ecommerce.ordermanagementservice.controller.order.OrderServiceRestEndpoints.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.project.anesu.ecommerce.ordermanagementservice.entity.customer.Customer;
import com.project.anesu.ecommerce.ordermanagementservice.model.repository.CustomerRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceTest {

  @LocalServerPort int port;

  @Autowired private CustomerRepository customerRepository;

  @Autowired RestTemplate restTemplate;

  private Long customerId;

  @TestConfiguration
  static class mockRestTemplateTestConfig {

    @Bean
    public RestTemplate restTemplate() {
      return mock(RestTemplate.class);
    }
  }

  @BeforeEach
  void setUp() {
    RestAssured.port = port;

    reset(restTemplate);
    Customer customer = new Customer();
    customer.setFirstName("Marge");
    customer.setLastName("Smith");
    customer.setEmail("marge.smith@gmail.com");
    customer.setPhoneNumber("+49 040 567 56");
    customer.setBirthDate(LocalDate.of(1978, 8, 9));

    Customer savedCustomer = customerRepository.save(customer);
    customerId = savedCustomer.getId();
  }

  @Test
  void shouldSuccessfullyCreateOrderAndLinkAddressWhenInventoryValidationHasPassed() {

    successfulInventoryValidationFromProductService();
    String createOrderRequestBody = getOrderRequestBodyBeforeAddressConfirmation();

    Long orderId =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderRequestBody)
            .when()
            .post(LANDING_PAGE + CREATE_ORDER)
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("orderStatus", equalTo("ORDER_PLACED"))
            .body("orderItem", hasSize(2))
            .body("orderItem[0].productId", equalTo(101))
            .body("orderItem[0].quantity", equalTo(2))
            .body("orderItem[1].productId", equalTo(102))
            .body("orderItem[1].quantity", equalTo(1))
            .extract()
            .jsonPath()
            .getLong("id");

    String linkAddressRequestBody =
        """
                                {

                                "streetName": "Haut str",
                                "streetNumber": "10",
                                "city": "Berlin",
                                "state": "Berlin",
                                "zipcode": "10115"

                               }
                         """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(linkAddressRequestBody)
        .when()
        .post(LANDING_PAGE + ADD_DELIVERY_ADDRESS, orderId)
        .then()
        .statusCode(200)
        .body("orderItem", hasSize(2))
        .body("orderItem[0].productId", equalTo(101))
        .body("orderItem[0].quantity", equalTo(2))
        .body("orderItem[1].productId", equalTo(102))
        .body("orderItem[1].quantity", equalTo(1))
        .body("deliveryAddress", hasSize(1))
        .body("deliveryAddress[0].streetName", equalTo("Haut str"));
  }

  @Test
  void createOrder_WhenValidationFails_OrderCannotBeCreatedSuccessfully() {

    when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenReturn(new ResponseEntity<>("Order validation failed", HttpStatus.BAD_REQUEST));

    String createOrderRequestBody = getOrderRequestBodyBeforeAddressConfirmation();

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(createOrderRequestBody)
        .when()
        .post(LANDING_PAGE + CREATE_ORDER)
        .then()
        .statusCode(400)
        .body("message", containsString("Order validation failed"));
  }

  @Test
  void shouldRetrieveAllOrdersFromTheDatabase() {
    successfulInventoryValidationFromProductService();
    String createOrderRequestBody = getOrderRequestBodyBeforeAddressConfirmation();

    Long orderIdOne =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderRequestBody)
            .when()
            .post(LANDING_PAGE + CREATE_ORDER)
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("orderStatus", equalTo("ORDER_PLACED"))
            .body("orderItem", hasSize(2))
            .body("orderItem[0].productId", equalTo(101))
            .body("orderItem[0].quantity", equalTo(2))
            .body("orderItem[1].productId", equalTo(102))
            .body("orderItem[1].quantity", equalTo(1))
            .extract()
            .jsonPath()
            .getLong("id");

    String linkAddressRequestBodyOne =
        """
                {
                  "streetName": "Haut str",
                  "streetNumber": "10",
                  "city": "Berlin",
                  "state": "Berlin",
                  "zipcode": "10115"
                }
                """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(linkAddressRequestBodyOne)
        .when()
        .post(LANDING_PAGE + ADD_DELIVERY_ADDRESS, orderIdOne)
        .then()
        .statusCode(200)
        .body("orderItem", hasSize(2))
        .body("deliveryAddress[0].streetName", equalTo("Haut str"));

    String createOrderRequestBody2 =
        """
                {
                  "customerId": 1,
                  "orderItem": [
                    { "productId": 101, "quantity": 2 },
                    { "productId": 102, "quantity": 1 }
                  ],
                  "deliveryAddress": []
                }
                """;

    Long orderIdTwo =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderRequestBody2)
            .when()
            .post(LANDING_PAGE + CREATE_ORDER)
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("orderStatus", equalTo("ORDER_PLACED"))
            .body("orderItem", hasSize(2))
            .extract()
            .jsonPath()
            .getLong("id");

    String linkAddressRequestBodyTwo =
        """
                {
                  "streetName": "Victor street",
                  "streetNumber": "20",
                  "city": "London",
                  "state": "London",
                  "zipcode": "2115"
                }
                """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(linkAddressRequestBodyTwo)
        .when()
        .post(LANDING_PAGE + ADD_DELIVERY_ADDRESS, orderIdTwo)
        .then()
        .statusCode(200)
        .body("orderItem", hasSize(2))
        .body("deliveryAddress[0].streetName", equalTo("Victor street"));

    RestAssured.given()
        .contentType(ContentType.JSON)
        .when()
        .get(LANDING_PAGE + GET_ALL_ORDERS)
        .then()
        .statusCode(200);
  }

  @Test
  void processPendingOrderToDelivered_TestFullOrderLifecycle() {

    successfulInventoryValidationFromProductService();
    String createOrderRequestBody = getOrderRequestBodyBeforeAddressConfirmation();

    Long orderId =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderRequestBody)
            .when()
            .post(LANDING_PAGE + CREATE_ORDER)
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("orderStatus", equalTo("ORDER_PLACED"))
            .body("orderItem", hasSize(2))
            .body("orderItem[0].productId", equalTo(101))
            .body("orderItem[0].quantity", equalTo(2))
            .body("orderItem[1].productId", equalTo(102))
            .body("orderItem[1].quantity", equalTo(1))
            .extract()
            .jsonPath()
            .getLong("id");

    String linkAddressRequestBody =
        """
                          {

                           "streetName": "Haut str",
                           "streetNumber": "10",
                           "city": "Berlin",
                           "state": "Berlin",
                           "zipcode": "10115"
                                   }
                      """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(linkAddressRequestBody)
        .when()
        .post(LANDING_PAGE + ADD_DELIVERY_ADDRESS, orderId)
        .then()
        .statusCode(200)
        .body("orderItem", hasSize(2))
        .body("orderItem[0].productId", equalTo(101))
        .body("orderItem[0].quantity", equalTo(2))
        .body("orderItem[1].productId", equalTo(102))
        .body("orderItem[1].quantity", equalTo(1))
        .body("deliveryAddress", hasSize(1))
        .body("deliveryAddress[0].streetName", equalTo("Haut str"));

    String processOrderRequestBody =
        String.format(
            """
                            {
                              "customerId": %d,
                              "totalPrice": 75.50,
                              "orderStatus": "PROCESSING",
                              "orderDate": "2025-06-25T10:30:00.000",
                              "cancellationReason": null,
                              "orderItem": [
                                    {
                                      "productId": 101,
                                      "quantity": 2
                                    }
                              ]
                            }
                            """,
            customerId);

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(processOrderRequestBody)
        .when()
        .put(LANDING_PAGE + PROCESS_ORDER, orderId)
        .then()
        .statusCode(200)
        .body("orderItem", hasSize(2))
        .body("orderStatus", equalTo("PROCESSING"));

    String sendOutOrderRequestBody =
        String.format(
            """
                          {
                           "customerId": %d,
                           "totalPrice": 75.50,
                           "orderStatus": "OUT_FOR_DELIVERY",
                           "orderDate": "2025-06-25T10:30:00.000",
                           "cancellationReason": null,
                           "orderItem": [
                                 {
                                  "productId": 101,
                                  "quantity": 2
                                     }
                                ]
                            }
                    """,
            customerId);

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(sendOutOrderRequestBody)
        .when()
        .put(LANDING_PAGE + SEND_ORDER_FOR_DELIVERY, orderId)
        .then()
        .log()
        .all()
        .statusCode(200)
        .body("orderItem", hasSize(2))
        .body("orderStatus", equalTo("OUT_FOR_DELIVERY"));

    String deliveredRequestBody =
        String.format(
            """
                                  {
                                   "customerId": %d,
                                   "totalPrice": 75.50,
                                   "orderStatus": "DELIVERED",
                                   "orderDate": "2025-06-25T10:30:00.000",
                                   "cancellationReason": null,
                                   "orderItem": [
                                         {
                                          "productId": 101,
                                          "quantity": 2
                                             }
                                        ]
                                    }
                            """,
            customerId);

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(deliveredRequestBody)
        .when()
        .put(LANDING_PAGE + MARK_AS_DELIVERED, orderId)
        .then()
        .log()
        .all()
        .statusCode(200)
        .body("orderItem", hasSize(2))
        .body("orderStatus", equalTo("DELIVERED"));
  }

  @Test
  void shouldRetrieveOrderByGivenOrderId() {

    successfulInventoryValidationFromProductService();
    String createOrderRequestBody = getOrderRequestBodyBeforeAddressConfirmation();

    Long orderId =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderRequestBody)
            .when()
            .post(LANDING_PAGE + CREATE_ORDER)
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("orderStatus", equalTo("ORDER_PLACED"))
            .body("orderItem", hasSize(2))
            .body("orderItem[0].productId", equalTo(101))
            .body("orderItem[0].quantity", equalTo(2))
            .body("orderItem[1].productId", equalTo(102))
            .body("orderItem[1].quantity", equalTo(1))
            .extract()
            .jsonPath()
            .getLong("id");

    String linkAddressRequestBody =
        """
                                    {

                                    "streetName": "Haut str",
                                    "streetNumber": "10",
                                    "city": "Berlin",
                                    "state": "Berlin",
                                    "zipcode": "10115"

                                   }
                             """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(linkAddressRequestBody)
        .when()
        .post(LANDING_PAGE + ADD_DELIVERY_ADDRESS, orderId)
        .then()
        .statusCode(200)
        .body("orderItem", hasSize(2))
        .body("orderItem[0].productId", equalTo(101))
        .body("orderItem[0].quantity", equalTo(2))
        .body("orderItem[1].productId", equalTo(102))
        .body("orderItem[1].quantity", equalTo(1))
        .body("deliveryAddress", hasSize(1))
        .body("deliveryAddress[0].streetName", equalTo("Haut str"));

    RestAssured.given()
        .contentType(ContentType.JSON)
        .when()
        .get(LANDING_PAGE + GET_ORDER_BY_ID, orderId)
        .then()
        .statusCode(200)
        .body("orderItem", hasSize(2))
        .body("orderItem[0].productId", equalTo(101))
        .body("orderItem[0].quantity", equalTo(2))
        .body("orderItem[1].productId", equalTo(102))
        .body("orderItem[1].quantity", equalTo(1))
        .body("deliveryAddress", hasSize(1))
        .body("deliveryAddress[0].streetName", equalTo("Haut str"));
  }

  @Test
  void shouldAllowCustomerUpdateDeliveryAddress() {

    successfulInventoryValidationFromProductService();
    String createOrderRequestBody = getOrderRequestBodyBeforeAddressConfirmation();

    Long orderId =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderRequestBody)
            .when()
            .post(LANDING_PAGE + CREATE_ORDER)
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("orderStatus", equalTo("ORDER_PLACED"))
            .body("orderItem", hasSize(2))
            .body("orderItem[0].productId", equalTo(101))
            .body("orderItem[0].quantity", equalTo(2))
            .body("orderItem[1].productId", equalTo(102))
            .body("orderItem[1].quantity", equalTo(1))
            .extract()
            .jsonPath()
            .getLong("id");

    String linkAddressRequestBody =
        """
                                    {

                                    "streetName": "Haut str",
                                    "streetNumber": "10",
                                    "city": "Berlin",
                                    "state": "Berlin",
                                    "zipCode": "10115"

                                   }
                             """;

    String responseBodyAfterAddressLink =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(linkAddressRequestBody)
            .when()
            .post(LANDING_PAGE + ADD_DELIVERY_ADDRESS, orderId)
            .then()
            .statusCode(200)
            .body("orderItem", hasSize(2))
            .body("orderItem[0].productId", equalTo(101))
            .body("orderItem[0].quantity", equalTo(2))
            .body("orderItem[1].productId", equalTo(102))
            .body("orderItem[1].quantity", equalTo(1))
            .body("deliveryAddress", hasSize(1))
            .body("deliveryAddress[0].streetName", equalTo("Haut str"))
            .extract()
            .asString();

    JsonPath jsonPath = new JsonPath(responseBodyAfterAddressLink);
    Long addressId = jsonPath.getLong("deliveryAddress[0].id");

    String updateAddressRequestBody =
        String.format(
            """
                             {
                               "id": %d,
                               "streetName": "Summer str",
                               "streetNumber": "15",
                               "city": "Berlin",
                               "state": "Berlin",
                               "zipCode": "11015"
                             }
                        """,
            addressId);

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(updateAddressRequestBody)
        .when()
        .put(LANDING_PAGE + UPDATE_DELIVERY_ADDRESS, orderId, addressId)
        .then()
        .log()
        .all()
        .statusCode(200)
        .body("orderItem", hasSize(2))
        .body("orderItem[0].productId", equalTo(101))
        .body("orderItem[0].quantity", equalTo(2))
        .body("orderItem[1].productId", equalTo(102))
        .body("orderItem[1].quantity", equalTo(1))
        .body("deliveryAddress", hasSize(1))
        .body("deliveryAddress[0].streetName", equalTo("Summer str"))
        .body("deliveryAddress[0].streetNumber", equalTo("15"))
        .body("deliveryAddress[0].zipCode", equalTo("11015"));
  }

  @Test
  void shouldCancelOrder_AndReturnDeductedInventoryBackTo_ProductServiceSuccessfully() {

    successfulInventoryValidationFromProductService();
    String createOrderRequestBody = getOrderRequestBodyBeforeAddressConfirmation();

    Long orderId =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderRequestBody)
            .when()
            .post(LANDING_PAGE + CREATE_ORDER)
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("orderStatus", equalTo("ORDER_PLACED"))
            .body("orderItem", hasSize(2))
            .body("orderItem[0].productId", equalTo(101))
            .body("orderItem[0].quantity", equalTo(2))
            .body("orderItem[1].productId", equalTo(102))
            .body("orderItem[1].quantity", equalTo(1))
            .extract()
            .jsonPath()
            .getLong("id");

    String linkAddressRequestBody =
        """
                                    {

                                    "streetName": "Haut str",
                                    "streetNumber": "10",
                                    "city": "Berlin",
                                    "state": "Berlin",
                                    "zipcode": "10115"

                                   }
                             """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(linkAddressRequestBody)
        .when()
        .post(LANDING_PAGE + ADD_DELIVERY_ADDRESS, orderId)
        .then()
        .statusCode(200)
        .body("orderItem", hasSize(2))
        .body("orderItem[0].productId", equalTo(101))
        .body("orderItem[0].quantity", equalTo(2))
        .body("orderItem[1].productId", equalTo(102))
        .body("orderItem[1].quantity", equalTo(1))
        .body("deliveryAddress", hasSize(1))
        .body("deliveryAddress[0].streetName", equalTo("Haut str"))
        .extract()
        .jsonPath()
        .getLong("id");

    String cancelOrderBodyRequest =
        """
                       {
                         "cancellationReason": "Valid cancellation reason.",
                         "orderStatus": "CANCELLED"
                       }
                       """;

    successInventoryReturnToDatabase();

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(cancelOrderBodyRequest)
        .when()
        .put(LANDING_PAGE + CANCEL_ORDER, orderId)
        .then()
        .statusCode(200);
  }

  @Test
  void
      shouldCancelOrder_ShouldThrowExceptionWhenAnErrorOccurs_WhileReturningInventoryBackTo_ProductDatabase() {

    successfulInventoryValidationFromProductService();
    String createOrderRequestBody = getOrderRequestBodyBeforeAddressConfirmation();

    Long orderId =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderRequestBody)
            .when()
            .post(LANDING_PAGE + CREATE_ORDER)
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("orderStatus", equalTo("ORDER_PLACED"))
            .body("orderItem", hasSize(2))
            .body("orderItem[0].productId", equalTo(101))
            .body("orderItem[0].quantity", equalTo(2))
            .body("orderItem[1].productId", equalTo(102))
            .body("orderItem[1].quantity", equalTo(1))
            .extract()
            .jsonPath()
            .getLong("id");

    String linkAddressRequestBody =
        """
        {
          "streetName": "Haut str",
          "streetNumber": "10",
          "city": "Berlin",
          "state": "Berlin",
          "zipcode": "10115"
        }
        """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(linkAddressRequestBody)
        .when()
        .post(LANDING_PAGE + ADD_DELIVERY_ADDRESS, orderId)
        .then()
        .statusCode(200)
        .body("orderItem", hasSize(2))
        .body("orderItem[0].productId", equalTo(101))
        .body("orderItem[0].quantity", equalTo(2))
        .body("orderItem[1].productId", equalTo(102))
        .body("orderItem[1].quantity", equalTo(1))
        .body("deliveryAddress", hasSize(1))
        .body("deliveryAddress[0].streetName", equalTo("Haut str"))
        .extract()
        .jsonPath()
        .getLong("id");

    String cancelOrderBodyRequest =
        """
        {
          "cancellationReason": "Valid cancellation reason.",
          "orderStatus": "CANCELLED"
        }
        """;

    when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenReturn(
            new ResponseEntity<>("Failed to connect to Product Service", HttpStatus.BAD_REQUEST));

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(cancelOrderBodyRequest)
        .when()
        .put(LANDING_PAGE + CANCEL_ORDER, orderId)
        .then()
        .log()
        .all()
        .statusCode(400);
  }

  private String getOrderRequestBodyBeforeAddressConfirmation() {
    return String.format(
        """
                                {
                                  "customerId": %d,
                                  "orderItem": [
                                    {
                                      "productId": 101,
                                      "quantity": 2
                                    },
                                    {
                                      "productId": 102,
                                      "quantity": 1
                                    }
                                  ],
                                   "deliveryAddress": []
                                }
                                """,
        customerId);
  }

  private void successfulInventoryValidationFromProductService() {
    when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenReturn(new ResponseEntity<>("Validation successful!", HttpStatus.OK));
  }

  private void successInventoryReturnToDatabase() {
    when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenReturn(new ResponseEntity<>("Inventory return successful!", HttpStatus.OK));
  }
}
