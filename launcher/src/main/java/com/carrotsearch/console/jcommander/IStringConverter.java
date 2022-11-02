/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander;

/**
 * An interface that converts strings to any arbitrary type.
 *
 * <p>If your class implements a constructor that takes a String, this constructor will be used to
 * instantiate your converter and the parameter will receive the name of the option that's being
 * parsed, which can be useful to issue a more useful error message if the conversion fails.
 *
 * <p>You can also extend BaseConverter to make your life easier.
 *
 * @author cbeust
 */
public interface IStringConverter<T> {
  /**
   * @return an object of type <T> created from the parameter value.
   */
  T convert(String value);
}
