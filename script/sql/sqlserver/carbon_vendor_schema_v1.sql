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
    issued_by NVARCHAR(64) NULL,
    issued_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    revoked_time DATETIME2 NULL,
    license_payload NVARCHAR(MAX) NOT NULL,
    signature_text NVARCHAR(MAX) NOT NULL,
    CONSTRAINT uk_cv_license_issue_id UNIQUE (license_id),
    CONSTRAINT fk_cv_license_issue_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
);
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

CREATE TABLE cv_factor_customer_scope (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    version_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    license_id NVARCHAR(128) NULL,
    scope_status NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uk_cv_factor_customer_scope UNIQUE (version_id, customer_id),
    CONSTRAINT fk_cv_factor_scope_version
        FOREIGN KEY (version_id) REFERENCES cv_factor_version (id),
    CONSTRAINT fk_cv_factor_scope_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
);
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
    customer_id BIGINT NOT NULL,
    license_id NVARCHAR(128) NULL,
    scope_status NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    create_time DATETIME2 NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uk_cv_report_template_scope UNIQUE (template_id, customer_id),
    CONSTRAINT fk_cv_report_scope_template
        FOREIGN KEY (template_id) REFERENCES cv_report_template (id),
    CONSTRAINT fk_cv_report_scope_customer
        FOREIGN KEY (customer_id) REFERENCES cv_customer (id)
);
GO

CREATE TABLE cv_renewal_order (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    order_no NVARCHAR(128) NOT NULL,
    customer_id BIGINT NOT NULL,
    license_id NVARCHAR(128) NULL,
    order_status NVARCHAR(32) NOT NULL DEFAULT 'pending',
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
