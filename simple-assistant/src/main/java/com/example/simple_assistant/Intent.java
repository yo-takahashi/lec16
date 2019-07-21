package com.example.simple_assistant;

import lombok.AllArgsConstructor;

import java.util.EnumSet;
import java.util.regex.Pattern;

// ユーザーとのやりとり状態を定義するenum
@AllArgsConstructor
public enum Intent {

  // メッセージの正規表現パターンに対応するやりとり状態の定義
  REMAINDER(Pattern.compile("^\\d{1,2}時\\d{1,2}分に.+$")),
  UNKNOWN(Pattern.compile(".+"));

  private Pattern pattern;

  // メッセージからやりとり状態を判断
  public static Intent makeIntent(String text) {
    var set = EnumSet.allOf(Intent.class);
    return set.stream()
      .filter(i -> i.pattern.matcher(text).matches())
      .findFirst().orElse(UNKNOWN);
  }
}
