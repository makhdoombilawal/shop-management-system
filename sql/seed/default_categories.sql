-- Seed default categories
INSERT INTO `categories` (`name`, `description`) VALUES
('Electronics', 'Electronic devices and accessories'),
('Groceries', 'Food and beverages'),
('Clothing', 'Apparel and fashion items'),
('Home & Garden', 'Home improvement and garden supplies'),
('Sports & Outdoors', 'Sports equipment and outdoor gear'),
('Automotive', 'Auto parts and accessories'),
('Health & Beauty', 'Healthcare and beauty products'),
('Books & Media', 'Books, music, and movies'),
('Toys & Games', 'Children toys and games'),
('Office Supplies', 'Office and stationery items')
ON DUPLICATE KEY UPDATE description=VALUES(description);
