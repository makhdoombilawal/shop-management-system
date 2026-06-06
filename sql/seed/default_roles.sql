-- Seed default roles
INSERT INTO `roles` (`name`, `description`) VALUES
('ADMIN', 'Full system access with all privileges'),
('MANAGER', 'Manage products, view reports, manage users'),
('CASHIER', 'Process sales, view products and customers')
ON DUPLICATE KEY UPDATE description=VALUES(description);
