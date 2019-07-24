/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.Parameter;
import com.carrotsearch.console.jcommander.Parameters;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LauncherTest extends TestBase {
  @Test
  public void testArgsParsing() {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitStatus> {
      @Parameter(
          names = {"--stringRequired"},
          required = true)
      public String stringRequired;

      @Parameter(
          names = {"--intRequired"},
          required = true)
      public int intRequired;

      @Parameter(names = {"--boolSwitchOn"})
      public boolean boolSwitchOn;

      @Parameter(
          names = {"--boolSwitchOff"},
          arity = 1)
      public boolean boolSwitchOff = true;

      @Parameter(names = {"--path"})
      public Path path;

      @Override
      public ExitStatus run() {
        return ExitStatus.SUCCESS;
      }
    }

    Cmd cmd = new Cmd();
    String[] args = {
      "--stringRequired",
      "value1",
      "--intRequired",
      "42",
      "--boolSwitchOn",
      "--boolSwitchOff",
      "false",
      "--path",
      "."
    };
    Assertions.assertThat(new Launcher2().runCommand(cmd, args)).isEqualTo(ExitStatus.SUCCESS);

    Assertions.assertThat(cmd.stringRequired).isEqualTo("value1");
    Assertions.assertThat(cmd.intRequired).isEqualTo(42);
    Assertions.assertThat(cmd.boolSwitchOn).isEqualTo(true);
    Assertions.assertThat(cmd.boolSwitchOff).isEqualTo(false);
    Assertions.assertThat(cmd.path).isEqualTo(Paths.get("."));
  }

  @Test
  public void testRequiredArgMissing() throws Throwable {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitStatus> {
      @Parameter(
          names = {"--requiredArg"},
          required = true)
      public String requiredArg;

      @Override
      public ExitStatus run() {
        return ExitStatus.SUCCESS;
      }
    }

    logsEqual(
        "LauncherTest_testRequiredArgMissing.txt",
        () -> {
          Cmd cmd = new Cmd();
          Assertions.assertThat(new Launcher2().runCommand(cmd))
              .isEqualTo(ExitStatus.ERROR_INVALID_ARGUMENTS);
        });
  }

  @Test
  public void testHelp() throws Throwable {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitStatus> {
      @Parameter(
          names = {"--requiredArg"},
          required = true)
      public String requiredArg;

      @Override
      public ExitStatus run() {
        return ExitStatus.SUCCESS;
      }
    }

    logsEqual(
        "LauncherTest_testHelp.txt",
        () -> {
          Cmd cmd = new Cmd();
          Assertions.assertThat(new Launcher2().runCommand(cmd, "--help"))
              .isEqualTo(ExitStatus.SUCCESS);
        });
  }

  @Test
  public void testLoggingLevels() throws Throwable {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitStatus> {
      @Override
      public ExitStatus run() {
        Logger console = Loggers.CONSOLE;
        console.trace("console:trace");
        console.debug("console:debug");
        console.info("console:info");
        console.warn("console:warn");
        console.error("console:error");

        Logger internal = LoggerFactory.getLogger("com.carrotsearch.Internal");
        internal.trace("internal:trace");
        internal.debug("internal:debug");
        internal.info("internal:info");
        internal.warn("internal:warn");
        internal.error("internal:error");

        return ExitStatus.SUCCESS;
      }
    }

    ThrowableAssert.ThrowingCallable call;

    // Default level.
    call = () -> new Launcher2().runCommand(new Cmd());

    Assertions.assertThat(captureLogs(Loggers.ROOT, call))
        .containsExactly("internal:warn", "internal:error");
    Assertions.assertThat(captureLogs(Loggers.CONSOLE, call))
        .containsExactly("console:info", "console:warn", "console:error");

    // --quiet
    call = () -> new Launcher2().runCommand(new Cmd(), LoggingParameters.OPT_QUIET);

    Assertions.assertThat(captureLogs(Loggers.ROOT, call))
        .containsExactly("internal:warn", "internal:error");
    Assertions.assertThat(captureLogs(Loggers.CONSOLE, call))
        .containsExactly("console:warn", "console:error");

    // --verbose
    call = () -> new Launcher2().runCommand(new Cmd(), LoggingParameters.OPT_VERBOSE);

    Assertions.assertThat(captureLogs(Loggers.ROOT, call))
        .containsExactly("internal:warn", "internal:error");
    Assertions.assertThat(captureLogs(Loggers.CONSOLE, call))
        .containsExactly("console:debug", "console:info", "console:warn", "console:error");

    // --trace
    call = () -> new Launcher2().runCommand(new Cmd(), LoggingParameters.OPT_TRACE);

    Assertions.assertThat(captureLogs(Loggers.ROOT, call))
        .containsExactly(
            "console:trace",
            "console:debug",
            "console:info",
            "console:warn",
            "console:error",
            "internal:trace",
            "internal:debug",
            "internal:info",
            "internal:warn",
            "internal:error");

    Assertions.assertThat(captureLogs(Loggers.CONSOLE, call))
        .containsExactly(
            "console:trace", "console:debug", "console:info", "console:warn", "console:error");
  }
}
