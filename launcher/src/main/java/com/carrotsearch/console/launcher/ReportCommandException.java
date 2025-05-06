package com.carrotsearch.console.launcher;

import java.util.Objects;

/**
 * An exception that propagates to the launcher causing an error message to be logged to the
 * console. Does not print the stack trace.
 */
@SuppressWarnings("serial")
public final class ReportCommandException extends RuntimeException {
  public final ExitCode exitCode;

  public ReportCommandException(ExitCode exitCode) {
    super();
    this.exitCode = exitCode;
  }

  public ReportCommandException(String message, ExitCode exitCode) {
    super(Objects.requireNonNull(message));
    this.exitCode = exitCode;
  }

  public ReportCommandException(String message, ExitCode exitCode, Throwable cause) {
    super(Objects.requireNonNull(message), Objects.requireNonNull(cause));
    this.exitCode = exitCode;
  }
}
