package com.carrotsearch.console.testing;

import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.Logger;

public class Logs {
  private Logs() {}

  public static void capture(
      Logger logger, BiConsumer<Logger, CapturedLogEvent> evConsumer, ThrowingCallable c) {
    PropertyChangeListener propertyChangeListener =
        (prop) -> {
          if (Objects.equals(prop.getPropertyName(), LoggerContext.PROPERTY_CONFIG)) {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            org.apache.logging.log4j.core.config.Configuration configuration =
                ctx.getConfiguration();

            LogMonitorAppender appender = new LogMonitorAppender(logger, evConsumer);
            appender.start();

            LoggerConfig loggerConfig = configuration.getLoggerConfig(logger.getName());
            loggerConfig.addAppender(appender, Level.ALL, null);
          }
        };

    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    org.apache.logging.log4j.core.config.Configuration prior = ctx.getConfiguration();
    try {
      ctx.addPropertyChangeListener(propertyChangeListener);
      ctx.updateLoggers();
      c.call();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    } finally {
      ctx.removePropertyChangeListener(propertyChangeListener);
      ctx.setConfiguration(prior);
    }
  }

  public static List<CapturedLogEvent> capture(Logger logger, ThrowingCallable c) {
    List<CapturedLogEvent> logs = new ArrayList<>();
    capture(logger, (lgr, event) -> logs.add(event), c);
    return logs;
  }

  public static List<String> captureAsStrings(Logger logger, ThrowingCallable c) {
    List<CapturedLogEvent> capture = capture(logger, c);
    return capture.stream()
        .map(
            event -> {
              String line = event.getMessage();

              if (event.getThrowable() != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream pw = new PrintStream(baos, true, StandardCharsets.UTF_8);
                event.getThrowable().printStackTrace(pw);
                pw.flush();

                line = line + "\n" + baos.toString(StandardCharsets.UTF_8);
              }
              return line;
            })
        .collect(Collectors.toList());
  }

  public static void restoreLogConfig(Runnable codeBlock) {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration configuration = ctx.getConfiguration();
    ConfigurationSource configurationSource = configuration.getConfigurationSource();
    if (configurationSource == ConfigurationSource.COMPOSITE_SOURCE
        || configurationSource == ConfigurationSource.NULL_SOURCE) {
      throw new RuntimeException(
          "The current log4j2 configuration is either a composite or null, it cannot be restored properly.");
    }

    URI previousConfiguration = configurationSource.getURI();
    if (previousConfiguration == null) {
      throw new RuntimeException(
          "The current log4j2 configuration has an empty URI, it cannot be restored properly.");
    }

    try {
      codeBlock.run();
    } finally {
      ctx.setConfigLocation(previousConfiguration);
    }
  }
}
