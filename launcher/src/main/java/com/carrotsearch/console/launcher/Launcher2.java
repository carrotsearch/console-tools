/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.JCommander;
import com.carrotsearch.console.jcommander.ParameterException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.carrotsearch.console.jcommander.Parameters;
import com.carrotsearch.console.jcommander.UsageOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;

public class Launcher2 {
  private String launcherScriptName;

  public <T extends ExitCode> ExitCode runCommand(Command<T> cmd, String... args) {
    try {
      configureLoggingDefaults();

      JCommander jc = new JCommander();
      jc.addObject(cmd);

      String cmdName = cmd.getClass().getName();
      Parameters parameters = cmd.getClass().getAnnotation(Parameters.class);
      if (parameters != null) {
        if (parameters.commandNames().length > 0) {
          cmdName = parameters.commandNames()[0];
        }
      }
      jc.setProgramName(cmdName, "");
      jc.addConverterFactory(new PathConverter());
      jc.parse(args);

      if (cmd.help) {
        Loggers.CONSOLE.info(commandHelp(jc, false));
        return ExitStatus.SUCCESS;
      } else {
        // TODO configureSysProperties(cmd.sysProps);
        reloadLoggingConfiguration(cmd.configureLogging(resolveLoggingConfigurations(cmd.logging)));
        return cmd.run();
      }
    } catch (ParameterException e) {
      Loggers.CONSOLE.error(
          "Invalid arguments (type {} for help): {}", Command.OPTION_HELP, e.getMessage());
      return ExitStatus.ERROR_INVALID_ARGUMENTS;
    } catch (Exception e) {
      System.err.println("An unhandled exception occurred launching command: " + e);
      System.err.println();
      e.printStackTrace(System.err);
      return ExitStatus.ERROR_INTERNAL;
    }
  }

  private String commandHelp(JCommander jc, boolean showHidden) {
    StringBuilder sb = new StringBuilder();
    jc.usage(sb, "",
        UsageOptions.DISPLAY_SYNTAX_LINE,
        UsageOptions.DISPLAY_PARAMETERS,
        showHidden ? UsageOptions.DISPLAY_COMMANDS_HIDDEN : UsageOptions.DISPLAY_COMMANDS,
        UsageOptions.DISPLAY_COMMANDS,
        UsageOptions.SORT_COMMANDS,
        UsageOptions.GROUP_COMMANDS);
    return sb.toString();
  }

  private void reloadLoggingConfiguration(List<URI> configurations) {
    if (configurations.isEmpty()) {
      return;
    }

    ClassLoader cl = getClass().getClassLoader();
    LoggerContext ctx = (LoggerContext) LogManager.getContext(cl, false);
    Launcher.reloadLoggingConfiguration(ctx, configurations.toArray(new URI[0]));

    ConfigurationFactory configFactory = ConfigurationFactory.getInstance();
    List<AbstractConfiguration> configs = new ArrayList<>();
    for (URI configUri : configurations) {
      Configuration configuration =
          configFactory.getConfiguration(ctx, configUri.toString(), configUri);
      if (!(configuration instanceof AbstractConfiguration)) {
        throw new RuntimeException("Not an AbstractConfiguration?: " + configUri);
      }
      configs.add((AbstractConfiguration) configuration);
    }

    ctx.start(new CompositeConfiguration(configs));
  }

  private List<URI> resolveLoggingConfigurations(LoggingParameters logging) {
    ArrayList<String> configs = new ArrayList<>();
    if (logging.configuration != null) configs.add(LoggingParameters.OPT_LOGGING_CONFIG);
    if (logging.trace) configs.add(LoggingParameters.OPT_TRACE);
    if (logging.verbose) configs.add(LoggingParameters.OPT_VERBOSE);
    if (logging.quiet) configs.add(LoggingParameters.OPT_QUIET);
    if (configs.size() > 1) {
      throw new ParameterException(
          "Conflicting logging configuration options: " + String.join(", ", configs));
    }

    List<URI> multiConfigs = new ArrayList<>();
    if (logging.configuration != null) {
      Path config = logging.configuration.toAbsolutePath().normalize();
      if (!Files.isRegularFile(config)) {
        throw new ParameterException("Logging configuration file does not exist: " + config);
      }
      multiConfigs.add(config.toUri());
    }

    if (logging.trace) {
      multiConfigs.add(getResourceAsURI("log4j2-trace.xml"));
    }

    if (logging.verbose) {
      multiConfigs.add(getResourceAsURI("log4j2-verbose.xml"));
    }

    if (logging.quiet) {
      multiConfigs.add(getResourceAsURI("log4j2-quiet.xml"));
    }

    if (multiConfigs.isEmpty()) {
      multiConfigs.add(getResourceAsURI("log4j2-default.xml"));
    }

    return multiConfigs;
  }

  private void configureLoggingDefaults() {
    try {
      Configurator.initialize(
          "log4j2-init.xml", Launcher2.class.getClassLoader(), getResourceAsURI("log4j2-init.xml"));
    } catch (RuntimeException | Error e) {
      throw new RuntimeException(
          "Could not load or initialize the default logging configuration.", e);
    }
  }

  private URI getResourceAsURI(String resource) {
    try {
      URL url = getClass().getResource(resource);
      if (url == null) {
        throw new RuntimeException("Required resource missing: " + resource);
      }
      return url.toURI();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
