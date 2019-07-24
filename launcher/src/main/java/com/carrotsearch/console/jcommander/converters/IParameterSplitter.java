/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander.converters;

import java.util.List;

/**
 * Convert a string representing several parameters (e.g. "a,b,c" or "d/e/f") into a list of
 * arguments ([a,b,c] and [d,e,f]).
 */
public interface IParameterSplitter {
  List<String> split(String value);
}
