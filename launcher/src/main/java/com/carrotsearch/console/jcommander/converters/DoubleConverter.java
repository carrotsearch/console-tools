/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander.converters;

import com.carrotsearch.console.jcommander.ParameterException;

/**
 * Convert a string to a double.
 *
 * @author acornejo
 */
public class DoubleConverter extends BaseConverter<Double> {

  public DoubleConverter(String optionName) {
    super(optionName);
  }

  public Double convert(String value) {
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException ex) {
      throw new ParameterException(getErrorString(value, "a double"));
    }
  }
}
