package com.example.simplereminder;

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
    assertEquals(Intent.REMAINDER, actual);
  }

  @DisplayName("Intentが〈10時20分に〉授業でREMAINDERを返す")
  @Test
  void makeIntentReturnsUNKNOWN02() {
    var actual = Intent.makeIntent("10時20分に");
    assertEquals(Intent.UNKNOWN, actual);
  }
}
