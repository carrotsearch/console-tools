/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.testing;

import java.io.Closeable;
import java.io.IOException;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

final class LogMonitor implements Closeable {
  private LogMonitorAppender appender;
  private LoggerContext ctx;
  private LoggerConfig config;

  public LogMonitor(LogMonitorAppender appender, LoggerConfig config, LoggerContext ctx) {
    this.appender = appender;
    this.ctx = ctx;
    this.config = config;
  }

  @Override
  public synchronized void close() throws IOException {
    if (appender != null) {
      try {
        config.removeAppender(appender.getName());
        appender.stop();
        ctx.updateLoggers();
      } finally {
        appender = null;
        ctx = null;
        config = null;
      }
    }
  }

  @Override
  public String toString() {
    return "LogMonitor@" + hashCode() + "#" + appender.hashCode();
  }
}
