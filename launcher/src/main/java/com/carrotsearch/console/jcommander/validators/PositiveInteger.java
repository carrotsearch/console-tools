package com.carrotsearch.console.jcommander.validators;

import com.carrotsearch.console.jcommander.IParameterValidator;
import com.carrotsearch.console.jcommander.ParameterException;

/**
 * A validator that makes sure the value of the parameter is a positive integer.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class PositiveInteger implements IParameterValidator {

  public void validate(String name, String value) throws ParameterException {
    int n = Integer.parseInt(value);
    if (n < 0) {
      throw new ParameterException(
          "Parameter " + name + " should be positive (found " + value + ")");
    }
  }
}
