-- =====================================================================
-- SMART RETAIL INVENTORY & BILLING SYSTEM
-- Database Schema for MySQL
-- =====================================================================

CREATE DATABASE IF NOT EXISTS smart_retail 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE smart_retail;

-- 1. Users Table (Role-Based Access Control: ADMIN, CASHIER)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CASHIER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Products Table (Catalog & Inventory)
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    barcode VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    min_stock INT NOT NULL DEFAULT 10,
    unit VARCHAR(20) NOT NULL DEFAULT 'unit',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Bills Table (Sales Transaction Master)
CREATE TABLE IF NOT EXISTS bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_number VARCHAR(30) NOT NULL UNIQUE,
    cashier_id INT,
    cashier_name VARCHAR(100) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH',
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cashier_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 4. Bill Items Table (Sales Transaction Detail)
CREATE TABLE IF NOT EXISTS bill_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    product_id INT,
    product_name VARCHAR(100) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Performance Indexes
CREATE INDEX idx_products_barcode ON products(barcode);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_bills_date ON bills(created_at);
CREATE INDEX idx_bill_items_bill_id ON bill_items(bill_id);

-- Default Users Seed Data
INSERT IGNORE INTO users (id, username, password, full_name, role, status) VALUES
(1, 'admin', 'admin123', 'System Administrator', 'ADMIN', 'ACTIVE'),
(2, 'cashier', 'cashier123', 'Rahul Verma', 'CASHIER', 'ACTIVE');

-- Default Products Seed Data
INSERT IGNORE INTO products (id, barcode, name, category, price, stock, min_stock, unit) VALUES
(1, '10001', 'Basmati Rice (1kg)', 'Grocery', 120.00, 85, 15, 'kg'),
(2, '10002', 'Fresh Cow Milk (1L)', 'Dairy', 32.00, 8, 12, 'packet'),
(3, '10003', 'Whole Wheat Bread (400g)', 'Bakery', 40.00, 35, 10, 'loaf'),
(4, '10004', 'Refined Sugar (1kg)', 'Grocery', 46.00, 110, 20, 'kg'),
(5, '10005', 'Sunflower Cooking Oil (1L)', 'Grocery', 145.00, 22, 10, 'bottle'),
(6, '10006', 'Farm Fresh Eggs (12 pcs)', 'Dairy', 84.00, 6, 10, 'pack'),
(7, '10007', 'Assam Tea Leaves (500g)', 'Beverages', 190.00, 45, 10, 'box'),
(8, '10008', 'Instant Filter Coffee (200g)', 'Beverages', 160.00, 9, 10, 'jar'),
(9, '10009', 'Organic Wheat Flour (5kg)', 'Grocery', 240.00, 40, 10, 'bag'),
(10, '10010', 'Bathing Soap (4x100g)', 'Personal Care', 115.00, 50, 15, 'pack'),
(11, '10011', 'Toor Dal (1kg)', 'Grocery', 165.00, 7, 15, 'kg'),
(12, '10012', 'Dark Chocolate Bar (100g)', 'Snacks', 95.00, 60, 12, 'bar');
