# E-commerce: Order management service

This microservice is part of an **E-commerce platform** that focuses operations such as managing customer inforamtion and handles order related operations. 
It is also a very important critical the Inventory Management Service, to verify and update stock in real-time during operations such as order creation or order cancellation.

##  Key Features:

- Order validation
  
- Product validation
  
- Fetching orders or customers from  DB

- Handles delivery address management for orders

- Implements robust order status transitions.

- handles the full lifecycle of customer orders (from order creation, processing, delivery, cancellation).

- Customer Management: Provides functionality to create, retrieve, and update customer details.

- Integrates with the Inventory Service for inventory validation, deduction and return.

- Exception Handling: Provides specific error messages and HTTP status codes during Rest calls to Inventory Service


## Tech Stack

- Java (SapMachine 21)  
- Spring Boot
- Spring Data JPA
- H2 Database (in-memory)  
- Maven for build management
- Lombok  
- JUnit 5 and Mockito for testing  
- Rest Assured for integration testing
- RESTful API Communication (to Order management service) 


##  Unit tests cover:

Validation rules

Test accurate exception handling

Test core business logic

Successful and failing approval flows of service logic eg. order creation/failure

##  Integration tests cover:

REST Endpoints functionality and responses

Error handling for REST calls

##  Validation rules


##  REST Endpints

Customer
| Method | Endpoint                                        | Description             |
| --------| -------------------------------------------    | ----------------------- |
| `POST`  | `/customer`                                    | Create customer         |
| `PUT`   | `/customers/{customerId}`                      | Update customer  
| `GET`   | `/customers/{customerId}`                      | Retrive specific customer through given Id  |
| `GET`| `/customer`                                       | Retrieve all customers in DB        |
| `DELETE`   | `/customers/{customerId}`                   | Delete customer
| `POST`   | `/{customerId}/addresses`                     | Link customer to desired delivery address |
| `PUT`| `/customer/{customerId}/addresses/{addressId}`    | Update customer address      |
| `DELETE`| `/customer/{customerId}/addresses/{addressId}` | Delete selected customer address     |


Order 
| Method | Endpoint                                                    | Description             |
| ------ | ----------------------------------------------------------- | ----------------------- |
| `POST` | `/create-order`                                             | Create order     |
| `POST`  | `/{orderId}/add-delivery-address`                          | Add delivery address to order |
| `GET`  | `/orders`                                                   | Get all orders   |
| `GET`  | `/{orderId}`                                                | Retreieve specific product through given Id   |
| `PUT`  | `/{orderId}/process`                                        | Process order  |
| `PUT`  | `/{orderId}/deliver`                                        | Send order out fror delivery   |
| `PUT`  | `/{orderId}/delivered`                                      | Mark order as delivered  |
| `PUT`  | `/{orderId}/cancel`                                         | Cancel order  |
| `PUT`  | `/{orderId}/address/{addressId}`                            | Update delivery address  |


## How to run locally:
```bash
Clone project:

git clone https://github.com/anesukuveya16/E-commerce-inventory-management-service
cd manager-microservice bash

Build project:

./mvn clean install 

Run the application:

./mvn spring-boot:run

