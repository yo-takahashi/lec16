create table if not exists reminder_item
(
    user_id   varchar(64) not null,
    push_At   time        not null,
    push_text varchar(32) not null,
    unique (user_id, push_at, push_text)
);
