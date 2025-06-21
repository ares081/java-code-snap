package com.ares.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd HH:mm:ss");

  @Override
  public LocalDateTime deserialize(JsonParser p, DeserializationContext context)
      throws IOException {
    String dateString = p.getValueAsString();
    if (dateString == null || dateString.trim().isEmpty()) {
      return null;
    }
    try {
      return LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IOException("无法解析日期时间: " + dateString, e);
    }
  }
}
