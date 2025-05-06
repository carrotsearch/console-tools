package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.IStringConverter;
import com.carrotsearch.console.jcommander.IStringConverterFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathConverter implements IStringConverter<Path>, IStringConverterFactory {
  @Override
  public Path convert(String value) {
    return Paths.get(value);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public <T> Class<? extends IStringConverter<T>> getConverter(Class<T> forType) {
    if (forType.equals(Path.class)) {
      return (Class) PathConverter.class;
    }
    return null;
  }
}
