package com.ares.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonExample {

  private static final Logger logger = LoggerFactory.getLogger(JsonExample.class);

  public static void main(String[] args) throws JsonProcessingException {
    User user = new User(1000L, "ares01", LocalDateTime.now());
    defaultFactory(user);
  }

  private static void defaultFactory(User user) throws JsonProcessingException {
    ObjectMapper mapper = DefaultObjectMapperFactory.createObjectMapperWithSerializerFactory();
    logger.info(mapper.writeValueAsString(user));
  }

  public record User(Long userId, String userName, LocalDateTime createTime) {

  }
}
