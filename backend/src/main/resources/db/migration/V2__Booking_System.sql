-- ============================================================================
-- Cetaring Catering Booking Platform - Phase 2 Database Schema
-- Version: 2.0.0
-- Description: Booking and Menu Management Core Schema
-- ============================================================================

-- Create Menus table
CREATE TABLE IF NOT EXISTS menus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    cuisine_type VARCHAR(100),
    image_url VARCHAR(500),
    base_price DECIMAL(10, 2),
    min_order_quantity INT,
    max_order_quantity INT,
    is_vegetarian BOOLEAN DEFAULT FALSE,
    is_vegan BOOLEAN DEFAULT FALSE,
    spicy_level INT,
    preparation_time_minutes INT,
    rating DECIMAL(3, 2) DEFAULT 0,
    total_reviews INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    available_on_days VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    INDEX idx_menus_business_id (business_id),
    INDEX idx_menus_cuisine_type (cuisine_type),
    INDEX idx_menus_is_active (is_active),
    INDEX idx_menus_rating (rating),
    INDEX idx_menus_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Menu Items table
CREATE TABLE IF NOT EXISTS menu_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    base_price DECIMAL(10, 2) NOT NULL,
    is_vegetarian BOOLEAN DEFAULT FALSE,
    is_vegan BOOLEAN DEFAULT FALSE,
    spicy_level INT,
    preparation_time_minutes INT,
    rating DECIMAL(3, 2) DEFAULT 0,
    total_reviews INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    available_on_days VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE,
    INDEX idx_menu_items_menu_id (menu_id),
    INDEX idx_menu_items_category_id (category_id),
    INDEX idx_menu_items_is_active (is_active),
    INDEX idx_menu_items_rating (rating),
    INDEX idx_menu_items_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_reference VARCHAR(40) NOT NULL UNIQUE,
    business_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    guest_count INT NOT NULL,
    meal_type VARCHAR(100) NOT NULL,
    event_date DATE NOT NULL,
    event_date_time TIMESTAMP NOT NULL,
    delivery_address TEXT NOT NULL,
    special_instructions TEXT,
    subtotal_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    delivery_fee DECIMAL(10, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    confirmed_at TIMESTAMP NULL,
    preparing_at TIMESTAMP NULL,
    ready_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    cancellation_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_bookings_booking_reference (booking_reference),
    INDEX idx_bookings_business_id (business_id),
    INDEX idx_bookings_user_id (user_id),
    INDEX idx_bookings_event_date (event_date),
    INDEX idx_bookings_status (status),
    INDEX idx_bookings_payment_status (payment_status),
    INDEX idx_bookings_created_at (created_at),
    INDEX idx_bookings_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Booking Items table
CREATE TABLE IF NOT EXISTS booking_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    menu_item_id BIGINT,
    item_name VARCHAR(255) NOT NULL,
    item_description TEXT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    special_instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE SET NULL,
    INDEX idx_booking_items_booking_id (booking_id),
    INDEX idx_booking_items_menu_item_id (menu_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Booking status history for audit tracking
CREATE TABLE IF NOT EXISTS booking_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_by BIGINT,
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_booking_status_history_booking_id (booking_id),
    INDEX idx_booking_status_history_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed booking-related system settings
INSERT INTO system_settings (setting_key, setting_value, setting_type, description) VALUES
('BOOKING_DEFAULT_STATUS', 'PENDING', 'STRING', 'Default booking status'),
('BOOKING_CANCELLATION_BUFFER_HOURS', '24', 'INTEGER', 'Hours before event after which cancellation is restricted'),
('BOOKING_MAX_GUEST_COUNT', '5000', 'INTEGER', 'Maximum supported guest count in a single booking'),
('BOOKING_MIN_GUEST_COUNT', '1', 'INTEGER', 'Minimum supported guest count in a single booking'),
('BOOKING_REFERENCE_PREFIX', 'BK', 'STRING', 'Booking reference id prefix'),
('MENU_MAX_ITEMS_PER_MENU', '200', 'INTEGER', 'Maximum items allowed in a menu'),
('MENU_ITEM_MAX_PRICE', '20000', 'DECIMAL', 'Maximum price for one menu item in INR')
ON DUPLICATE KEY UPDATE setting_value = setting_value;