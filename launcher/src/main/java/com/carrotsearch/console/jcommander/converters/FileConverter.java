/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander.converters;

import com.carrotsearch.console.jcommander.IStringConverter;
import java.io.File;

/**
 * Convert a string into a file.
 *
 * @author cbeust
 */
public class FileConverter implements IStringConverter<File> {

  public File convert(String value) {
    return new File(value);
  }
}
