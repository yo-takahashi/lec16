package com.example.simple_assistant.c;

import com.example.simple_assistant.Intent;
import com.example.simple_assistant.m.ReminderItem;
import com.example.simple_assistant.m.ReminderRepository;
import com.example.simple_assistant.m.UserIntent;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.*;
import com.linecorp.bot.model.message.flex.component.*;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.unit.FlexAlign;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;
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

  // PostBackEventに対応する
  @EventMapping
  public Message handlePostBack(PostbackEvent event) {
    var userIntent = new UserIntent(event);
    return handleProc(userIntent);
  }


  // 返答メッセージを作る
  private TextMessage reply(String text) {
    return new TextMessage(text);
  }

  // doc2vecAPIにキーワードを渡して小説のタイトルをもらう
//  @EventMapping
//  public Message handleMessage(MessageEvent<TextMessageContent> event) {
//    var keyWord = new UserIntent(event);
//    return doc2vecAPI(keyWord);
//  }

//   MessageEventに対応する/文章で話しかけられたとき（テキストメッセージのイベント）に対応するリマインダ版
  @EventMapping
  public Message handleMessage(MessageEvent<TextMessageContent> event) {
    var userIntent = new UserIntent(event);
    return handleIntent(userIntent);
  }

//  // 文章で話しかけられたとき（テキストメッセージのイベント）に対応する(先々週)
//  @EventMapping
//  public Message handleMessage(MessageEvent<TextMessageContent> event) {
//    TextMessageContent tmc = event.getMessage();
//    String text = tmc.getText();
//    switch (text) {
//      case "やあ":
//        return greet();
//      case "おみくじ":
//        return replyOmikuji();
//      case "バブル":
//        return replyBubble();
//      case "カルーセル":
//        return replyCarousel();
//      default:
//        return reply(text);
//    }
//  }

  private TextMessage doc2vecAPI(UserIntent text) {

    try {
      var jsonf = "{\"sentence\" : \"%s\"}";
      var json = String.format(jsonf, text);
      var req = HttpRequest.newBuilder()
              .uri(URI.create("http://localhost:3004/doc2vec-sample/"))
//              .uri(URI.create("http://172.25.2.146:8080/doc2vec-sample/"))
//              .uri(URI.create("http://172.25.2.148:8000/doc2vec-sample/"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(json))
              .build();

      HttpResponse<String> resp = HttpClient.newBuilder()
              .build()
              .send(req, HttpResponse.BodyHandlers.ofString());

      return new TextMessage(resp.body());
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  // あいさつする
  private TextMessage greet() {
    LocalTime lt = LocalTime.now();
    int hour = lt.getHour();
    if (hour >= 17) {
      return reply("こんばんは！");
    }
    if (hour >= 11) {
      return reply("こんにちは！");
    }
    return reply("おはよう！");
  }

  // 画像メッセージを作る
  private ImageMessage replyImage(String url) {
    // 本来は、第一引数が実際の画像URL、第二画像がサムネイルのurl
    return new ImageMessage(url, url);
  }

  // ランダムにおみくじ画像を返す
  private ImageMessage replyOmikuji() {
    int ranNum = new Random().nextInt(3);
    switch (ranNum) {
      case 2:
        return replyImage("https://3.bp.blogspot.com/-vQSPQf-ytsc/T3K7QM3qaQI/AAAAAAAAE-s/6SB2q7ltxwg/s1600/omikuji_daikichi.png");
      case 1:
        return replyImage("https://2.bp.blogspot.com/-27IG0CNV-ZE/VKYfn_1-ycI/AAAAAAAAqXw/fr6Y72lOP9s/s400/omikuji_kichi.png");
      case 0:
      default:
        return replyImage("https://4.bp.blogspot.com/-qCfF4H7YOvE/T3K7R5ZjQVI/AAAAAAAAE-4/Hd1u2tzMG3Q/s1600/omikuji_kyou.png");
    }
  }

  private FlexMessage replyBubble() {
    Text hello = Text.builder()
            .text("Hello")
            .build();

    Text world = Text.builder()
            .text("world")
            .weight(Text.TextWeight.BOLD)
            .size(FlexFontSize.XL)
            .align(FlexAlign.CENTER)
            .color("#FF0000")
            .build();

    Separator separator = Separator.builder().build();

    Box box = Box.builder()
            .layout(FlexLayout.HORIZONTAL)
            .contents(Arrays.asList(hello, separator, world))
            .build();

    Bubble bubble = Bubble.builder()
            .body(box)
            .build();

    return new FlexMessage("BubbleSample", bubble);
  }

  private FlexMessage replyCarousel() {
    Text currentTitle = Text.builder()
            .text("今日のイベントはこちら")
            .build();

    Box currentHeader = Box.builder()
            .layout(FlexLayout.VERTICAL)
            .contents(Arrays.asList(currentTitle))
            .build();

    Image currentImage = Image.builder()
            .url("https://connpass-tokyo.s3.amazonaws.com/thumbs/3e/b8/3eb8be3f66515598c47c76bd65e3ebb2.png")
            .size(Image.ImageSize.FULL_WIDTH)
            .aspectMode(Image.ImageAspectMode.Fit)
            .build();

    Text currentText = Text.builder()
            .text("LINE Messaging API for Java でLINE Botを作ってみませんか？\n" +
                    "エントリーを考えている方・考えていない方、社会人、学生の皆さん、誰でも大歓迎です！")
            .wrap(true)
            .build();

    Button currentBtn = Button.builder()
            .style(Button.ButtonStyle.SECONDARY)
            .action(new URIAction("表示",
                    "https://javado.connpass.com/event/97107/",
                    new URIAction.AltUri(URI.create("https://javado.connpass.com/event/97107/"))))
            .build();

    Box currentBody = Box.builder()
            .layout(FlexLayout.VERTICAL)
            .contents(Arrays.asList(currentText, currentBtn))
            .build();

    Bubble currentBbl = Bubble.builder()
            .header(currentHeader)
            .hero(currentImage)
            .body(currentBody)
            .build();

    Text nextTitle = Text.builder()
            .text("次回のイベントはこちら")
            .build();

    Box nextHeader = Box.builder()
            .layout(FlexLayout.VERTICAL)
            .contents(Arrays.asList(nextTitle))
            .build();

    Image nextImage = Image.builder()
            .url("https://connpass-tokyo.s3.amazonaws.com/thumbs/9a/82/9a82ae80521b1f119cc6ed1e3e5edac0.png")
            .size(Image.ImageSize.FULL_WIDTH)
            .aspectMode(Image.ImageAspectMode.Fit)
            .build();

    Text nextText = Text.builder()
            .text("待ちに待ったスキルの開発環境・CEK(Clova Extension Kit)がお目見えしました!!\n" +
                    "Clovaスキルを作ってみたい！Clovaと触れ合いたい！とお考えの皆さんのためにCEKのハンズオンを行います。")
            .wrap(true)
            .build();

    Button nextBtn = Button.builder()
            .style(Button.ButtonStyle.PRIMARY)
            .action(new URIAction("申し込み",
                    "https://linedev.connpass.com/event/96793/",
                    new URIAction.AltUri(URI.create("https://linedev.connpass.com/event/96793/"))))
            .build();

    Box nextBody = Box.builder()
            .layout(FlexLayout.VERTICAL)
            .contents(Arrays.asList(nextText, nextBtn))
            .build();

    Bubble nextBbl = Bubble.builder()
            .header(nextHeader)
            .hero(nextImage)
            .body(nextBody)
            .build();

    Carousel carousel = Carousel.builder()
            .contents(Arrays.asList(currentBbl, nextBbl))
            .build();

    return new FlexMessage("カルーセル", carousel);
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
      case BOOK:
//        var text = userIntent.getText();
//        msg = (Message) userIntent.getIntent().getGroups(text);
//        msg = getUserIntent(userIntent.getText(), Intent.BOOK).map(this::handleIntent).orElse(msg);
        msg = doc2vecAPI(userIntent);
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

  //追加部分削除予定
//  // userIntentsフィールドに、指定された条件のものがあれば取り出す
//  Optional<UserIntent> getBookIntent(BookIntent bookIntent) {
//    return userIntents.stream()
//            .filter(ui -> ui.contains(bookIntent))
//            .findFirst();
//  }

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
