/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander.validators;

import com.carrotsearch.console.jcommander.IParameterValidator;
import com.carrotsearch.console.jcommander.ParameterException;

/**
 * This is the default value of the validateWith attribute.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class NoValidator implements IParameterValidator {

  public void validate(String parameterName, String parameterValue) throws ParameterException {}
}
