/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.Parameter;
import java.nio.file.Path;

public class LoggingParameters {
  public static final String OPT_QUIET = "--quiet";
  public static final String OPT_VERBOSE = "--verbose";
  public static final String OPT_TRACE = "--trace";

  public static final String OPT_SKIP = "--logging-skip";
  public static final String OPT_LOGGING_CONFIG = "--logging-config";

  @Parameter(
      names = {"-q", OPT_QUIET},
      description = "Quiet logging configuration.")
  public boolean quiet;

  @Parameter(
      names = {"-v", OPT_VERBOSE},
      description = "Verbose logging configuration.")
  public boolean verbose;

  @Parameter(
      hidden = true,
      names = {OPT_TRACE},
      description = "Very verbose logging.")
  public boolean trace;

  @Parameter(
      hidden = true,
      names = {OPT_SKIP},
      description = "Skip logging system configuration.")
  public boolean skip;

  @Parameter(
      required = false,
      names = {OPT_LOGGING_CONFIG},
      description = "Explicit Log4j2 XML configuration location.")
  public Path configuration;
}
