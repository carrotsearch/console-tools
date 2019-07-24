/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander.converters;

import com.carrotsearch.console.jcommander.ParameterException;

/**
 * Convert a string to a long.
 *
 * @author cbeust
 */
public class LongConverter extends BaseConverter<Long> {

  public LongConverter(String optionName) {
    super(optionName);
  }

  public Long convert(String value) {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException ex) {
      throw new ParameterException(getErrorString(value, "a long"));
    }
  }
}
