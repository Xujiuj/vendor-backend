-- Vendor carbon operations foundation, MySQL development version.
-- This script intentionally excludes enterprise-local business detail tables.

CREATE TABLE IF NOT EXISTS cv_customer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_code VARCHAR(64) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(128) DEFAULT NULL,
    contact_email VARCHAR(255) DEFAULT NULL,
    contact_phone VARCHAR(64) DEFAULT NULL,
    customer_status VARCHAR(32) NOT NULL DEFAULT 'active',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_customer_code (customer_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor managed customer account';

CREATE TABLE IF NOT EXISTS cv_signing_key (
    id BIGINT NOT NULL AUTO_INCREMENT,
    key_id VARCHAR(64) NOT NULL,
    algorithm VARCHAR(64) NOT NULL,
    public_key_pem TEXT NOT NULL,
    private_key_ref VARCHAR(512) NOT NULL,
    key_status VARCHAR(32) NOT NULL DEFAULT 'active',
    valid_from DATETIME NOT NULL,
    valid_to DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_signing_key_id (key_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor signing key metadata';

CREATE TABLE IF NOT EXISTS cv_license_issue (
    id BIGINT NOT NULL AUTO_INCREMENT,
    license_id VARCHAR(128) NOT NULL,
    customer_id BIGINT NOT NULL,
    package_id BIGINT DEFAULT NULL,
    package_name VARCHAR(64) DEFAULT NULL,
    key_id VARCHAR(64) NOT NULL,
    algorithm VARCHAR(64) NOT NULL,
    schema_version VARCHAR(32) NOT NULL,
    edition VARCHAR(64) NOT NULL,
    feature_codes VARCHAR(1000) NOT NULL,
    install_id VARCHAR(128) NOT NULL,
    valid_from DATETIME NOT NULL,
    valid_to DATETIME NOT NULL,
    issue_status VARCHAR(32) NOT NULL DEFAULT 'issued',
    issue_type VARCHAR(32) NOT NULL DEFAULT 'manual',
    source_license_id VARCHAR(128) DEFAULT NULL,
    issued_by VARCHAR(64) DEFAULT NULL,
    issued_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    revoked_time DATETIME DEFAULT NULL,
    revoked_by VARCHAR(64) DEFAULT NULL,
    revoke_reason VARCHAR(500) DEFAULT NULL,
    license_payload TEXT NOT NULL,
    signature_text TEXT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_license_issue_id (license_id),
    UNIQUE KEY uk_cv_license_reissue_source (source_license_id),
    KEY idx_cv_license_issue_customer (customer_id, issue_status),
    KEY idx_cv_license_issue_package (package_id),
    CONSTRAINT fk_cv_license_issue_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor manual license issue record';

CREATE TABLE IF NOT EXISTS cv_factor_version (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version_code VARCHAR(64) NOT NULL,
    version_name VARCHAR(128) NOT NULL,
    publish_status VARCHAR(32) NOT NULL DEFAULT 'draft',
    frozen_flag TINYINT(1) NOT NULL DEFAULT 0,
    published_by VARCHAR(64) DEFAULT NULL,
    published_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_factor_version_code (version_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor factor library version';

CREATE TABLE IF NOT EXISTS cv_dimension_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dimension_code VARCHAR(64) NOT NULL,
    record_code VARCHAR(128) NOT NULL,
    record_name VARCHAR(255) NOT NULL,
    parent_code VARCHAR(128) DEFAULT NULL,
    field01 VARCHAR(255) DEFAULT NULL,
    field02 VARCHAR(255) DEFAULT NULL,
    field03 VARCHAR(255) DEFAULT NULL,
    field04 VARCHAR(255) DEFAULT NULL,
    field05 VARCHAR(255) DEFAULT NULL,
    field06 VARCHAR(255) DEFAULT NULL,
    field07 VARCHAR(255) DEFAULT NULL,
    field08 VARCHAR(255) DEFAULT NULL,
    field09 VARCHAR(255) DEFAULT NULL,
    field10 VARCHAR(255) DEFAULT NULL,
    field11 VARCHAR(255) DEFAULT NULL,
    field12 VARCHAR(255) DEFAULT NULL,
    field13 VARCHAR(255) DEFAULT NULL,
    field14 VARCHAR(255) DEFAULT NULL,
    field15 VARCHAR(255) DEFAULT NULL,
    field16 VARCHAR(255) DEFAULT NULL,
    field17 VARCHAR(255) DEFAULT NULL,
    field18 VARCHAR(255) DEFAULT NULL,
    field19 VARCHAR(255) DEFAULT NULL,
    field20 VARCHAR(255) DEFAULT NULL,
    field21 VARCHAR(255) DEFAULT NULL,
    field22 VARCHAR(255) DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_dimension_record (dimension_code, record_code),
    KEY idx_cv_dimension_record_name (dimension_code, record_name),
    KEY idx_cv_dimension_record_status (dimension_code, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor-owned dimension record exposed by open APIs';

CREATE TABLE IF NOT EXISTS cv_vendor_table_field (
    id BIGINT NOT NULL AUTO_INCREMENT,
    table_group VARCHAR(32) NOT NULL,
    table_code VARCHAR(64) NOT NULL,
    field_key VARCHAR(128) NOT NULL,
    field_label VARCHAR(255) NOT NULL,
    field_type VARCHAR(32) NOT NULL DEFAULT 'text',
    field_precision INT DEFAULT NULL,
    field_width INT DEFAULT NULL,
    field_options TEXT DEFAULT NULL,
    required_flag TINYINT(1) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_vendor_table_field (table_group, table_code, field_key),
    KEY idx_cv_vendor_table_field_table (table_group, table_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor managed table field definition';

CREATE TABLE IF NOT EXISTS cv_factor_customer_scope (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version_id BIGINT NOT NULL,
    customer_id BIGINT DEFAULT NULL,
    package_id BIGINT DEFAULT NULL,
    package_name VARCHAR(64) DEFAULT NULL,
    edition VARCHAR(64) DEFAULT NULL,
    license_id VARCHAR(128) DEFAULT NULL,
    scope_customer_key BIGINT GENERATED ALWAYS AS (IFNULL(customer_id, 0)) STORED,
    scope_package_key BIGINT GENERATED ALWAYS AS (IFNULL(package_id, 0)) STORED,
    scope_edition_key VARCHAR(64) GENERATED ALWAYS AS (IFNULL(edition, '')) STORED,
    scope_license_key VARCHAR(128) GENERATED ALWAYS AS (IFNULL(license_id, '')) STORED,
    scope_status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_factor_scope_entitlement (version_id, scope_customer_key, scope_package_key, scope_edition_key, scope_license_key),
    KEY idx_cv_factor_scope_lookup (version_id, scope_status, customer_id, package_id, edition),
    CONSTRAINT fk_cv_factor_scope_version
        FOREIGN KEY (version_id) REFERENCES cv_factor_version (id),
    CONSTRAINT fk_cv_factor_scope_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor factor version customer scope';

CREATE TABLE IF NOT EXISTS cv_report_template (
    id BIGINT NOT NULL AUTO_INCREMENT,
    template_code VARCHAR(64) NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    template_version VARCHAR(64) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_uri VARCHAR(512) NOT NULL,
    publish_status VARCHAR(32) NOT NULL DEFAULT 'draft',
    published_by VARCHAR(64) DEFAULT NULL,
    published_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_report_template (template_code, template_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor report template version';

CREATE TABLE IF NOT EXISTS cv_report_template_scope (
    id BIGINT NOT NULL AUTO_INCREMENT,
    template_id BIGINT NOT NULL,
    customer_id BIGINT DEFAULT NULL,
    license_id VARCHAR(128) DEFAULT NULL,
    package_id BIGINT DEFAULT NULL,
    package_name VARCHAR(64) DEFAULT NULL,
    edition VARCHAR(64) DEFAULT NULL,
    scope_customer_key BIGINT GENERATED ALWAYS AS (IFNULL(customer_id, 0)) STORED,
    scope_package_key BIGINT GENERATED ALWAYS AS (IFNULL(package_id, 0)) STORED,
    scope_edition_key VARCHAR(64) GENERATED ALWAYS AS (IFNULL(edition, '')) STORED,
    scope_license_key VARCHAR(128) GENERATED ALWAYS AS (IFNULL(license_id, '')) STORED,
    scope_status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_report_template_scope (template_id, scope_customer_key, scope_package_key, scope_edition_key, scope_license_key),
    KEY idx_cv_report_template_scope_lookup (template_id, scope_status, customer_id, package_id, edition, license_id),
    CONSTRAINT chk_cv_report_template_scope_entitlement
        CHECK (customer_id IS NOT NULL OR package_id IS NOT NULL OR edition IS NOT NULL OR license_id IS NOT NULL),
    CONSTRAINT fk_cv_report_scope_template
        FOREIGN KEY (template_id) REFERENCES cv_report_template (id),
    CONSTRAINT fk_cv_report_scope_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor report template customer scope';

CREATE TABLE IF NOT EXISTS cv_report_template_download_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    download_token VARCHAR(128) NOT NULL,
    license_id VARCHAR(128) NOT NULL,
    install_id VARCHAR(128) NOT NULL,
    customer_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_uri VARCHAR(512) NOT NULL,
    token_status VARCHAR(32) NOT NULL DEFAULT 'issued',
    expires_time DATETIME NOT NULL,
    consumed_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_report_template_download_token (download_token),
    KEY idx_cv_template_download_token_license (license_id, template_id, token_status),
    KEY idx_cv_template_download_token_customer (customer_id, create_time),
    CONSTRAINT fk_cv_template_download_token_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id),
    CONSTRAINT fk_cv_template_download_token_template
        FOREIGN KEY (template_id) REFERENCES cv_report_template (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor report template one-time download token';

CREATE TABLE IF NOT EXISTS cv_renewal_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(128) NOT NULL,
    customer_id BIGINT NOT NULL,
    license_id VARCHAR(128) DEFAULT NULL,
    install_id VARCHAR(128) DEFAULT NULL,
    requested_package_id BIGINT DEFAULT NULL,
    requested_package_name VARCHAR(64) DEFAULT NULL,
    requested_edition VARCHAR(64) DEFAULT NULL,
    renewal_period VARCHAR(64) DEFAULT NULL,
    contact_name VARCHAR(128) DEFAULT NULL,
    contact_email VARCHAR(255) DEFAULT NULL,
    contact_phone VARCHAR(64) DEFAULT NULL,
    idempotency_key VARCHAR(128) DEFAULT NULL,
    request_source VARCHAR(32) DEFAULT NULL,
    order_status VARCHAR(32) NOT NULL DEFAULT 'pending',
    issue_status VARCHAR(32) NOT NULL DEFAULT 'pending_issue',
    pay_channel VARCHAR(32) DEFAULT NULL,
    amount DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    paid_time DATETIME DEFAULT NULL,
    issued_license_id VARCHAR(128) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_renewal_order_no (order_no),
    UNIQUE KEY uk_cv_renewal_order_idempotency (idempotency_key),
    KEY idx_cv_renewal_order_customer (customer_id, order_status),
    KEY idx_cv_renewal_order_issue_status (issue_status),
    KEY idx_cv_renewal_order_package (requested_package_id),
    KEY idx_cv_renewal_order_license (license_id, install_id),
    CONSTRAINT fk_cv_renewal_order_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor renewal order placeholder';

CREATE TABLE IF NOT EXISTS cv_open_api_audit (
    id BIGINT NOT NULL AUTO_INCREMENT,
    api_path VARCHAR(255) NOT NULL,
    http_method VARCHAR(16) NOT NULL,
    license_id VARCHAR(128) DEFAULT NULL,
    install_id VARCHAR(128) DEFAULT NULL,
    customer_id BIGINT DEFAULT NULL,
    request_summary VARCHAR(1000) DEFAULT NULL,
    response_status VARCHAR(32) NOT NULL,
    error_message VARCHAR(1000) DEFAULT NULL,
    remote_addr VARCHAR(128) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_cv_open_api_audit_license (license_id, create_time),
    KEY idx_cv_open_api_audit_customer (customer_id, create_time),
    KEY idx_cv_open_api_audit_path (api_path, response_status, create_time),
    CONSTRAINT fk_cv_open_api_audit_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor open API call audit';

INSERT INTO cv_dimension_record (
    dimension_code, record_code, record_name, parent_code,
    field01, field02, field03, field04, field05, field06,
    sort_order, status, remark
)
SELECT * FROM (
    SELECT 'admin-division' AS dimension_code, '330000' AS record_code, '浙江省' AS record_name, NULL AS parent_code, '省级' AS field01, '华东电网' AS field02, '中国' AS field03, NULL AS field04, NULL AS field05, NULL AS field06, 1 AS sort_order, '0' AS status, '行政区划示例' AS remark UNION ALL
    SELECT 'admin-division', '330200', '宁波市', '330000', '市级', '华东电网', '中国', NULL, NULL, NULL, 2, '0', '行政区划示例' UNION ALL
    SELECT 'emission-source-category', 'SCOPE2-PURCHASED-ELEC', '外购电力', NULL, '范围二', 'kWh', '区域电网平均因子', NULL, NULL, NULL, 1, '0', '排放源分类示例' UNION ALL
    SELECT 'base-year', 'BASE-2025', '2025基准年', NULL, '2025', '通用企业', '单位营收排放强度', NULL, NULL, NULL, 1, '0', '基准年维度示例' UNION ALL
    SELECT 'ef-electricity-factor', 'EF-ELEC-ZJ-2025', '浙江电力排放因子', NULL, '330000', '0.5703', 'kgCO2e/kWh', '2025', NULL, NULL, 1, '0', '电力因子示例' UNION ALL
    SELECT 'ef-electricity-version', 'EV-2025-ZJ', '2025浙江电力因子对应', NULL, '2025', '2025版', '330000', NULL, NULL, NULL, 1, '0', '版本对应示例' UNION ALL
    SELECT 'ef-electricity-scope', 'GRID-REGIONAL', '区域电网口径', NULL, '区域', '按区域电网平均排放因子核算', NULL, NULL, NULL, NULL, 1, '0', '口径示例' UNION ALL
    SELECT 'greenhouse-gas', 'CO2', '二氧化碳', NULL, '1', 'AR6', 'CO2', NULL, NULL, NULL, 1, '0', '温室气体示例' UNION ALL
    SELECT 'report-template-download', 'TPL-PBI-001', '企业碳报表Power BI模板', NULL, 'Power BI', 'v1.0', '/templates/carbon-report.pbix', '2026-06-01', NULL, NULL, 1, '0', '报表模板示例'
) seed
WHERE NOT EXISTS (
    SELECT 1
    FROM cv_dimension_record existing
    WHERE existing.dimension_code = seed.dimension_code
      AND existing.record_code = seed.record_code
);
