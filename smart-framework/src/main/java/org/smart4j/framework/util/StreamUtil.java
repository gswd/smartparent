package org.smart4j.framework.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StreamUtil {
  private static final Logger logger = LoggerFactory.getLogger(StreamUtil.class);

  public static String getString(InputStream is) {
    StringBuilder sb = new StringBuilder();

    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    } catch (IOException e) {
      logger.error("get string failure", e);
      throw new RuntimeException(e);
    }

    return sb.toString();
  }
}
