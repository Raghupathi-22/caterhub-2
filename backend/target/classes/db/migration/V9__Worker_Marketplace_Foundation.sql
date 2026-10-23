-- ============================================================================
-- CaterHub Worker Marketplace Foundation
-- Version: 1.0.0
-- Description: Worker profiles, availability, documents, and booking assignments.
-- ============================================================================

CREATE TABLE IF NOT EXISTS worker_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    worker_type ENUM('CHEF', 'ASSISTANT_CHEF', 'SERVING_BOY', 'SERVING_GIRL', 'CLEANER', 'KITCHEN_HELPER', 'SUPERVISOR') NOT NULL,
    status ENUM('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'REJECTED') NOT NULL DEFAULT 'PENDING_VERIFICATION',
    experience_years INT NOT NULL DEFAULT 0,
    skills TEXT,
    preferred_areas TEXT,
    languages TEXT,
    bio TEXT,
    rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    total_ratings INT NOT NULL DEFAULT 0,
    approved_by BIGINT NULL,
    approved_at TIMESTAMP NULL,
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_worker_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_worker_profiles_approved_by FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_worker_profiles_user_id (user_id),
    INDEX idx_worker_profiles_worker_type (worker_type),
    INDEX idx_worker_profiles_status (status),
    INDEX idx_worker_profiles_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS worker_availabilities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_profile_id BIGINT NOT NULL,
    available_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('AVAILABLE', 'UNAVAILABLE', 'BOOKED') NOT NULL DEFAULT 'AVAILABLE',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_worker_availabilities_profile FOREIGN KEY (worker_profile_id) REFERENCES worker_profiles(id) ON DELETE CASCADE,
    CONSTRAINT chk_worker_availability_time CHECK (start_time < end_time),
    UNIQUE KEY uk_worker_availability_slot (worker_profile_id, available_date, start_time, end_time),
    INDEX idx_worker_availabilities_profile_date (worker_profile_id, available_date),
    INDEX idx_worker_availabilities_date_status (available_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS worker_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_profile_id BIGINT NOT NULL,
    document_type ENUM('AADHAAR', 'PHOTO', 'RESUME', 'OTHER') NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    content_type VARCHAR(100),
    file_size_bytes BIGINT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT NULL,
    reviewed_at TIMESTAMP NULL,
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_worker_documents_profile FOREIGN KEY (worker_profile_id) REFERENCES worker_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_worker_documents_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_worker_documents_profile (worker_profile_id),
    INDEX idx_worker_documents_type_status (document_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS job_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    worker_profile_id BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    worker_type ENUM('CHEF', 'ASSISTANT_CHEF', 'SERVING_BOY', 'SERVING_GIRL', 'CLEANER', 'KITCHEN_HELPER', 'SUPERVISOR') NOT NULL,
    status ENUM('OFFERED', 'ACCEPTED', 'DECLINED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'OFFERED',
    offered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    notes VARCHAR(500),
    decline_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_job_assignments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_assignments_worker_profile FOREIGN KEY (worker_profile_id) REFERENCES worker_profiles(id) ON DELETE RESTRICT,
    CONSTRAINT fk_job_assignments_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_job_assignment_booking_worker (booking_id, worker_profile_id),
    INDEX idx_job_assignments_booking (booking_id),
    INDEX idx_job_assignments_worker_status (worker_profile_id, status),
    INDEX idx_job_assignments_status (status),
    INDEX idx_job_assignments_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

