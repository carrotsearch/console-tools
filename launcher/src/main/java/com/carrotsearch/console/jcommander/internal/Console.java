/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander.internal;

public interface Console {

  void print(String msg);

  void println(String msg);

  char[] readPassword(boolean echoInput);
}
