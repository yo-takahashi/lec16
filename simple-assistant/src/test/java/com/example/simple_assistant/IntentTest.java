package com.example.simple_assistant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntentTest {

  @DisplayName("IntentがUNKNOWNを返す")
  @Test
  void makeIntentReturnsUNKNOWN01() {
    var actual = Intent.makeIntent("こんにちは");
    assertEquals(Intent.UNKNOWN, actual);
  }

  @DisplayName("Intentが〈10時20分に授業〉でREMAINDERを返す")
  @Test
  void makeIntentReturnsREMINDER() {
    var actual = Intent.makeIntent("10時20分に授業");
    assertEquals(Intent.REMINDER, actual);
  }

  @DisplayName("Intentが〈10時20分に〉でREMAINDERを返す")
  @Test
  void makeIntentReturnsUNKNOWN02() {
    var actual = Intent.makeIntent("10時20分に");
    assertEquals(Intent.UNKNOWN, actual);
  }

  @DisplayName("Intentは用件が32文字以上でえあればUNKNOWNを返す")
  @Test
  void makeIntentReturnsUNKNOWN03() {
    var actual = Intent.makeIntent("10時20分に1234567890123456789012345678901234567890");
    assertEquals(Intent.UNKNOWN, actual);
  }
}
