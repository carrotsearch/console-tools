/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.slf4j.Logger;

@Plugin(
    name = "LogMonitorAppender",
    category = "Core",
    elementType = "appender",
    printObject = false)
public class LogMonitorAppender extends AbstractAppender {
  private static final IdentityHashMap<Level, org.slf4j.event.Level> levelMap;

  static {
    levelMap = new IdentityHashMap<>();
    levelMap.put(Level.TRACE, org.slf4j.event.Level.TRACE);
    levelMap.put(Level.DEBUG, org.slf4j.event.Level.DEBUG);
    levelMap.put(Level.INFO, org.slf4j.event.Level.INFO);
    levelMap.put(Level.WARN, org.slf4j.event.Level.WARN);
    levelMap.put(Level.ERROR, org.slf4j.event.Level.ERROR);
    levelMap.put(Level.FATAL, org.slf4j.event.Level.ERROR);

    // These two shouldn't appear in events?
    levelMap.put(Level.ALL, org.slf4j.event.Level.TRACE);
    levelMap.put(Level.OFF, org.slf4j.event.Level.TRACE);
  }

  private final BiConsumer<Logger, LogEventData> logEventConsumer;
  private final Logger logger;

  public LogMonitorAppender(Logger logger, BiConsumer<Logger, LogEventData> logEventConsumer) {
    super("LogMonitorAppender", null, null, false, Property.EMPTY_ARRAY);
    this.logEventConsumer = logEventConsumer;
    this.logger = logger;
  }

  @Override
  public synchronized void append(LogEvent event) {
    logEventConsumer.accept(
        logger,
        new LogEventData() {
          @Override
          public Throwable getThrown() {
            return event.getThrown();
          }

          @Override
          public String getMessage() {
            return event.getMessage().getFormattedMessage();
          }

          @Override
          public org.slf4j.event.Level getLevel() {
            return Objects.requireNonNull(levelMap.get(event.getLevel()));
          }
        });
  }
}
