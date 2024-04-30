create sequence accounts_seq as bigint increment by 50;

create table accounts
(
    account_id    bigint      not null primary key default nextval('accounts_seq'),
    owner_user_id bigint      not null,

    name          varchar(60) not null,
    currency      varchar(3)  not null,

    foreign key (owner_user_id)
        references users (user_id)
);

alter sequence accounts_seq owned by accounts.account_id;
