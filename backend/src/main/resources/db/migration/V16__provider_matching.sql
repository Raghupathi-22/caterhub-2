ALTER TABLE businesses ADD COLUMN IF NOT EXISTS min_capacity INT NULL;
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS max_capacity INT NULL;
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS service_radius_km INT NULL;
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS service_category VARCHAR(80) NULL;
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS price_per_unit DECIMAL(12,2) NULL;
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS veg_only BOOLEAN NULL;
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS completed_events INT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS business_service_offerings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    service_key VARCHAR(80) NOT NULL,
    min_capacity INT NULL,
    max_capacity INT NULL,
    price_per_unit DECIMAL(12,2) NULL,
    unit VARCHAR(30) NULL,
    city VARCHAR(100) NULL,
    veg_supported BOOLEAN NOT NULL DEFAULT TRUE,
    non_veg_supported BOOLEAN NOT NULL DEFAULT TRUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_bso_business FOREIGN KEY (business_id) REFERENCES businesses(id),
    UNIQUE KEY uk_bso_business_service (business_id, service_key),
    INDEX idx_bso_service (service_key),
    INDEX idx_bso_city (city)
);
