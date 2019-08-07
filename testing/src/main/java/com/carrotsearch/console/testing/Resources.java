/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.testing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Resources {
  private Resources() {}

  public static String resourceAsString(Class<?> clazz, String resourceName) {
    return resourceAsString(clazz, resourceName, StandardCharsets.UTF_8);
  }

  public static String resourceAsString(Class<?> clazz, String resourceName, Charset charset) {
    return new String(resourceAsBytes(clazz, resourceName), charset);
  }

  public static byte[] resourceAsBytes(Class<?> clazz, String resourceName) {
    try (InputStream is = clazz.getResourceAsStream(resourceName)) {
      Objects.requireNonNull(is, () -> "Resource missing: " + resourceName);

      byte[] buf = new byte[1024];
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int len;
      while ((len = is.read(buf)) >= 0) {
        baos.write(buf, 0, len);
      }
      return baos.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
