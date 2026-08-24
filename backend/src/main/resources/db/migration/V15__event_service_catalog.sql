CREATE TABLE IF NOT EXISTS event_service_catalog (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    service_key VARCHAR(80) NOT NULL UNIQUE,
    category VARCHAR(80) NOT NULL,
    service_name VARCHAR(150) NOT NULL,
    default_unit VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_event_svc_category (category)
);

CREATE TABLE IF NOT EXISTS event_checklist_templates (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    service_key VARCHAR(80) NOT NULL,
    required_flag BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_event_checklist (event_type, service_key),
    INDEX idx_event_checklist_type (event_type)
);
