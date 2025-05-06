package com.carrotsearch.console.testing;

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
public final class LogMonitorAppender extends AbstractAppender {
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

  private final BiConsumer<Logger, CapturedLogEvent> logEventConsumer;
  private final Logger logger;

  public LogMonitorAppender(Logger logger, BiConsumer<Logger, CapturedLogEvent> logEventConsumer) {
    super("LogMonitorAppender", null, null, false, Property.EMPTY_ARRAY);
    this.logEventConsumer = logEventConsumer;
    this.logger = logger;
  }

  @Override
  public synchronized void append(LogEvent event) {
    logEventConsumer.accept(
        logger,
        new CapturedLogEvent(
            event.getMessage().getFormattedMessage(),
            Objects.requireNonNull(levelMap.get(event.getLevel())),
            event.getThrown()));
  }
}
