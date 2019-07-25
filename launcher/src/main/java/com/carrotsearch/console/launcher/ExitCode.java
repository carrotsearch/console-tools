/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

public interface ExitCode {
  int processReturnValue();

  static ExitCode of(int value) {
    return () -> value;
  }
}
