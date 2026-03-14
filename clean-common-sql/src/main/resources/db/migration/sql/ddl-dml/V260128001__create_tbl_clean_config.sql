-- Migration: V251229001__create_tbl_clean_config.sql
-- Description: Create TBL_CLEAN_CONFIG table for environment-specific configuration properties
-- Author: Clean Architecture Project
-- Date: 2025-12-29

CREATE TABLE TBL_CLEAN_CONFIG (
    -- Primary key: auto-increment id (JPA standard pattern)
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Auto-generated surrogate primary key',

    -- Property key: unique business identifier
    prop_key VARCHAR(255) NOT NULL UNIQUE COMMENT 'Property key/name (e.g., app.feature.enabled)',

    -- Environment-specific values (wide table design)
    dev_value TEXT COMMENT 'Property value for DEV environment',
    sit_value TEXT COMMENT 'Property value for SIT (System Integration Testing) environment',
    uat_value TEXT COMMENT 'Property value for UAT (User Acceptance Testing) environment',
    prod_value TEXT COMMENT 'Property value for PROD (Production) environment',
    dr_value TEXT COMMENT 'Property value for DR (Disaster Recovery) environment',

    -- Metadata fields
    description TEXT COMMENT 'Description of what this property is for',
    category VARCHAR(100) COMMENT 'Category/group for organizing properties (e.g., database, feature-flags, api)',
    data_type VARCHAR(50) DEFAULT 'string' COMMENT 'Data type: string, int, boolean, json',
    is_sensitive BOOLEAN DEFAULT FALSE COMMENT 'Flag for sensitive/encrypted properties (passwords, API keys)',

    -- Audit fields (managed by Spring Data JPA auditing)
    created_on DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when property was created',
    created_by VARCHAR(255) COMMENT 'Username who created the property',
    updated_on DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Timestamp when property was last updated',
    updated_by VARCHAR(255) COMMENT 'Username who last updated the property',

    -- Indexes
    INDEX idx_prop_key (prop_key),
    INDEX idx_category (category),
    INDEX idx_is_sensitive (is_sensitive)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Environment-specific configuration properties with wide table design';
