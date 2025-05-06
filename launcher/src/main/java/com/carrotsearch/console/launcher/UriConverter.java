package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.IStringConverter;
import com.carrotsearch.console.jcommander.IStringConverterFactory;
import com.carrotsearch.console.jcommander.ParameterException;
import java.net.URI;
import java.net.URISyntaxException;

public class UriConverter implements IStringConverter<URI>, IStringConverterFactory {
  @Override
  public URI convert(String value) {
    try {
      return new URI(value);
    } catch (URISyntaxException e) {
      throw new ParameterException("Invalid URI syntax: " + value);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public <T> Class<? extends IStringConverter<T>> getConverter(Class<T> forType) {
    if (forType.equals(URI.class)) {
      return (Class) UriConverter.class;
    }
    return null;
  }
}
