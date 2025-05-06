package com.carrotsearch.console.jcommander.converters;

import com.carrotsearch.console.jcommander.IStringConverter;

/**
 * Base class for converters that stores the name of the option.
 *
 * @author cbeust
 */
public abstract class BaseConverter<T> implements IStringConverter<T> {

  private String m_optionName;

  public BaseConverter(String optionName) {
    m_optionName = optionName;
  }

  public String getOptionName() {
    return m_optionName;
  }

  protected String getErrorString(String value, String to) {
    return "\"" + getOptionName() + "\": couldn't convert \"" + value + "\" to " + to;
  }
}
