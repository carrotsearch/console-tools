/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import java.security.AccessController;
import java.security.PrivilegedAction;

/** Copied from commons-lang, with changes. */
public enum JavaVersion {
  JAVA_1_1(1.1f, "1.1"),
  JAVA_1_2(1.2f, "1.2"),
  JAVA_1_3(1.3f, "1.3"),
  JAVA_1_4(1.4f, "1.4"),
  JAVA_1_5(1.5f, "1.5"),
  JAVA_1_6(1.6f, "1.6"),
  JAVA_1_7(1.7f, "1.7"),
  JAVA_1_8(1.8f, "1.8"),

  JAVA_9(9f, "9"),
  JAVA_10(10f, "10"),
  JAVA_11(11f, "11"),
  JAVA_12(12f, "12"),
  JAVA_13(13f, "13");

  private static final JavaVersion JAVA_LATEST_SUPPORTED = JAVA_13;

  /** Compat. ordering. */
  private final float value;

  /** The standard name. */
  private final String name;

  /**
   * Constructor.
   *
   * @param value the float value
   * @param name the standard name, not null
   */
  JavaVersion(final float value, final String name) {
    this.value = value;
    this.name = name;
  }

  /**
   * Whether this version of Java is at least the version of Java passed in.
   *
   * <p>For example:<br>
   * {@code myVersion.atLeast(JavaVersion.JAVA_1_4)}
   *
   * <p>
   *
   * @param requiredVersion the version to check against, not null
   * @return true if this version is equal to or greater than the specified version
   */
  public boolean atLeast(final JavaVersion requiredVersion) {
    return this.value >= requiredVersion.value;
  }

  /**
   * Transforms the given string with a Java version number to the corresponding constant of this
   * enumeration class. This method is used internally.
   *
   * @return the corresponding enumeration constant or <b>null</b> if the version is unknown
   */
  public static JavaVersion get() {
    return AccessController.doPrivileged(
        new PrivilegedAction<JavaVersion>() {
          @Override
          public JavaVersion run() {
            return parse(System.getProperty("java.specification.version"));
          }
        });
  }

  protected static JavaVersion parse(String ver) {
    if ("1.1".equals(ver)) {
      return JAVA_1_1;
    } else if ("1.2".equals(ver)) {
      return JAVA_1_2;
    } else if ("1.3".equals(ver)) {
      return JAVA_1_3;
    } else if ("1.4".equals(ver)) {
      return JAVA_1_4;
    } else if ("1.5".equals(ver)) {
      return JAVA_1_5;
    } else if ("1.6".equals(ver)) {
      return JAVA_1_6;
    } else if ("1.7".equals(ver)) {
      return JAVA_1_7;
    } else if ("1.8".equals(ver) || "8".equals(ver)) {
      return JAVA_1_8;
    } else if ("1.9".equals(ver) || "9".equals(ver)) {
      return JAVA_9;
    } else if ("10".equals(ver)) {
      return JAVA_10;
    } else if ("11".equals(ver)) {
      return JAVA_11;
    } else if ("12".equals(ver)) {
      return JAVA_12;
    } else if ("13".equals(ver)) {
      return JAVA_13;
    } else {
      // TODO: L4G-1168: use JEP 223 to parse version string?
      throw new RuntimeException(
          "Java version not supported (support up to " + JAVA_LATEST_SUPPORTED + "): " + ver);
    }
  }

  // -----------------------------------------------------------------------
  /**
   * The string value is overridden to return the standard name.
   *
   * <p>For example, <code>"1.5"</code>.
   *
   * @return the name, not null
   */
  @Override
  public String toString() {
    return name;
  }
}
