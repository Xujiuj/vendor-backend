-- Add tenant package price settings for online purchase.
-- Execute only against the vendor database. This script is idempotent for MySQL 8.x.

SET @schema_name := DATABASE();

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE sys_tenant_package ADD COLUMN price_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT ''套餐价格金额'' AFTER menu_check_strictly',
        'SELECT ''price_amount column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'sys_tenant_package' AND column_name = 'price_amount'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE sys_tenant_package ADD COLUMN price_currency VARCHAR(3) DEFAULT ''CNY'' COMMENT ''价格币种'' AFTER price_amount',
        'SELECT ''price_currency column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'sys_tenant_package' AND column_name = 'price_currency'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE sys_tenant_package ADD COLUMN billing_cycle VARCHAR(20) DEFAULT ''YEAR'' COMMENT ''计费周期'' AFTER price_currency',
        'SELECT ''billing_cycle column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'sys_tenant_package' AND column_name = 'billing_cycle'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE sys_tenant_package ADD COLUMN online_purchase_enabled TINYINT(1) DEFAULT 0 COMMENT ''是否允许在线购买'' AFTER billing_cycle',
        'SELECT ''online_purchase_enabled column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'sys_tenant_package' AND column_name = 'online_purchase_enabled'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE sys_tenant_package
SET price_amount = IFNULL(price_amount, 0.00),
    price_currency = IFNULL(NULLIF(price_currency, ''), 'CNY'),
    billing_cycle = IFNULL(NULLIF(billing_cycle, ''), 'YEAR'),
    online_purchase_enabled = IFNULL(online_purchase_enabled, 0);
