-- Initial schema creation for sample_entity table

CREATE TABLE sample_entity (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  status VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on status for better query performance
CREATE INDEX idx_sample_entity_status ON sample_entity(status);

-- Insert sample data for testing
INSERT INTO sample_entity (name, status) VALUES
  ('Sample Item 1', 'NEW'),
  ('Sample Item 2', 'NEW'),
  ('Sample Item 3', 'NEW'),
  ('Sample Item 4', 'NEW'),
  ('Sample Item 5', 'NEW');
