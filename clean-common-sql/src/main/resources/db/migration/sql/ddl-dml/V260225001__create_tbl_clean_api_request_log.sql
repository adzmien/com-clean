-- Migration: V260225001__create_tbl_clean_api_request_log.sql
-- Description: Create TBL_CLEAN_API_REQUEST_LOG table for storing external API request and response payload logs
-- Author: Clean Architecture Project
-- Date: 2026-02-25

CREATE TABLE TBL_CLEAN_API_REQUEST_LOG (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    ref_no VARCHAR(64) NOT NULL COMMENT 'Unique reference number per API call',
    status VARCHAR(50) DEFAULT 'SUCCESS' COMMENT 'SUCCESS | FAILED',

    x_trace_id VARCHAR(64) COMMENT 'Distributed tracing ID',
    client_trace_id VARCHAR(64) COMMENT 'Client side trace ID',

    request_json JSON NOT NULL COMMENT 'Full request payload in JSON format',
    response_json JSON COMMENT 'Full response payload in JSON format',

    http_status INT COMMENT 'HTTP response status code',
    duration_ms INT COMMENT 'Execution time in milliseconds',

    created_on DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_on DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),

    UNIQUE KEY uk_ref_no (ref_no),
    INDEX idx_status (status),
    INDEX idx_x_trace_id (x_trace_id),
    INDEX idx_client_trace_id (client_trace_id),
    INDEX idx_created_on (created_on)

) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci;