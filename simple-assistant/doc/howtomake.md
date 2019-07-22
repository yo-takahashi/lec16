# 課題

[TOC]

実用的なシステムの例として、時間と用件を送信すると、その時間に用件をリマインダーとしてLineにpushしてくれるlinebotを作成する。

わからない語句があった場合は、Web検索などで確認しておくとよい。

## 前準備

`simple-assistant.zip` ファイルをダウンロードし、わかりやすい場所に展開（解凍）する。

IntelliJ を起動し、展開された `simple-assistant` の pom.xml を選ぶ。

このプロジェクトには、前回利用した line-bot-sdk と、5月頃の授業で学んだデータベース（H2Database）が利用できるように事前セッティングしている。

<div style="page-break-after: always"></div>
## インテント（要求意図）を定義する

### Intent enumを作成

Chatbotやスマートスピーカーのような対話システムの場合、**ユーザーのメッセージから、ユーザが何を要求しているのかの意図**（これを**インテント**と呼ぶ）を見いだす必要がある。

本来はこの部分も自然言語処理やAI技術を使うことで、より幅広い会話の中に適応したシステムが作れるが、今回は最も基本的な方法として、正規表現で判断をする。

正規表現のマッチ条件と、それに対応するインテントの定義として、 `com.example.simple_assistant` パッケージに、Intent enum を作成する。（クラスの作成時に、`Enum` を選ぶ）

```java
package com.example.simple_assistant;

import lombok.AllArgsConstructor;

import java.util.EnumSet;
import java.util.regex.Pattern;

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
}
```

この例では `xx（1 or 2桁の数字）時xx（1 or 2桁の数字）分にxx（32文字までの文字列）` というStringを使って、会話の意図を `REMINDER`：リマインダーの依頼、 `UNKNOWN`：それ以外の状態をわけることができる。

### UserIntent クラスを作成

chatbotは複数人で平行・同時に利用されることがあり得るが、chatbot側のプログラムはひとつだけである。そのため、chatbot側はユーザーごとに「どんな意図の会話をしているか（つまり、インテント）」を分けて管理する必要がある。

ユーザーごとのインテントの状態を管理するためのクラスとして、  `com.example.simple_assistant.m` パッケージに、UserIntent クラスを作成する。

```java
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

  public boolean containsUserId(String userId) {
    return Objects.equals(this.userId, userId);
  }

}
```

<div style="page-break-after: always"></div>
## インテントにあわせて返答する

準備してきたクラスを組みあせる処理を作れば、LineBotがユーザーに投げかけられたメッセージから、 インテント が `UNKNOWN`, `REMINDER` のどちらかを判断し、返答を変えるChatbotが作れる。

 `com.example.simple_assistant.c` パッケージに、このchatbotの主たる中身になる CallBack クラスを作成する（クラスのコードが長いので間違えないように）。

import は先に作るのではなく、コード入力時にIntelliJに補完してもらうように進めた方が早く完成する。

```java
package com.example.simple_assistant.c;

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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@LineMessageHandler
public class CallBack {

  // ユーザごとの処理状態を記録する
  private Set<UserIntent> userIntents;

  @Autowired
  public CallBack(Set<UserIntent> userIntents) {
    this.userIntents = userIntents;
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

  // 返すメッセージのハンドリングをする
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
        msg = new TextMessage("リマインダーを登録しました");
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
```

クラスが完成したら、一度ソースコードを読み、処理の流れを確認しなさい。

また、LINE Developer のサイトから、先週までに作成したchatbotの `Channel Secret`, `アクセストークン（ロングターム）` の値をもらい、 `application.properties` ファイルの対応項目を修正しなさい。

さらに ngrok で取得したURLを LINE Developer のサイトに登録し、アプリケーションを動作させ、自らのLINEから動作確認を行いなさい。

（このあたりの準備や動作を忘れた学生は、前回の課題ファイルを参照）

下の画像のように会話を行い、動作を確認しなさい。<br>特に、handleIntent メソッドのswitch文では再帰処理を使い、リマインダーの確認状態の時に他の話題が投稿されても、確認を続けるように工夫している。

![fig01](./fig01.png)

<div style="page-break-after: always"></div>
## リマインダーをデータベースに保存する

確認メッセージで〈はい〉が押された場合、時間と用件をデータベースに記録する。

このプロジェクトはすでに `src/main/resources/schema.sql` でh2dbによるデータベースが作られている。

<dL>
<dt><strong>reminder_item テーブル</strong></dt>
<dd>

|  user_id | push_At | push_text |
|:----:|:----:|:----:|
|||

</dd>
<dd>user_id は varchar(64) 型である。</dd>
<dd>push_At は time 型（時分秒を表す）である。</dd>
<dd>push_text はvarchar(32) 型である。</dd>
<dd>3列の組み合わせは一意である。</dd>
<dd>初期データはない。</dd>
</dl>

このテーブルに、`xx時xx分にxx` という文章から得られる変数データ（xxの部分）を記録し、後から使えるようにする。

この変数データの部分を、対話システムでは **エンティティ** と呼ぶ。ただし、システム開発ではエンティティという言葉が違う意味でよく使われる（タプルのマッピングデータ、概念データモデルでのデータの枠組み、etc...）ので、ここではあえて〈**変数データ**〉と呼ぶ。


### Intent enumを修正

