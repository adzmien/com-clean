-- Migration: V251229002__dev_sample_config.sql
-- Description: Insert sample configuration properties for development/testing
-- Purpose: Development and QA testing data
-- Author: Clean Architecture Project
-- Date: 2025-12-29

-- Feature flags
INSERT INTO TBL_CLEAN_CONFIG (prop_key, dev_value, sit_value, uat_value, prod_value, dr_value, description, category, data_type, is_sensitive, created_by, updated_by) VALUES
('app.feature.new-ui', 'true', 'true', 'true', 'false', 'false', 'Enable new UI redesign', 'feature-flags', 'boolean', FALSE, 'SYSTEM', 'SYSTEM'),
('app.feature.enable-cache', 'false', 'true', 'true', 'true', 'true', 'Enable application caching', 'feature-flags', 'boolean', FALSE, 'SYSTEM', 'SYSTEM'),
('app.feature.maintenance-mode', 'false', 'false', 'false', 'false', 'false', 'Enable maintenance mode banner', 'feature-flags', 'boolean', FALSE, 'SYSTEM', 'SYSTEM'),
('app.feature.dark-mode', 'true', 'true', 'false', 'false', 'false', 'Enable dark mode theme', 'feature-flags', 'boolean', FALSE, 'SYSTEM', 'SYSTEM');

