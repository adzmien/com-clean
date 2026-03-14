-- Migration: V260228002__add_deleted_to_tbl_clean_audit_entity_snapshot.sql
-- Description: Add soft-delete column required by BaseEntity (@SQLRestriction("deleted = false"))
-- Author: Clean Architecture Project
-- Date: 2026-02-28

ALTER TABLE TBL_CLEAN_AUDIT_ENTITY_SNAPSHOT
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Soft-delete flag' AFTER updated_by,
    ADD INDEX idx_deleted (deleted);
