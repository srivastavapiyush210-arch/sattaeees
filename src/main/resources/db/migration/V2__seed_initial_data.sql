-- ====================================================================
-- Sattaees Data Seed: V2__seed_initial_data.sql
-- Password for demo customer is: password123
-- Password for demo workers is: pass123
-- Hash: $2a$10$w8m8zU78P2m3p2tV1dG9qu1B7M2yG6fP5r5bZ7kH3qN2q2yR.Sy (BCrypt)
-- ====================================================================

-- 1. Demo Customer
INSERT INTO customers (name, email, password, phone_number, address, version, created_at, updated_at)
VALUES (
    'Demo Customer',
    'demo@customer.com',
    '$2a$10$7Z.7n80sE6z8U6/30L7xteh1F2FhWkP.qM3n00k0y/yZ1h5R6v1O2',
    '+91 9999999999',
    'Gateway of India, Mumbai',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 2. Demo Workers
INSERT INTO workers (name, email, password, phone_number, skill, experience, city, available, hourly_rate, average_rating, total_reviews, version, created_at, updated_at)
VALUES
('Rahul Verma', 'rahul@sattaees.com', '$2a$10$7Z.7n80sE6z8U6/30L7xteh1F2FhWkP.qM3n00k0y/yZ1h5R6v1O2', '+91 9876543210', 'Electrician', 5, 'Delhi', true, 45.00, 4.80, 120, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Priya Sharma', 'priya@sattaees.com', '$2a$10$7Z.7n80sE6z8U6/30L7xteh1F2FhWkP.qM3n00k0y/yZ1h5R6v1O2', '+91 8765432109', 'House Cleaning', 3, 'Mumbai', true, 25.00, 4.50, 45, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Amit Kumar', 'amit@sattaees.com', '$2a$10$7Z.7n80sE6z8U6/30L7xteh1F2FhWkP.qM3n00k0y/yZ1h5R6v1O2', '+91 7654321098', 'Plumber', 4, 'Bangalore', true, 35.00, 4.90, 210, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Neha Singh', 'neha@sattaees.com', '$2a$10$7Z.7n80sE6z8U6/30L7xteh1F2FhWkP.qM3n00k0y/yZ1h5R6v1O2', '+91 6543210987', 'Interior Designer', 8, 'Delhi', true, 85.00, 5.00, 89, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sanjay Patel', 'sanjay@sattaees.com', '$2a$10$7Z.7n80sE6z8U6/30L7xteh1F2FhWkP.qM3n00k0y/yZ1h5R6v1O2', '+91 5432109876', 'Electrician', 6, 'Mumbai', true, 50.00, 4.60, 150, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Vikram Rathore', 'vikram@sattaees.com', '$2a$10$7Z.7n80sE6z8U6/30L7xteh1F2FhWkP.qM3n00k0y/yZ1h5R6v1O2', '+91 4432109876', 'Carpenter', 8, 'Delhi', true, 40.00, 4.20, 70, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sneha Reddy', 'sneha@sattaees.com', '$2a$10$7Z.7n80sE6z8U6/30L7xteh1F2FhWkP.qM3n00k0y/yZ1h5R6v1O2', '+91 3432109876', 'Plumber', 5, 'Bangalore', true, 38.00, 4.80, 114, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Arjun Kapoor', 'arjun@sattaees.com', '$2a$10$7Z.7n80sE6z8U6/30L7xteh1F2FhWkP.qM3n00k0y/yZ1h5R6v1O2', '+91 2432109876', 'House Cleaning', 1, 'Mumbai', true, 20.00, 3.90, 15, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
