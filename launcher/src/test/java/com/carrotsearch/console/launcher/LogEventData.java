/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

interface LogEventData {
  String getMessage();

  org.slf4j.event.Level getLevel();

  Throwable getThrowable();
}
