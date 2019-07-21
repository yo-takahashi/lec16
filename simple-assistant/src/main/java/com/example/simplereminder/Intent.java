package com.example.simplereminder;

import lombok.AllArgsConstructor;

import java.util.EnumSet;
import java.util.regex.Pattern;

@AllArgsConstructor
public enum Intent {
  REMAINDER(Pattern.compile("^\\d{1,2}時\\d{1,2}分に.+$")),
  UNKNOWN(Pattern.compile(".+"));

  private Pattern pattern;

  public static Intent makeIntent(String text) {
    var set = EnumSet.allOf(Intent.class);
    return set.stream()
      .filter(i -> i.pattern.matcher(text).matches())
      .findFirst().orElse(UNKNOWN);
  }
}
