-- ============================================================================
-- Cetaring Catering Booking Platform - Phase 5 Database Schema
-- Version: 5.0.0
-- Description: Payments System - Razorpay Integration, Invoicing, Refunds
-- ============================================================================

-- Create Payment Gateways table
CREATE TABLE IF NOT EXISTS payment_gateways (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gateway_name VARCHAR(100) NOT NULL UNIQUE,
    gateway_type VARCHAR(50),
    api_key VARCHAR(500),
    api_secret VARCHAR(500),
    webhook_url VARCHAR(500),
    webhook_secret VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    is_sandbox BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_payment_gateways_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    business_id BIGINT NOT NULL,
    gateway_id BIGINT NOT NULL,
    payment_reference VARCHAR(100) NOT NULL UNIQUE,
    razorpay_order_id VARCHAR(100),
    razorpay_payment_id VARCHAR(100),
    razorpay_signature VARCHAR(500),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    payment_method VARCHAR(50),
    payment_type ENUM('ADVANCE', 'BALANCE', 'FULL', 'ONLINE') DEFAULT 'FULL',
    status ENUM('INITIATED', 'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'INITIATED',
    error_message TEXT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE RESTRICT,
    FOREIGN KEY (gateway_id) REFERENCES payment_gateways(id) ON DELETE RESTRICT,
    INDEX idx_payments_booking_id (booking_id),
    INDEX idx_payments_user_id (user_id),
    INDEX idx_payments_business_id (business_id),
    INDEX idx_payments_status (status),
    INDEX idx_payments_payment_reference (payment_reference),
    INDEX idx_payments_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Payment Methods table
CREATE TABLE IF NOT EXISTS payment_methods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    method_type VARCHAR(50),
    method_name VARCHAR(255),
    last_four_digits VARCHAR(4),
    card_brand VARCHAR(50),
    razorpay_token_id VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_payment_methods_user_id (user_id),
    INDEX idx_payment_methods_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Refunds table
CREATE TABLE IF NOT EXISTS refunds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    refund_reference VARCHAR(100) NOT NULL UNIQUE,
    razorpay_refund_id VARCHAR(100),
    refund_amount DECIMAL(10, 2) NOT NULL,
    refund_reason VARCHAR(255),
    refund_type ENUM('FULL', 'PARTIAL', 'CANCELLATION') DEFAULT 'FULL',
    status ENUM('INITIATED', 'PROCESSING', 'COMPLETED', 'FAILED', 'REJECTED') DEFAULT 'INITIATED',
    refund_to ENUM('ORIGINAL_METHOD', 'WALLET', 'BANK_TRANSFER') DEFAULT 'ORIGINAL_METHOD',
    notes TEXT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE RESTRICT,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_refunds_payment_id (payment_id),
    INDEX idx_refunds_booking_id (booking_id),
    INDEX idx_refunds_status (status),
    INDEX idx_refunds_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Invoices table
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    business_id BIGINT NOT NULL,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    invoice_type ENUM('TAX', 'RECEIPT', 'PROFORMA') DEFAULT 'TAX',
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2),
    tax_percentage DECIMAL(5, 2),
    discount_amount DECIMAL(10, 2),
    delivery_charge DECIMAL(10, 2),
    total_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    invoice_date DATE NOT NULL,
    due_date DATE,
    status ENUM('DRAFT', 'ISSUED', 'PAID', 'OVERDUE', 'CANCELLED') DEFAULT 'ISSUED',
    pdf_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE RESTRICT,
    INDEX idx_invoices_booking_id (booking_id),
    INDEX idx_invoices_invoice_number (invoice_number),
    INDEX idx_invoices_status (status),
    INDEX idx_invoices_invoice_date (invoice_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Payment Logs table
CREATE TABLE IF NOT EXISTS payment_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id BIGINT NOT NULL,
    action_type VARCHAR(100),
    action_description TEXT,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    metadata JSON,
    created_by BIGINT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_payment_logs_payment_id (payment_id),
    INDEX idx_payment_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Wallet Transactions table
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    transaction_type ENUM('CREDIT', 'DEBIT', 'REFUND', 'BONUS') DEFAULT 'DEBIT',
    amount DECIMAL(10, 2) NOT NULL,
    balance_before DECIMAL(10, 2),
    balance_after DECIMAL(10, 2),
    reference_id BIGINT,
    reference_type VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_wallet_transactions_user_id (user_id),
    INDEX idx_wallet_transactions_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Payout Transactions table
CREATE TABLE IF NOT EXISTS payout_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    payout_reference VARCHAR(100) NOT NULL UNIQUE,
    payout_amount DECIMAL(10, 2) NOT NULL,
    commission_amount DECIMAL(10, 2),
    net_amount DECIMAL(10, 2),
    payout_date DATE,
    payout_method VARCHAR(50),
    bank_account_id VARCHAR(100),
    status ENUM('INITIATED', 'PROCESSING', 'COMPLETED', 'FAILED', 'REJECTED') DEFAULT 'INITIATED',
    currency VARCHAR(3) DEFAULT 'INR',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE RESTRICT,
    INDEX idx_payout_transactions_business_id (business_id),
    INDEX idx_payout_transactions_status (status),
    INDEX idx_payout_transactions_payout_date (payout_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Payment Reconciliation table
CREATE TABLE IF NOT EXISTS payment_reconciliation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reconciliation_date DATE NOT NULL,
    business_id BIGINT,
    total_payments DECIMAL(10, 2),
    total_refunds DECIMAL(10, 2),
    total_commissions DECIMAL(10, 2),
    net_settlement DECIMAL(10, 2),
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    reconciliation_report TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE SET NULL,
    UNIQUE KEY uk_payment_reconciliation (reconciliation_date, business_id),
    INDEX idx_payment_reconciliation_date (reconciliation_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Payment Settings table
CREATE TABLE IF NOT EXISTS payment_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT,
    setting_key VARCHAR(255) NOT NULL,
    setting_value TEXT,
    setting_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_payment_settings (business_id, setting_key),
    INDEX idx_payment_settings_business_id (business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default payment gateway (Razorpay)
INSERT INTO payment_gateways (gateway_name, gateway_type, is_active, is_sandbox) VALUES
('Razorpay', 'ONLINE_PAYMENT', TRUE, TRUE)
ON DUPLICATE KEY UPDATE is_active = is_active;

-- Create view for payment summary
CREATE OR REPLACE VIEW payment_summary AS
SELECT
    DATE(p.created_at) as payment_date,
    p.business_id,
    COUNT(p.id) as total_transactions,
    SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END) as total_collected,
    SUM(CASE WHEN p.status = 'FAILED' THEN p.amount ELSE 0 END) as failed_amount,
    SUM(CASE WHEN p.status = 'REFUNDED' THEN p.amount ELSE 0 END) as refunded_amount
FROM payments p
GROUP BY DATE(p.created_at), p.business_id;

-- Create view for wallet summary
CREATE OR REPLACE VIEW wallet_summary AS
SELECT
    user_id,
    SUM(CASE WHEN transaction_type IN ('CREDIT', 'REFUND', 'BONUS') THEN amount ELSE 0 END) -
    SUM(CASE WHEN transaction_type = 'DEBIT' THEN amount ELSE 0 END) as current_balance,
    MAX(created_at) as last_transaction
FROM wallet_transactions
GROUP BY user_id;

-- Update system settings for Phase 5
INSERT INTO system_settings (setting_key, setting_value, setting_type, description) VALUES
('RAZORPAY_KEY_ID', '', 'STRING', 'Razorpay API Key ID'),
('RAZORPAY_KEY_SECRET', '', 'STRING', 'Razorpay API Key Secret'),
('PAYMENT_COMMISSION_PERCENTAGE', '8', 'DECIMAL', 'Commission percentage for each payment'),
('AUTO_REFUND_ENABLED', 'true', 'BOOLEAN', 'Enable automatic refund processing'),
('INVOICE_AUTO_GENERATION', 'true', 'BOOLEAN', 'Auto-generate invoices'),
('TAX_PERCENTAGE', '5', 'DECIMAL', 'GST/Tax percentage'),
('SETTLEMENT_CYCLE_DAYS', '3', 'INTEGER', 'Days for settlement after payment'),
('MIN_PAYOUT_AMOUNT', '1000', 'DECIMAL', 'Minimum amount for payout in INR')
ON DUPLICATE KEY UPDATE setting_value = setting_value;

