INSERT INTO payment.credit_entry(id, customer_id, total_credit_amount)
  VALUES ('fe1668cc-c384-483a-84bd-dc99fd02ca78', 'c4ac66fc-4f28-488c-86ec-7d54664e2fcb', 500.00);
INSERT INTO payment.credit_history(id, customer_id, amount, type)
  VALUES ('a6cb9aaf-df3d-4e1f-92a4-98600228b934', 'c4ac66fc-4f28-488c-86ec-7d54664e2fcb', 100.00, 'CREDIT');
INSERT INTO payment.credit_history(id, customer_id, amount, type)
  VALUES ('7bd8db6f-a9c6-4e5b-aa19-ca143fc5f17f', 'c4ac66fc-4f28-488c-86ec-7d54664e2fcb', 600.00, 'CREDIT');
INSERT INTO payment.credit_history(id, customer_id, amount, type)
  VALUES ('4d1f7204-16c2-463d-bf3f-8be80064fc2e', 'c4ac66fc-4f28-488c-86ec-7d54664e2fcb', 200.00, 'DEBIT');


INSERT INTO payment.credit_entry(id, customer_id, total_credit_amount)
  VALUES ('9c3d47de-a4be-46f9-afe7-e9662e704aa1', '9c21cbbd-35f9-4cee-b2b2-faec9d95c299', 100.00);
INSERT INTO payment.credit_history(id, customer_id, amount, type)
  VALUES ('6b280c72-aed2-4a63-bacf-237e14a89f1b', '9c21cbbd-35f9-4cee-b2b2-faec9d95c299', 100.00, 'CREDIT');