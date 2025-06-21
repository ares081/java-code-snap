package com.ares.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultJsonFactory extends JsonFactory {

  private final Logger logger = LoggerFactory.getLogger(DefaultJsonFactory.class);
  // 自定义配置标志
  private final String customPrefix;

  public DefaultJsonFactory() {
    this("default");
  }

  public DefaultJsonFactory(String customPrefix) {
    this.customPrefix = customPrefix;
  }

  @Override
  public JsonParser createParser(String content) throws IOException {
    return super.createParser(content);
  }

  @Override
  public JsonParser createParser(InputStream in) throws IOException {
    return super.createParser(in);
  }

  @Override
  public JsonGenerator createGenerator(DataOutput out, JsonEncoding enc) throws IOException {
    return super.createGenerator(out, enc);
  }

  @Override
  public JsonGenerator createGenerator(Writer w) throws IOException {
    return super.createGenerator(w);
  }

  @Override
  public JsonGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException {
    return super.createGenerator(out, enc);
  }
}
