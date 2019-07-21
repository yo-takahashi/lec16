create table if not exists reminder_item (
  id bigserial primary key,
  user_id varchar(32) not null,
  push_At time not null,
  push_text varchar(32) not null
);
