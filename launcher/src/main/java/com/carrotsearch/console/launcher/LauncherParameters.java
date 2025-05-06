package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.Parameter;

public class LauncherParameters {
  public static final String OPT_HIDDEN = "--hidden";
  public static final String OPT_HELP = "--help";

  @Parameter(
      names = {OPT_HIDDEN},
      description = "Display hidden commands and options.",
      hidden = true)
  public boolean hidden;

  @Parameter(
      hidden = true,
      names = {OPT_HELP},
      description = "Display all available commands.",
      help = true)
  public boolean help;
}
