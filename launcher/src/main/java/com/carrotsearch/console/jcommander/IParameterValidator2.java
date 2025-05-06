package com.carrotsearch.console.jcommander;

public interface IParameterValidator2 extends IParameterValidator {

  /**
   * Validate the parameter.
   *
   * @param name The name of the parameter (e.g. "-host").
   * @param value The value of the parameter that we need to validate
   * @param pd The description of this parameter
   * @throws ParameterException Thrown if the value of the parameter is invalid.
   */
  void validate(String name, String value, ParameterDescription pd) throws ParameterException;
}
