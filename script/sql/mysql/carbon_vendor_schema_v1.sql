-- Vendor carbon operations foundation, MySQL development version.
-- This script intentionally excludes enterprise-local business detail tables.
-- SQL Server migration notes:
--   * BIGINT AUTO_INCREMENT -> BIGINT IDENTITY(1,1)
--   * TEXT -> NVARCHAR(MAX)
--   * TINYINT(1) -> BIT
--   * DATETIME -> DATETIME2

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
    issued_by VARCHAR(64) DEFAULT NULL,
    issued_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    revoked_time DATETIME DEFAULT NULL,
    license_payload TEXT NOT NULL,
    signature_text TEXT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_license_issue_id (license_id),
    KEY idx_cv_license_issue_customer (customer_id, issue_status),
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

CREATE TABLE IF NOT EXISTS cv_factor_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version_id BIGINT NOT NULL,
    factor_code VARCHAR(128) NOT NULL,
    factor_name VARCHAR(255) NOT NULL,
    factor_category VARCHAR(128) NOT NULL,
    factor_value DECIMAL(28, 10) NOT NULL,
    factor_unit VARCHAR(64) NOT NULL,
    source_ref VARCHAR(512) DEFAULT NULL,
    enabled_flag TINYINT(1) NOT NULL DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_factor_record (version_id, factor_code),
    CONSTRAINT fk_cv_factor_record_version
        FOREIGN KEY (version_id) REFERENCES cv_factor_version (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor factor library record';

CREATE TABLE IF NOT EXISTS cv_factor_customer_scope (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    license_id VARCHAR(128) DEFAULT NULL,
    scope_status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_factor_customer_scope (version_id, customer_id),
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
    customer_id BIGINT NOT NULL,
    license_id VARCHAR(128) DEFAULT NULL,
    scope_status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_report_template_scope (template_id, customer_id),
    CONSTRAINT fk_cv_report_scope_template
        FOREIGN KEY (template_id) REFERENCES cv_report_template (id),
    CONSTRAINT fk_cv_report_scope_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor report template customer scope';

CREATE TABLE IF NOT EXISTS cv_renewal_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(128) NOT NULL,
    customer_id BIGINT NOT NULL,
    license_id VARCHAR(128) DEFAULT NULL,
    order_status VARCHAR(32) NOT NULL DEFAULT 'pending',
    pay_channel VARCHAR(32) DEFAULT NULL,
    amount DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    paid_time DATETIME DEFAULT NULL,
    issued_license_id VARCHAR(128) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_renewal_order_no (order_no),
    KEY idx_cv_renewal_order_customer (customer_id, order_status),
    CONSTRAINT fk_cv_renewal_order_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor renewal order placeholder';
