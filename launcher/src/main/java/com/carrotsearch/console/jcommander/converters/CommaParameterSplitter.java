/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander.converters;

import java.util.Arrays;
import java.util.List;

public class CommaParameterSplitter implements IParameterSplitter {

  public List<String> split(String value) {
    return Arrays.asList(value.split(","));
  }
}
