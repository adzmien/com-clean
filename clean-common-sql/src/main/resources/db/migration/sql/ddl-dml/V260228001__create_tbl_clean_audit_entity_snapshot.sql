-- Migration: V260228001__create_tbl_clean_audit_entity_snapshot.sql
-- Description: Create TBL_CLEAN_AUDIT_ENTITY_SNAPSHOT table for storing entity-level before and after snapshots
-- Author: Clean Architecture Project
-- Date: 2026-02-28

CREATE TABLE tbl_clean_audit_entity_snapshot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    ref_no VARCHAR(64) NOT NULL COMMENT 'Unique reference number per transaction',

    txn_type VARCHAR(80) NOT NULL COMMENT 'Business transaction type (e.g. USER_UPDATE, NOTICE_APPROVE)',
    action VARCHAR(20) NOT NULL COMMENT 'CREATE | UPDATE | DELETE',

    entity_name VARCHAR(120) NOT NULL COMMENT 'Entity class name',
    entity_id VARCHAR(80) NOT NULL COMMENT 'Primary key value of the entity',
    entity_version BIGINT COMMENT 'Entity version (if using @Version for optimistic locking)',

    before_json JSON COMMENT 'Full entity snapshot before change (NULL for CREATE)',
    after_json JSON COMMENT 'Full entity snapshot after change (NULL for DELETE)',

    status VARCHAR(50) DEFAULT 'SUCCESS' COMMENT 'SUCCESS | FAILED',
    error_code VARCHAR(100) COMMENT 'Application error code if failed',
    error_message VARCHAR(255) COMMENT 'Short error message if failed',

    x_trace_id VARCHAR(64) COMMENT 'Distributed tracing ID',
    client_trace_id VARCHAR(64) COMMENT 'Client side trace ID',

    actor_type VARCHAR(30) DEFAULT 'USER' COMMENT 'USER | SYSTEM | SERVICE',
    actor_id VARCHAR(80) COMMENT 'User ID performing the transaction',
    actor_name VARCHAR(120) COMMENT 'User display name',
    actor_group_id VARCHAR(80) COMMENT 'User group ID',

    http_method VARCHAR(10) COMMENT 'HTTP method (GET, POST, PUT, DELETE)',
    path_pattern VARCHAR(255) COMMENT 'Best matching request mapping pattern',
    handler VARCHAR(255) COMMENT 'Controller or handler method name',

    duration_ms INT COMMENT 'Execution time in milliseconds',

    created_on DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_on DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),

    UNIQUE KEY uk_ref_no (ref_no),
    INDEX idx_txn_type (txn_type),
    INDEX idx_entity (entity_name, entity_id),
    INDEX idx_status (status),
    INDEX idx_x_trace_id (x_trace_id),
    INDEX idx_client_trace_id (client_trace_id),
    INDEX idx_actor_id (actor_id),
    INDEX idx_created_on (created_on)

) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci;