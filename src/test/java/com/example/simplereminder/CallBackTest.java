package com.example.simplereminder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;


public class CallBackTest {

  @Test
  void fixStageTest() {
    var sut = new CallBack();
    var actual = sut.fixStage("test");
    assertEquals(Stage.UNKNOWN, actual);
  }
}
