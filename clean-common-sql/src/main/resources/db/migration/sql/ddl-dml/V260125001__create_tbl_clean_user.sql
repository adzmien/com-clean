-- Migration: V260125001__create_tbl_clean_user.sql
-- Description: Create TBL_CLEAN_USER table
-- Author: Clean Architecture Project
-- Date: 2026-01-25

-- Create user management table
CREATE TABLE TBL_CLEAN_USER (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    domain VARCHAR(255) COMMENT 'User domain / tenant',
    status VARCHAR(50) DEFAULT 'ACTIVE',
    name1 VARCHAR(255),
    name2 VARCHAR(255),
    name3 VARCHAR(255),
    addr1 VARCHAR(255),
    addr2 VARCHAR(255),
    addr3 VARCHAR(255),
    postcode VARCHAR(20),
    state VARCHAR(100),
    country VARCHAR(100),
    mobile_no VARCHAR(20),
    email VARCHAR(255),
    created_on DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_on DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    UNIQUE KEY uk_username (username),
    INDEX idx_domain (domain),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
