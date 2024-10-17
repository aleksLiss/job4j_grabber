create table post(
    id serial primary key unique,
    name text,
    text text,
    link text unique,
    created timestamp
);