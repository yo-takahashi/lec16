package com.example.simplereminder;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@LineMessageHandler
public class CallBack {

  @EventMapping
  public Message handleMessage(MessageEvent<TextMessageContent> event) {
    var tmc = event.getMessage();
    var text = tmc.getText();

    
  }

}
