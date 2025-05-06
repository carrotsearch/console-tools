package com.carrotsearch.console.jcommander.converters;

import com.carrotsearch.console.jcommander.ParameterException;

/**
 * Convert a string to an integer.
 *
 * @author cbeust
 */
public class IntegerConverter extends BaseConverter<Integer> {

  public IntegerConverter(String optionName) {
    super(optionName);
  }

  public Integer convert(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      throw new ParameterException(getErrorString(value, "an integer"));
    }
  }
}
