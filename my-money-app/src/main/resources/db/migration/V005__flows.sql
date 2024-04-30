create sequence flows_seq as bigint increment by 50;

create table flows
(
    flow_id     bigint         not null default nextval('flows_seq') primary key,
    account_id  bigint         not null,

    amount      numeric(20, 2) not null,

    time        timestamp      not null,
    category_id bigint         not null default 0,
    description varchar(200)   not null default '',

    foreign key (account_id)
        references accounts (account_id),

    foreign key (category_id)
        references categories (category_id)
);

alter sequence flows_seq owned by flows.flow_id;
