CREATE DATABASE IF NOT EXISTS cinema DEFAULT CHARACTER SET utf8mb4;
USE cinema;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'USER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cinemas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    city VARCHAR(64) NOT NULL,
    address VARCHAR(256) NOT NULL,
    phone VARCHAR(32),
    cover VARCHAR(512),
    status VARCHAR(16) DEFAULT 'OPEN'
);

CREATE TABLE IF NOT EXISTS movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    poster VARCHAR(512),
    genre VARCHAR(64),
    duration INT,
    rating DECIMAL(3,1) DEFAULT 0,
    description TEXT,
    status VARCHAR(16) DEFAULT 'SHOWING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS snacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    category VARCHAR(32) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description VARCHAR(256),
    image VARCHAR(512),
    status VARCHAR(16) DEFAULT 'ON_SALE'
);

CREATE TABLE IF NOT EXISTS screenings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    cinema_id BIGINT,
    hall_name VARCHAR(64) NOT NULL,
    start_time DATETIME NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    seat_rows INT NOT NULL DEFAULT 8,
    seat_cols INT NOT NULL DEFAULT 12,
    status VARCHAR(16) DEFAULT 'OPEN',
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (cinema_id) REFERENCES cinemas(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    screening_id BIGINT NOT NULL,
    cinema_id BIGINT,
    total_price DECIMAL(10,2) NOT NULL,
    snack_total DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    paid_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (screening_id) REFERENCES screenings(id),
    FOREIGN KEY (cinema_id) REFERENCES cinemas(id)
);

CREATE TABLE IF NOT EXISTS order_seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    row_num INT NOT NULL,
    col_num INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE IF NOT EXISTS order_snacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    snack_id BIGINT NOT NULL,
    snack_name VARCHAR(128) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (snack_id) REFERENCES snacks(id)
);
