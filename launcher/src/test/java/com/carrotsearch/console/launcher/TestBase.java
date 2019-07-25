/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.slf4j.Logger;

abstract class TestBase extends RandomizedTest {
  public static List<String> captureLogs(Logger logger, ThrowingCallable c) throws Throwable {
    List<String> logs = new ArrayList<>();
    PropertyChangeListener propertyChangeListener =
        (prop) -> {
          if (Objects.equals(prop.getPropertyName(), LoggerContext.PROPERTY_CONFIG)) {
            BiConsumer<Logger, LogEventData> evConsumer =
                (lgr, event) -> {
                  logs.add(event.getMessage());
                  if (event.getThrowable() != null) {
                    try {
                      ByteArrayOutputStream baos = new ByteArrayOutputStream();
                      PrintStream pw = new PrintStream(baos, true, "UTF-8");
                      event.getThrowable().printStackTrace(pw);
                      pw.flush();
                      logs.add(new String(baos.toByteArray(), StandardCharsets.UTF_8));
                    } catch (UnsupportedEncodingException e) {
                      throw new RuntimeException(e);
                    }
                  }
                };
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration configuration = ctx.getConfiguration();

            LogMonitorAppender appender = new LogMonitorAppender(logger, evConsumer);
            appender.start();
            LoggerConfig loggerConfig = configuration.getLoggerConfig(logger.getName());

            loggerConfig.addAppender(appender, Level.ALL, null);
          }
        };

    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    try {
      ctx.addPropertyChangeListener(propertyChangeListener);
      ctx.reconfigure();
      c.call();
      return logs;
    } finally {
      ctx.removePropertyChangeListener(propertyChangeListener);
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
