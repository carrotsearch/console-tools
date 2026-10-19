package com.carrotsearch.console.launcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Suppresses forbidden-apis checks on the annotated element. */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface SuppressForbidden {
  /** The reason the forbidden API is used. */
  String value();
}
