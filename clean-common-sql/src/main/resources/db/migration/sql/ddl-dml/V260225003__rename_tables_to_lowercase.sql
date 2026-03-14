-- Migration: V260225003__rename_tables_to_lowercase.sql
-- Description: Rename all tables from UPPERCASE to lowercase for Hibernate schema validation compatibility
-- Background: MariaDB 11.8 enforces case-sensitive table name lookups in information_schema;
--             all entities use lowercase @Table names so DB tables must match exactly.
-- Author: Clean Architecture Project
-- Date: 2026-02-25

RENAME TABLE TBL_CLEAN_USER TO tbl_clean_user;
RENAME TABLE TBL_CLEAN_CONFIG TO tbl_clean_config;
RENAME TABLE TBL_CLEAN_API_REQUEST_LOG TO tbl_clean_api_request_log;
