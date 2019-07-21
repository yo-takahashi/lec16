package com.example.simple_assistant.c;

import com.example.simple_assistant.Intent;
import com.example.simple_assistant.m.UserIntent;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;

public class CallBackTest {

  @DisplayName("handleActionはIntentに反応しないメッセージに説明を返す")
  @Test
  void handleAction01() {
    var mock = new CallBack(Collections.synchronizedSet(new HashSet<>()), null);
    var ui = new UserIntent("1234", Intent.UNKNOWN, "こんにちは");
    var msg = (TextMessage) mock.handleIntent(ui);
    var actual = msg.getText();
    Assertions.assertEquals("リマインダを設定したい時分と用件（32文字まで）を送信してください\n" +
      "例）10時20分に授業", actual);
  }

  @DisplayName("handleActionはリマインダーインテントに確認メッセージを返す")
  @Test
  void handleAction02() {
    var mock = new CallBack(Collections.synchronizedSet(new HashSet<>()), null);
    var ui = new UserIntent("1234", Intent.REMINDER, "10時20分に授業");
    var msg = (TemplateMessage) mock.handleIntent(ui);
    var template = (ConfirmTemplate) msg.getTemplate();
    var actual = template.getText();
    Assertions.assertEquals("10時20分に授業をリマインダーしますか？", actual);
  }


  @DisplayName("handleActionはリマインダー確認中に別メッセージがくれば復帰する")
  @Test
  void handleAction03() {
    var mock = new HashSet<UserIntent>();
    mock.add(new UserIntent("1234", Intent.REMINDER, "10時20分に授業"));
    var sut = new CallBack(Collections.synchronizedSet(mock), null);
    var ui = new UserIntent("1234", Intent.UNKNOWN, "こんにちは");
    var msg = (TemplateMessage) sut.handleIntent(ui);
    var template = (ConfirmTemplate) msg.getTemplate();
    var actual = template.getText();
    Assertions.assertEquals("10時20分に授業をリマインダーしますか？", actual);
  }

}
