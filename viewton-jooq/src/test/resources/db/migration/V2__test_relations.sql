create table authors (
    id bigserial primary key,
    name varchar(120) not null,
    active boolean not null,
    profile jsonb,
    created_at timestamptz not null default now()
);

create table books (
    id bigserial primary key,
    author_id bigint not null references authors(id),
    title varchar(200) not null,
    pages integer not null,
    price numeric(10, 2),
    published_at date,
    tags jsonb,
    in_print boolean not null default true
);

create table categories (
    id bigserial primary key,
    code varchar(40) not null unique,
    label varchar(120) not null,
    metadata jsonb
);

create table book_categories (
    book_id bigint not null references books(id) on delete cascade,
    category_id bigint not null references categories(id) on delete cascade,
    assigned_at timestamptz not null default now(),
    primary key (book_id, category_id)
);

create table bookstores (
    id bigserial primary key,
    name varchar(160) not null,
    city varchar(120) not null,
    open boolean not null,
    details jsonb
);

create table bookstore_books (
    bookstore_id bigint not null references bookstores(id) on delete cascade,
    book_id bigint not null references books(id) on delete cascade,
    stock integer not null,
    last_stocked_at timestamptz,
    primary key (bookstore_id, book_id)
);

insert into authors (id, name, active, profile, created_at) values
    (1, 'Iryna Koval', true, '{"country":"UA","genres":["tech","design"]}', '2024-02-01T10:00:00Z'),
    (2, 'Oleh Martyn', false, '{"country":"UA","genres":["history"]}', '2024-02-05T12:30:00Z'),
    (3, 'Anna Petrova', true, '{"country":"PL","genres":["fiction","drama"]}', '2024-02-07T09:15:00Z');

insert into books (id, author_id, title, pages, price, published_at, tags, in_print) values
    (10, 1, 'Viewton Patterns', 320, 29.99, '2023-11-10', '{"series":"engineering","level":"intermediate"}', true),
    (11, 1, 'Data Pipelines', 240, 24.50, '2022-08-01', '{"series":"engineering","level":"beginner"}', true),
    (12, 2, 'Kyiv Chronicles', 410, 34.00, '2019-05-20', '{"series":"history"}', false),
    (13, 3, 'Northern Lights', 280, 19.99, '2021-03-15', '{"series":"fiction","awards":["silver-pen"]}', true);

insert into categories (id, code, label, metadata) values
    (100, 'tech', 'Technology', '{"priority":1}'),
    (101, 'history', 'History', '{"priority":2}'),
    (102, 'fiction', 'Fiction', '{"priority":3}'),
    (103, 'design', 'Design', '{"priority":4}');

insert into book_categories (book_id, category_id, assigned_at) values
    (10, 100, '2024-02-10T08:00:00Z'),
    (10, 103, '2024-02-10T08:05:00Z'),
    (11, 100, '2024-02-11T09:00:00Z'),
    (12, 101, '2024-02-12T10:00:00Z'),
    (13, 102, '2024-02-13T11:00:00Z');

insert into bookstores (id, name, city, open, details) values
    (200, 'Central Reads', 'Kyiv', true, '{"floor":2,"sections":["tech","fiction"]}'),
    (201, 'Old Town Books', 'Lviv', false, '{"floor":1,"sections":["history"]}'),
    (202, 'Riverside Store', 'Warsaw', true, '{"floor":3,"sections":["design","tech"]}');

insert into bookstore_books (bookstore_id, book_id, stock, last_stocked_at) values
    (200, 10, 12, '2024-02-20T10:00:00Z'),
    (200, 11, 6, '2024-02-22T11:00:00Z'),
    (200, 13, 4, '2024-02-23T12:00:00Z'),
    (201, 12, 3, '2024-02-24T09:00:00Z'),
    (202, 10, 8, '2024-02-25T14:00:00Z'),
    (202, 13, 10, '2024-02-26T16:00:00Z');
