/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander.converters;

import com.carrotsearch.console.jcommander.IStringConverter;

/**
 * Default value for a converter when none is specified.
 *
 * @author cbeust
 */
public class NoConverter implements IStringConverter<String> {

  public String convert(String value) {
    throw new UnsupportedOperationException();
  }
}
