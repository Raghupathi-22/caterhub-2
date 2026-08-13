-- ============================================================================
-- Cetaring Catering Booking Platform - Phase 3 Database Schema
-- Version: 3.0.0
-- Description: Menu Management System - Templates, Customizations, Inventory
-- ============================================================================

-- Create Menu Templates table
CREATE TABLE IF NOT EXISTS menu_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    thumbnail_url VARCHAR(500),
    cuisine_type VARCHAR(100),
    prep_time_minutes INT DEFAULT 30,
    base_price DECIMAL(10, 2),
    serves_count INT,
    is_public BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    total_uses INT DEFAULT 0,
    rating DECIMAL(3, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    INDEX idx_templates_business_id (business_id),
    INDEX idx_templates_is_public (is_public),
    INDEX idx_templates_is_active (is_active),
    INDEX idx_templates_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Menu Template Items table
CREATE TABLE IF NOT EXISTS menu_template_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_id BIGINT NOT NULL,
    menu_item_id BIGINT,
    item_name VARCHAR(255) NOT NULL,
    category_id BIGINT,
    base_price DECIMAL(10, 2) NOT NULL,
    quantity INT DEFAULT 1,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES menu_templates(id) ON DELETE CASCADE,
    INDEX idx_template_items_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Item Customizations table
CREATE TABLE IF NOT EXISTS item_customizations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_item_id BIGINT NOT NULL,
    customization_name VARCHAR(255) NOT NULL,
    customization_type ENUM('SELECTION', 'MULTIPLE', 'ADDON', 'SIZE') DEFAULT 'SELECTION',
    is_required BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    INDEX idx_item_customizations_menu_item_id (menu_item_id),
    INDEX idx_item_customizations_type (customization_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Customization Options table
CREATE TABLE IF NOT EXISTS customization_options (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customization_id BIGINT NOT NULL,
    option_name VARCHAR(255) NOT NULL,
    option_value VARCHAR(255),
    price_adjustment DECIMAL(10, 2) DEFAULT 0,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customization_id) REFERENCES item_customizations(id) ON DELETE CASCADE,
    INDEX idx_customization_options_customization_id (customization_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Item Addons table
CREATE TABLE IF NOT EXISTS item_addons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_item_id BIGINT NOT NULL,
    addon_name VARCHAR(255) NOT NULL,
    addon_description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    is_vegetarian BOOLEAN DEFAULT FALSE,
    is_vegan BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    INDEX idx_item_addons_menu_item_id (menu_item_id),
    INDEX idx_item_addons_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Menu Tags table
CREATE TABLE IF NOT EXISTS menu_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tag_name VARCHAR(100) NOT NULL UNIQUE,
    tag_color VARCHAR(7),
    description VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_menu_tags_tag_name (tag_name),
    INDEX idx_menu_tags_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Menu-Tags Relationship table
CREATE TABLE IF NOT EXISTS menu_tags_mapping (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES menu_tags(id) ON DELETE CASCADE,
    UNIQUE KEY uk_menu_tag (menu_id, tag_id),
    INDEX idx_menu_tags_mapping_menu_id (menu_id),
    INDEX idx_menu_tags_mapping_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Ingredient Inventory table
CREATE TABLE IF NOT EXISTS ingredient_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    ingredient_name VARCHAR(255) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL DEFAULT 0,
    unit VARCHAR(50) NOT NULL,
    cost_per_unit DECIMAL(10, 2),
    min_stock_level DECIMAL(10, 2),
    max_stock_level DECIMAL(10, 2),
    supplier_name VARCHAR(255),
    last_restocked_at TIMESTAMP,
    expires_at DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    INDEX idx_ingredient_inventory_business_id (business_id),
    INDEX idx_ingredient_inventory_is_active (is_active),
    INDEX idx_ingredient_inventory_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Ingredient-MenuItem Mapping table
CREATE TABLE IF NOT EXISTS menu_item_ingredients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_item_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity_needed DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient_inventory(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_menu_item_ingredient (menu_item_id, ingredient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Item Popularity table
CREATE TABLE IF NOT EXISTS item_popularity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_item_id BIGINT NOT NULL,
    order_date DATE NOT NULL,
    order_count INT DEFAULT 1,
    revenue DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    UNIQUE KEY uk_item_popularity (menu_item_id, order_date),
    INDEX idx_item_popularity_menu_item_id (menu_item_id),
    INDEX idx_item_popularity_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Menu Schedule table
CREATE TABLE IF NOT EXISTS menu_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_id BIGINT NOT NULL,
    schedule_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'CUSTOM') DEFAULT 'DAILY',
    day_of_week INT,
    start_time TIME,
    end_time TIME,
    start_date DATE,
    end_date DATE,
    is_available BOOLEAN DEFAULT TRUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE,
    INDEX idx_menu_schedules_menu_id (menu_id),
    INDEX idx_menu_schedules_start_date (start_date),
    INDEX idx_menu_schedules_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Menu Published History table (for auditing)
CREATE TABLE IF NOT EXISTS menu_publish_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_id BIGINT NOT NULL,
    event_type ENUM('PUBLISHED', 'UNPUBLISHED', 'UPDATED', 'ARCHIVED') DEFAULT 'PUBLISHED',
    published_by BIGINT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE,
    FOREIGN KEY (published_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_menu_publish_history_menu_id (menu_id),
    INDEX idx_menu_publish_history_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default menu tags
INSERT INTO menu_tags (tag_name, tag_color, description) VALUES
('Vegetarian', '#90EE90', 'Vegetarian dishes'),
('Vegan', '#98FF98', 'Vegan dishes'),
('Spicy', '#FF6347', 'Spicy items'),
('Mild', '#FFB6C1', 'Mild flavors'),
('Premium', '#FFD700', 'Premium selection'),
('Budget', '#4169E1', 'Budget-friendly'),
('Organic', '#228B22', 'Organic ingredients'),
('Gluten-Free', '#8B4513', 'Gluten-free options'),
('Dairy-Free', '#696969', 'Dairy-free items'),
('Low-Calorie', '#FF69B4', 'Low calorie options')
ON DUPLICATE KEY UPDATE tag_color = tag_color;

-- Create view for menu popularity stats
CREATE OR REPLACE VIEW menu_item_popularity_stats AS
SELECT
    ip.menu_item_id,
    mi.name as item_name,
    SUM(ip.order_count) as total_orders,
    SUM(ip.revenue) as total_revenue,
    AVG(ip.order_count) as avg_daily_orders,
    MAX(ip.order_date) as last_ordered_date
FROM item_popularity ip
JOIN menu_items mi ON ip.menu_item_id = mi.id
GROUP BY ip.menu_item_id;

-- Create view for inventory status
CREATE OR REPLACE VIEW inventory_status AS
SELECT
    id,
    ingredient_name,
    quantity,
    unit,
    min_stock_level,
    max_stock_level,
    CASE
        WHEN quantity <= min_stock_level THEN 'LOW'
        WHEN quantity >= max_stock_level THEN 'OVERSTOCKED'
        ELSE 'OK'
    END as status,
    expires_at
FROM ingredient_inventory
WHERE is_active = TRUE
ORDER BY status DESC;

-- Create trigger for menu item cost calculation
DELIMITER $$
CREATE TRIGGER calculate_menu_item_cost
AFTER INSERT ON menu_item_ingredients
FOR EACH ROW
BEGIN
    UPDATE menu_items
    SET updated_at = NOW()
    WHERE id = NEW.menu_item_id;
END$$
DELIMITER ;

-- Update system settings for Phase 3
INSERT INTO system_settings (setting_key, setting_value, setting_type, description) VALUES
('TEMPLATE_MAX_ITEMS', '50', 'INTEGER', 'Maximum items per template'),
('CUSTOMIZATION_OPTIONS_LIMIT', '10', 'INTEGER', 'Max customization options per item'),
('ADDON_MAX_PRICE', '5000', 'DECIMAL', 'Maximum addon price in INR'),
('LOW_STOCK_ALERT_ENABLED', 'true', 'BOOLEAN', 'Enable low stock alerts'),
('INVENTORY_COST_TRACKING', 'true', 'BOOLEAN', 'Track ingredient costs'),
('MENU_TEMPLATE_APPROVAL_REQUIRED', 'false', 'BOOLEAN', 'Require approval for templates'),
('ALLERGEN_TRACKING_ENABLED', 'true', 'BOOLEAN', 'Track allergen information')
ON DUPLICATE KEY UPDATE setting_value = setting_value;

