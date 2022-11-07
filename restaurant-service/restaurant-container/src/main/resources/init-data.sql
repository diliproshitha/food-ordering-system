INSERT INTO restaurant.restaurants(id, name, active)
  VALUES ('89ba169a-2504-46b0-ba8a-48749b784f4e', 'restaurant_1', TRUE);
INSERT INTO restaurant.restaurants(id, name, active)
  VALUES ('7726940b-a322-4ce1-b8b5-031aba461983', 'restaurant_2', TRUE);

INSERT INTO restaurant.products(id, name, price, available)
  VALUES ('138195d9-85d8-47b3-9954-4a0e65adabf8', 'product_1', 25.00, TRUE);
INSERT INTO restaurant.products(id, name, price, available)
  VALUES ('e70ea73b-47f9-4fff-b5d3-1074d172552e', 'product_2', 50.00, TRUE);
INSERT INTO restaurant.products(id, name, price, available)
  VALUES ('aedb95f9-6565-42f6-bfdb-be775d40a4c5', 'product_3', 20.00, TRUE);
INSERT INTO restaurant.products(id, name, price, available)
  VALUES ('6364db58-9c96-494a-9e97-b910221ac7b3', 'product_4', 40.00, FALSE);

INSERT INTO restaurant.restaurant_products(id, restaurant_id, product_id)
  VALUES ('6d03998b-b06a-4fca-b57c-60fadec29f8f', '89ba169a-2504-46b0-ba8a-48749b784f4e', 'e70ea73b-47f9-4fff-b5d3-1074d172552e');
INSERT INTO restaurant.restaurant_products(id, restaurant_id, product_id)
  VALUES ('950341eb-9622-409b-8e6f-41a16ab7e4e8', '89ba169a-2504-46b0-ba8a-48749b784f4e', 'e70ea73b-47f9-4fff-b5d3-1074d172552e');
INSERT INTO restaurant.restaurant_products(id, restaurant_id, product_id)
  VALUES ('36f58a69-7811-488b-baa4-e298d029f60f', '7726940b-a322-4ce1-b8b5-031aba461983', 'aedb95f9-6565-42f6-bfdb-be775d40a4c5');
INSERT INTO restaurant.restaurant_products(id, restaurant_id, product_id)
  VALUES ('68b9a23a-b0cb-40e1-9b29-f509a97ddffd', '7726940b-a322-4ce1-b8b5-031aba461983', '6364db58-9c96-494a-9e97-b910221ac7b3');