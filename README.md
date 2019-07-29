# 課題


先週と先々週作ったLinebotにAIを接続する。今回は青空文庫の単語をDoc2Vecで学習したモデルをAPIで呼び出す。LINEで「〇〇の本」と送るとその単語ベクトルに近い小説タイトルを10作返してくれる。


### CallBackに追加


ChatbotからDoc2VecのAPIを呼び出すためにCallBackに以下を追加する。新しいメソッドはdoc2vecAPI()とそれを呼び出すhandlePostBack()だけであとは先週と先々週のもの。

```java
package com.example.simple_assistant;

//-------doc2vecAPI()を追加して-----------

private TextMessage doc2vecAPI(String text) {

    try {
      var jsonf = "{\"sentence\" : \"%s\"}";
      var json = String.format(jsonf, text);
      var req = HttpRequest.newBuilder()
             .uri(URI.create("http://*.*.*.*:80/doc2vec-sample/"))
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


//-------handleIntent()を変更する-----------

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
        String pushText;
        var text = userIntent.getText();
        var groups = userIntent.getIntent().getGroups(text);
        pushText = groups.get(0);
        msg = doc2vecAPI(pushText);
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

  

```

```java
public enum Intent {

// -----以下を追加変更-----
  // メッセージの正規表現パターンに対応するやりとり状態の定義
  REMINDER(Pattern.compile("(\\d{1,2})時(\\d{1,2})分に(.{1,32})")),
  BOOK(Pattern.compile("(.{1,32})の本")),
  UNKNOWN(Pattern.compile(".+"));

// -----以下を変更なし-----
```


### 参考資料

[先週までのlinebot資料](https://github.com/gishi-yama/linebot-java-handson)