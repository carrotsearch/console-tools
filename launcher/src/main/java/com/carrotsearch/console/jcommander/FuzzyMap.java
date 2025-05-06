package com.carrotsearch.console.jcommander;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Helper class to perform fuzzy key look ups: looking up case insensitive or abbreviated keys. */
public class FuzzyMap {
  interface IKey {
    String getName();
  }

  public static <V> V findInMap(
      Map<? extends IKey, V> map, IKey name, boolean caseSensitive, boolean allowAbbreviations) {
    if (allowAbbreviations) {
      return findAbbreviatedValue(map, name, caseSensitive);
    } else {
      if (caseSensitive) {
        return map.get(name);
      } else {
        for (IKey c : map.keySet()) {
          if (c.getName().equalsIgnoreCase(name.getName())) {
            return map.get(c);
          }
        }
      }
    }
    return null;
  }

  private static <V> V findAbbreviatedValue(
      Map<? extends IKey, V> map, IKey name, boolean caseSensitive) {
    String string = name.getName();
    Map<String, V> results = new HashMap<String, V>();
    for (IKey c : map.keySet()) {
      String n = c.getName();
      boolean match =
          (caseSensitive && n.startsWith(string))
              || ((!caseSensitive)
                  && n.toLowerCase(Locale.ROOT).startsWith(string.toLowerCase(Locale.ROOT)));
      if (match) {
        results.put(n, map.get(c));
      }
    }

    V result;
    if (results.size() > 1) {
      throw new ParameterException("Ambiguous option: " + name + " matches " + results.keySet());
    } else if (results.size() == 1) {
      result = results.values().iterator().next();
    } else {
      result = null;
    }

    return result;
  }
}
