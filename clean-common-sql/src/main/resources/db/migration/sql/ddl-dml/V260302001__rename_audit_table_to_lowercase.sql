-- Migration: V260302001__rename_audit_table_to_lowercase.sql
-- Description: Rename audit entity snapshot table from UPPERCASE to lowercase for Hibernate schema validation compatibility
-- Background: MariaDB 11.8 enforces case-sensitive table name lookups in information_schema;
--             all entities use lowercase @Table names so DB tables must match exactly.
-- Author: Clean Architecture Project
-- Date: 2026-03-02

RENAME TABLE TBL_CLEAN_AUDIT_ENTITY_SNAPSHOT TO tbl_clean_audit_entity_snapshot;
