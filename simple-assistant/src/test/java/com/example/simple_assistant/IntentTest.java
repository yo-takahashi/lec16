package com.example.simple_assistant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class IntentTest {

  @DisplayName("IntentがUNKNOWNを返す")
  @Test
  void makeIntentReturnsUNKNOWN01() {
    var actual = Intent.makeIntent("こんにちは");
    assertEquals(Intent.UNKNOWN, actual);
  }

  @DisplayName("Intentが〈10時20分に授業〉でREMAINDERを返す")
  @Test
  void makeIntentReturnsREMINDER01() {
    var actual = Intent.makeIntent("10時20分に授業");
    assertEquals(Intent.REMINDER, actual);
  }

  @DisplayName("Intentが〈10時20分に〉でUNKNOWNを返す")
  @Test
  void makeIntentReturnsUNKNOWN02() {
    var actual = Intent.makeIntent("10時20分に");
    assertEquals(Intent.UNKNOWN, actual);
  }

  @DisplayName("Intentは用件が32文字以上であればUNKNOWNを返す")
  @Test
  void makeIntentReturnsUNKNOWN03() {
    var actual = Intent.makeIntent("10時20分に1234567890123456789012345678901234567890");
    assertEquals(Intent.UNKNOWN, actual);
  }

  @DisplayName("Intentが〈10時20分に授業〉で10, 20, 授業を返す")
  @Test
  void makeIntentReturnsREMINDER02() {
    var msg = "10時20分に授業";
    var intent = Intent.makeIntent(msg);
    var actual = intent.getGroups(msg);
    assertLinesMatch(actual, List.of("10", "20", "授業"));
  }

}
