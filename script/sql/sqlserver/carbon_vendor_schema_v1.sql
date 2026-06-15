-- Vendor carbon operations foundation, SQL Server migration skeleton.
-- Final acceptance target uses SQL Server 2016+.

CREATE TABLE cv_customer (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    customer_code NVARCHAR(64) NOT NULL,
    customer_name NVARCHAR(255) NOT NULL,
    contact_name NVARCHAR(128) NULL,
    contact_email NVARCHAR(255) NULL,
    contact_phone NVARCHAR(64) NULL,
    customer_status NVARCHAR(32) NOT NULL DEFAULT 'active',
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    update_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    remark NVARCHAR(500) NULL,
    CONSTRAINT uk_cv_customer_code UNIQUE (customer_code)
);
GO

CREATE TABLE cv_signing_key (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    key_id NVARCHAR(64) NOT NULL,
    algorithm NVARCHAR(64) NOT NULL,
    public_key_pem NVARCHAR(MAX) NOT NULL,
    private_key_ref NVARCHAR(512) NOT NULL,
    key_status NVARCHAR(32) NOT NULL DEFAULT 'active',
    valid_from DATETIME2 NOT NULL,
    valid_to DATETIME2 NULL,
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uk_cv_signing_key_id UNIQUE (key_id)
);
GO

CREATE TABLE cv_license_issue (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    license_id NVARCHAR(128) NOT NULL,
    customer_id BIGINT NOT NULL,
    package_id BIGINT NULL,
    package_name NVARCHAR(64) NULL,
    key_id NVARCHAR(64) NOT NULL,
    algorithm NVARCHAR(64) NOT NULL,
    schema_version NVARCHAR(32) NOT NULL,
    edition NVARCHAR(64) NOT NULL,
    feature_codes NVARCHAR(1000) NOT NULL,
    install_id NVARCHAR(128) NOT NULL,
    valid_from DATETIME2 NOT NULL,
    valid_to DATETIME2 NOT NULL,
    issue_status NVARCHAR(32) NOT NULL DEFAULT 'issued',
    issue_type NVARCHAR(32) NOT NULL DEFAULT 'manual',
    source_license_id NVARCHAR(128) NULL,
    issued_by NVARCHAR(64) NULL,
    issued_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    revoked_time DATETIME2 NULL,
    revoked_by NVARCHAR(64) NULL,
    revoke_reason NVARCHAR(500) NULL,
    license_payload NVARCHAR(MAX) NOT NULL,
    signature_text NVARCHAR(MAX) NOT NULL,
    CONSTRAINT uk_cv_license_issue_id UNIQUE (license_id),
    CONSTRAINT fk_cv_license_issue_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
);
GO

CREATE UNIQUE INDEX uk_cv_license_reissue_source
    ON cv_license_issue (source_license_id)
    WHERE source_license_id IS NOT NULL;
GO

CREATE INDEX idx_cv_license_issue_package
    ON cv_license_issue (package_id);
GO

CREATE TABLE cv_factor_version (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    version_code NVARCHAR(64) NOT NULL,
    version_name NVARCHAR(128) NOT NULL,
    publish_status NVARCHAR(32) NOT NULL DEFAULT 'draft',
    frozen_flag BIT NOT NULL DEFAULT 0,
    published_by NVARCHAR(64) NULL,
    published_time DATETIME2 NULL,
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    remark NVARCHAR(500) NULL,
    CONSTRAINT uk_cv_factor_version_code UNIQUE (version_code)
);
GO

CREATE TABLE cv_factor_record (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    version_id BIGINT NOT NULL,
    factor_code NVARCHAR(128) NOT NULL,
    factor_name NVARCHAR(255) NOT NULL,
    factor_category NVARCHAR(128) NOT NULL,
    factor_value DECIMAL(28, 10) NOT NULL,
    factor_unit NVARCHAR(64) NOT NULL,
    source_ref NVARCHAR(512) NULL,
    enabled_flag BIT NOT NULL DEFAULT 1,
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uk_cv_factor_record UNIQUE (version_id, factor_code),
    CONSTRAINT fk_cv_factor_record_version
        FOREIGN KEY (version_id) REFERENCES cv_factor_version (id)
);
GO

