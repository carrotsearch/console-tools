/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.DynamicParameter;
import com.carrotsearch.console.jcommander.Parameter;
import java.util.HashMap;
import java.util.Map;

class DefaultParameters {
  @DynamicParameter(
      hidden = true,
      names = "-D",
      description = "Sets a system property to a given value (JVM syntax).")
  public Map<String, String> sysProperties = new HashMap<String, String>();

  @Parameter(
      hidden = true,
      names = {"--exit"},
      description = "Call System.exit() at end of command.")
  public boolean callSystemExit;

  @Parameter(
      hidden = true,
      names = {"--help", "-h"},
      description = "Display all available commands.",
      help = true)
  public boolean help;

  @Parameter(
      hidden = true,
      names = {"--hidden"},
      description = "Include hidden commands in command listing.")
  public boolean showHidden;
}
