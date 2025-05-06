package com.carrotsearch.console.launcher;

public interface ExitCode {
  int processReturnValue();

  static ExitCode of(int value) {
    return () -> value;
  }
}
