/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

public enum ExitStatus implements ExitCode {
  /** The command was successful. */
  SUCCESS(0),

  /** Unknown error cause. */
  ERROR_OTHER(1),

  /** Invalid input arguments or their combination. */
  ERROR_INVALID_ARGUMENTS(2),

  ERROR_INTERNAL(3);

  private final int code;

  ExitStatus(int systemExitCode) {
    this.code = systemExitCode;
  }

  @Override
  public int processReturnValue() {
    return code;
  }
}
