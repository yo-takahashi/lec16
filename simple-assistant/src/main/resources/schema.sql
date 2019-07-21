create table if not exists reminder (
  id bigserial primary key,
  user_id varchar(32) not null,
  push_At timestamp not null,
  push_text varchar(256) not null
);
