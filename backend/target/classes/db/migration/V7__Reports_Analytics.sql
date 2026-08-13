-- ============================================================================
-- Cetaring Catering Booking Platform - Phase 7 Database Schema
-- Version: 7.0.0
-- Description: Reports & Analytics System - BI, Reports, Insights
-- ============================================================================

-- Create Analytics Snapshots table
CREATE TABLE IF NOT EXISTS analytics_snapshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    total_orders INT DEFAULT 0,
    total_revenue DECIMAL(10, 2) DEFAULT 0,
    total_customers INT DEFAULT 0,
    repeat_customers INT DEFAULT 0,
    new_customers INT DEFAULT 0,
    average_order_value DECIMAL(10, 2) DEFAULT 0,
    pending_orders INT DEFAULT 0,
    completed_orders INT DEFAULT 0,
    cancelled_orders INT DEFAULT 0,
    total_refunds DECIMAL(10, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_analytics_snapshots (business_id, snapshot_date),
    INDEX idx_analytics_snapshots_business_id (business_id),
    INDEX idx_analytics_snapshots_snapshot_date (snapshot_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Revenue Analytics table
CREATE TABLE IF NOT EXISTS revenue_analytics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    analytics_date DATE NOT NULL,
    gross_revenue DECIMAL(10, 2) NOT NULL,
    commissions_paid DECIMAL(10, 2),
    refunds_issued DECIMAL(10, 2),
    net_revenue DECIMAL(10, 2),
    payment_method_breakdown JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_revenue_analytics (business_id, analytics_date),
    INDEX idx_revenue_analytics_business_id (business_id),
    INDEX idx_revenue_analytics_date (analytics_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Customer Analytics table
CREATE TABLE IF NOT EXISTS customer_analytics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    analytics_date DATE NOT NULL,
    total_customers INT DEFAULT 0,
    new_customers INT DEFAULT 0,
    repeat_customers INT DEFAULT 0,
    customer_lifetime_value DECIMAL(10, 2),
    average_purchase_frequency DECIMAL(5, 2),
    customer_satisfaction_score DECIMAL(3, 2),
    churn_rate DECIMAL(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_customer_analytics (business_id, analytics_date),
    INDEX idx_customer_analytics_business_id (business_id),
    INDEX idx_customer_analytics_date (analytics_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Menu Item Analytics table
CREATE TABLE IF NOT EXISTS menu_item_analytics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    analytics_date DATE NOT NULL,
    total_orders INT DEFAULT 0,
    total_revenue DECIMAL(10, 2) DEFAULT 0,
    average_rating DECIMAL(3, 2),
    popularity_rank INT,
    profit_margin DECIMAL(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    UNIQUE KEY uk_menu_item_analytics (business_id, menu_item_id, analytics_date),
    INDEX idx_menu_item_analytics_business_id (business_id),
    INDEX idx_menu_item_analytics_date (analytics_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Staff Performance table
CREATE TABLE IF NOT EXISTS staff_performance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    performance_date DATE NOT NULL,
    orders_handled INT DEFAULT 0,
    tasks_completed INT DEFAULT 0,
    average_rating DECIMAL(3, 2),
    efficiency_score DECIMAL(5, 2),
    attendance_status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_staff_performance (business_id, user_id, performance_date),
    INDEX idx_staff_performance_business_id (business_id),
    INDEX idx_staff_performance_date (performance_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Delivery Analytics table
CREATE TABLE IF NOT EXISTS delivery_analytics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    analytics_date DATE NOT NULL,
    total_deliveries INT DEFAULT 0,
    on_time_deliveries INT DEFAULT 0,
    late_deliveries INT DEFAULT 0,
    cancelled_deliveries INT DEFAULT 0,
    average_delivery_time INT,
    on_time_percentage DECIMAL(5, 2),
    customer_satisfaction DECIMAL(3, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_delivery_analytics (business_id, analytics_date),
    INDEX idx_delivery_analytics_business_id (business_id),
    INDEX idx_delivery_analytics_date (analytics_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Business Insights table
CREATE TABLE IF NOT EXISTS business_insights (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    insight_type VARCHAR(100) NOT NULL,
    insight_title VARCHAR(255) NOT NULL,
    insight_description TEXT,
    insight_value VARCHAR(255),
    recommendation TEXT,
    priority VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    INDEX idx_business_insights_business_id (business_id),
    INDEX idx_business_insights_type (insight_type),
    INDEX idx_business_insights_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Report Templates table
CREATE TABLE IF NOT EXISTS report_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    template_type VARCHAR(100) NOT NULL,
    report_description TEXT,
    data_fields JSON,
    chart_configs JSON,
    is_public BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    INDEX idx_report_templates_business_id (business_id),
    INDEX idx_report_templates_type (template_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Generated Reports table
CREATE TABLE IF NOT EXISTS generated_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    template_id BIGINT,
    report_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(100),
    report_data JSON,
    generated_by BIGINT,
    start_date DATE,
    end_date DATE,
    file_url VARCHAR(500),
    file_type VARCHAR(50),
    status ENUM('GENERATING', 'READY', 'FAILED', 'EXPIRED') DEFAULT 'READY',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES report_templates(id) ON DELETE SET NULL,
    FOREIGN KEY (generated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_generated_reports_business_id (business_id),
    INDEX idx_generated_reports_status (status),
    INDEX idx_generated_reports_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Report Schedules table
CREATE TABLE IF NOT EXISTS report_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    schedule_name VARCHAR(255) NOT NULL,
    frequency VARCHAR(50),
    day_of_week INT,
    day_of_month INT,
    scheduled_time TIME,
    recipients_email TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    last_generated_at TIMESTAMP,
    next_scheduled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES report_templates(id) ON DELETE CASCADE,
    INDEX idx_report_schedules_business_id (business_id),
    INDEX idx_report_schedules_next_scheduled_at (next_scheduled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create views for common analytics queries
CREATE OR REPLACE VIEW daily_business_summary AS
SELECT
    ans.business_id,
    ans.snapshot_date,
    ans.total_orders,
    ans.total_revenue,
    ans.total_customers,
    ans.average_order_value,
    ra.net_revenue,
    da.on_time_percentage,
    ca.customer_satisfaction_score
FROM analytics_snapshots ans
LEFT JOIN revenue_analytics ra ON ans.business_id = ra.business_id AND ans.snapshot_date = ra.analytics_date
LEFT JOIN delivery_analytics da ON ans.business_id = da.business_id AND ans.snapshot_date = da.analytics_date
LEFT JOIN customer_analytics ca ON ans.business_id = ca.business_id AND ans.snapshot_date = ca.analytics_date;

CREATE OR REPLACE VIEW monthly_performance AS
SELECT
    business_id,
    YEAR(snapshot_date) as year,
    MONTH(snapshot_date) as month,
    SUM(total_orders) as monthly_orders,
    SUM(total_revenue) as monthly_revenue,
    AVG(average_order_value) as avg_order_value,
    COUNT(DISTINCT snapshot_date) as days_active
FROM analytics_snapshots
GROUP BY business_id, YEAR(snapshot_date), MONTH(snapshot_date);

-- Update system settings for Phase 7
INSERT INTO system_settings (setting_key, setting_value, setting_type, description) VALUES
('ANALYTICS_RETENTION_DAYS', '365', 'INTEGER', 'Days to retain analytics data'),
('REPORT_GENERATION_TIMEOUT', '300', 'INTEGER', 'Report generation timeout in seconds'),
('MAX_REPORT_SIZE_MB', '100', 'INTEGER', 'Maximum report file size'),
('AUTO_DELETE_EXPIRED_REPORTS', 'true', 'BOOLEAN', 'Auto delete expired reports'),
('REPORT_EXPIRY_DAYS', '30', 'INTEGER', 'Days before report expires'),
('EXPORT_BATCH_SIZE', '5000', 'INTEGER', 'Rows per batch for export'),
('ANALYTICS_CACHE_HOURS', '24', 'INTEGER', 'Cache analytics data for hours')
ON DUPLICATE KEY UPDATE setting_value = setting_value;

