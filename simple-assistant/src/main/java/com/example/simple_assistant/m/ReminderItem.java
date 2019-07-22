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

  public ReminderItem() {
    this("", LocalTime.MIN, "");
  }

  public ReminderItem(UserIntent userIntent) {
    try {
      if (!Objects.equals(userIntent.getIntent(), Intent.REMINDER)) {
        throw new IllegalArgumentException("IntentTypeが異なります:"
          + userIntent.getIntent().name());
      }
      this.userId = userIntent.getUserId();
      var text = userIntent.getText();

      // Intentに追加したgetGroupsメソッドを呼び出す部分
      var groups = userIntent.getIntent().getGroups(text);

      // groups のListに格納された時間・分・用件をフィールドに格納する
      int hour = Integer.valueOf(groups.get(0));
      int time = Integer.valueOf(groups.get(1));
      this.pushAt = LocalTime.of(hour, time);
      this.pushText = groups.get(2);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
  }
}
