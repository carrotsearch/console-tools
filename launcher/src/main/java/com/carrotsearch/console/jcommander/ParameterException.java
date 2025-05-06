package com.carrotsearch.console.jcommander;

/**
 * The main exception that JCommand will throw when something goes wrong while parsing parameters.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
@SuppressWarnings("serial")
public class ParameterException extends RuntimeException {

  public ParameterException(Throwable t) {
    super(t);
  }

  public ParameterException(String string) {
    super(string);
  }
}
