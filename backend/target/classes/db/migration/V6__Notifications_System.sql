-- ============================================================================
-- Cetaring Catering Booking Platform - Phase 6 Database Schema
-- Version: 6.0.0
-- Description: Notifications System - FCM, Email, SMS, In-App
-- ============================================================================

-- Create Notification Templates table
CREATE TABLE IF NOT EXISTS notification_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    channel_type ENUM('FCM', 'EMAIL', 'SMS', 'IN_APP', 'WHATSAPP') DEFAULT 'FCM',
    subject VARCHAR(255),
    body TEXT NOT NULL,
    template_variables VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    INDEX idx_notification_templates_business_id (business_id),
    INDEX idx_notification_templates_channel_type (channel_type),
    INDEX idx_notification_templates_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Notification Channels table
CREATE TABLE IF NOT EXISTS notification_channels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    channel_name VARCHAR(100) NOT NULL,
    channel_type ENUM('FCM', 'EMAIL', 'SMS', 'IN_APP', 'WHATSAPP') DEFAULT 'FCM',
    is_enabled BOOLEAN DEFAULT TRUE,
    api_key VARCHAR(500),
    api_secret VARCHAR(500),
    configuration JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    INDEX idx_notification_channels_business_id (business_id),
    INDEX idx_notification_channels_channel_type (channel_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    business_id BIGINT,
    template_id BIGINT,
    notification_type VARCHAR(100) NOT NULL,
    channel_type ENUM('FCM', 'EMAIL', 'SMS', 'IN_APP', 'WHATSAPP') DEFAULT 'FCM',
    recipient_address VARCHAR(500),
    title VARCHAR(255),
    body TEXT NOT NULL,
    payload JSON,
    status ENUM('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE SET NULL,
    FOREIGN KEY (template_id) REFERENCES notification_templates(id) ON DELETE SET NULL,
    INDEX idx_notifications_user_id (user_id),
    INDEX idx_notifications_business_id (business_id),
    INDEX idx_notifications_status (status),
    INDEX idx_notifications_channel_type (channel_type),
    INDEX idx_notifications_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Notification Delivery Logs table
CREATE TABLE IF NOT EXISTS notification_delivery_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    notification_id BIGINT NOT NULL,
    attempt_number INT,
    status ENUM('PENDING', 'SENT', 'DELIVERED', 'FAILED') DEFAULT 'PENDING',
    error_message TEXT,
    provider_response JSON,
    response_code VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE,
    INDEX idx_notification_delivery_logs_notification_id (notification_id),
    INDEX idx_notification_delivery_logs_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Notification Preferences table
CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    fcm_enabled BOOLEAN DEFAULT TRUE,
    email_enabled BOOLEAN DEFAULT TRUE,
    sms_enabled BOOLEAN DEFAULT FALSE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    order_notifications BOOLEAN DEFAULT TRUE,
    payment_notifications BOOLEAN DEFAULT TRUE,
    promotional_notifications BOOLEAN DEFAULT FALSE,
    system_notifications BOOLEAN DEFAULT TRUE,
    marketing_notifications BOOLEAN DEFAULT FALSE,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR(50) DEFAULT 'UTC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notification_preferences_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create FCM Tokens table
CREATE TABLE IF NOT EXISTS fcm_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    fcm_token VARCHAR(500) NOT NULL UNIQUE,
    device_type VARCHAR(50),
    os_version VARCHAR(50),
    app_version VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_fcm_tokens_user_id (user_id),
    INDEX idx_fcm_tokens_device_id (device_id),
    INDEX idx_fcm_tokens_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Notification Schedules table
CREATE TABLE IF NOT EXISTS notification_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    notification_id BIGINT NOT NULL,
    schedule_type ENUM('ONCE', 'RECURRING', 'BATCH') DEFAULT 'ONCE',
    recurrence_pattern VARCHAR(255),
    next_scheduled_at TIMESTAMP,
    last_executed_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE,
    INDEX idx_notification_schedules_notification_id (notification_id),
    INDEX idx_notification_schedules_next_scheduled_at (next_scheduled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Notification Campaigns table
CREATE TABLE IF NOT EXISTS notification_campaigns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    campaign_name VARCHAR(255) NOT NULL,
    campaign_type VARCHAR(100),
    target_audience VARCHAR(500),
    template_id BIGINT,
    channel_type ENUM('FCM', 'EMAIL', 'SMS', 'IN_APP', 'WHATSAPP') DEFAULT 'FCM',
    status ENUM('DRAFT', 'SCHEDULED', 'SENDING', 'SENT', 'PAUSED', 'CANCELLED') DEFAULT 'DRAFT',
    total_recipients INT DEFAULT 0,
    sent_count INT DEFAULT 0,
    delivered_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    scheduled_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES notification_templates(id) ON DELETE SET NULL,
    INDEX idx_notification_campaigns_business_id (business_id),
    INDEX idx_notification_campaigns_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Email Configurations table
CREATE TABLE IF NOT EXISTS email_configurations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    smtp_host VARCHAR(255) NOT NULL,
    smtp_port INT DEFAULT 587,
    smtp_username VARCHAR(255),
    smtp_password VARCHAR(255),
    from_email VARCHAR(255) NOT NULL,
    from_name VARCHAR(255),
    use_tls BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    INDEX idx_email_configurations_business_id (business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create SMS Configurations table
CREATE TABLE IF NOT EXISTS sms_configurations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    provider_name VARCHAR(100),
    api_key VARCHAR(500),
    api_secret VARCHAR(500),
    sender_id VARCHAR(50),
    base_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    INDEX idx_sms_configurations_business_id (business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create views for notification analytics
CREATE OR REPLACE VIEW notification_delivery_stats AS
SELECT
    DATE(n.created_at) as delivery_date,
    n.channel_type,
    COUNT(n.id) as total_sent,
    SUM(CASE WHEN n.status = 'DELIVERED' THEN 1 ELSE 0 END) as delivered,
    SUM(CASE WHEN n.status = 'FAILED' THEN 1 ELSE 0 END) as failed,
    ROUND(SUM(CASE WHEN n.status = 'DELIVERED' THEN 1 ELSE 0 END) * 100.0 / COUNT(n.id), 2) as delivery_rate
FROM notifications n
GROUP BY DATE(n.created_at), n.channel_type;

CREATE OR REPLACE VIEW campaign_performance AS
SELECT
    nc.id,
    nc.campaign_name,
    nc.total_recipients,
    nc.sent_count,
    nc.delivered_count,
    nc.failed_count,
    ROUND(nc.delivered_count * 100.0 / nc.sent_count, 2) as delivery_rate,
    nc.status
FROM notification_campaigns nc;

-- Update system settings for Phase 6
INSERT INTO system_settings (setting_key, setting_value, setting_type, description) VALUES
('FCM_API_KEY', '', 'STRING', 'Firebase Cloud Messaging API Key'),
('EMAIL_BATCH_SIZE', '100', 'INTEGER', 'Email batch size for sending'),
('SMS_BATCH_SIZE', '50', 'INTEGER', 'SMS batch size for sending'),
('NOTIFICATION_RETRY_INTERVAL', '300', 'INTEGER', 'Retry interval in seconds'),
('NOTIFICATION_MAX_RETRIES', '3', 'INTEGER', 'Maximum retry attempts'),
('QUIET_HOURS_ENABLED', 'true', 'BOOLEAN', 'Enable quiet hours'),
('NOTIFICATION_RETENTION_DAYS', '90', 'INTEGER', 'Days to retain notification logs')
ON DUPLICATE KEY UPDATE setting_value = setting_value;

