package com.carrotsearch.console.jcommander.internal;

import com.carrotsearch.console.jcommander.ParameterException;
import com.carrotsearch.progresso.annotations.SuppressForbidden;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SuppressForbidden("sysout allowed.")
public class DefaultConsole implements Console {
  public void print(String msg) {
    System.out.print(msg);
  }

  public void println(String msg) {
    System.out.println(msg);
  }

  public char[] readPassword(boolean echoInput) {
    try {
      // Do not close the readers since System.in should not be closed
      InputStreamReader isr = new InputStreamReader(System.in);
      BufferedReader in = new BufferedReader(isr);
      String result = in.readLine();
      return result.toCharArray();
    } catch (IOException e) {
      throw new ParameterException(e);
    }
  }
}
