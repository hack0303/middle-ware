create table t_order_item
(
    id       varchar(32) not null comment '订单项ID',
    order_id varchar(32) not null comment '订单ID'
        primary key,
    constraint t_order_item_id_uindex
        unique (id)
)
    comment '订单项';

