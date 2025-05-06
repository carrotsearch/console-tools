package com.carrotsearch.console.formatters;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TabularOutputTest {
  @Test
  public void oneRow() {
    checkEquals(
        (sw) -> TabularOutput.to(sw).outputHeaders(false).addColumns("A", "B").build(),
        (to) -> {
          to.append("a", "b").nextRow();
        },
        "a b");
  }

  @Test
  public void changedWidth() {
    checkEquals(
        (sw) ->
            TabularOutput.to(sw)
                .outputHeaders(false)
                .columnSeparator("|")
                .addColumns("A", "B")
                .build(),
        (to) -> {
          to.append("a", "bb").nextRow();
          to.append("aa", "b").nextRow();
        },
        "a|bb",
        "aa|b ");
  }

  @Test
  public void headers() {
    checkEquals(
        (sw) -> TabularOutput.to(sw).columnSeparator("|").addColumns("A", "B").build(),
        (to) -> {
          to.append("a", "b").nextRow();
        },
        "A|B",
        "a|b");
  }

  @Test
  public void headersUpdateAfterSizedChanged() {
    checkEquals(
        (sw) -> TabularOutput.to(sw).columnSeparator("|").addColumns("A", "B").build(),
        (to) -> {
          to.append("a", "b").nextRow();
          to.append("aa", "bb").nextRow();
        },
        "A|B",
        "a|b",
        "A |B ",
        "aa|bb");
  }

  @Test
  public void noAutoflushAlignments() {
    checkEquals(
        (sw) ->
            TabularOutput.to(sw)
                .columnSeparator("|")
                .noAutoFlush()
                .addColumn("A", c -> c.alignLeft())
                .addColumn("B", c -> c.alignCenter())
                .addColumn("C", c -> c.alignRight())
                .build(),
        (to) -> {
          to.append("a", "b", "c").nextRow();
          to.append("aa", "bb", "cc").nextRow();
          to.append("aaa", "bbb", "ccc").nextRow();
          to.flush();
        },
        "A  | B |  C",
        "a  | b |  c",
        "aa |bb | cc",
        "aaa|bbb|ccc");
  }

  @Test
  public void customFormatter() {
    checkEquals(
        (sw) ->
            TabularOutput.to(sw)
                .columnSeparator("|")
                .noAutoFlush()
                .outputHeaders(false)
                .addColumn("A", c -> c.format("%6.2f"))
                .build(),
        (to) -> {
          to.append(1.2f).nextRow();
          to.flush();
        },
        "  1.20");
  }

  @Test
  public void headersLongerThanData() {
    checkEquals(
        (sw) ->
            TabularOutput.to(sw)
                .columnSeparator("|")
                .outputHeaders(true)
                .addColumn("AAA", c -> c.alignCenter())
                .build(),
        (to) -> {
          to.append("a").nextRow();
          to.flush();
        },
        "AAA",
        " a ");
  }

  private void checkEquals(
      Function<Writer, TabularOutput> configure,
      Consumer<TabularOutput> appender,
      String... expectedRows) {
    StringWriter sw = new StringWriter();
    TabularOutput to = configure.apply(sw);
    appender.accept(to);

    String expected = Arrays.stream(expectedRows).map(e -> e + "\n").collect(Collectors.joining());
    String actual = sw.toString();
    if (!Objects.equals(expected, actual)) {
      System.out.println("Expected:\n" + expected);
      System.out.println("Actual  :\n" + actual);
      Assertions.assertThat(actual).isEqualTo(expected);
    }
  }
}
