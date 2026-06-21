-- Vendor online purchase payment fields for cv_renewal_order.
-- Execute only against the vendor database. This script is idempotent for MySQL 8.x.

SET @schema_name := DATABASE();

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE cv_renewal_order ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT ''CNY'' COMMENT ''支付币种'' AFTER amount',
        'SELECT ''currency column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'cv_renewal_order' AND column_name = 'currency'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE cv_renewal_order ADD COLUMN pay_trade_no VARCHAR(128) DEFAULT NULL COMMENT ''第三方支付交易号'' AFTER currency',
        'SELECT ''pay_trade_no column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'cv_renewal_order' AND column_name = 'pay_trade_no'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE cv_renewal_order ADD COLUMN pay_form TEXT DEFAULT NULL COMMENT ''支付表单或二维码内容'' AFTER pay_trade_no',
        'SELECT ''pay_form column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'cv_renewal_order' AND column_name = 'pay_form'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE cv_renewal_order ADD COLUMN pay_url VARCHAR(1000) DEFAULT NULL COMMENT ''厂商端收银台跳转地址'' AFTER pay_form',
        'SELECT ''pay_url column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'cv_renewal_order' AND column_name = 'pay_url'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'CREATE INDEX idx_cv_renewal_order_trade ON cv_renewal_order (pay_channel, pay_trade_no)',
        'SELECT ''idx_cv_renewal_order_trade index already exists''')
    FROM information_schema.statistics
    WHERE table_schema = @schema_name AND table_name = 'cv_renewal_order' AND index_name = 'idx_cv_renewal_order_trade'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
