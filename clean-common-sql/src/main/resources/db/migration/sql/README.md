# SQL Migration Organization

This directory contains Flyway migration scripts organized by type for better maintainability and deployment control.

## Directory Structure

### `/ddl-dml` - Data Definition & Manipulation Language
Contains both schema definitions and data manipulation migrations:
- Table creation and modifications
- Index definitions
- Constraint definitions
- View definitions
- Stored procedure definitions
- INSERT statements for reference/master data
- UPDATE statements for data corrections
- DELETE statements for data cleanup
- Data transformation scripts

**Deployment:** Production-safe, can be deployed independently

### `/dummy` - Test/Development Data
Contains test data and dummy records:
- Sample data for development
- Test dataset for QA environments
- Performance testing data
- Demo data for presentations

**Deployment:** Development and QA only, **never production**

### `/rollback` - Rollback Scripts
Contains date-organized rollback scripts:
- Organized by migration date (YYMMDD format)
- Used by the `flywayRollback` custom task
- Auto-created when needed

**Structure:** `rollback/{YYMMDD}/V{YYMMDD}{XXX}__rollback_{description}.sql`

## Migration Naming Convention

All migrations follow the date-based pattern:

```
V{YYMMDD}{XXX}__{description}.sql

Where:
- V = Flyway version prefix (required)
- YYMMDD = Year/Month/Day (e.g., 260125 for Jan 25, 2026)
- XXX = Sequential number for same day (001, 002, 003...)
- __ = Double underscore separator (Flyway requirement)
- description = Snake_case description
```

### Examples:
- `ddl-dml/V260125001__create_users_table.sql`
- `ddl-dml/V260125002__insert_user_roles.sql`
- `dummy/V260125003__dev_sample_users.sql`

## Execution Order

Flyway executes migrations from all folders in version order:
1. V260125001 (from any folder)
2. V260125002 (from any folder)
3. V260125003 (from any folder)

Use custom tasks to control which folders are deployed:
- `./gradlew flywayMigrateDummy` - Dummy data only
- `./gradlew flywayMigrateDdlDml` - DDL + DML (production recommended)
- `./gradlew flywayMigrate` - All folders (includes dummy)

## Best Practices

1. **Separate Concerns** - Keep production migrations (DDL/DML) separate from dummy data
2. **Production Safety** - Use `flywayMigrateDdlDml` for production deployments
3. **Version Numbers** - Use sequential version numbers to control execution order
4. **Rollback Scripts** - Create rollback scripts for every migration
5. **Test First** - Always test migrations in development before production

See the main README.md for detailed usage instructions.