CREATE TABLE cv_dimension_record (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    dimension_code NVARCHAR(64) NOT NULL,
    record_code NVARCHAR(128) NOT NULL,
    record_name NVARCHAR(255) NOT NULL,
    parent_code NVARCHAR(128) NULL,
    field01 NVARCHAR(255) NULL,
    field02 NVARCHAR(255) NULL,
    field03 NVARCHAR(255) NULL,
    field04 NVARCHAR(255) NULL,
    field05 NVARCHAR(255) NULL,
    field06 NVARCHAR(255) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status NCHAR(1) NOT NULL DEFAULT '0',
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    update_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    remark NVARCHAR(500) NULL,
    CONSTRAINT uk_cv_dimension_record UNIQUE (dimension_code, record_code)
);
GO

CREATE INDEX idx_cv_dimension_record_name
    ON cv_dimension_record (dimension_code, record_name);
GO

CREATE INDEX idx_cv_dimension_record_status
    ON cv_dimension_record (dimension_code, status);
GO

CREATE TABLE cv_factor_customer_scope (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    version_id BIGINT NOT NULL,
    customer_id BIGINT NULL,
    package_id BIGINT NULL,
    package_name NVARCHAR(64) NULL,
    edition NVARCHAR(64) NULL,
    license_id NVARCHAR(128) NULL,
    scope_customer_key AS ISNULL(customer_id, CONVERT(BIGINT, 0)) PERSISTED,
    scope_package_key AS ISNULL(package_id, CONVERT(BIGINT, 0)) PERSISTED,
    scope_edition_key AS ISNULL(edition, N'') PERSISTED,
    scope_license_key AS ISNULL(license_id, N'') PERSISTED,
    scope_status NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uk_cv_factor_scope_entitlement UNIQUE (version_id, scope_customer_key, scope_package_key, scope_edition_key, scope_license_key),
    CONSTRAINT fk_cv_factor_scope_version
        FOREIGN KEY (version_id) REFERENCES cv_factor_version (id),
    CONSTRAINT fk_cv_factor_scope_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
);
GO

CREATE INDEX idx_cv_factor_scope_lookup
    ON cv_factor_customer_scope (version_id, scope_status, customer_id, package_id, edition);
GO

CREATE TABLE cv_report_template (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    template_code NVARCHAR(64) NOT NULL,
    template_name NVARCHAR(255) NOT NULL,
    template_version NVARCHAR(64) NOT NULL,
    file_name NVARCHAR(255) NOT NULL,
    file_uri NVARCHAR(512) NOT NULL,
    publish_status NVARCHAR(32) NOT NULL DEFAULT 'draft',
    published_by NVARCHAR(64) NULL,
    published_time DATETIME2 NULL,
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    remark NVARCHAR(500) NULL,
    CONSTRAINT uk_cv_report_template UNIQUE (template_code, template_version)
);
GO

CREATE TABLE cv_report_template_scope (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    customer_id BIGINT NULL,
    license_id NVARCHAR(128) NULL,
    package_id BIGINT NULL,
    package_name NVARCHAR(64) NULL,
    edition NVARCHAR(64) NULL,
    scope_customer_key AS ISNULL(customer_id, CONVERT(BIGINT, 0)) PERSISTED,
    scope_package_key AS ISNULL(package_id, CONVERT(BIGINT, 0)) PERSISTED,
    scope_edition_key AS ISNULL(edition, N'') PERSISTED,
    scope_license_key AS ISNULL(license_id, N'') PERSISTED,
    scope_status NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uk_cv_report_template_scope UNIQUE (template_id, scope_customer_key, scope_package_key, scope_edition_key, scope_license_key),
    CONSTRAINT chk_cv_report_template_scope_entitlement 
        CHECK (customer_id IS NOT NULL OR package_id IS NOT NULL OR edition IS NOT NULL OR license_id IS NOT NULL),
    CONSTRAINT fk_cv_report_scope_template
        FOREIGN KEY (template_id) REFERENCES cv_report_template (id),
    CONSTRAINT fk_cv_report_scope_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
);
GO

