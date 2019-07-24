/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander;

public class Strings {
  public static boolean isStringEmpty(String s) {
    return s == null || "".equals(s);
  }
}
