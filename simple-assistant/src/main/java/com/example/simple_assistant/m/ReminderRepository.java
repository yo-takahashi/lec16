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

  @Transactional
  public List<ReminderItem> select(LocalTime time) {
    var sql = "select * from reminder_item " +
      "where push_at = ?";

    var items = jdbc.query(sql, new BeanPropertyRowMapper<>(ReminderItem.class), time);
    return items;
  }
}
