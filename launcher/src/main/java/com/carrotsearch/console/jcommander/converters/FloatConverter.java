/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander.converters;

import com.carrotsearch.console.jcommander.ParameterException;

/**
 * Convert a string to a float.
 *
 * @author acornejo
 */
public class FloatConverter extends BaseConverter<Float> {

  public FloatConverter(String optionName) {
    super(optionName);
  }

  public Float convert(String value) {
    try {
      return Float.parseFloat(value);
    } catch (NumberFormatException ex) {
      throw new ParameterException(getErrorString(value, "a float"));
    }
  }
}
