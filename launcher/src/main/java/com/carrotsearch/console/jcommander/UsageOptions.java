/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander;

public enum UsageOptions {
  /** Display non-command parameters. */
  DISPLAY_PARAMETERS,

  /** Display "syntax" line. */
  DISPLAY_SYNTAX_LINE,

  /** Display commands. */
  DISPLAY_COMMANDS,

  /** Display all commands, including those hidden. */
  DISPLAY_COMMANDS_HIDDEN,

  /**
   * Displays options for each command, if commands are present and {@link #DISPLAY_COMMANDS} is
   * also passed as an option.
   */
  DISPLAY_OPTIONS_FOR_EACH_COMMAND,

  /** Sort commands when displaying help. */
  SORT_COMMANDS,

  /** Groups commands according to their */
  GROUP_COMMANDS;
}
