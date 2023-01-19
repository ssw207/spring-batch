create table product
(
    id    varchar(255) not null,
    name  varchar(255),
    price integer      not null,
    type  varchar(255) not null,

    primary key (id)
);

create table book
(
    id bigint not null,
    primary key (id)
);

create table book_shop
(
    id bigint not null,
    primary key (id)
);

create table customer
(
    id    varchar(255) not null,
    name  varchar(255),
    price integer      not null,
    primary key (id)
);