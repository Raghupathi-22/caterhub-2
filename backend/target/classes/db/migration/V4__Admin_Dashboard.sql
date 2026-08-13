-- ============================================================================
-- Cetaring Catering Booking Platform - Phase 4 Database Schema
-- Version: 4.0.0
-- Description: Admin Dashboard, Analytics, User Management
-- ============================================================================

-- Create Admin Dashboard table
CREATE TABLE IF NOT EXISTS admin_dashboards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    dashboard_name VARCHAR(255) NOT NULL,
    dashboard_type ENUM('DEFAULT', 'CUSTOM', 'ANALYTICS', 'OPERATIONS') DEFAULT 'DEFAULT',
    is_public BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_admin_dashboards_business_id (business_id),
    INDEX idx_admin_dashboards_user_id (user_id),
    INDEX idx_admin_dashboards_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Dashboard Widgets table
CREATE TABLE IF NOT EXISTS dashboard_widgets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dashboard_id BIGINT NOT NULL,
    widget_type VARCHAR(100) NOT NULL,
    widget_name VARCHAR(255),
    position_x INT DEFAULT 0,
    position_y INT DEFAULT 0,
    width INT DEFAULT 4,
    height INT DEFAULT 3,
    configuration JSON,
    is_visible BOOLEAN DEFAULT TRUE,
    refresh_interval_seconds INT DEFAULT 300,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (dashboard_id) REFERENCES admin_dashboards(id) ON DELETE CASCADE,
    INDEX idx_dashboard_widgets_dashboard_id (dashboard_id),
    INDEX idx_dashboard_widgets_type (widget_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Business Hours table
CREATE TABLE IF NOT EXISTS business_hours (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    day_of_week INT NOT NULL,
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    is_closed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_business_hours (business_id, day_of_week),
    INDEX idx_business_hours_business_id (business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Delivery Zones table
CREATE TABLE IF NOT EXISTS delivery_zones (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    zone_name VARCHAR(255) NOT NULL,
    description TEXT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    radius_km DECIMAL(5, 2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    INDEX idx_delivery_zones_business_id (business_id),
    INDEX idx_delivery_zones_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Zone Pricing table
CREATE TABLE IF NOT EXISTS zone_pricing (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    zone_id BIGINT NOT NULL,
    min_distance_km DECIMAL(5, 2),
    max_distance_km DECIMAL(5, 2),
    delivery_charge DECIMAL(10, 2) NOT NULL,
    min_order_value DECIMAL(10, 2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (zone_id) REFERENCES delivery_zones(id) ON DELETE CASCADE,
    INDEX idx_zone_pricing_zone_id (zone_id),
    INDEX idx_zone_pricing_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Coupons table
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    coupon_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    discount_type ENUM('PERCENTAGE', 'FLAT_AMOUNT', 'FREE_DELIVERY', 'BUY_ONE_GET_ONE') DEFAULT 'PERCENTAGE',
    discount_value DECIMAL(10, 2) NOT NULL,
    min_order_value DECIMAL(10, 2),
    max_discount DECIMAL(10, 2),
    max_usage_per_user INT,
    max_total_usage INT,
    current_usage INT DEFAULT 0,
    valid_from DATETIME NOT NULL,
    valid_until DATETIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    INDEX idx_coupons_coupon_code (coupon_code),
    INDEX idx_coupons_business_id (business_id),
    INDEX idx_coupons_is_active (is_active),
    INDEX idx_coupons_valid_until (valid_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Coupon Usage table
CREATE TABLE IF NOT EXISTS coupon_usage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coupon_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    discount_amount DECIMAL(10, 2),
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_coupon_usage_coupon_id (coupon_id),
    INDEX idx_coupon_usage_booking_id (booking_id),
    INDEX idx_coupon_usage_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Promotion Campaigns table
CREATE TABLE IF NOT EXISTS promotion_campaigns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    campaign_name VARCHAR(255) NOT NULL,
    campaign_description TEXT,
    campaign_type VARCHAR(100),
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    target_audience VARCHAR(255),
    budget DECIMAL(10, 2),
    spent DECIMAL(10, 2) DEFAULT 0,
    status ENUM('DRAFT', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED') DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    INDEX idx_promotion_campaigns_business_id (business_id),
    INDEX idx_promotion_campaigns_status (status),
    INDEX idx_promotion_campaigns_start_date (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Staff Roles table
CREATE TABLE IF NOT EXISTS staff_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    role_description TEXT,
    is_system_role BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_staff_roles (business_id, role_name),
    INDEX idx_staff_roles_business_id (business_id),
    INDEX idx_staff_roles_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Staff Permissions table
CREATE TABLE IF NOT EXISTS staff_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_name VARCHAR(255) NOT NULL,
    permission_description TEXT,
    resource_type VARCHAR(100),
    action_type ENUM('READ', 'CREATE', 'UPDATE', 'DELETE', 'EXECUTE') DEFAULT 'READ',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES staff_roles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_staff_permissions (role_id, permission_name),
    INDEX idx_staff_permissions_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Business Settings table
CREATE TABLE IF NOT EXISTS business_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    setting_key VARCHAR(255) NOT NULL,
    setting_value TEXT,
    setting_type VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_business_settings (business_id, setting_key),
    INDEX idx_business_settings_business_id (business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Dashboard Analytics Snapshot table
CREATE TABLE IF NOT EXISTS dashboard_analytics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    total_orders INT DEFAULT 0,
    total_revenue DECIMAL(10, 2) DEFAULT 0,
    total_customers INT DEFAULT 0,
    average_order_value DECIMAL(10, 2) DEFAULT 0,
    pending_orders INT DEFAULT 0,
    completed_orders INT DEFAULT 0,
    cancelled_orders INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_dashboard_analytics (business_id, snapshot_date),
    INDEX idx_dashboard_analytics_business_id (business_id),
    INDEX idx_dashboard_analytics_snapshot_date (snapshot_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default staff roles for existing businesses
-- Insert default staff roles for existing businesses.
-- INSERT IGNORE is used because staff_roles already has a unique constraint
-- on (business_id, role_name). Existing roles will therefore be skipped.

INSERT IGNORE INTO staff_roles
(
    business_id,
    role_name,
    role_description,
    is_system_role
)
SELECT
    b.id,
    default_roles.role_name,
    default_roles.role_description,
    TRUE
FROM businesses b
CROSS JOIN
(
    SELECT
        'Manager' AS role_name,
        'Business manager with full access' AS role_description

    UNION ALL

    SELECT
        'Kitchen Manager',
        'Manages kitchen operations'

    UNION ALL

    SELECT
        'Delivery Manager',
        'Manages delivery operations'

    UNION ALL

    SELECT
        'Staff',
        'Regular staff member'

    UNION ALL

    SELECT
        'Admin',
        'Administrator with system access'
) AS default_roles;

-- Create view for dashboard KPIs
CREATE OR REPLACE VIEW dashboard_kpis AS
SELECT
    b.id as business_id,
    DATE(b.created_at) as date,
    COUNT(DISTINCT bo.id) as total_orders,
    SUM(bo.total_amount) as total_revenue,
    COUNT(DISTINCT bo.user_id) as unique_customers,
    AVG(bo.total_amount) as avg_order_value,
    SUM(CASE WHEN bo.status = 'PENDING' THEN 1 ELSE 0 END) as pending_orders,
    SUM(CASE WHEN bo.status = 'DELIVERED' THEN 1 ELSE 0 END) as completed_orders,
    SUM(CASE WHEN bo.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled_orders
FROM businesses b
LEFT JOIN bookings bo ON b.id = bo.business_id
GROUP BY b.id, DATE(bo.created_at);

-- Create view for coupon performance
CREATE OR REPLACE VIEW coupon_performance AS
SELECT
    c.id,
    c.coupon_code,
    c.business_id,
    COUNT(cu.id) as total_usage,
    SUM(cu.discount_amount) as total_discount_given,
    COUNT(DISTINCT cu.user_id) as unique_users,
    c.valid_from,
    c.valid_until
FROM coupons c
LEFT JOIN coupon_usage cu ON c.id = cu.coupon_id
GROUP BY c.id;

-- Update system settings for Phase 4
INSERT INTO system_settings (setting_key, setting_value, setting_type, description) VALUES
('DASHBOARD_REFRESH_INTERVAL', '30', 'INTEGER', 'Dashboard refresh interval in seconds'),
('MAX_DELIVERY_ZONES', '100', 'INTEGER', 'Maximum delivery zones per business'),
('COUPON_CODE_LENGTH', '10', 'INTEGER', 'Length of auto-generated coupon codes'),
('ANALYTICS_RETENTION_DAYS', '365', 'INTEGER', 'Days to retain analytics data'),
('DASHBOARD_EXPORT_FORMAT', 'PDF,CSV,EXCEL', 'STRING', 'Supported export formats'),
('ADMIN_PERMISSION_CACHING', 'true', 'BOOLEAN', 'Cache permissions in admin operations'),
('ANALYTICS_CALCULATION_INTERVAL', '3600', 'INTEGER', 'Interval for analytics calculation in seconds')
ON DUPLICATE KEY UPDATE setting_value = setting_value;
