create database demoWebApp

use demoWebApp

-- Tạo bảng users
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    phone NVARCHAR(20),
    created_date DATETIME2 DEFAULT GETDATE()
);

-- Insert dữ liệu mẫu
INSERT INTO users (name, email, phone) VALUES 
('Nguyen Van A', 'nguyenvana@email.com', '0123456789'),
('Tran Thi B', 'tranthib@email.com', '0987654321'),
('Le Van C', 'levanc@email.com', '0111222333');

CREATE TABLE products (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    created_date DATETIME2 DEFAULT GETDATE()
);

-- Insert dữ liệu mẫu
INSERT INTO products (name, description, price) VALUES
('Sách học lập trình', 'Hướng dẫn Python cơ bản', 150000),
('Tai nghe Bluetooth', 'Tai nghe không dây chất lượng cao', 850000),
('Bàn phím cơ', 'Bàn phím cơ cho game thủ', 1200000);

CREATE TABLE orders (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    order_date DATETIME2 DEFAULT GETDATE(),
    total_amount DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Insert dữ liệu mẫu
INSERT INTO orders (user_id, total_amount) VALUES
(1, 200000),
(2, 850000),
(3, 1350000);

CREATE TABLE user_addresses (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    address NVARCHAR(255) NOT NULL,
    city NVARCHAR(100),
    postal_code NVARCHAR(20),
    created_date DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Insert dữ liệu mẫu
INSERT INTO user_addresses (user_id, address, city, postal_code) VALUES
(1, '123 Đường Lê Lợi', 'Hà Nội', '100000'),
(2, '456 Đường Trần Phú', 'TP. HCM', '700000'),
(3, '789 Đường Huỳnh Thúc Kháng', 'Đà Nẵng', '550000');


