-- Align tenant package configuration with online license issuing.
-- Execute only against the vendor database. This script is idempotent for MySQL 8.x.

SET @schema_name := DATABASE();

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE sys_tenant_package ADD COLUMN license_auto_issue_enabled TINYINT(1) DEFAULT 0 COMMENT ''是否支付后自动签发License'' AFTER online_purchase_enabled',
        'SELECT ''license_auto_issue_enabled column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'sys_tenant_package' AND column_name = 'license_auto_issue_enabled'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE sys_tenant_package ADD COLUMN license_key_id VARCHAR(64) DEFAULT NULL COMMENT ''License签名keyId'' AFTER license_auto_issue_enabled',
        'SELECT ''license_key_id column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'sys_tenant_package' AND column_name = 'license_key_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE sys_tenant_package ADD COLUMN license_validity_days INT DEFAULT NULL COMMENT ''License有效天数'' AFTER license_key_id',
        'SELECT ''license_validity_days column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'sys_tenant_package' AND column_name = 'license_validity_days'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE sys_tenant_package ADD COLUMN license_feature_codes VARCHAR(1000) DEFAULT NULL COMMENT ''License功能码，逗号分隔'' AFTER license_validity_days',
        'SELECT ''license_feature_codes column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'sys_tenant_package' AND column_name = 'license_feature_codes'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE sys_tenant_package ADD COLUMN license_template_entitlements TEXT DEFAULT NULL COMMENT ''License报表模板授权JSON'' AFTER license_feature_codes',
        'SELECT ''license_template_entitlements column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'sys_tenant_package' AND column_name = 'license_template_entitlements'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
