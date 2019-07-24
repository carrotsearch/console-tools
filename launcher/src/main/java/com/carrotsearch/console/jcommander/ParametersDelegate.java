/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.jcommander;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * When applied to a field all of its child fields annotated with {@link Parameter} will be included
 * during arguments parsing.
 *
 * <p>Mainly useful when creating complex command based CLI interfaces, where several commands can
 * share a set of arguments, but using object inheritance is not enough, due to
 * no-multiple-inheritance restriction. Using {@link ParametersDelegate} any number of command sets
 * can be shared by using composition pattern.
 *
 * <p>Delegations can be chained (nested).
 *
 * @author rodionmoiseev
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({FIELD})
public @interface ParametersDelegate {}
