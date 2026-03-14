-- Migration: V260221001__alter_tbl_clean_config_add_deleted.sql
-- Description: Add soft-delete flag column to TBL_CLEAN_CONFIG to match current schema
-- Author: Clean Architecture Project
-- Date: 2026-02-21

ALTER TABLE TBL_CLEAN_CONFIG
    ADD COLUMN IF NOT EXISTS deleted TINYINT(1) NOT NULL DEFAULT 0
    COMMENT 'Soft-delete flag (0=active, 1=deleted)'
    AFTER updated_by;
