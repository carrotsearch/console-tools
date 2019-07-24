/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.JCommander;
import com.carrotsearch.console.jcommander.MissingCommandException;
import com.carrotsearch.console.jcommander.ParameterException;
import com.carrotsearch.console.jcommander.Parameters;
import com.carrotsearch.console.jcommander.UsageOptions;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Launcher {
  private static final String LAUNCHER_COMMAND_NAME = "l4g";

  /** Launcher commands lookup section in MANIFEST.MF. */
  public static final String MANIFEST_COMMANDS_SECTION = "launcher.commands";

  /** Separates command lookup locations from actual parameters then passed to the command(s). */
  public static final String PARAMS_SEPARATOR = "--";

  public static void main(String[] args) throws Exception {
    List<String> argsList = Arrays.asList(args);

    List<String> commandLookupArgs = Collections.emptyList();
    List<String> commandArgs = Collections.emptyList();
    for (int i = 0; i < argsList.size(); ) {
      if (argsList.get(i).equals(PARAMS_SEPARATOR)) {
        commandLookupArgs = argsList.subList(0, i);
        commandArgs = argsList.subList(i + 1, argsList.size());
        break;
      }

      if (++i == argsList.size()) {
        commandLookupArgs = argsList;
        break;
      }
    }

    // JVM version printout for script launchers.
    if (Arrays.asList("--jvmversion").equals(commandLookupArgs)) {
      System.out.print(JavaVersion.get());
      System.out.flush();
      System.exit(ExitStatus.SUCCESS.processReturnValue());
    }

    // Require at least Java 1.8, even for the launcher.
    JavaVersion platform = JavaVersion.get();
    JavaVersion minRequired = JavaVersion.JAVA_1_8;
    if (!platform.atLeast(minRequired)) {
      System.err.println(
          "Minimum JVM version required is " + minRequired + ". This JVM is: " + platform);
      System.exit(ExitStatus.ERROR_OTHER.processReturnValue());
    }

    runSingleCommand(
        new CollectAndRunCommands(commandArgs),
        commandLookupArgs.toArray(new String[commandLookupArgs.size()]));
  }

  public static ExitStatus run(Iterable<? extends Callable<ExitStatus>> commands, String... args) {
    final DefaultParameters defaultParams = new DefaultParameters();
    JCommander jc = new JCommander(defaultParams);
    for (Callable<?> c : commands) {
      jc.addCommand(c);
    }
    jc.setProgramName(LAUNCHER_COMMAND_NAME, "");
    jc.addConverterFactory(new PathConverter());

    // By default, be verbose (commands will reconfigure).
    try {
      Configurator.initialize(
          "log4j2-default.xml",
          Launcher.class.getClassLoader(),
          Launcher.class.getResource("log4j2-default.xml").toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    ExitStatus exitStatus = ExitStatus.SUCCESS;
    try {
      jc.parse(args);

      if (defaultParams.sysProperties != null) {
        for (Map.Entry<String, String> e : defaultParams.sysProperties.entrySet()) {
          System.setProperty(e.getKey(), e.getValue());
        }
      }

      final String commandName = jc.getParsedCommand();
      if (commandName == null || defaultParams.help) {
        helpDisplayCommands(System.out, jc, defaultParams.showHidden);
      } else {
        exitStatus = launchCommand(jc);
      }
    } catch (MissingCommandException e) {
      System.err.println("Invalid argument: " + e);
      System.err.println();
      helpDisplayCommands(System.err, jc, defaultParams.showHidden);

      exitStatus = ExitStatus.ERROR_INVALID_ARGUMENTS;
    } catch (ParameterException e) {
      System.err.println("Invalid argument: " + e.getMessage());
      System.err.println();

      if (jc.getParsedCommand() == null) {
        helpDisplayCommands(System.err, jc, defaultParams.showHidden);
      } else {
        helpDisplayCommandOptions(System.err, jc.getParsedCommand(), jc);
      }
      exitStatus = ExitStatus.ERROR_INVALID_ARGUMENTS;
    } catch (ReportCommandException e) {
      Logger logger = LoggerFactory.getLogger("console");
      logger.error(e.getCause().getMessage(), e.getCause());
      exitStatus = e.exitStatus;
    } catch (Throwable t) {
      Logger logger = LoggerFactory.getLogger("console");
      logger.error(
          "An unhandled exception occurred: "
              + t.getClass().getSimpleName()
              + ": "
              + t.getMessage(),
          t);
      exitStatus = ExitStatus.ERROR_OTHER;
    }

    // Flush all streams just to make sure we've emitted all the information.
    System.err.flush();
    System.out.flush();

    // Call sysexit if requested.
    if (defaultParams.callSystemExit) {
      System.exit(exitStatus.processReturnValue());
    }

    return exitStatus;
  }

  private static ExitStatus launchCommand(JCommander jc) throws Exception {
    final String commandName = jc.getParsedCommand();
    assert commandName != null;

    final JCommander commandParser = jc.getCommands().get(commandName);
    List<Object> objects = commandParser.getObjects();
    assert objects.size() == 1
        : "Expected exactly one object for command '" + commandName + "': " + objects;

    Callable<?> selectedCommand = (Callable<?>) objects.get(0);
    Thread.currentThread().setContextClassLoader(selectedCommand.getClass().getClassLoader());
    return ExitStatus.class.cast(selectedCommand.call());
  }

  private static void helpDisplayCommandOptions(PrintStream pw, String command, JCommander jc) {
    StringBuilder sb = new StringBuilder();
    jc = jc.getCommands().get(command);
    jc.usage(sb, "");
    pw.print(sb);
  }

  private static void helpDisplayCommands(PrintStream pw, JCommander jc, boolean displayAll) {
    StringBuilder sb = new StringBuilder();
    jc.usage(
        sb,
        "",
        UsageOptions.DISPLAY_SYNTAX_LINE,
        UsageOptions.DISPLAY_PARAMETERS,
        displayAll ? UsageOptions.DISPLAY_COMMANDS_HIDDEN : UsageOptions.DISPLAY_COMMANDS,
        UsageOptions.DISPLAY_COMMANDS,
        UsageOptions.SORT_COMMANDS,
        UsageOptions.GROUP_COMMANDS);
    pw.print(sb);
  }

  public static void reloadLoggingConfiguration(LoggerContext ctx, URI... configurations) {
    ConfigurationFactory configFactory = ConfigurationFactory.getInstance();

    List<AbstractConfiguration> configs = new ArrayList<AbstractConfiguration>();
    for (URI configUri : configurations) {
      Configuration configuration =
          configFactory.getConfiguration(ctx, configUri.toString(), configUri);
      if (configuration == null || !(configuration instanceof AbstractConfiguration)) {
        throw new RuntimeException("Oddball config problem: " + configUri);
      }
      configs.add((AbstractConfiguration) configuration);
    }

    ctx.start(new CompositeConfiguration(configs));
  }

  public static void runSingleCommand(Callable<ExitStatus> command, String... args) {
    runSingleCommand(command, new String[] {}, args);
  }

  public static void runSingleCommand(
      Callable<ExitStatus> command, String[] launcherArgs, String... args) {
    List<String> argLine = new ArrayList<String>();
    String[] commandNames = command.getClass().getAnnotation(Parameters.class).commandNames();
    if (commandNames.length == 0) {
      throw new RuntimeException("No command names on class: " + command.getClass().getName());
    }
    argLine.addAll(Arrays.asList(launcherArgs));
    argLine.add(commandNames[0]);
    argLine.addAll(Arrays.asList(args));

    run(Arrays.asList(command), argLine.toArray(new String[argLine.size()]));
  }
}
