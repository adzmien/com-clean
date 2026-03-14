# DDL and DML Migrations - Schema and Data Management

This directory contains Flyway migrations for database schema definitions and data manipulation.

## Structure

This unified folder combines two types of migrations:

### DDL (Data Definition Language) - Schema Migrations
- Table creation and structure modifications
- Index and constraint definitions
- View and stored procedure definitions
- Database object management

### DML (Data Manipulation Language) - Data Migrations
- Reference/master data insertion
- Data corrections and updates
- Bulk data operations
- Data migration between environments

## Naming Convention

```
V{YYMMDD}{XXX}__{description}.sql

Example: V260125001__create_users_table.sql
```

Where:
- V = Flyway version prefix (required)
- YYMMDD = Year/Month/Day (e.g., 260125 for Jan 25, 2026)
- XXX = Sequential number for same day (001, 002, 003...)
- __ = Double underscore separator (Flyway requirement)
- description = Snake_case description

### Examples:
- `V260125001__create_users_table.sql` - Create users table
- `V260125002__insert_user_roles.sql` - Standard user roles
- `V260125003__add_users_email_index.sql` - Add email index
- `V260125004__update_legacy_user_status.sql` - Data corrections

## File Organization

- Group related tables by domain/module
- Use descriptive names indicating the action
- Follow date-based versioning for chronological order
- For DDL: Prefix with "create_", "alter_", "add_", "drop_"
- For DML: Prefix with "insert_", "update_", "delete_"

## Best Practices

### 1. DDL Best Practices

**Table Creation:**
- Always use InnoDB engine
- Set charset to utf8mb4
- Include audit fields (created_on, created_by, updated_on, updated_by)
- Add appropriate indexes
- Include table and column comments

**Migration Headers:**
```sql
-- Migration: V260125001__create_users_table.sql
-- Description: Create users table with authentication fields
-- Author: Clean Architecture Project
-- Date: 2026-01-25
```

**Example Table Structure:**
```sql
CREATE TABLE IF NOT EXISTS TBL_USER (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique username',
    email VARCHAR(255) NOT NULL COMMENT 'User email address',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT 'User status',
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    created_by VARCHAR(100) COMMENT 'Creator username',
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update timestamp',
    updated_by VARCHAR(100) COMMENT 'Last updater username',
    UNIQUE KEY uk_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User accounts table';
```

**Indexes:**
- Prefix unique keys with `uk_`
- Prefix regular indexes with `idx_`
- Create composite indexes for multi-column queries
- Index foreign keys for join performance

### 2. DML Best Practices

**Migration Headers:**
```sql
-- Migration: V260125005__insert_user_roles.sql
-- Description: Insert standard user roles (admin, user, guest)
-- Author: Clean Architecture Project
-- Date: 2026-01-25
```

**Reference Data Example:**
```sql
-- Insert standard user roles
INSERT INTO TBL_USER_ROLE (role_name, description, permissions, created_on, created_by) VALUES
('ADMIN', 'System administrator with full access', 'ALL', CURRENT_TIMESTAMP, 'SYSTEM'),
('USER', 'Standard user with basic access', 'READ,WRITE', CURRENT_TIMESTAMP, 'SYSTEM'),
('GUEST', 'Guest user with read-only access', 'READ', CURRENT_TIMESTAMP, 'SYSTEM');
```

**Use Transactions:**
```sql
START TRANSACTION;
-- Your DML statements here
UPDATE TBL_USER SET status = 'ACTIVE' WHERE status = 'PENDING';
COMMIT;
```

**Make Migrations Idempotent:**
```sql
-- Safe to run multiple times
INSERT IGNORE INTO TBL_CONFIG (config_key, config_value) VALUES
('max_login_attempts', '3'),
('session_timeout', '30');

-- Or use ON DUPLICATE KEY UPDATE
INSERT INTO TBL_CONFIG (config_key, config_value) VALUES
('max_login_attempts', '3')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);
```

**Data Validation:**
```sql
-- Validate data before update
SELECT COUNT(*) INTO @count FROM TBL_USER WHERE status IS NULL;
-- Only proceed if validation passes
UPDATE TBL_USER SET status = 'INACTIVE' WHERE status IS NULL AND @count > 0;
```

### 3. Coordination Between DDL and DML

When creating related migrations:

1. **DDL First**: Create the table structure (e.g., V260125001)
2. **DML Second**: Insert reference data (e.g., V260125002)
3. **Maintain Dependencies**: Document relationships in migration headers

Example:
```sql
-- V260125001__create_roles_table.sql
CREATE TABLE TBL_ROLE (id INT PRIMARY KEY, name VARCHAR(50));

-- V260125002__insert_standard_roles.sql
INSERT INTO TBL_ROLE VALUES (1, 'ADMIN'), (2, 'USER'), (3, 'GUEST');
```

### 4. Rollback Consideration

For each migration, create a corresponding rollback script in:
`/rollback/{YYMMDD}/V{YYMMDD}{XXX}__rollback_{description}.sql`

**Example rollback for DDL:**
```sql
-- Rollback: Drop users table
DROP TABLE IF EXISTS TBL_USER;
```

**Example rollback for DML:**
```sql
-- Rollback: Remove inserted roles
DELETE FROM TBL_USER_ROLE WHERE role_name IN ('ADMIN', 'USER', 'GUEST');
```

## Deployment

Safe for production when using:
```bash
./gradlew flywayMigrateDdlDml
./gradlew flywayMigrateDdlDml -Pflyway.url=jdbc:mariadb://prod-db:3306/clean
```

## Important Notes

- **Never include production credentials** in DML scripts
- **Test thoroughly** in development before production deployment
- **Document dependencies** between DDL and DML migrations
- **Use appropriate version numbers** to ensure correct execution order
- **Consider data volume** for performance implications

See the main README.md for detailed usage instructions.
