/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.testing;

import org.slf4j.event.Level;

public final class CapturedLogEvent {

  private final String message;
  private final Throwable throwable;
  private final Level level;

  public CapturedLogEvent(String message, Level level, Throwable throwable) {
    this.message = message;
    this.level = level;
    this.throwable = throwable;
  }

  public String getMessage() {
    return message;
  }

  public Level getLevel() {
    return level;
  }

  public Throwable getThrowable() {
    return throwable;
  }
}
