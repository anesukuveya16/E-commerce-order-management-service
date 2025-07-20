package com.project.anesu.ecommerce.ordermanagementservice.entity.order;

import lombok.Getter;

@Getter
public enum OrderValidationEndpoints {
  VALIDATE_AND_DEDUCT_PRODUCT("http://localhost:9091/api/stock/validate-and-deduct-product"),

  ADD_RETURNED_INVENTORY("http://localhost:9091/api/stock/add-returned-inventory");

  private final String url;

  OrderValidationEndpoints(String url) {
    this.url = url;
  }
}
