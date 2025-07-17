package com.project.anesu.ecommerce.ordermanagementservice.controller.customer;

public class CustomerServiceRestEndpoints {

  public static final String LANDING_PAGE = "api/customers";

  public static final String CREATE_CUSTOMER = "/customer";
  public static final String UPDATE_CUSTOMER = "/customers/{customerId}";
  public static final String GET_CUSTOMER_BY_ID = "/customer/{customerId}";
  public static final String DELETE_CUSTOMER = "/customer/{customerId}";
  public static final String GET_ALL_CUSTOMERS = "/customers";

  public static final String LINK_CUSTOMER_DELIVERY_ADDRESSES = "/{customerId}/addresses";
  public static final String UPDATE_CUSTOMER_ADDRESS =
      "/customer/{customerId}/addresses/{addressId}";
  public static final String DELETE_SELECTED_CUSTOMER_ADDRESS =
      "/customer/{customerId}/addresses/{addressId}";

  private CustomerServiceRestEndpoints() {}
}
