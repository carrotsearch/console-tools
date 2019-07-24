/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander;

public interface IValueValidator<T> {
  /**
   * Validate the parameter.
   *
   * @param name The name of the parameter (e.g. "-host").
   * @param value The value of the parameter that we need to validate
   * @throws ParameterException Thrown if the value of the parameter is invalid.
   */
  void validate(String name, T value) throws ParameterException;
}
