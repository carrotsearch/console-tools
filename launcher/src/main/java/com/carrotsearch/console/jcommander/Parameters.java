/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation used to specify settings for parameter parsing.
 *
 * @author cbeust
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({TYPE})
@Inherited
public @interface Parameters {

  public static final String DEFAULT_OPTION_PREFIXES = "-";

  /** The name of the resource bundle to use for this class. */
  String resourceBundle() default "";

  /** The character(s) that separate options. */
  String separators() default " ";

  /** What characters an option starts with. */
  String optionPrefixes() default DEFAULT_OPTION_PREFIXES;

  /**
   * If the annotated class was added to {@link JCommander} as a command with {@link
   * JCommander#addCommand}, then this string will be displayed in the description when @{link
   * JCommander#usage} is invoked.
   */
  String commandDescription() default "";

  /**
   * @return the key used to find the command description in the resource bundle.
   */
  String commandDescriptionKey() default "";

  /** An array of allowed command names. */
  String[] commandNames() default {};

  /** If true, this command won't appear in the usage(). */
  boolean hidden() default false;
}
