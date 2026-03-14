# Dummy Data Migrations - Test & Development Data

This directory contains Flyway migrations for test data and sample records.

## Purpose
- Development environment sample data
- QA testing datasets
- Performance testing data
- Demo and presentation data

## Naming Convention

```
V{YYMMDD}{XXX}__{env}_sample_{description}.sql

Example: V260125010__dev_sample_users.sql
```

## File Organization
- Prefix with environment (dev, test, staging)
- Group by functional area or user story
- Include realistic but anonymized data
- Use higher version numbers to execute after DDL/DML

## Examples
- `V260125010__dev_sample_users.sql` - Development user accounts
- `V260125011__test_auth_scenarios.sql` - Authentication test cases
- `V260126010__demo_product_catalog.sql` - Demo product data
- `V260126011__perf_large_dataset.sql` - Performance testing data

## Best Practices

### 1. Migration Headers
Always include a header comment:
```sql
-- Migration: V260125010__dev_sample_users.sql
-- Description: Sample user accounts for development environment
-- Author: Clean Architecture Project
-- Date: 2026-01-25
-- Environment: DEVELOPMENT ONLY
```

### 2. Sample Data Example
```sql
-- Insert sample users for development
INSERT INTO TBL_USER (username, email, status, created_on, created_by) VALUES
('dev.admin', 'admin@example.local', 'ACTIVE', CURRENT_TIMESTAMP, 'SYSTEM'),
('dev.user1', 'user1@example.local', 'ACTIVE', CURRENT_TIMESTAMP, 'SYSTEM'),
('dev.user2', 'user2@example.local', 'ACTIVE', CURRENT_TIMESTAMP, 'SYSTEM'),
('dev.guest', 'guest@example.local', 'INACTIVE', CURRENT_TIMESTAMP, 'SYSTEM');
```

### 3. Environment Safety
Add environment checks to prevent accidental production execution:
```sql
-- Safety check: Only run in development
SET @env = (SELECT config_value FROM TBL_CONFIG WHERE config_key = 'environment');

-- Conditional execution (MariaDB/MySQL)
INSERT INTO TBL_USER (username, email, status)
SELECT 'dev.admin', 'admin@example.local', 'ACTIVE'
WHERE @env IN ('dev', 'development', 'local');
```

### 4. Realistic but Fictional
Use realistic data patterns without real personal information:
```sql
-- Good: Fictional but realistic
INSERT INTO TBL_USER (username, email, phone) VALUES
('john.doe', 'john.doe@example.local', '+1-555-0100'),
('jane.smith', 'jane.smith@example.local', '+1-555-0101');

-- Bad: Real personal information
-- NEVER use real names, emails, or phone numbers
```

### 5. Data Relationships
Document and maintain relationships between test data:
```sql
-- Create test users
INSERT INTO TBL_USER (id, username, email) VALUES
(1, 'test.user1', 'user1@example.local'),
(2, 'test.user2', 'user2@example.local');

-- Create related test orders (depends on users above)
INSERT INTO TBL_ORDER (user_id, order_number, status) VALUES
(1, 'ORD-001', 'COMPLETED'),
(1, 'ORD-002', 'PENDING'),
(2, 'ORD-003', 'SHIPPED');
```

### 6. Performance Testing Data
For large datasets, use procedures or loops:
```sql
-- Generate 1000 test users
DELIMITER $$
CREATE PROCEDURE generate_test_users()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 1000 DO
        INSERT INTO TBL_USER (username, email, status)
        VALUES (
            CONCAT('test.user', i),
            CONCAT('user', i, '@example.local'),
            'ACTIVE'
        );
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL generate_test_users();
DROP PROCEDURE IF EXISTS generate_test_users;
```

## Deployment

**NEVER deploy to production.** Use only in development/test:

```bash
# Apply dummy migrations (uses separate history table)
./gradlew flywayMigrateDummy

# Check dummy migration status
./gradlew flywayInfoDummy

# Validate dummy migrations
./gradlew flywayValidateDummy
```

**Migration History Tables:**
- Dummy migrations → `flyway_schema_history_dummy` (isolated tracking)
- Production migrations → `flyway_schema_history` (clean, production-only)

**For production, exclude dummy data:**
```bash
./gradlew flywayMigrateDdlDml  # Recommended for production
```

**Best Practice - Development Environment:**
```bash
# First apply production migrations
./gradlew flywayMigrateDdlDml

# Then apply dummy data (separate history table)
./gradlew flywayMigrateDummy

# Verify separation
./gradlew flywayInfo       # Production migrations only
./gradlew flywayInfoDummy  # Dummy migrations only
```

## Guidelines

- **Never include production data** - Use only fictional test data
- **Use realistic patterns** - Data should mimic real-world scenarios
- **Ensure privacy compliance** - No real personal information
- **Document dependencies** - Note relationships between test data
- **Keep it maintainable** - Avoid overly complex or large datasets
- **Version appropriately** - Use higher version numbers (e.g., 010+) to execute last
- **Environment labeling** - Clearly mark as development/test only

## Important Notes

⚠️ **WARNING**: This folder should NEVER be deployed to production environments.

Configure your CI/CD pipeline to exclude dummy migrations:
```yaml
# Production deployment (CI/CD example)
- name: Deploy Database
  run: ./gradlew flywayMigrateDdlDml  # Excludes dummy folder
```
