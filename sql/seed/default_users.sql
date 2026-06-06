-- Seed default users (passwords are BCrypt hashes of admin123, manager123, cashier123)
INSERT INTO `users` (`username`, `password`, `full_name`, `role`, `is_active`) VALUES
('admin', '$2a$10$Z1eE1N54f6B0L65x25O1E.cE3.o4s0Wq.v9hB4iY5nK6m4r3L8.G6', 'System Administrator', 'ADMIN', TRUE),
('manager', '$2a$10$S9W6B3o1M3V5r6T1A8p0E.n3q7S1d.w7uY8kG6vR7q8z9J3t4h2a6', 'Store Manager', 'MANAGER', TRUE),
('cashier', '$2a$10$J5s3f2e1m7g4T6s7Y8U1O.p9oR5v1W5y7h8v3f2e4w1t3y5u6i7o8', 'Store Cashier', 'CASHIER', TRUE)
ON DUPLICATE KEY UPDATE username=username;
