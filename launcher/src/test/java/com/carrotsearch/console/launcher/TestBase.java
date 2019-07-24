/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.slf4j.Logger;

import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

abstract class TestBase extends RandomizedTest {
  // static AtomicInteger cnt = new AtomicInteger();

  public static List<String> captureLogs(Logger logger, ThrowingCallable c) throws Throwable {
    // String id = "#" + cnt.incrementAndGet();

    List<String> logs = new ArrayList<>();
    PropertyChangeListener propertyChangeListener =
        (prop) -> {
          if (Objects.equals(prop.getPropertyName(), LoggerContext.PROPERTY_CONFIG)) {
            BiConsumer<Logger, LogEventData> evConsumer =
                (lgr, event) -> {
                  // System.out.println("# consume event: " + id + ": " + event.getMessage());
                  logs.add(event.getMessage());
                };
            LogMonitorAppender appender = new LogMonitorAppender(logger, evConsumer);
            appender.start();

            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration configuration = ctx.getConfiguration();

            LoggerConfig loggerConfig = configuration.getLoggerConfig(logger.getName());
            loggerConfig.addAppender(appender, Level.ALL, null);
          }
        };

    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    try {
      // System.out.println("# addPropertyChangeListener: " + id);
      ctx.addPropertyChangeListener(propertyChangeListener);
      ctx.reconfigure();
      c.call();
      return logs;
    } finally {
      ctx.removePropertyChangeListener(propertyChangeListener);
      // System.out.println("# removePropertyChangeListener: " + id);
    }
  }

  protected void logsEqual(String resourceName, ThrowableAssert.ThrowingCallable callable)
      throws Throwable {
    Assertions.assertThat(String.join("\n", captureLogs(Loggers.CONSOLE, callable)))
        .isEqualToNormalizingWhitespace(resourceAsString(resourceName));
  }

  protected String resourceAsString(String resourceName) {
    try (InputStream is = this.getClass().getResourceAsStream(resourceName)) {
      Objects.requireNonNull(is, () -> "Resource missing: " + resourceName);
      byte[] buf = new byte[1024];
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int len;
      while ((len = is.read(buf)) >= 0) {
        baos.write(buf, 0, len);
      }
      return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