Intent enumに、メッセージから変数データ（時・分・用件）を抜き出すメソッドを作成する。

```java
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

  /*----　ソースコード上で変更がない部分は、省略している ----*/

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
```

このメソッドで、`xx時xx分にxx` という文章から、変数データがString型のListの形で、

|  List |
|:----:|
|10|
|02|
|食事|


のように抜き出されている。

この情報をデータベースに格納するために、ReminderItem クラスを作る。

### ReminderItem クラスを作成

テーブルに登録した情報を後から使えるようにするためには、 時・分・用件 だけでなく、 誰のリマインダかを区別するユーザIdも必要である。

これらをまとめて管理する RemainderItem クラスを  `com.example.simple_assistant.m` パッケージに作る。

引数付きコンストラクタを作り、UserIntentからUserIdと上記のList を生成し、インスタンス化できるようにしておく。

```java
package com.example.simple_assistant.m;

import com.example.simple_assistant.Intent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;
import java.util.Objects;

// あるメッセージから作られたリマインダー1つ分
@AllArgsConstructor
@Data
public class ReminderItem {

  private String userId;
  private LocalTime pushAt;
  private String pushText;

  public ReminderItem(UserIntent userIntent) {
    try {
      if (!Objects.equals(userIntent.getIntent(), Intent.REMINDER)) {
        throw new IllegalArgumentException("IntentTypeが異なります:" + userIntent.getIntent().name());
      }
      this.userId = userIntent.getUserId();
      var text = userIntent.getText();
      
      // Intentに追加したgetGroupsメソッドを呼び出す部分
      var groups = userIntent.getIntent().getGroups(text);
      
      // groups（変数データ）のListに格納された時間・分・用件を、
      // ReminderItem インスタンスのフィールドに格納する
      int hour = Integer.valueOf(groups.get(0));
      int time = Integer.valueOf(groups.get(1));
      this.pushAt = LocalTime.of(hour, time);
      this.pushText = groups.get(2);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
  }
}
```

### ReminderRepository クラスを作成

ReminderItem インスタンスを使って、実際にテーブルにデータを書き込む ReminderRepository クラスを `com.example.simple_assistant.m` パッケージに作成する。

```java
package com.example.simple_assistant.m;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReminderRepository {

  private JdbcTemplate jdbc;

  @Autowired
  public ReminderRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public int insert(ReminderItem reminderItem) {
    var sql = "insert into " +
      "reminder_item (user_id, push_at, push_text) " +
      "values (?, ?, ?)";
    return jdbc.update(sql, 
      reminderItem.getUserId(), reminderItem.getPushAt(), reminderItem.getPushText());
  }

}
```

### CallBack クラスを変更

準備したクラスをCallBackクラスに組み込んで、確認メッセージに〈はい〉と答えたときに、データベースにそのユーザーにリマインダーを送る時間と用件を記録できるChatBotにバージョンアップする。

変更する箇所が多いので、間違わないように。

```java
package com.example.simple_assistant.c;

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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@LineMessageHandler
public class CallBack {

  // ユーザごとの処理状態を記録する
  private Set<UserIntent> userIntents;

  // データベースにアクセスするためのクラス　←追加
  private ReminderRepository repos;

　// ↓コンストラクタにも引数を追加
  @Autowired
  public CallBack(Set<UserIntent> userIntents, ReminderRepository repos) {
    this.userIntents = userIntents;
    this.repos = repos;
  }

  /* ---- handleProc メソッドまでは変更なし ---- */
  
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
  /* ---- handleProc メソッドより下は変更なし ---- */

}

```

LineBotを起動し、動作確認を行う。

実際にデータが追加されているか確認したい場合は、IntelliJのデータベースウィンドウに `jdbc:h2:~/h2db/SimpleAssistant;MODE=PostgreSQL;AUTO_SERVER=TRUE;` (ユーザー名・パスワードは両方とも `sa` ）でh2データベースを登録して，テーブルの中身を実際に確認しても良い。

<div style="page-break-after: always"></div>

## リマインダを送る

データベースのテーブルに記録されたリマインダの情報を元に、ユーザーごとにメッセージをpush通知する。

そのためにはまず、データベースに記録されたリマインダの情報を検索できる必要がある。また、リマインダが不要となったものはゴミデータとして削除する必要がある。

このため、 ReminderRepository クラスに、select, delete メソッドを追加する。


```java
package com.example.simple_assistant.m;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Repository
public class ReminderRepository {

  private JdbcTemplate jdbc;

  @Autowired
  public ReminderRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public int insert(ReminderItem reminderItem) {
    var sql = "insert into " +
      "reminder_item (user_id, push_at, push_text) " +
      "values (?, ?, ?)";
    return jdbc.update(sql,
      reminderItem.getUserId(), reminderItem.getPushAt(), reminderItem.getPushText());
  }

  public List<ReminderItem> select(LocalTime time) {
    var sql = "select * from reminder_item " +
      "where push_at = ?";

    var items = jdbc.query(sql, new BeanPropertyRowMapper<>(ReminderItem.class), time);
    return items;
  }
}

```


`com.example.simple_assistant.c` パッケージに、Push クラスを作成する。

```java
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
      .map(ri -> new PushMessage(ri.getUserId(), new TextMessage(ri.getPushText())))
      .forEach(this::push);
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
```

