create sequence transactions_seq as bigint increment by 50;

create table transactions
(
    transaction_id  bigint         not null default nextval('transactions_seq') primary key,
    from_account_id bigint         not null,
    to_account_id   bigint         not null,

    from_amount     numeric(20, 2) not null,
    to_amount       numeric(20, 2) not null,

    time            timestamp      not null,
    category_id     bigint         not null default 0,
    description     varchar(200)   not null default '',

    foreign key (from_account_id)
        references accounts (account_id),

    foreign key (to_account_id)
        references accounts (account_id),

    foreign key (category_id)
        references categories (category_id)
);

alter sequence transactions_seq owned by transactions.transaction_id;
