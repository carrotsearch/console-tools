/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.DynamicParameter;
import java.util.HashMap;
import java.util.Map;

public final class SysPropertiesParameters {
  @DynamicParameter(
      names = "-D",
      description = "Sets a system property to a given value (JVM syntax).")
  public Map<String, String> sysProperties = new HashMap<>();
}
