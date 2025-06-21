package com.ares.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonExample {

  private static final Logger logger = LoggerFactory.getLogger(JsonExample.class);

  public static void main(String[] args) throws JsonProcessingException {
    User user1 = new User(1000L, "ares01", LocalDateTime.now());
    User user2 = new User(1000L, "ares02", LocalDateTime.now());
    List<User> list = Arrays.asList(user1, user2);
    //defaultFactory(user1);
    listExample(list);
  }

  private static void defaultFactory(User user) throws JsonProcessingException {
    ObjectMapper mapper = DefaultObjectMapperFactory.createObjectMapperWithSerializerFactory();
    String json = mapper.writeValueAsString(user);
    logger.info(json);
  }

  private static void listExample(List<User> list) throws JsonProcessingException {
    ObjectMapper mapper = DefaultObjectMapperFactory.createObjectMapperWithSerializerFactory();
    String json = mapper.writeValueAsString(list);
    logger.info(json);
    List<User> readValue = mapper.readValue(json, new TypeReference<>() {});
    logger.info(readValue.toString());
  }

  public record User(Long userId, String userName, LocalDateTime createTime) {

  }
}
