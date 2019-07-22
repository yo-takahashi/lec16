package com.example.simple_assistant.c;

import com.example.simple_assistant.m.ReminderRepository;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class Push {

  private LineMessagingClient client;
  private ReminderRepository repository;

  @Autowired
  public Push(LineMessagingClient client, ReminderRepository repository) {
    this.client = client;
    this.repository = repository;
  }

  @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Tokyo")
  public void pushTimeTone() {
    log.info("try push...");
    var time = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
    var remainderItems = repository.select(time);
    remainderItems.stream()
      .map(ri -> new PushMessage(ri.getUserId(), new TextMessage(ri.getPushText() + "の時間です！")))
      .forEach(this::push);
    repository.delete(time);
  }

  private void push(PushMessage pMsg) {
    try {
      var resp = client.pushMessage(pMsg).get();
      log.info("Sent messages: {}", resp);
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }
}
