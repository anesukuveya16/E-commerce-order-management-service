package com.project.anesu.ecommerce.ordermanagementservice.controller.order;

public class OrderServiceRestEndpoints {

  public static final String LANDING_PAGE = "/api/orders";

  public static final String CREATE_ORDER = "/create-order";
  public static final String ADD_DELIVERY_ADDRESS = "/{orderId}/add-delivery-address";
  public static final String GET_ALL_ORDERS = "/orders";
  public static final String GET_ORDER_BY_ID = "/{orderId}";
  public static final String PROCESS_ORDER = "/{orderId}/process";
  public static final String SEND_ORDER_FOR_DELIVERY = "/{orderId}/deliver";
  public static final String MARK_AS_DELIVERED = "/{orderId}/delivered";
  public static final String CANCEL_ORDER = "/{orderId}/cancel";
  public static final String UPDATE_DELIVERY_ADDRESS = "/{orderId}/address/{addressId}";

  private OrderServiceRestEndpoints() {}
}
