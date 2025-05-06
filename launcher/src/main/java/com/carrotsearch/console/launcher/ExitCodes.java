package com.carrotsearch.console.launcher;

public enum ExitCodes implements ExitCode {
  /** The command was successful. */
  SUCCESS(0),

  /** Unknown error cause. */
  ERROR_UNKNOWN(1),

  /** Invalid input arguments or their combination. */
  ERROR_INVALID_ARGUMENTS(2),

  /** An internal error. */
  ERROR_INTERNAL(3);

  private final int code;

  ExitCodes(int systemExitCode) {
    this.code = systemExitCode;
  }

  @Override
  public int processReturnValue() {
    return code;
  }
}
