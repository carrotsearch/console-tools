/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander;

/**
 * Thrown when a command was expected.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
@SuppressWarnings("serial")
public class MissingCommandException extends ParameterException {

  public MissingCommandException(String string) {
    super(string);
  }

  public MissingCommandException(Throwable t) {
    super(t);
  }
}
