create sequence users_seq as bigint increment by 50;

create table users
(
    user_id                          bigint     not null primary key default nextval('users_seq'),

    created_at                       timestamp  not null             default (now() at time zone 'utc'),
    last_operation_at                timestamp  not null             default (now() at time zone 'utc'),

    -- user settings
    timezone                         varchar(6) not null             default 0,    -- millis
    language_code                    varchar(3) not null             default 'ru', -- IETF language tag

    favorite_account_id              bigint,
    favorite_category_flow_id        bigint,
    favorite_category_transaction_id bigint,

    -- links
    telegram_chat_id                 bigint unique
);

alter sequence users_seq owned by users.user_id;
