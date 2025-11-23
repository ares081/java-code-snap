package com.ares.delay;

import java.io.Serial;
import java.io.Serializable;


public record DelayTask<T>(String bizType, T payload, long delayTime, long expireTime) implements
    Serializable {

  @Serial
  private static final long serialVersionUID = -8097910962846184246L;

}
