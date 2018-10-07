package org.smart4j.framework.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CodecUtil {
  private static final Logger logger = LoggerFactory.getLogger(CodecUtil.class);

  public static String encodeURL(String source) {
    try {
      return URLEncoder.encode(source, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.error("encode url failure", e);
      throw new RuntimeException(e);
    }
  }
  public static String decodeURL(String source) {
    try {
      return URLDecoder.decode(source, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.error("decode url failure", e);
      throw new RuntimeException(e);
    }
  }
}
