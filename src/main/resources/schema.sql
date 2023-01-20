create table product
(
    id    varchar(255) not null,
    name  varchar(255),
    price integer      not null,
    type  varchar(255) not null,

    primary key (id)
);