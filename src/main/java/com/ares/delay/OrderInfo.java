package com.ares.delay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfo {

  private Long orderId;
  private Long userId;
  private Long skuId;
  private String skuName;
}
