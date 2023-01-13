/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.JCommander;
import com.carrotsearch.console.jcommander.ParameterException;
import com.carrotsearch.console.jcommander.Parameters;
import com.carrotsearch.console.jcommander.UsageOptions;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;

public class Launcher {
  public static final String ENV_SCRIPT_NAME = "SCRIPT_NAME";

  public final boolean configureLoggingDefaults;

  public Launcher() {
    this(true);
  }

  public Launcher(boolean configureLoggingDefaults) {
    this.configureLoggingDefaults = configureLoggingDefaults;
  }

  public ExitCode runCommands(
      Collection<? extends Command<? extends ExitCode>> cmds, String... args) {
    return runCommands("", cmds, args);
  }

  public ExitCode runCommands(
      String launchScriptName,
      Collection<? extends Command<? extends ExitCode>> cmds,
      String... args) {
    try {
      configureLoggingDefaults();

      if (cmds.isEmpty()) {
        Loggers.CONSOLE.error("The list of available commands is empty.");
        return ExitCodes.ERROR_INTERNAL;
      }

      LauncherParameters launcherParameters = new LauncherParameters();
      JCommander jc = new JCommander(launcherParameters);
      jc.addConverterFactory(new PathConverter());
      jc.addConverterFactory(new UriConverter());

      cmds.forEach(cmd -> cmd.configure(jc));
      cmds.forEach(jc::addCommand);

      jc.setProgramName(launchScriptName, "");
      jc.parse(args);

      String cmd = jc.getParsedCommand();
      if (cmd == null || launcherParameters.help) {
        Loggers.CONSOLE.info(commandHelp(jc, launcherParameters.hidden));
        return ExitCodes.SUCCESS;
      } else {
        JCommander cmdJc = jc.getCommands().get(cmd);
        List<Object> objects = cmdJc.getObjects();
        assert objects.size() == 1
            : "Expected exactly one object for command '" + cmd + "': " + objects;
        Command<? extends ExitCode> command =
            (Command<? extends ExitCode>) objects.iterator().next();

        cmdJc.setProgramName(getCommandName(command), "");
        return launchCommand(cmdJc, command);
      }
    } catch (ParameterException e) {
      Loggers.CONSOLE.error(
          "Invalid arguments (type {} for help): {}", Command.OPT_HELP, e.getMessage());
      return ExitCodes.ERROR_INVALID_ARGUMENTS;
    } catch (Throwable e) {
      Loggers.CONSOLE.error(
          "An unhandled exception occurred while launching commands (use {} to display stack trace): {}",
          LoggingParameters.OPT_VERBOSE,
          e.toString(),
          e);
      return ExitCodes.ERROR_INTERNAL;
    }
  }

  public <T extends ExitCode> ExitCode runCommand(Command<T> cmd, String... args) {
    String cmdName = getCommandName(cmd);

    try {
      configureLoggingDefaults();

      JCommander jc = new JCommander();
      jc.addObject(cmd);

      jc.setProgramName(cmdName, "");
      jc.addConverterFactory(new PathConverter());
      jc.addConverterFactory(new UriConverter());

      cmd.configure(jc);

      jc.parse(args);

      return launchCommand(jc, cmd);
    } catch (ParameterException e) {
      Loggers.CONSOLE.error(
          "Invalid arguments (type {} for help): {}", Command.OPT_HELP, e.getMessage());
      return ExitCodes.ERROR_INVALID_ARGUMENTS;
    } catch (Exception e) {
      Loggers.CONSOLE.error(
          "An unhandled exception occurred while launching command '{}' (use {} to display stack trace): {}",
          cmdName,
          LoggingParameters.OPT_VERBOSE,
          e.toString(),
          e);
      return ExitCodes.ERROR_INTERNAL;
    }
  }

  public static List<Command<? extends ExitCode>> lookupCommands() {
    ServiceLoader<Command> sl = ServiceLoader.load(Command.class);
    return sl.stream()
        .map(prov -> ((Command<? extends ExitCode>) prov.get()))
        .collect(Collectors.toList());
  }

  public static List<Command<? extends ExitCode>> lookupCommands(ClassLoader classLoader) {
    ServiceLoader<Command> sl = ServiceLoader.load(Command.class, classLoader);
    return sl.stream()
        .map(prov -> ((Command<? extends ExitCode>) prov.get()))
        .collect(Collectors.toList());
  }

  public static void main(String[] args) {
    List<Command<? extends ExitCode>> cmds = Launcher.lookupCommands();

    ExitCode exitCode;
    if (cmds.size() == 1) {
      exitCode = new Launcher().runCommand(cmds.iterator().next(), args);
    } else {
      String scriptName =
          Stream.of(
                  AccessController.doPrivileged(
                      (PrivilegedAction<String>) () -> System.getenv(ENV_SCRIPT_NAME)),
                  "")
              .filter(Objects::nonNull)
              .findFirst()
              .get();

      exitCode = new Launcher().runCommands(scriptName, cmds, args);
    }
    Runtime.getRuntime().exit(exitCode.processReturnValue());
  }

  private <T extends ExitCode> ExitCode launchCommand(JCommander jc, Command<T> cmd) {
    if (cmd.help) {
      Loggers.CONSOLE.info(commandHelp(jc, false));
      return ExitCodes.SUCCESS;
    } else {
      try {
        configureSysProps(cmd.sysProps);
        List<URI> configurations = cmd.configureLogging(resolveLoggingConfigurations(cmd.logging));
        if (!cmd.logging.skip) {
          reloadLoggingConfiguration(configurations);
        }
        return cmd.run();
      } catch (ReportCommandException e) {
        String msg = e.getMessage();
        if (msg != null) {
          Loggers.CONSOLE.error(msg, e.getCause());
        }
        return e.exitCode;
      }
    }
  }

  private <T extends ExitCode> String getCommandName(Command<T> cmd) {
    String cmdName = cmd.getClass().getName();
    Parameters parameters = cmd.getClass().getAnnotation(Parameters.class);
    if (parameters != null) {
      if (parameters.commandNames().length > 0) {
        cmdName = parameters.commandNames()[0];
      }
    }
    return cmdName;
  }

  private void configureSysProps(SysPropertiesParameters sysProps) {
    if (sysProps.sysProperties != null) {
      sysProps.sysProperties.forEach(System::setProperty);
    }
  }

  private String commandHelp(JCommander jc, boolean showHidden) {
    StringBuilder sb = new StringBuilder();
    jc.usage(
        sb,
        "",
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
    if (logging.skip) configs.add(LoggingParameters.OPT_SKIP);
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
    if (configureLoggingDefaults) {
      try {
        Configurator.initialize(
            "log4j2-init.xml",
            Launcher.class.getClassLoader(),
            getResourceAsURI("log4j2-init.xml"));
      } catch (RuntimeException | Error e) {
        throw new RuntimeException(
            "Could not load or initialize the default logging configuration.", e);
      }
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
