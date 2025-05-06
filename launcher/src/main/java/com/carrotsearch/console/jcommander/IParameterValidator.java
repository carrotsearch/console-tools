package com.carrotsearch.console.jcommander;

/**
 * The class used to validate parameters.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public interface IParameterValidator {

  /**
   * Validate the parameter.
   *
   * @param name The name of the parameter (e.g. "-host").
   * @param value The value of the parameter that we need to validate
   * @throws ParameterException Thrown if the value of the parameter is invalid.
   */
  void validate(String name, String value) throws ParameterException;
}
