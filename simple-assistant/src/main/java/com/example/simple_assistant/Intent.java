package com.example.simple_assistant;

import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// ユーザーとのやりとり状態を定義するenum
@AllArgsConstructor
public enum Intent {

  // メッセージの正規表現パターンに対応するやりとり状態の定義
  REMINDER(Pattern.compile("^(\\d{1,2})時(\\d{1,2})分に(.{1,32})$")),
  UNKNOWN(Pattern.compile(".+"));

  private Pattern pattern;

  // メッセージからやりとり状態を判断
  public static Intent makeIntent(String text) {
    var set = EnumSet.allOf(Intent.class);
    return set.stream()
      .filter(i -> i.pattern.matcher(text).matches())
      .findFirst().orElse(UNKNOWN);
  }

  // メッセージから時・分・用件を抜き出す
  public List<String> getGroups(String text) {
    var matcher = pattern.matcher(text);
    if (!Objects.equals(this, UNKNOWN)) {
      int n = matcher.groupCount();
      if (matcher.matches()) {
        return IntStream.rangeClosed(1, n)
          .mapToObj(matcher::group)
          .collect(Collectors.toUnmodifiableList());
      }
    }
    return Collections.emptyList();
  }

}
