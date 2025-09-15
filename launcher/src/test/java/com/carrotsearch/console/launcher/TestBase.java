package com.carrotsearch.console.launcher;

import com.carrotsearch.console.testing.Logs;
import com.carrotsearch.console.testing.Resources;
import com.carrotsearch.console.testing.ThrowingCallable;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import org.assertj.core.api.Assertions;

public abstract class TestBase extends RandomizedTest {

  protected void logsEqual(String resourceName, ThrowingCallable callable) throws Throwable {
    Assertions.assertThat(String.join("\n", Logs.captureAsStrings(Loggers.CONSOLE, callable)))
        .isEqualToNormalizingWhitespace(Resources.resourceAsString(this.getClass(), resourceName));
  }
}
