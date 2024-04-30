create sequence categories_seq as bigint increment by 50;

create table categories
(
    category_id bigint      not null primary key default nextval('categories_seq'),
    name        varchar(50) not null unique
);

alter sequence categories_seq owned by categories.category_id;


create table user_categories
(
    user_id     bigint not null,
    category_id bigint not null,

    unique (user_id, category_id)
);

insert into categories
values (0, ''),
       (1, 'Прочее'),
       (2, 'Other');