# Entity Relationship Diagram

**Date:** 2026-03-14
**Module:** `clean-common-sql`
**Database:** MariaDB | **Schema:** `clean_dev`

---

## Entity Relationship Diagram

```mermaid
erDiagram
    direction TB

    tbl_clean_user {
        bigint      id              PK  "AUTO_INCREMENT"
        varchar(100) username       UK  "Unique login identifier"
        varchar(100) domain             "Tenant domain (e.g. corp.com)"
        varchar(20)  status             "ACTIVE | INACTIVE | PENDING | SUSPENDED"
        varchar(100) name1              "First name"
        varchar(100) name2              "Middle name"
        varchar(100) name3              "Last name"
        varchar(200) addr1              "Address line 1"
        varchar(200) addr2              "Address line 2"
        varchar(200) addr3              "Address line 3"
        varchar(20)  postcode           "Postal code"
        varchar(100) state              "State / Province"
        varchar(100) country            "Country"
        varchar(20)  mobile_no          "Mobile number"
        varchar(100) email              "Email address"
        timestamp    created_on         "DEFAULT CURRENT_TIMESTAMP"
        varchar(100) created_by
        timestamp    updated_on         "DEFAULT CURRENT_TIMESTAMP ON UPDATE"
        varchar(100) updated_by
        tinyint      deleted            "DEFAULT 0 — soft-delete flag"
    }

    tbl_clean_config {
        bigint      id              PK  "AUTO_INCREMENT"
        varchar(200) prop_key       UK  "Unique property key"
        text         dev_value          "Value for dev environment"
        text         sit_value          "Value for SIT environment"
        text         uat_value          "Value for UAT environment"
        text         prod_value         "Value for production environment"
        text         dr_value           "Value for DR environment"
        text         description        "Human-readable description"
        varchar(100) category           "Grouping label (e.g. feature-flags, security)"
        varchar(50)  data_type          "string | int | boolean | json"
        tinyint      is_sensitive       "1 = masked in logs/UI"
        timestamp    created_on         "DEFAULT CURRENT_TIMESTAMP"
        varchar(100) created_by
        timestamp    updated_on         "DEFAULT CURRENT_TIMESTAMP ON UPDATE"
        varchar(100) updated_by
        tinyint      deleted            "DEFAULT 0 — soft-delete flag"
    }

    tbl_clean_api_request_log {
        bigint      id              PK  "AUTO_INCREMENT"
        varchar(100) ref_no         UK  "Unique request reference number"
        varchar(20)  status             "SUCCESS | FAILED"
        varchar(100) x_trace_id         "Inbound trace header"
        varchar(100) client_trace_id    "Client-supplied trace ID"
        json         request_json       "Full request payload"
        json         response_json      "Full response payload"
        int          http_status        "HTTP response code"
        bigint       duration_ms        "Request processing time (ms)"
        timestamp    created_on         "DEFAULT CURRENT_TIMESTAMP"
        varchar(100) created_by
        timestamp    updated_on         "DEFAULT CURRENT_TIMESTAMP ON UPDATE"
        varchar(100) updated_by
        boolean      deleted            "DEFAULT FALSE — soft-delete flag"
    }

    tbl_clean_audit_entity_snapshot {
        bigint      id              PK  "AUTO_INCREMENT"
        varchar(100) ref_no         UK  "Unique audit record reference"
        varchar(100) txn_type           "Transaction/operation type label"
        varchar(20)  action             "CREATE | UPDATE | DELETE"
        varchar(200) entity_name        "Fully-qualified entity class name"
        varchar(100) entity_id          "ID of the audited entity"
        bigint       entity_version     "Entity @Version at time of snapshot"
        json         before_json        "Entity state before the operation"
        json         after_json         "Entity state after the operation"
        varchar(20)  status             "SUCCESS | FAILED"
        varchar(100) error_code         "Error code (on failure)"
        text         error_message      "Error detail (on failure)"
        varchar(100) x_trace_id         "Inbound trace header"
        varchar(100) client_trace_id    "Client-supplied trace ID"
        varchar(20)  actor_type         "USER | SYSTEM | SERVICE"
        varchar(100) actor_id           "ID of the acting principal"
        varchar(200) actor_name         "Display name of the actor"
        varchar(100) actor_group_id     "Group/role of the actor"
        varchar(10)  http_method        "HTTP method of originating request"
        varchar(500) path_pattern       "URL path pattern of originating request"
        varchar(200) handler            "Controller handler method"
        bigint       duration_ms        "Audit operation processing time (ms)"
        timestamp    created_on         "DEFAULT CURRENT_TIMESTAMP"
        varchar(100) created_by
        timestamp    updated_on         "DEFAULT CURRENT_TIMESTAMP ON UPDATE"
        varchar(100) updated_by
        boolean      deleted            "DEFAULT FALSE — soft-delete flag"
    }

    tbl_clean_user                  ||--o{ tbl_clean_audit_entity_snapshot : "actor_id (logical — no FK)"
    tbl_clean_api_request_log       ||--o{ tbl_clean_audit_entity_snapshot : "x_trace_id correlation (logical — no FK)"
```

