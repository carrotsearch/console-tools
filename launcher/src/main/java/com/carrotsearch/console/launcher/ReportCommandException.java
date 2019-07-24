/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

/**
 * An exception class that, by default, won't cause the stack traces to be logged to the console.
 * Only exception messages (and their sub-causes) will be logged.
 */
@SuppressWarnings("serial")
public final class ReportCommandException extends RuntimeException {
  public final ExitStatus exitStatus;

  private ReportCommandException(Throwable t, ExitStatus exitStatus) {
    super(checkNotNull(t));

    this.exitStatus = exitStatus;
  }

  private static Throwable checkNotNull(Throwable t) {
    if (t == null) throw new IllegalArgumentException("Cause cannot be null.");
    return t;
  }

  /**
   * Throws {@link ReportCommandException} wrapper.
   *
   * @return Returns fake {@link RuntimeException} to make the compiler happy.
   */
  public static RuntimeException causedBy(Throwable t, ExitStatus s) throws ReportCommandException {
    throw new ReportCommandException(t, s);
  }
}