-- Database configuration
INSERT INTO TBL_CLEAN_CONFIG (prop_key, dev_value, sit_value, uat_value, prod_value, dr_value, description, category, data_type, is_sensitive, created_by, updated_by) VALUES
('db.pool.max-size', '5', '10', '20', '50', '50', 'Maximum database connection pool size', 'database', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('db.pool.min-idle', '2', '5', '10', '20', '20', 'Minimum idle database connections', 'database', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('db.query.timeout', '5000', '10000', '15000', '30000', '30000', 'Database query timeout in milliseconds', 'database', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('db.connection.timeout', '3000', '5000', '10000', '15000', '15000', 'Database connection timeout in milliseconds', 'database', 'int', FALSE, 'SYSTEM', 'SYSTEM');

-- API configuration
INSERT INTO TBL_CLEAN_CONFIG (prop_key, dev_value, sit_value, uat_value, prod_value, dr_value, description, category, data_type, is_sensitive, created_by, updated_by) VALUES
('api.retry.max-attempts', '3', '3', '5', '5', '5', 'Maximum retry attempts for failed API calls', 'api', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('api.timeout.connect', '3000', '5000', '10000', '15000', '15000', 'API connection timeout in milliseconds', 'api', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('api.timeout.read', '5000', '10000', '15000', '30000', '30000', 'API read timeout in milliseconds', 'api', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('api.rate-limit.requests-per-minute', '100', '200', '500', '1000', '1000', 'Rate limit: maximum requests per minute', 'api', 'int', FALSE, 'SYSTEM', 'SYSTEM');

-- Batch processing
INSERT INTO TBL_CLEAN_CONFIG (prop_key, dev_value, sit_value, uat_value, prod_value, dr_value, description, category, data_type, is_sensitive, created_by, updated_by) VALUES
('batch.chunk-size', '10', '50', '100', '500', '500', 'Batch processing chunk size', 'batch-processing', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('batch.parallel-threads', '2', '4', '8', '16', '16', 'Number of parallel threads for batch processing', 'batch-processing', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('batch.retry.max-attempts', '2', '3', '3', '5', '5', 'Maximum retry attempts for failed batch items', 'batch-processing', 'int', FALSE, 'SYSTEM', 'SYSTEM');

-- Security configuration
INSERT INTO TBL_CLEAN_CONFIG (prop_key, dev_value, sit_value, uat_value, prod_value, dr_value, description, category, data_type, is_sensitive, created_by, updated_by) VALUES
('security.jwt.expiration', '86400000', '43200000', '28800000', '3600000', '3600000', 'JWT token expiration time in milliseconds', 'security', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('security.session.timeout', '3600', '1800', '1800', '900', '900', 'Session timeout in seconds', 'security', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('security.password.min-length', '6', '8', '10', '12', '12', 'Minimum password length', 'security', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('security.password.require-special-char', 'false', 'true', 'true', 'true', 'true', 'Require special characters in password', 'security', 'boolean', FALSE, 'SYSTEM', 'SYSTEM');

-- Logging configuration
INSERT INTO TBL_CLEAN_CONFIG (prop_key, dev_value, sit_value, uat_value, prod_value, dr_value, description, category, data_type, is_sensitive, created_by, updated_by) VALUES
('logging.level.root', 'DEBUG', 'INFO', 'INFO', 'WARN', 'WARN', 'Root logging level', 'logging', 'string', FALSE, 'SYSTEM', 'SYSTEM'),
('logging.level.sql', 'DEBUG', 'DEBUG', 'INFO', 'WARN', 'WARN', 'SQL logging level', 'logging', 'string', FALSE, 'SYSTEM', 'SYSTEM'),
('logging.retention.days', '7', '30', '60', '90', '90', 'Log file retention period in days', 'logging', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('logging.max-file-size', '10MB', '50MB', '100MB', '500MB', '500MB', 'Maximum log file size', 'logging', 'string', FALSE, 'SYSTEM', 'SYSTEM');

-- Integration endpoints (environment-specific)
INSERT INTO TBL_CLEAN_CONFIG (prop_key, dev_value, sit_value, uat_value, prod_value, dr_value, description, category, data_type, is_sensitive, created_by, updated_by) VALUES
('integration.payment.gateway.url', 'https://sandbox.payment.com', 'https://test.payment.com', 'https://uat.payment.com', 'https://api.payment.com', 'https://dr.payment.com', 'Payment gateway API endpoint', 'integrations', 'string', FALSE, 'SYSTEM', 'SYSTEM'),
('integration.email.smtp.host', 'localhost', 'smtp-test.local', 'smtp-uat.local', 'smtp.prod.com', 'smtp.dr.com', 'SMTP server hostname', 'integrations', 'string', FALSE, 'SYSTEM', 'SYSTEM'),
('integration.email.smtp.port', '1025', '587', '587', '587', '587', 'SMTP server port', 'integrations', 'int', FALSE, 'SYSTEM', 'SYSTEM');

-- Example: Sensitive properties (using dummy values for non-prod)
INSERT INTO TBL_CLEAN_CONFIG (prop_key, dev_value, sit_value, uat_value, prod_value, dr_value, description, category, data_type, is_sensitive, created_by, updated_by) VALUES
('api.payment.secret-key', 'sk_test_dev_12345', 'sk_test_sit_67890', 'sk_test_uat_abcde', 'REPLACE_WITH_PROD_KEY', 'REPLACE_WITH_DR_KEY', 'Payment gateway API secret key', 'api-keys', 'string', TRUE, 'SYSTEM', 'SYSTEM'),
('db.encryption.key', 'dev-encryption-key-123', 'sit-encryption-key-456', 'uat-encryption-key-789', 'REPLACE_WITH_PROD_KEY', 'REPLACE_WITH_DR_KEY', 'Database field encryption key', 'api-keys', 'string', TRUE, 'SYSTEM', 'SYSTEM'),
('integration.email.smtp.password', 'dev-pass', 'test-pass', 'uat-pass', 'REPLACE_WITH_PROD_PASS', 'REPLACE_WITH_DR_PASS', 'SMTP server password', 'api-keys', 'string', TRUE, 'SYSTEM', 'SYSTEM');

-- Cache configuration
INSERT INTO TBL_CLEAN_CONFIG (prop_key, dev_value, sit_value, uat_value, prod_value, dr_value, description, category, data_type, is_sensitive, created_by, updated_by) VALUES
('cache.ttl.default', '300', '600', '1800', '3600', '3600', 'Default cache TTL in seconds', 'cache', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('cache.max-entries', '100', '500', '1000', '10000', '10000', 'Maximum cache entries', 'cache', 'int', FALSE, 'SYSTEM', 'SYSTEM'),
('cache.provider', 'caffeine', 'caffeine', 'redis', 'redis', 'redis', 'Cache provider: caffeine, redis, hazelcast', 'cache', 'string', FALSE, 'SYSTEM', 'SYSTEM');
