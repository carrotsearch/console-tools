/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.Parameter;
import com.carrotsearch.console.jcommander.Parameters;
import com.carrotsearch.console.testing.Logs;
import com.carrotsearch.console.testing.ThrowingCallable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LauncherTest extends TestBase {
  @Test
  public void testArgsParsing() {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitCodes> {
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
      public ExitCodes run() {
        return ExitCodes.SUCCESS;
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
    Assertions.assertThat(new Launcher().runCommand(cmd, args)).isEqualTo(ExitCodes.SUCCESS);

    Assertions.assertThat(cmd.stringRequired).isEqualTo("value1");
    Assertions.assertThat(cmd.intRequired).isEqualTo(42);
    Assertions.assertThat(cmd.boolSwitchOn).isEqualTo(true);
    Assertions.assertThat(cmd.boolSwitchOff).isEqualTo(false);
    Assertions.assertThat(cmd.path).isEqualTo(Paths.get("."));
  }

  @Test
  public void testRequiredArgMissing() throws Throwable {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitCodes> {
      @Parameter(
          names = {"--requiredArg"},
          required = true)
      public String requiredArg;

      @Override
      public ExitCodes run() {
        return ExitCodes.SUCCESS;
      }
    }

    logsEqual(
        "LauncherTest_testRequiredArgMissing.txt",
        () -> {
          Cmd cmd = new Cmd();
          Assertions.assertThat(new Launcher().runCommand(cmd))
              .isEqualTo(ExitCodes.ERROR_INVALID_ARGUMENTS);
        });
  }

  @Test
  public void testHelp() throws Throwable {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitCodes> {
      @Parameter(
          names = {"--requiredArg"},
          required = true)
      public String requiredArg;

      @Override
      public ExitCodes run() {
        return ExitCodes.SUCCESS;
      }
    }

    logsEqual(
        "LauncherTest_testHelp.txt",
        () -> {
          Cmd cmd = new Cmd();
          Assertions.assertThat(new Launcher().runCommand(cmd, "--help"))
              .isEqualTo(ExitCodes.SUCCESS);
        });
  }

  @Test
  public void testLoggingLevels() throws Throwable {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitCodes> {
      @Override
      public ExitCodes run() {
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

        return ExitCodes.SUCCESS;
      }
    }

    ThrowingCallable call;

    // Default level.
    call = () -> new Launcher().runCommand(new Cmd());

    Assertions.assertThat(Logs.captureAsStrings(Loggers.ROOT, call))
        .containsExactly("internal:warn", "internal:error");
    Assertions.assertThat(Logs.captureAsStrings(Loggers.CONSOLE, call))
        .containsExactly("console:info", "console:warn", "console:error");

    // --quiet
    call = () -> new Launcher().runCommand(new Cmd(), LoggingParameters.OPT_QUIET);

    Assertions.assertThat(Logs.captureAsStrings(Loggers.ROOT, call))
        .containsExactly("internal:warn", "internal:error");
    Assertions.assertThat(Logs.captureAsStrings(Loggers.CONSOLE, call))
        .containsExactly("console:warn", "console:error");

    // --verbose
    call = () -> new Launcher().runCommand(new Cmd(), LoggingParameters.OPT_VERBOSE);

    Assertions.assertThat(Logs.captureAsStrings(Loggers.ROOT, call))
        .containsExactly("internal:warn", "internal:error");
    Assertions.assertThat(Logs.captureAsStrings(Loggers.CONSOLE, call))
        .containsExactly("console:debug", "console:info", "console:warn", "console:error");

    // --trace
    call = () -> new Launcher().runCommand(new Cmd(), LoggingParameters.OPT_TRACE);

    Assertions.assertThat(Logs.captureAsStrings(Loggers.ROOT, call))
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

    Assertions.assertThat(Logs.captureAsStrings(Loggers.CONSOLE, call))
        .containsExactly(
            "console:trace", "console:debug", "console:info", "console:warn", "console:error");
  }

  @Test
  public void testCustomLogConfig() throws Throwable {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitCodes> {
      @Override
      public ExitCodes run() {
        Loggers.CONSOLE.info("console:before");
        Logger internal = LoggerFactory.getLogger("com.carrotsearch.Internal");
        internal.error("internal:info");
        Loggers.CONSOLE.info("console:after");
        return ExitCodes.SUCCESS;
      }

      @Override
      protected List<URI> configureLogging(List<URI> defaults) {
        try {
          defaults.add(LauncherTest.class.getResource("LauncherTest_customLogConfig.xml").toURI());
          return defaults;
        } catch (URISyntaxException e) {
          throw new RuntimeException();
        }
      }
    }

    Assertions.assertThat(
            Logs.captureAsStrings(
                Loggers.ROOT,
                () -> new Launcher().runCommand(new Cmd(), LoggingParameters.OPT_TRACE)))
        .containsExactly("console:before", "console:after");
  }

  @Test
  public void testSysProperty() throws Throwable {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitCodes> {
      @Override
      public ExitCodes run() {
        Loggers.CONSOLE.info(System.getProperty("sysProp", "--"));
        return ExitCodes.SUCCESS;
      }
    }

    Assertions.assertThat(
            Logs.captureAsStrings(
                Loggers.CONSOLE, () -> new Launcher().runCommand(new Cmd(), "-DsysProp=value")))
        .containsExactly("value");
  }

  @Test
  public void testReportCommandException() throws Throwable {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitCodes> {
      @Override
      public ExitCodes run() {
        throw new ReportCommandException("Error message.", ExitCodes.ERROR_UNKNOWN);
      }
    }

    Assertions.assertThat(
            Logs.captureAsStrings(
                Loggers.CONSOLE,
                () -> {
                  Assertions.assertThat(new Launcher().runCommand(new Cmd()))
                      .isEqualTo(ExitCodes.ERROR_UNKNOWN);
                }))
        .containsExactly("Error message.");
  }

  @Test
  public void testReportCommandExceptionWithCause() throws Throwable {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitCodes> {
      @Override
      public ExitCodes run() {
        try {
          throw new Exception("Nested cause");
        } catch (Exception e) {
          throw new ReportCommandException("Error message.", ExitCodes.ERROR_UNKNOWN, e);
        }
      }
    }

    List<String> logs =
        Logs.captureAsStrings(
            Loggers.CONSOLE,
            () -> {
              Assertions.assertThat(
                      new Launcher().runCommand(new Cmd(), LoggingParameters.OPT_VERBOSE))
                  .isEqualTo(ExitCodes.ERROR_UNKNOWN);
            });
    Assertions.assertThat(String.join("\n", logs))
        .contains("Error message.")
        .contains("Nested cause");
  }

  @Test
  public void testRunCommands() throws Throwable {
    @Parameters(commandNames = "cmd1", commandDescription = "Command 1.")
    class Cmd1 extends Command<ExitCodes> {
      @Parameter(
          names = {"--opt1"},
          required = true)
      public String opt1;

      @Override
      public ExitCodes run() {
        return ExitCodes.SUCCESS;
      }
    }

    @Parameters(commandNames = "cmd2", commandDescription = "Command 2.")
    class Cmd2 extends Command<ExitCodes> {
      @Parameter(names = {"--opt2"})
      public String opt2;

      @Override
      public ExitCodes run() {
        return ExitCodes.SUCCESS;
      }
    }

    @Parameters(commandNames = "cmd3", commandDescription = "Command 3.", hidden = true)
    class Cmd3 extends Command<ExitCodes> {
      @Override
      public ExitCodes run() {
        return ExitCodes.SUCCESS;
      }
    }

    // Empty arg list.
    logsEqual(
        "LauncherTest_testRunCommands1.txt",
        () -> {
          List<Command<ExitCodes>> commands = Arrays.asList(new Cmd1(), new Cmd2(), new Cmd3());
          Assertions.assertThat(new Launcher().runCommands("launch", commands))
              .isEqualTo(ExitCodes.SUCCESS);
        });

    // Display hidden commands.
    logsEqual(
        "LauncherTest_testRunCommands2.txt",
        () -> {
          List<Command<ExitCodes>> commands = Arrays.asList(new Cmd1(), new Cmd2(), new Cmd3());
          Assertions.assertThat(new Launcher().runCommands(commands, LauncherParameters.OPT_HIDDEN))
              .isEqualTo(ExitCodes.SUCCESS);
        });

    // Invalid command name.
    logsEqual(
        "LauncherTest_testRunCommands3.txt",
        () -> {
          List<Command<ExitCodes>> commands = Arrays.asList(new Cmd1(), new Cmd2(), new Cmd3());
          Assertions.assertThat(new Launcher().runCommands(commands, "foobar"))
              .isEqualTo(ExitCodes.ERROR_INVALID_ARGUMENTS);
        });

    // Display commands on --help
    logsEqual(
        "LauncherTest_testRunCommands4.txt",
        () -> {
          List<Command<ExitCodes>> commands = Arrays.asList(new Cmd1(), new Cmd2(), new Cmd3());
          Assertions.assertThat(new Launcher().runCommands(commands, LauncherParameters.OPT_HELP))
              .isEqualTo(ExitCodes.SUCCESS);
        });

    // Display single command's options on --help.
    logsEqual(
        "LauncherTest_testRunCommands5.txt",
        () -> {
          List<Command<ExitCodes>> commands = Arrays.asList(new Cmd1(), new Cmd2(), new Cmd3());
          Assertions.assertThat(
                  new Launcher()
                      .runCommands("launch", commands, "cmd1", LauncherParameters.OPT_HELP))
              .isEqualTo(ExitCodes.SUCCESS);
        });

    // Missing required option from a command.
    logsEqual(
        "LauncherTest_testRunCommands6.txt",
        () -> {
          List<Command<ExitCodes>> commands = Arrays.asList(new Cmd1(), new Cmd2(), new Cmd3());
          Assertions.assertThat(new Launcher().runCommands("launch", commands, "cmd1"))
              .isEqualTo(ExitCodes.ERROR_INVALID_ARGUMENTS);
        });

    // Successfull launch.
    List<Command<ExitCodes>> commands = Collections.singletonList(new Cmd1());
    Assertions.assertThat(new Launcher().runCommands("launch", commands, "cmd1", "--opt1", "foo"))
        .isEqualTo(ExitCodes.SUCCESS);
  }

  @Test
  public void testJCommanderCustomisation() throws Throwable {
    @Parameters(commandNames = "cmd")
    class Cmd extends Command<ExitCodes> {
      @Parameter(
          names = {"--uri"},
          required = true)
      public URI uri;

      @Override
      public ExitCodes run() {
        Loggers.CONSOLE.info("URI: " + uri);
        return ExitCodes.SUCCESS;
      }
    }

    String testUri = "http://localhost/foo";
    List<String> logs =
        Logs.captureAsStrings(
            Loggers.CONSOLE,
            () -> {
              Assertions.assertThat(new Launcher().runCommand(new Cmd(), "--uri", testUri))
                  .isEqualTo(ExitCodes.SUCCESS);
            });

    Assertions.assertThat(String.join("\n", logs)).contains("URI: " + testUri);
  }
}
