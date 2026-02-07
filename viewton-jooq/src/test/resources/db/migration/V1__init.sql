create table payments (
    id bigint primary key,
    amount numeric(10, 2) not null,
    status varchar(32) not null,
    created_at date not null
);

insert into payments (id, amount, status, created_at) values
    (1, 250.00, 'PAID', '2024-01-10'),
    (2, 150.00, 'PAID', '2024-01-11'),
    (3, 75.00, 'FAILED', '2024-01-12');
