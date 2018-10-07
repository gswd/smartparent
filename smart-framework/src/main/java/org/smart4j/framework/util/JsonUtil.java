package org.smart4j.framework.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtil {
  private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static <T> String toJson(T obj) {

    try {
      return OBJECT_MAPPER.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      logger.error("convert POJO to JSON failure", e);
      throw new RuntimeException(e);
    }

  }
  public static <T> T fromJson(String json, Class<T> type) {

    try {
      return OBJECT_MAPPER.readValue(json, type);
    } catch (IOException e) {
      logger.error("convert JSON to POJO failure", e);
      throw new RuntimeException(e);
    }

  }
}
