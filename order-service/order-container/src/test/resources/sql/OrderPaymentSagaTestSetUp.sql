insert into "order".orders(id, customer_id, restaurant_id, tracking_id, price, order_status, failure_messages)
values('3148c572-540d-480d-9485-4906c7854e8a', 'c4ac66fc-4f28-488c-86ec-7d54664e2fcb', '89ba169a-2504-46b0-ba8a-48749b784f4e',
'a3be5d33-0cbf-41ee-9b11-5651019c8a70', 100.00, 'PENDING', '');

insert into "order".order_items(id, order_id, product_id, price, quantity, sub_total)
values(1, '3148c572-540d-480d-9485-4906c7854e8a', 'e70ea73b-47f9-4fff-b5d3-1074d172552e', 100.00, 1, 100.00);

insert into "order".order_address(id, order_id, street, postal_code, city)
values('8357acb8-1f73-4eb8-bbb5-3564dcbcc3fe', '3148c572-540d-480d-9485-4906c7854e8a', 'test street', '1000AA', 'test city');

insert into "order".payment_outbox(id, saga_id, created_at, type, payload, outbox_status, saga_status, order_status, version)
values  ('800a29ca-7bb7-48dc-9bf5-c2c3c1956def', '3d176b23-7723-43b0-b61e-47e9bd000938', current_timestamp, 'OrderProcessingSaga',
  '{"price": 100, "orderId": "c5bfc590-67eb-4a0e-83c6-bdd8d32caf15", "createdAt": "2022-01-07T16:21:42.917756+01:00",
  "customerId": "c4ac66fc-4f28-488c-86ec-7d54664e2fcb", "paymentOrderStatus": "PENDING"}',
  'STARTED', 'STARTED', 'PENDING', 0);