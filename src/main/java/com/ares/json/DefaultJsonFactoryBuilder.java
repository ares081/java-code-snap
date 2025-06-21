package com.ares.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class DefaultJsonFactoryBuilder {

  private String customPrefix = "default";
  private boolean failOnUnknownProperties = false;
  private boolean failOnEmptyBeans = false;
  private boolean indentOutput = false;
  private boolean includeNonNull = true;

  public static DefaultJsonFactoryBuilder builder() {
    return new DefaultJsonFactoryBuilder();
  }


  public DefaultJsonFactoryBuilder customPrefix(String prefix) {
    this.customPrefix = prefix;
    return this;
  }

  public DefaultJsonFactoryBuilder failOnUnknownProperties(boolean fail) {
    this.failOnUnknownProperties = fail;
    return this;
  }

  public DefaultJsonFactoryBuilder failOnEmptyBeans(boolean fail) {
    this.failOnEmptyBeans = fail;
    return this;
  }

  public DefaultJsonFactoryBuilder indentOutput(boolean indent) {
    this.indentOutput = indent;
    return this;
  }

  public DefaultJsonFactoryBuilder includeNonNull(boolean include) {
    this.includeNonNull = include;
    return this;
  }


  public ObjectMapper build() {
    // 创建自定义JsonFactory
    DefaultJsonFactory jsonFactory = new DefaultJsonFactory(customPrefix);
    ObjectMapper mapper = new ObjectMapper(jsonFactory);
    // 应用配置
    applyCustomSettings(mapper);
    return mapper;
  }

  private void applyCustomSettings(ObjectMapper mapper) {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownProperties);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, failOnEmptyBeans);
    mapper.configure(SerializationFeature.INDENT_OUTPUT, indentOutput);

    if (includeNonNull) {
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
  }
}
