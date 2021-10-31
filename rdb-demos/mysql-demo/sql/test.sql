create schema if not exists ds collate utf8mb4_0900_ai_ci;
create table if not exists t_order
(
	order_id bigint not null comment '订单号'
		primary key,
	user_id bigint not null comment '用户ID'
);

