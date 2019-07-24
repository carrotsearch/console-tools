/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import java.util.concurrent.Callable;

public interface ILauncherCommand extends Callable<ExitStatus> {}
