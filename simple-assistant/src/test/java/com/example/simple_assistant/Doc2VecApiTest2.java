package com.example.simple_assistant;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Doc2VecApiTest2 {

    public static void main(String args[]) throws IOException {

        String json = "{\"sentence\" : \"猫\"}";
        URL url = new URL("http://localhost:3004/doc2vec-sample/");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // HTTPリクエストコード
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "jp");
        // データがJSONであること、エンコードを指定する
        con.setRequestProperty("Content-Type", "application/JSON; charset=utf-8");

        // POSTデータの長さを設定
        con.setRequestProperty("Content-Length", String.valueOf(json.length()));
        // リクエストのbodyにJSON文字列を書き込む
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        out.write(json);
        out.flush();
        con.connect();

        // HTTPレスポンスコード
        final int status = con.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            // テキストを取得する
            final InputStream in = con.getInputStream();
            String encoding = "UTF-8";
            if (null == encoding) {
                encoding = "UTF-8";
            }
            InputStreamReader inReader = new InputStreamReader(in, encoding);
            BufferedReader bufReader = new BufferedReader(inReader);
            String line = "";
            // 1行ずつテキストを読み込む
            while ((line = bufReader.readLine()) != null) {
                System.out.println(line);
            }
            bufReader.close();
            inReader.close();
            in.close();
        } else {
            // 通信が失敗した場合のレスポンスコードを表示
            System.out.println(status);
        }
    }
}
