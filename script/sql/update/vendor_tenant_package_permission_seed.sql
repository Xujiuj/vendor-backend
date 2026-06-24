-- Seed the three vendor package permission tiers.
-- Execute only against the vendor database. This script is idempotent for MySQL 8.x.

SET @schema_name := DATABASE();

ALTER TABLE sys_tenant_package
    MODIFY COLUMN remark VARCHAR(1000) COMMENT '备注';

SET @ddl := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE sys_tenant_package ADD COLUMN license_feature_codes VARCHAR(1000) DEFAULT NULL COMMENT ''License功能码，逗号分隔''',
        'SELECT ''license_feature_codes column already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'sys_tenant_package' AND column_name = 'license_feature_codes'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE sys_tenant_package
SET license_feature_codes = 'capture',
    remark = '默认业务套餐：开放基础客户、License与数据填报能力
[PACKAGE_POLICY]{"editionLevel":"standard","directFeatures":[],"effectiveFeatures":["capture"]}',
    update_by = 1,
    update_time = SYSDATE()
WHERE package_id = 1001
   OR package_name = '标准版';

UPDATE sys_tenant_package
SET license_feature_codes = 'capture,factor-sync,template-sync',
    remark = '默认业务套餐：包含标准版全部能力，并开放因子同步与模板同步
[PACKAGE_POLICY]{"editionLevel":"professional","directFeatures":[],"effectiveFeatures":["capture","factor-sync","template-sync"]}',
    update_by = 1,
    update_time = SYSDATE()
WHERE package_id = 1002
   OR package_name = '专业版';

UPDATE sys_tenant_package
SET license_feature_codes = 'capture,factor-sync,template-sync,report-gate,premium-support',
    remark = '默认业务套餐：包含专业版全部能力，并开放报表门禁与高级服务
[PACKAGE_POLICY]{"editionLevel":"group","directFeatures":[],"effectiveFeatures":["capture","factor-sync","template-sync","report-gate","premium-support"]}',
    update_by = 1,
    update_time = SYSDATE()
WHERE package_id = 1003
   OR package_name = '集团版';
