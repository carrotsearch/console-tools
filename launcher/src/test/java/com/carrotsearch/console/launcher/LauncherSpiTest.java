package com.carrotsearch.console.launcher;

import com.carrotsearch.console.jcommander.Parameter;
import com.carrotsearch.console.jcommander.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class LauncherSpiTest extends TestBase {
  @Parameters(commandNames = "cmd1", commandDescription = "Command 1 (SPI).")
  public static class Cmd1 extends Command<ExitCodes> {
    @Parameter(
        names = {"--opt1"},
        required = true)
    public String opt1;

    @Override
    public ExitCodes run() {
      return ExitCodes.SUCCESS;
    }
  }

  @Test
  public void testCommandDiscovery() {
    Assertions.assertThat(Launcher.lookupCommands().stream().map(c -> c.getClass().getName()))
        .containsOnly(Cmd1.class.getName());
  }
}
