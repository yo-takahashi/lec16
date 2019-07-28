package com.example.simple_assistant.c;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class Doc2VecApiTest {

    @Test
    public void Java11のHTTPClientで取得する() throws IOException, InterruptedException {
        var json = "{\"sentence\" : \"猫\"}";
        var req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:3004/doc2vec-sample/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = HttpClient.newBuilder()
                .build()
                .send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println(resp.body());
        assertThat(resp.statusCode(), is(200));
    }
}