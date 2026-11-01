CREATE DATABASE IF NOT EXISTS cravedrop;
USE cravedrop;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone CHAR(10) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role ENUM('customer','owner') NOT NULL DEFAULT 'customer',
    reset_token VARCHAR(40),
    reset_expiry DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Upgrade databases created by earlier project versions. Public signup still
-- accepts only customer/owner; administrator access is granted manually.
ALTER TABLE users MODIFY role ENUM('customer','owner','admin') NOT NULL DEFAULT 'customer';

CREATE TABLE IF NOT EXISTS menu_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(40) NOT NULL,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(200) NOT NULL,
    price DECIMAL(8,2) NOT NULL CHECK(price > 0),
    image_url VARCHAR(300) NOT NULL,
    is_veg BOOLEAN NOT NULL DEFAULT TRUE,
    restaurant_name VARCHAR(100),
    INDEX idx_menu_category(category),
    INDEX idx_menu_restaurant(restaurant_name)
);

CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    status ENUM('Placed','Preparing','Out for delivery','Delivered') DEFAULT 'Placed',
    payment_method VARCHAR(30) NOT NULL,
    address VARCHAR(300) NOT NULL,
    phone CHAR(10) NOT NULL,
    delivery_partner_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS delivery_partners (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    phone CHAR(10) NOT NULL UNIQUE,
    area VARCHAR(100) NOT NULL,
    status ENUM('Available','On delivery','Offline') NOT NULL DEFAULT 'Available',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    item_name VARCHAR(80) NOT NULL,
    qty INT NOT NULL CHECK(qty > 0),
    price DECIMAL(8,2) NOT NULL,
    FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- The application loads the restaurant-specific 180-item catalog from
-- src/main/resources/catalog/menu-items.tsv on the first restaurant menu request.
