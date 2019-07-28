# 課題

[TOC]

実用的なシステムの例として、時間と用件を送信すると、その時間に用件をリマインダーとしてLineにpushしてくれるlinebotを作成する。

わからない語句があった場合は、Web検索などで確認しておくとよい。

## 前準備

先週作った`simple-assistant` に以下のファイルを追加する。(Callback.classをに追加する)

<!-- これ必要？ -->
<div style="page-break-after: always"></div>
## インテント（要求意図）を定義する

### CallBackに追加

Chatbotやスマートスピーカーのような対話システムの場合、**ユーザーのメッセージから、ユーザが何を要求しているのかの意図**（これを**インテント**と呼ぶ）を見いだす必要がある。

ChatbotからDoc2VecのAPIを呼び出すために
本来はこの部分も自然言語処理やAI技術を使うことで、より幅広い会話の中に適応したシステムが作れるが、今回は最も基本的な方法として、正規表現で判断をする。

正規表現のマッチ条件と、それに対応するインテントの定義として、 `com.example.simple_assistant` パッケージに、Intent enum を作成する。（クラスの作成時に、`Enum` を選ぶ）

```java
package com.example.simple_assistant;

// -------------中略以上変更なし-------------

// PostBackEventに対応する
  @EventMapping
  public Message handlePostBack(PostbackEvent event) {
    var userIntent = new UserIntent(event);
    return handleProc(userIntent);
  }
// -------------ここから:追加-------------

 // 返答メッセージを作る
  private TextMessage reply(String text) {
    return new TextMessage(text);
  }

  // doc2vecAPIにキーワードを渡して小説のタイトルをもらう
  @EventMapping
  public Message handleMessage(MessageEvent<TextMessageContent> event) {
    var keyWord = new UserIntent(event);
    return doc2vecAPI(keyWord);
  }

//   MessageEventに対応する/文章で話しかけられたとき（テキストメッセージのイベント）に対応するリマインダ版
//  @EventMapping
//  public Message handleMessage(MessageEvent<TextMessageContent> event) {
//    var userIntent = new UserIntent(event);
//    return handleIntent(userIntent);
//  }

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
              .uri(URI.create("http://172.25.2.52:8080/doc2vec-sample/"))
//              .uri(URI.create("http://172.25.2.65:8000/doc2vec-sample/"))
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
// -------------ここまで:追加-------------
  // 通常状態で返すメッセージのハンドリングをする
  Message handleIntent(UserIntent userIntent) {
    Message msg = getUnknownReaMsg();
    var intent = userIntent.getIntent();
    switch (intent) {
      case REMINDER:
      // -------------中略以下変更なし-------------

```

コメントアウトしているhandleMessageを切り替えることで先週と先々週の処理を行うChatbotになる。１つの処理にまとめたい人がいたら自分で頑張ってみてください。(ヒント：先々週のhandleMessageを使って"リマインダ"と"青空文庫"みたいなのをcaseで分けてもそれらの処理をする前にreplyがおうむ返ししてしまうので要対策。)

### 参考資料

[先週までのlinebot資料](https://github.com/gishi-yama/linebot-java-handson)