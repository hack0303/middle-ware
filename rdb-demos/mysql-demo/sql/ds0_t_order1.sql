create table t_order1
(
    order_id varchar(35) not null comment '订单号',
    user_id  varchar(32) not null comment '用户ID'
        primary key,
    constraint t_order_id_uindex
        unique (order_id)
);