CREATE INDEX idx_cv_report_template_scope_lookup
    ON cv_report_template_scope (template_id, scope_status, customer_id, package_id, edition, license_id);
GO

CREATE TABLE cv_report_template_download_token (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    download_token NVARCHAR(128) NOT NULL,
    license_id NVARCHAR(128) NOT NULL,
    install_id NVARCHAR(128) NOT NULL,
    customer_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    file_name NVARCHAR(255) NOT NULL,
    file_uri NVARCHAR(512) NOT NULL,
    token_status NVARCHAR(32) NOT NULL DEFAULT 'issued',
    expires_time DATETIME2 NOT NULL,
    consumed_time DATETIME2 NULL,
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uk_cv_report_template_download_token UNIQUE (download_token),
    CONSTRAINT fk_cv_template_download_token_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id),
    CONSTRAINT fk_cv_template_download_token_template
        FOREIGN KEY (template_id) REFERENCES cv_report_template (id)
);
GO

CREATE INDEX idx_cv_template_download_token_license
    ON cv_report_template_download_token (license_id, template_id, token_status);
GO

CREATE INDEX idx_cv_template_download_token_customer
    ON cv_report_template_download_token (customer_id, create_time);
GO

CREATE TABLE cv_renewal_order (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    order_no NVARCHAR(128) NOT NULL,
    customer_id BIGINT NOT NULL,
    license_id NVARCHAR(128) NULL,
    install_id NVARCHAR(128) NULL,
    requested_package_id BIGINT NULL,
    requested_package_name NVARCHAR(64) NULL,
    requested_edition NVARCHAR(64) NULL,
    renewal_period NVARCHAR(64) NULL,
    contact_name NVARCHAR(128) NULL,
    contact_email NVARCHAR(255) NULL,
    contact_phone NVARCHAR(64) NULL,
    idempotency_key NVARCHAR(128) NULL,
    request_source NVARCHAR(32) NULL,
    order_status NVARCHAR(32) NOT NULL DEFAULT 'pending',
    issue_status NVARCHAR(32) NOT NULL DEFAULT 'pending_issue',
    pay_channel NVARCHAR(32) NULL,
    amount DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    paid_time DATETIME2 NULL,
    issued_license_id NVARCHAR(128) NULL,
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    update_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uk_cv_renewal_order_no UNIQUE (order_no),
    CONSTRAINT fk_cv_renewal_order_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
);
GO

CREATE UNIQUE INDEX uk_cv_renewal_order_idempotency
    ON cv_renewal_order (idempotency_key)
    WHERE idempotency_key IS NOT NULL;
GO

CREATE INDEX idx_cv_renewal_order_license
    ON cv_renewal_order (license_id, install_id);
GO

CREATE INDEX idx_cv_renewal_order_issue_status
    ON cv_renewal_order (issue_status);
GO

CREATE INDEX idx_cv_renewal_order_package
    ON cv_renewal_order (requested_package_id);
GO

CREATE TABLE cv_open_api_audit (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    api_path NVARCHAR(255) NOT NULL,
    http_method NVARCHAR(16) NOT NULL,
    license_id NVARCHAR(128) NULL,
    install_id NVARCHAR(128) NULL,
    customer_id BIGINT NULL,
    request_summary NVARCHAR(1000) NULL,
    response_status NVARCHAR(32) NOT NULL,
    error_message NVARCHAR(1000) NULL,
    remote_addr NVARCHAR(128) NULL,
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_cv_open_api_audit_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
);
GO

CREATE INDEX idx_cv_open_api_audit_license
    ON cv_open_api_audit (license_id, create_time);
GO

CREATE INDEX idx_cv_open_api_audit_customer
    ON cv_open_api_audit (customer_id, create_time);
GO

CREATE INDEX idx_cv_open_api_audit_path
    ON cv_open_api_audit (api_path, response_status, create_time);
GO
