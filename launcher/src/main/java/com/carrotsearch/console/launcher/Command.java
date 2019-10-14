/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.JCommander;
import com.carrotsearch.console.jcommander.Parameter;
import com.carrotsearch.console.jcommander.ParametersDelegate;
import java.net.URI;
import java.util.List;

public abstract class Command<T extends ExitCode> {
  public static final String OPT_HELP = "--help";

  @ParametersDelegate public LoggingParameters logging = new LoggingParameters();

  @ParametersDelegate public SysPropertiesParameters sysProps = new SysPropertiesParameters();

  @Parameter(
      hidden = true,
      names = {"--help", "-h"},
      description = "Display help for options.",
      help = true)
  public boolean help;

  protected abstract T run();

  protected List<URI> configureLogging(List<URI> defaults) {
    return defaults;
  }

  protected void configure(JCommander jcommander) {
    // Do nothing by default.
  }
}
