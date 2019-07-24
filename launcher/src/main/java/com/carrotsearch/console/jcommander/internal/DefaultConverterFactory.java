/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander.internal;

import com.carrotsearch.console.jcommander.IStringConverter;
import com.carrotsearch.console.jcommander.IStringConverterFactory;
import com.carrotsearch.console.jcommander.converters.BigDecimalConverter;
import com.carrotsearch.console.jcommander.converters.BooleanConverter;
import com.carrotsearch.console.jcommander.converters.DoubleConverter;
import com.carrotsearch.console.jcommander.converters.FileConverter;
import com.carrotsearch.console.jcommander.converters.FloatConverter;
import com.carrotsearch.console.jcommander.converters.IntegerConverter;
import com.carrotsearch.console.jcommander.converters.LongConverter;
import com.carrotsearch.console.jcommander.converters.StringConverter;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class DefaultConverterFactory implements IStringConverterFactory {
  /** A map of converters per class. */
  private static Map<Class<?>, Class<? extends IStringConverter<?>>> m_classConverters;

  static {
    m_classConverters = new HashMap();
    m_classConverters.put(String.class, StringConverter.class);
    m_classConverters.put(Integer.class, IntegerConverter.class);
    m_classConverters.put(int.class, IntegerConverter.class);
    m_classConverters.put(Long.class, LongConverter.class);
    m_classConverters.put(long.class, LongConverter.class);
    m_classConverters.put(Float.class, FloatConverter.class);
    m_classConverters.put(float.class, FloatConverter.class);
    m_classConverters.put(Double.class, DoubleConverter.class);
    m_classConverters.put(double.class, DoubleConverter.class);
    m_classConverters.put(Boolean.class, BooleanConverter.class);
    m_classConverters.put(boolean.class, BooleanConverter.class);
    m_classConverters.put(File.class, FileConverter.class);
    m_classConverters.put(BigDecimal.class, BigDecimalConverter.class);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Class<? extends IStringConverter<?>> getConverter(Class forType) {
    return m_classConverters.get(forType);
  }
}
