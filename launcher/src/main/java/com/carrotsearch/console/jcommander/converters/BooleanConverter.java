package com.carrotsearch.console.jcommander.converters;

import com.carrotsearch.console.jcommander.ParameterException;

/**
 * Converts a string to a boolean.
 *
 * @author cbeust
 */
public class BooleanConverter extends BaseConverter<Boolean> {

  public BooleanConverter(String optionName) {
    super(optionName);
  }

  public Boolean convert(String value) {
    if ("false".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
      return Boolean.parseBoolean(value);
    } else {
      throw new ParameterException(getErrorString(value, "a boolean"));
    }
  }
}
