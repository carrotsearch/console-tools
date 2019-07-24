/*
 * console-tools
 *
 * Copyright (C) 2019, Carrot Search s.c.
 * All rights reserved.
 */
package com.carrotsearch.console.launcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Locale;

public class PrintStreamDelegationWriter extends PrintWriter {
  /**
   * We do *not* want the {@link PrintWriter} to actually use any delegate methods - we should be
   * handling everything ourselves.
   */
  private static class FailOutputWriter extends Writer {
    @Override
    public void flush() throws IOException {
      notReachable();
    }

    @Override
    public void close() throws IOException {
      notReachable();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
      notReachable();
    }
  }

  private final PrintStream delegate;

  public PrintStreamDelegationWriter(PrintStream out) throws FileNotFoundException {
    super(new FailOutputWriter());
    this.delegate = out;
  }

  @Override
  public void flush() {
    delegate.flush();
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public boolean checkError() {
    throw notReachable();
  }

  @Override
  protected void setError() {
    notReachable();
  }

  @Override
  protected void clearError() {
    notReachable();
  }

  @Override
  public void write(int c) {
    delegate.append((char) c);
  }

  @Override
  public void write(char[] buf, int off, int len) {
    delegate.print(Arrays.copyOfRange(buf, off, off + len));
  }

  @Override
  public void write(char[] buf) {
    delegate.print(buf);
  }

  @Override
  public void write(String s, int off, int len) {
    delegate.append(s, off, off + len);
  }

  @Override
  public void write(String s) {
    delegate.print(s);
  }

  @Override
  public void print(boolean b) {
    delegate.print(b);
  }

  @Override
  public void print(char c) {
    delegate.print(c);
  }

  @Override
  public void print(int i) {
    delegate.print(i);
  }

  @Override
  public void print(long l) {
    delegate.print(l);
  }

  @Override
  public void print(float f) {
    delegate.print(f);
  }

  @Override
  public void print(double d) {
    delegate.print(d);
  }

  @Override
  public void print(char[] s) {
    delegate.print(s);
  }

  @Override
  public void print(String s) {
    delegate.print(s);
  }

  @Override
  public void print(Object obj) {
    delegate.print(obj);
  }

  @Override
  public void println() {
    delegate.println();
  }

  @Override
  public void println(boolean x) {
    delegate.println(x);
  }

  @Override
  public void println(char x) {
    delegate.println(x);
  }

  @Override
  public void println(int x) {
    delegate.println(x);
  }

  @Override
  public void println(long x) {
    delegate.println(x);
  }

  @Override
  public void println(float x) {
    delegate.println(x);
  }

  @Override
  public void println(double x) {
    delegate.println(x);
  }

  @Override
  public void println(char[] x) {
    delegate.println(x);
  }

  @Override
  public void println(String x) {
    delegate.println(x);
  }

  @Override
  public void println(Object x) {
    delegate.println(x);
  }

  @Override
  public PrintWriter printf(String format, Object... args) {
    throw new RuntimeException("Do not use locale-sensitive printf()");
  }

  @Override
  public PrintWriter printf(Locale l, String format, Object... args) {
    delegate.printf(l, format, args);
    return this;
  }

  @Override
  public PrintWriter format(String format, Object... args) {
    throw new RuntimeException("Do not use locale-sensitive printf()");
  }

  @Override
  public PrintWriter format(Locale l, String format, Object... args) {
    delegate.format(l, format, args);
    return this;
  }

  @Override
  public PrintWriter append(CharSequence csq) {
    delegate.append(csq);
    return this;
  }

  @Override
  public PrintWriter append(CharSequence csq, int start, int end) {
    delegate.append(csq, start, end);
    return this;
  }

  @Override
  public PrintWriter append(char c) {
    delegate.append(c);
    return this;
  }

  static RuntimeException notReachable() {
    throw new RuntimeException("Bad delegation somewhere. Shouldn't be reachable.");
  }
}
