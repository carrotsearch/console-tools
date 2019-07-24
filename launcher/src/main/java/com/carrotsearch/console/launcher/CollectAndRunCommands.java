/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.Parameter;
import com.carrotsearch.console.jcommander.ParameterException;
import com.carrotsearch.console.jcommander.Parameters;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.jar.JarFile;

@Parameters(
    commandNames = "search",
    commandDescription = "Search commands in one or more JAR files or directories.")
class CollectAndRunCommands implements Callable<ExitStatus> {
  private final List<String> commandArgs;

  @Parameter(description = "JAR files or directories")
  private List<String> locations = new ArrayList<String>();

  @Parameter(
      names = "--auto",
      description = "Adds the launcher's classpath location to lookup paths.")
  private boolean auto;

  @Parameter(names = "--min-jvm", description = "Minimum JVM version to require.")
  private String minJvmVersion = "1.8";

  public CollectAndRunCommands(List<String> commandArgs) {
    this.commandArgs = commandArgs;
  }

  @Override
  public ExitStatus call() throws Exception {
    if (minJvmVersion != null) {
      JavaVersion required = JavaVersion.parse(minJvmVersion);
      if (required == null) {
        throw new ParameterException("Invalid Java version: " + minJvmVersion);
      }

      JavaVersion platform = JavaVersion.get();
      if (!platform.atLeast(required)) {
        System.err.println(
            "Minimum JVM version required is " + minJvmVersion + ". This JVM is: " + platform);
        return ExitStatus.ERROR_OTHER;
      }
    }

    final Set<Path> commandJars = new LinkedHashSet<>();
    final Set<Path> scannedLocations = new HashSet<>();

    if (auto) {
      final URL jarLocation = getClass().getProtectionDomain().getCodeSource().getLocation();
      File location = new File(jarLocation.toURI()).getAbsoluteFile();
      if (location.isFile()) {
        location = location.getParentFile();
      }
      locations.add(location.getAbsolutePath());
    }

    if (locations.isEmpty()) {
      throw new ParameterException("At least one command lookup location is required.");
    }

    for (String location : locations) {
      Path path = Paths.get(location);
      scannedLocations.add(path.toAbsolutePath());

      Files.walkFileTree(
          path,
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
              if (file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                JarFile jarFile = new JarFile(file.toFile());
                try {
                  final String commandImpls =
                      "META-INF/services/" + ILauncherCommand.class.getName();
                  if (jarFile.getEntry(commandImpls) != null) {
                    commandJars.add(file);
                  }
                } finally {
                  jarFile.close();
                }
              }
              return FileVisitResult.CONTINUE;
            }
          });
    }

    if (commandJars.isEmpty()) {
      System.err.println("No launchable commands at any of these locations: ");
      for (Path p : scannedLocations) {
        System.err.println("  - " + p);
      }
      return ExitStatus.ERROR_INVALID_ARGUMENTS;
    }

    // Load command classes.
    List<ILauncherCommand> cmds = new ArrayList<>();
    ClassLoader parent = Thread.currentThread().getContextClassLoader();

    for (Path jarPath : commandJars) {
      URLClassLoader cl = new URLClassLoader(new URL[] {jarPath.toUri().toURL()}, parent);

      ServiceLoader<ILauncherCommand> loader = ServiceLoader.load(ILauncherCommand.class, cl);
      for (ILauncherCommand command : loader) {
        cmds.add(command);
      }
    }

    return Launcher.run(cmds, commandArgs.toArray(new String[commandArgs.size()]));
  }

  public static void main(String[] args) {
    Launcher.runSingleCommand(new CollectAndRunCommands(Collections.emptyList()), args);
  }
}
