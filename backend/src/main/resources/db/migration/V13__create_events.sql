-- Create events and event_requirements tables

CREATE TABLE IF NOT EXISTS events (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_date DATE NOT NULL,
    start_time TIME NULL,
    end_time TIME NULL,
    location VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,
    guest_count INT,
    budget DECIMAL(15,2),
    created_by BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_events_created_by (created_by),
    INDEX idx_events_event_date (event_date)
);

CREATE TABLE IF NOT EXISTS event_requirements (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    category VARCHAR(100) NOT NULL,
    planned_amount DECIMAL(15,2),
    booked_amount DECIMAL(15,2),
    required_flag BOOLEAN DEFAULT TRUE,
    INDEX idx_event_requirements_event_id (event_id),
    CONSTRAINT fk_event_req_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);
