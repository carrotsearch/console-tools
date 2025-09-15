package com.carrotsearch.console.jcommander;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Java port of argument-file specification <a
 * href="https://docs.oracle.com/en/java/javase/21/docs/specs/man/java.html#java-command-line-argument-files">supported
 * by the JDK</a>:
 *
 * <ul>
 *   <li>Whitespace delimits tokens (outside of quotes)
 *   <li>Single and double quotes group text; quotes are not included in the result
 *   <li>Inside quotes, backslash introduces escapes: \n, \r, \t, \f, or any other char literally
 *   <li>A backslash followed by a newline or carriage return means line-concatenation; subsequent
 *       leading whitespace is skipped (still "in quote")
 *   <li>'#' starts a comment to end-of-line unless inside quotes
 * </ul>
 */
final class ArgFileParser {
  private ArgFileParser() {}

  private enum State {
    FIND_NEXT,
    IN_COMMENT,
    IN_QUOTE,
    IN_ESCAPE,
    SKIP_LEAD_WS,
    IN_TOKEN
  }

  /** Expand the given argfile into a list of arguments, using UTF-8. */
  public static List<String> expandArgFile(Path path) {
    return expandArgFile(path, StandardCharsets.UTF_8);
  }

  /** Expand the given argfile into a list of arguments, using the provided charset. */
  public static List<String> expandArgFile(Path path, Charset charset) {
    try {
      BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
      if (!attrs.isRegularFile()) {
        throw new ParameterException("Cannot open arg file: " + path);
      }

      return tokenize(Files.readString(path, charset));
    } catch (IOException e) {
      throw new ParameterException("Could not read arg file: " + path + ": " + e.getMessage());
    }
  }

  static List<String> tokenize(String s) {
    List<String> args = new ArrayList<>();

    State state = State.FIND_NEXT;
    StringBuilder token = new StringBuilder();
    char quoteChar = '"';

    final int n = s.length();
    int i = 0;

    while (i < n) {
      char ch = s.charAt(i);

      // Handle states that consume runs of whitespace immediately
      if (state == State.FIND_NEXT || state == State.SKIP_LEAD_WS) {
        // Skip whitespace
        while (i < n) {
          ch = s.charAt(i);
          if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '\f') {
            i++;
          } else {
            break;
          }
        }
        if (i >= n) break;
        // Enter token or continue quote after concat
        state = (state == State.FIND_NEXT) ? State.IN_TOKEN : State.IN_QUOTE;
        continue; // re-evaluate current char in new state
      }

      if (state == State.IN_COMMENT) {
        // Skip to end-of-line
        while (i < n) {
          ch = s.charAt(i++);
          if (ch == '\n' || ch == '\r') break;
        }
        state = State.FIND_NEXT;
        continue;
      }

      if (state == State.IN_ESCAPE) {
        // In-quote escape handling (concat or escaped char)
        if (ch == '\n' || ch == '\r') {
          // Line concatenation: resume quote after skipping leading WS
          state = State.SKIP_LEAD_WS;
          i++; // consume EOL
        } else {
          token.append(translateEscape(ch));
          state = State.IN_QUOTE;
          i++; // consume escaped char
        }
        continue;
      }

      // Normal switch on current char
      switch (ch) {
        case ' ':
        case '\t':
        case '\f':
          if (state == State.IN_QUOTE) {
            token.append(ch);
            i++;
            break;
          }
          // end of token
          args.add(token.toString());
          token.setLength(0);
          state = State.FIND_NEXT;
          i++;
          break;

        case '\n':
        case '\r':
          if (state == State.IN_QUOTE) {
            token.append(ch);
            i++;
            break;
          }
          args.add(token.toString());
          token.setLength(0);
          state = State.FIND_NEXT;
          i++;
          break;

        case '#':
          if (state == State.IN_QUOTE) {
            token.append('#');
            i++;
            break;
          }

          if (!token.isEmpty()) {
            args.add(token.toString());
            token.setLength(0);
          }
          state = State.IN_COMMENT;
          i++; // consume '#'
          break;

        case '\\':
          if (state == State.IN_QUOTE) {
            // Enter escape mode inside quotes
            state = State.IN_ESCAPE;
            i++; // consume '\'
          } else {
            // Outside quotes: literal backslash
            token.append('\\');
            i++;
          }
          break;

        case '\'':
        case '"':
          if (state == State.IN_QUOTE) {
            if (ch == quoteChar) {
              // close quote
              state = State.IN_TOKEN;
              i++; // consume quote
            } else {
              // different quote inside quote → literal
              token.append(ch);
              i++;
            }
          } else { // IN_TOKEN
            // open quote
            quoteChar = ch;
            state = State.IN_QUOTE;
            i++; // consume quote
          }
          break;

        default:
          token.append(ch);
          i++;
          break;
      }
    }

    // End of input: emit remaining partial token if any (even if IN_QUOTE).
    if (state == State.IN_TOKEN || state == State.IN_QUOTE) {
      if (!token.isEmpty()) {
        args.add(token.toString());
        token.setLength(0);
      }
    }

    return args;
  }

  private static char translateEscape(char ch) {
    return switch (ch) {
      case 'n' -> '\n';
      case 'r' -> '\r';
      case 't' -> '\t';
      case 'f' -> '\f';
      default -> ch;
    };
  }
}
