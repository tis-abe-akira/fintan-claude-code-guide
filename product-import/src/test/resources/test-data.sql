-- Test data for unit tests

DELETE FROM sample_entity;

INSERT INTO sample_entity (name, status, created_at, updated_at) VALUES
  ('Test Item 1', 'NEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Test Item 2', 'NEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Test Item 3', 'NEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
