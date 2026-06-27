-- Vendor Source(A) strong-typed master-data tables.
-- Database boundary: vendor only.
--
-- These tables back the vendor open dimension APIs used by the
-- enterprise-local deployment for dropdowns and auto-fill. Enterprise fact
-- tables remain in the enterprise database.

CREATE TABLE IF NOT EXISTS cv_admin_division (
    id BIGINT NOT NULL AUTO_INCREMENT,
    division_code VARCHAR(64) NOT NULL,
    division_name VARCHAR(255) NOT NULL,
    parent_code VARCHAR(64) DEFAULT NULL,
    level_type VARCHAR(20) DEFAULT NULL COMMENT 'province/city/district',
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_admin_division_code (division_code),
    KEY idx_cv_admin_division_parent (parent_code),
    CONSTRAINT fk_cv_admin_division_parent
        FOREIGN KEY (parent_code) REFERENCES cv_admin_division (division_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='101 administrative division';

CREATE TABLE IF NOT EXISTS cv_emission_source_category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_code VARCHAR(64) NOT NULL,
    business_key VARCHAR(64) DEFAULT NULL,
    category_name VARCHAR(255) NOT NULL,
    category_name_en VARCHAR(255) DEFAULT NULL,
    ghg_scope VARCHAR(128) DEFAULT NULL,
    ghg_scope_category VARCHAR(255) DEFAULT NULL,
    iso_category VARCHAR(128) DEFAULT NULL,
    iso_category_en VARCHAR(128) DEFAULT NULL,
    iso_category_description VARCHAR(500) DEFAULT NULL,
    gb_scope_category VARCHAR(255) DEFAULT NULL,
    gb_subcategory VARCHAR(255) DEFAULT NULL,
    parent_code VARCHAR(64) DEFAULT NULL,
    effective_date DATE NOT NULL DEFAULT '2024-01-01',
    expire_date DATE NOT NULL DEFAULT '9999-12-31',
    current_flag CHAR(1) NOT NULL DEFAULT 'Y',
    version_no VARCHAR(64) NOT NULL DEFAULT '1',
    standard_category VARCHAR(255) DEFAULT NULL,
    category_current_key VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN current_flag = 'Y' THEN category_code ELSE NULL END) STORED,
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_emission_source_category_scd (category_code, version_no, effective_date),
    UNIQUE KEY uk_cv_emission_source_category_current (category_current_key),
    KEY idx_cv_emission_source_category_parent (parent_code),
    CONSTRAINT fk_cv_emission_source_category_parent
        FOREIGN KEY (parent_code) REFERENCES cv_emission_source_category (category_current_key),
    CONSTRAINT chk_cv_emission_source_category_current
        CHECK (current_flag IN ('Y', 'N')),
    CONSTRAINT chk_cv_emission_source_category_dates
        CHECK (effective_date <= expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='103 emission source category';

CREATE TABLE IF NOT EXISTS cv_base_year (
    id BIGINT NOT NULL AUTO_INCREMENT,
    base_year_key VARCHAR(64) NOT NULL,
    base_year INT NOT NULL,
    is_current TINYINT(1) NOT NULL DEFAULT 1,
    description VARCHAR(500) DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_base_year_key (base_year_key),
    UNIQUE KEY uk_cv_base_year_year (base_year),
    UNIQUE KEY uk_cv_base_year_current ((CASE WHEN is_current = 1 THEN 1 ELSE NULL END)),
    KEY idx_cv_base_year_current (is_current, base_year),
    CONSTRAINT chk_cv_base_year_current
        CHECK (is_current IN (0, 1)),
    CONSTRAINT chk_cv_base_year_range
        CHECK (base_year BETWEEN 1900 AND 9999)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='106 base year dimension';

CREATE TABLE IF NOT EXISTS cv_electricity_factor_version (
    id BIGINT NOT NULL AUTO_INCREMENT,
    factor_version VARCHAR(64) NOT NULL,
    effective_year INT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_electricity_factor_version (factor_version),
    UNIQUE KEY uk_cv_electricity_factor_effective_year (effective_year),
    CONSTRAINT chk_cv_electricity_factor_version_year
        CHECK (effective_year BETWEEN 1900 AND 9999)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='203 electricity factor version map';

CREATE TABLE IF NOT EXISTS cv_electricity_factor (
    id BIGINT NOT NULL AUTO_INCREMENT,
    factor_version VARCHAR(64) NOT NULL,
    division_code VARCHAR(64) NOT NULL,
    division_name VARCHAR(255) DEFAULT NULL,
    region_name VARCHAR(255) DEFAULT NULL,
    province_factor DECIMAL(28, 10) DEFAULT NULL,
    region_factor DECIMAL(28, 10) DEFAULT NULL,
    national_factor DECIMAL(28, 10) DEFAULT NULL,
    non_fossil_excluded_factor DECIMAL(28, 10) DEFAULT NULL,
    national_fossil_power_factor DECIMAL(28, 10) DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_electricity_factor (factor_version, division_code),
    KEY idx_cv_electricity_factor_division (division_code),
    KEY idx_cv_electricity_factor_version (factor_version),
    CONSTRAINT fk_cv_electricity_factor_division
        FOREIGN KEY (division_code) REFERENCES cv_admin_division (division_code),
    CONSTRAINT chk_cv_electricity_factor_non_negative
        CHECK (
            (province_factor IS NULL OR province_factor >= 0)
            AND (region_factor IS NULL OR region_factor >= 0)
            AND (national_factor IS NULL OR national_factor >= 0)
            AND (non_fossil_excluded_factor IS NULL OR non_fossil_excluded_factor >= 0)
            AND (national_fossil_power_factor IS NULL OR national_fossil_power_factor >= 0)
        )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='202 electricity emission factor';

CREATE TABLE IF NOT EXISTS cv_electricity_factor_scope (
    id BIGINT NOT NULL AUTO_INCREMENT,
    scope_key VARCHAR(64) NOT NULL,
    scope_name VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_electricity_factor_scope (scope_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='205 electricity factor scope dimension';

CREATE TABLE IF NOT EXISTS cv_greenhouse_gas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    gas_code VARCHAR(64) NOT NULL,
    gas_name VARCHAR(128) NOT NULL,
    gas_name_en VARCHAR(128) DEFAULT NULL,
    gwp_value DECIMAL(28, 10) DEFAULT NULL,
    gwp_version VARCHAR(64) DEFAULT NULL,
    chemical_formula VARCHAR(128) DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_greenhouse_gas_code (gas_code),
    CONSTRAINT chk_cv_greenhouse_gas_gwp
        CHECK (gwp_value IS NULL OR gwp_value >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='206 greenhouse gas dimension';

CREATE TABLE IF NOT EXISTS cv_report_content_catalog (
    id BIGINT NOT NULL AUTO_INCREMENT,
    catalog_no VARCHAR(64) DEFAULT NULL,
    catalog_name VARCHAR(255) DEFAULT NULL,
    subcatalog_no VARCHAR(64) NOT NULL,
    subcatalog_name VARCHAR(255) NOT NULL,
    chart_list TEXT DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_report_content_subcatalog (subcatalog_no),
    KEY idx_cv_report_content_catalog (catalog_no, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Source(A) report content catalog';
