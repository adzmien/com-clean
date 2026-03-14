-- Migration: V260225002__add_deleted_to_tbl_clean_api_request_log.sql
-- Description: Add soft-delete column to TBL_CLEAN_API_REQUEST_LOG for BaseEntity compatibility
-- Author: Clean Architecture Project
-- Date: 2026-02-25

ALTER TABLE TBL_CLEAN_API_REQUEST_LOG
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Soft-delete flag' AFTER updated_by,
    ADD INDEX idx_deleted (deleted);
