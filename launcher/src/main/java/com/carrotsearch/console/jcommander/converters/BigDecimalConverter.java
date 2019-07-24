/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander.converters;

import com.carrotsearch.console.jcommander.ParameterException;
import java.math.BigDecimal;

/**
 * Converts a String to a BigDecimal.
 *
 * @author Angus Smithson
 */
public class BigDecimalConverter extends BaseConverter<BigDecimal> {

  public BigDecimalConverter(String optionName) {
    super(optionName);
  }

  public BigDecimal convert(String value) {
    try {
      return new BigDecimal(value);
    } catch (NumberFormatException nfe) {
      throw new ParameterException(getErrorString(value, "a BigDecimal"));
    }
  }
}
