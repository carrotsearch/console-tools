/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander.converters;

import com.carrotsearch.console.jcommander.IStringConverter;

/**
 * Default converter for strings.
 *
 * @author cbeust
 */
public class StringConverter implements IStringConverter<String> {

  public String convert(String value) {
    return value;
  }
}
