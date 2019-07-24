/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Loggers {
  public static final Logger ROOT = LoggerFactory.getLogger("");
  public static final Logger CONSOLE = LoggerFactory.getLogger("console");

  private Loggers() {}
}
