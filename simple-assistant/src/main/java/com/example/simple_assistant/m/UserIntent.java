package com.example.simple_assistant.m;

import com.example.simple_assistant.Intent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Objects;

// あるユーザーの状態とメッセージを記録しておく
@Value
@AllArgsConstructor
public class UserIntent {

  private String userId;
  private Intent intent;
  private String text;

  public UserIntent(MessageEvent<TextMessageContent> event) {
    this.userId = event.getSource().getUserId();
    this.text = event.getMessage().getText();
    this.intent = Intent.makeIntent(text);
  }

  public UserIntent(PostbackEvent event) {
    this.userId = event.getSource().getUserId();
    this.text = event.getPostbackContent().getData();
    this.intent = Intent.makeIntent(text);
  }

  public boolean contains(String userId) {
    return Objects.equals(this.userId, userId);
  }

  public boolean contains(Intent intent) {
    return Objects.equals(this.intent, intent);
  }

}
