package com.example.simple_assistant.c;

import com.example.simple_assistant.Intent;
import com.example.simple_assistant.m.ReminderItem;
import com.example.simple_assistant.m.ReminderRepository;
import com.example.simple_assistant.m.UserIntent;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;

import java.util.*;

@LineMessageHandler
public class CallBack {

  // ユーザごとの処理状態を記録する
  private Set<UserIntent> userIntents;

  // データベースにアクセスする
  private ReminderRepository repos;

  @Autowired
  public CallBack(Set<UserIntent> userIntents, ReminderRepository repos) {
    this.userIntents = userIntents;
    this.repos = repos;
  }

  // 起動時にuserIntentsを初期化する
  @Bean
  public Set<UserIntent> userIntents() {
    return Collections.synchronizedSet(new HashSet<>());
  }

  // MessageEventに対応する
  @EventMapping
  public Message handleMessage(MessageEvent<TextMessageContent> event) {
    var userIntent = new UserIntent(event);
    return handleIntent(userIntent);
  }

  // PostBackEventに対応する
  @EventMapping
  public Message handlePostBack(PostbackEvent event) {
    var userIntent = new UserIntent(event);
    return handleProc(userIntent);
  }

  // 通常状態で返すメッセージのハンドリングをする
  Message handleIntent(UserIntent userIntent) {
    Message msg = getUnknownReaMsg();
    var intent = userIntent.getIntent();
    switch (intent) {
      case REMINDER:
        upsertUserIntent(userIntent);
        msg = getRemainderConfirmMsg(userIntent.getText());
        break;
      case UNKNOWN:
      default:
        // 確認画面(REMINDER)の段階で想定外な通常呼び出し(UNKNOWN)があれば、再帰的に再確認する
        msg = getUserIntent(userIntent.getUserId(), Intent.REMINDER)
          .map(this::handleIntent)
          .orElse(msg);
    }
    return msg;
  }

  // 確認画面で返すメッセージおよびデータベース記録処理のハンドリングをする
  Message handleProc(UserIntent userIntent) {
    TextMessage msg = new TextMessage("中断しました");
    switch (userIntent.getText()) {
      case "RY":
        try {
          // リマインダ情報が入った(REMINDER)のUserIntentを取り出す
          var previous = getUserIntent(userIntent.getUserId(), Intent.REMINDER)
            .orElseThrow();
          var item = new ReminderItem(previous);
          repos.insert(item);
          msg = new TextMessage("リマインダーを登録しました");
        } catch (DataAccessException e) {
          e.printStackTrace();
          msg = new TextMessage("データベースの登録に失敗しました");
        } catch (NoSuchElementException e) {
          e.printStackTrace();
          msg = new TextMessage("期限切れのため、もう一度最初からやりなおしてください");
        }
        break;
      case "RN":
      default:
        break;
    }
    upsertUserIntent(userIntent);
    return msg;
  }

  // userIntentsフィールドに、指定された条件のものがあれば取り出す
  Optional<UserIntent> getUserIntent(String userId, Intent intent) {
    return userIntents.stream()
      .filter(ui -> ui.contains(userId))
      .filter(ui -> ui.contains(intent))
      .findFirst();
  }

  // userIntentsフィールドを、引数のものに置き換える
  void upsertUserIntent(UserIntent newOne) {
    userIntents.removeIf(ui -> ui.contains(newOne.getUserId()));
    userIntents.add(newOne);
  }

  // TextEventで、指示不明の場合の返答メッセージを作る
  Message getUnknownReaMsg() {
    return new TextMessage("リマインダを設定したい時分と用件（32文字まで）を送信してください\n例）10時20分に授業");
  }

  // TextEventで、リマインダーの場合の返答メッセージを作る
  Message getRemainderConfirmMsg(String text) {
    var template = new ConfirmTemplate(text + "をリマインダーしますか？",
      new PostbackAction("はい", "RY"),
      new PostbackAction("いいえ", "RN"));
    return new TemplateMessage(text, template);
  }

}
