-- Sports Complex Management System - MySQL schema and seed
DROP DATABASE IF EXISTS sports_complex;
CREATE DATABASE sports_complex CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sports_complex;

-- Users
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  role ENUM('ADMIN','COACH','MEMBER') NOT NULL,
  phone VARCHAR(64),
  coach_fee DECIMAL(10,2) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Facilities
CREATE TABLE facilities (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  hourly_rate DECIMAL(10,2) NOT NULL,
  status ENUM('AVAILABLE','UNDER_MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE'
);

-- Bookings
CREATE TABLE bookings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  facility_id BIGINT NOT NULL,
  coach_id BIGINT,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  type ENUM('FACILITY','TRAINING') NOT NULL,
  status ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
  facility_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  coach_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  total_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  CONSTRAINT fk_booking_member FOREIGN KEY (member_id) REFERENCES users(id),
  CONSTRAINT fk_booking_facility FOREIGN KEY (facility_id) REFERENCES facilities(id),
  CONSTRAINT fk_booking_coach FOREIGN KEY (coach_id) REFERENCES users(id)
);

-- Prevent overlapping bookings for the same facility
-- Application should enforce; index helps search
CREATE INDEX idx_bookings_facility_time ON bookings(facility_id, start_time, end_time);

-- Payments
CREATE TABLE payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  booking_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  discount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  method ENUM('CASH','CARD','ONLINE') NOT NULL,
  reference VARCHAR(255),
  CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
  CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Maintenance Requests
CREATE TABLE maintenance_requests (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  facility_id BIGINT NOT NULL,
  requested_by BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  status ENUM('OPEN','IN_PROGRESS','RESOLVED') NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_mr_facility FOREIGN KEY (facility_id) REFERENCES facilities(id),
  CONSTRAINT fk_mr_user FOREIGN KEY (requested_by) REFERENCES users(id)
);

-- Feedback
CREATE TABLE feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  facility_id BIGINT,
  rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comments TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_feedback_facility FOREIGN KEY (facility_id) REFERENCES facilities(id)
);

-- Seed data
INSERT INTO users(email, password_hash, full_name, role, phone)
VALUES
('admin@scms.local', '$2a$10$K2Y2p1wX6q1L7rDkV1kz3e3YyQnN3w7y1jI5rYOXR6hGfW6z7t6yK', 'System Administrator', 'ADMIN', '000-000-0000');
-- Password hash corresponds to bcrypt('admin123') placeholder. Replace if needed.

INSERT INTO facilities(name, description, hourly_rate, status)
VALUES
('Gym', 'Indoor gym facility', 15.00, 'AVAILABLE'),
('Swimming Pool', 'Olympic-size pool', 25.00, 'AVAILABLE'),
('Tennis Court', 'Outdoor clay court', 20.00, 'AVAILABLE');


