/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander;

/**
 * A factory for IStringConverter. This interface lets you specify your converters in one place
 * instead of having them repeated all over your argument classes.
 *
 * @author cbeust
 */
public interface IStringConverterFactory {
  <T> Class<? extends IStringConverter<T>> getConverter(Class<T> forType);
}
