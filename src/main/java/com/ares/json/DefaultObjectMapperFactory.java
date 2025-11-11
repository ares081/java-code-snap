package com.ares.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.time.LocalDateTime;

public class DefaultObjectMapperFactory {

  public static ObjectMapper createStandardObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    // 基本配置
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    // 忽略null值
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper;
  }

  public static ObjectMapper createObjectMapperWithDefaultFactory() {
    DefaultJsonFactory jsonFactory = new DefaultJsonFactory();
    ObjectMapper mapper = new ObjectMapper(jsonFactory);
    // 应用标准配置
    configureStandardSettings(mapper);
    return mapper;
  }

  public static ObjectMapper createObjectMapperWithSerializerFactory() {
    ObjectMapper mapper = createStandardObjectMapper();
    // 注册自定义模块
    SimpleModule customModule = new SimpleModule("CustomModule");
    customModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
    customModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    mapper.registerModule(customModule);
    return mapper;
  }

  public static ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    // 严格配置
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
    mapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
    return mapper;
  }

  private static void configureStandardSettings(ObjectMapper mapper) {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }
}
