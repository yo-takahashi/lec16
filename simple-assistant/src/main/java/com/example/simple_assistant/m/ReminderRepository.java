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
