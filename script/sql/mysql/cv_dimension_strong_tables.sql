-- Vendor dimension strong-typed tables.
-- Replaces cv_dimension_record field01-field22 generic storage
-- with semantic column names matching enterprise ce_* tables.
--
-- SQL Server migration notes:
--   * BIGINT AUTO_INCREMENT -> BIGINT IDENTITY(1,1)
--   * TEXT -> NVARCHAR(MAX)
--   * TINYINT(1) -> BIT
--   * DATETIME -> DATETIME2

-- 101 行政区划
CREATE TABLE IF NOT EXISTS cv_admin_division (
    id BIGINT NOT NULL AUTO_INCREMENT,
    division_code VARCHAR(64) NOT NULL,
    division_name VARCHAR(255) NOT NULL,
    parent_code VARCHAR(64) DEFAULT NULL,
    level_type VARCHAR(20) DEFAULT NULL COMMENT 'province/city/district',
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '0=启用 1=停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_admin_division_code (division_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='101 administrative division';

-- 103 排放源分类
CREATE TABLE IF NOT EXISTS cv_emission_source_category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_code VARCHAR(64) NOT NULL,
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
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_emission_source_category_code (category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='103 emission source category';

-- 106 基准年
CREATE TABLE IF NOT EXISTS cv_base_year (
    id BIGINT NOT NULL AUTO_INCREMENT,
    factory_code VARCHAR(64) NOT NULL,
    factory_name VARCHAR(255) DEFAULT NULL,
    base_year INT NOT NULL,
    is_current TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_base_year (factory_code, base_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='106 base year dimension';

-- 202 EF电力因子
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_electricity_factor (factor_version, division_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='202 electricity emission factor';

-- 203 EF电力因子版本对应
CREATE TABLE IF NOT EXISTS cv_electricity_factor_version (
    id BIGINT NOT NULL AUTO_INCREMENT,
    factor_version VARCHAR(64) NOT NULL,
    effective_year INT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_electricity_factor_version (factor_version, effective_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='203 electricity factor version map';

-- 205 EF电力因子口径维度
CREATE TABLE IF NOT EXISTS cv_electricity_factor_scope (
    id BIGINT NOT NULL AUTO_INCREMENT,
    scope_key VARCHAR(64) NOT NULL,
    scope_name VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_electricity_factor_scope (scope_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='205 electricity factor scope dimension';

-- 206 温室气体维度
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_greenhouse_gas_code (gas_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='206 greenhouse gas dimension';
