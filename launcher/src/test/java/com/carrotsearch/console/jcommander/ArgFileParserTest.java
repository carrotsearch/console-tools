package com.carrotsearch.console.jcommander;

import com.carrotsearch.console.launcher.TestBase;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ArgFileParserTest extends TestBase {
  @Test
  public void testSimpleCases() {
    Assertions.assertThat(
            ArgFileParser.tokenize(
                """
                --foo --bar
                  --baz\s\s
                """))
        .containsExactly("--foo", "--bar", "--baz");
  }

  @Test
  public void testComments() {
    Assertions.assertThat(
            ArgFileParser.tokenize(
                """
                # comment
                --foo
                # comment
                --bar
                """))
        .containsExactly("--foo", "--bar");
  }

  @Test
  public void testQuoting() {
    Assertions.assertThat(
            ArgFileParser.tokenize(
                """
                "longer argument with \\"quotes\\""
                """))
        .containsExactly("longer argument with \"quotes\"");
  }
}
