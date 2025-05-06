package com.carrotsearch.console.jcommander.validators;

import com.carrotsearch.console.jcommander.IValueValidator;
import com.carrotsearch.console.jcommander.ParameterException;

/**
 * This is the default value of the validateValueWith attribute.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class NoValueValidator<T> implements IValueValidator<T> {

  public void validate(String parameterName, T parameterValue) throws ParameterException {}
}