---

## Relationships

| From | To | Join Field | Type |
|------|----|------------|------|
| `tbl_clean_user` | `tbl_clean_audit_entity_snapshot` | `actor_id` ↔ `username` | Logical (no FK) |
| `tbl_clean_api_request_log` | `tbl_clean_audit_entity_snapshot` | `x_trace_id` ↔ `x_trace_id` | Logical (no FK) |

> **No foreign key constraints exist in the DDL.** Relationships are logical and resolved at the application/query level. This is intentional — the audit snapshot table must remain writable even if the referenced user or log record is deleted or unavailable.

---

## Index Summary

### tbl_clean_user

| Index | Columns | Type |
|-------|---------|------|
| `uk_username` | `username` | UNIQUE |
| `idx_domain` | `domain` | INDEX |
| `idx_status` | `status` | INDEX |

### tbl_clean_config

| Index | Columns | Type |
|-------|---------|------|
| `uk_prop_key` | `prop_key` | UNIQUE |
| `idx_prop_key` | `prop_key` | INDEX |
| `idx_category` | `category` | INDEX |
| `idx_is_sensitive` | `is_sensitive` | INDEX |

### tbl_clean_api_request_log

| Index | Columns | Type |
|-------|---------|------|
| `uk_ref_no` | `ref_no` | UNIQUE |
| `idx_status` | `status` | INDEX |
| `idx_x_trace_id` | `x_trace_id` | INDEX |
| `idx_client_trace_id` | `client_trace_id` | INDEX |
| `idx_created_on` | `created_on` | INDEX |
| `idx_deleted` | `deleted` | INDEX |

### tbl_clean_audit_entity_snapshot

| Index | Columns | Type |
|-------|---------|------|
| `uk_ref_no` | `ref_no` | UNIQUE |
| `idx_txn_type` | `txn_type` | INDEX |
| `idx_entity` | `entity_name`, `entity_id` | COMPOSITE INDEX |
| `idx_status` | `status` | INDEX |
| `idx_x_trace_id` | `x_trace_id` | INDEX |
| `idx_client_trace_id` | `client_trace_id` | INDEX |
| `idx_actor_id` | `actor_id` | INDEX |
| `idx_created_on` | `created_on` | INDEX |

---

## Migration History

| Version | File | Table Affected | Change |
|---------|------|----------------|--------|
| V260125001 | `create_tbl_clean_user.sql` | `tbl_clean_user` (as TBL_CLEAN_USER) | Initial create |
| V260128001 | `create_tbl_clean_config.sql` | `tbl_clean_config` (as TBL_CLEAN_CONFIG) | Initial create |
| V260221001 | `alter_tbl_clean_config_add_deleted.sql` | `tbl_clean_config` | Add `deleted TINYINT(1)` |
| V260225001 | `create_tbl_clean_api_request_log.sql` | `tbl_clean_api_request_log` (uppercase) | Initial create |
| V260225002 | `add_deleted_to_tbl_clean_api_request_log.sql` | `tbl_clean_api_request_log` (uppercase) | Add `deleted BOOLEAN` + `idx_deleted` |
| V260225003 | `rename_tables_to_lowercase.sql` | user, config, api_request_log | Rename all to lowercase |
| V260228001 | `create_tbl_clean_audit_entity_snapshot.sql` | `tbl_clean_audit_entity_snapshot` (uppercase) | Initial create |
| V260228002 | `add_deleted_to_tbl_clean_audit_entity_snapshot.sql` | `tbl_clean_audit_entity_snapshot` (uppercase) | Add `deleted BOOLEAN` |
| V260302001 | `rename_audit_table_to_lowercase.sql` | audit snapshot | Rename to lowercase |
