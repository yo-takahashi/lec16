package com.example.simplereminder;

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
    var sut = new CallBack(Collections.synchronizedSet(new HashSet<>()));
    var ui = new UserIntent("1234", Intent.UNKNOWN, "こんにちは");
    var msg = (TextMessage) sut.handleAction(ui);
    var actual = msg.getText();
    Assertions.assertEquals("リマインダを設定したい時間と要件を送信してください\n" +
      "例）10時20分に授業", actual);
  }

  @DisplayName("handleActionはリマインダーインテントに確認メッセージを返す")
  @Test
  void handleAction02() {
    var sut = new CallBack(Collections.synchronizedSet(new HashSet<>()));
    var ui = new UserIntent("1234", Intent.REMAINDER, "10時20分に授業");
    var msg = (TemplateMessage) sut.handleAction(ui);
    var template = (ConfirmTemplate) msg.getTemplate();
    var actual = template.getText();
    Assertions.assertEquals("10時20分に授業をリマインダーしますか？", actual);
  }


  @DisplayName("handleActionはリマインダー確認中に別メッセージがくれば、復帰する")
  @Test
  void handleAction03() {
    var set = new HashSet<UserIntent>();
    set.add(new UserIntent("1234", Intent.REMAINDER, "10時20分に授業"));
    var sut = new CallBack(Collections.synchronizedSet(set));
    var ui = new UserIntent("1234", Intent.UNKNOWN, "こんにちは");
    var msg = (TemplateMessage) sut.handleAction(ui);
    var template = (ConfirmTemplate) msg.getTemplate();
    var actual = template.getText();
    Assertions.assertEquals("10時20分に授業をリマインダーしますか？", actual);
  }

}
