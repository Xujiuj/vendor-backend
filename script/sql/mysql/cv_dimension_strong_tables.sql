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
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
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
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
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
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
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
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
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
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
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
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
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
    create_dept BIGINT DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cv_greenhouse_gas_code (gas_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='206 greenhouse gas dimension';

-- Seed master data required by enterprise dropdowns and auto-fill behavior.
-- Keep this block idempotent: vendor open APIs read these strong-typed tables.
INSERT INTO cv_admin_division
(division_code, division_name, parent_code, level_type, sort_order, status, remark)
VALUES
('330000', '浙江省', NULL, 'province', 10, '0', '企业端主数据下拉样例'),
('330200', '宁波市', '330000', 'city', 20, '0', '企业端主数据下拉样例'),
('330212', '鄞州区', '330200', 'district', 30, '0', '企业端主数据下拉样例')
ON DUPLICATE KEY UPDATE
    division_name = VALUES(division_name),
    parent_code = VALUES(parent_code),
    level_type = VALUES(level_type),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_emission_source_category
(category_code, category_name, category_name_en, ghg_scope, ghg_scope_category, iso_category, iso_category_en,
 iso_category_description, gb_scope_category, gb_subcategory, parent_code, sort_order, status, remark)
VALUES
('SCOPE2-ELEC', '外购电力', 'Purchased electricity', 'Scope 2', 'Purchased electricity', 'ISO-14064-1:2018-2',
 'Energy indirect emissions', 'Indirect emissions from purchased electricity', '能源间接排放', '外购电力', NULL, 10, '0', '企业端活动数据下拉主数据'),
('SCOPE1-FUEL', '固定燃烧', 'Stationary combustion', 'Scope 1', 'Stationary combustion', 'ISO-14064-1:2018-1',
 'Direct emissions', 'Direct emissions from fuel combustion', '直接排放', '固定燃烧', NULL, 20, '0', '企业端活动数据下拉主数据')
ON DUPLICATE KEY UPDATE
    category_name = VALUES(category_name),
    category_name_en = VALUES(category_name_en),
    ghg_scope = VALUES(ghg_scope),
    ghg_scope_category = VALUES(ghg_scope_category),
    iso_category = VALUES(iso_category),
    iso_category_en = VALUES(iso_category_en),
    iso_category_description = VALUES(iso_category_description),
    gb_scope_category = VALUES(gb_scope_category),
    gb_subcategory = VALUES(gb_subcategory),
    parent_code = VALUES(parent_code),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_base_year
(factory_code, factory_name, base_year, is_current, sort_order, status, remark)
VALUES
('FACTORY-NB-01', '宁波一厂', 2024, 1, 10, '0', '企业端基准年主数据')
ON DUPLICATE KEY UPDATE
    factory_name = VALUES(factory_name),
    is_current = VALUES(is_current),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_electricity_factor_version
(factor_version, effective_year, sort_order, status, remark)
VALUES
('EF-ELEC-2025', 2025, 10, '0', '企业端电力因子版本下拉主数据')
ON DUPLICATE KEY UPDATE
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_electricity_factor
(factor_version, division_code, division_name, region_name, province_factor, region_factor, national_factor,
 non_fossil_excluded_factor, national_fossil_power_factor, sort_order, status, remark)
VALUES
('EF-ELEC-2025', '330000', '浙江省', '华东区域电网', 0.5366000000, 0.5703000000, 0.5703000000,
 0.5942000000, 0.6101000000, 10, '0', '企业端电力因子自动带出样例'),
('EF-ELEC-2025', '330200', '宁波市', '华东区域电网', 0.5366000000, 0.5703000000, 0.5703000000,
 0.5942000000, 0.6101000000, 20, '0', '企业端电力因子自动带出样例')
ON DUPLICATE KEY UPDATE
    division_name = VALUES(division_name),
    region_name = VALUES(region_name),
    province_factor = VALUES(province_factor),
    region_factor = VALUES(region_factor),
    national_factor = VALUES(national_factor),
    non_fossil_excluded_factor = VALUES(non_fossil_excluded_factor),
    national_fossil_power_factor = VALUES(national_fossil_power_factor),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_electricity_factor_scope
(scope_key, scope_name, sort_order, status, remark)
VALUES
('province_factor', '省级电网排放因子', 10, '0', '企业端电力因子口径下拉主数据'),
('region_factor', '区域电网排放因子', 20, '0', '企业端电力因子口径下拉主数据'),
('national_factor', '全国电网排放因子', 30, '0', '企业端电力因子口径下拉主数据')
ON DUPLICATE KEY UPDATE
    scope_name = VALUES(scope_name),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_greenhouse_gas
(gas_code, gas_name, gas_name_en, gwp_value, gwp_version, chemical_formula, sort_order, status, remark)
VALUES
('CO2', '二氧化碳', 'Carbon dioxide', 1.0000000000, 'IPCC AR6', 'CO2', 10, '0', '企业端温室气体下拉主数据'),
('CH4', '甲烷', 'Methane', 27.9000000000, 'IPCC AR6', 'CH4', 20, '0', '企业端温室气体下拉主数据'),
('N2O', '氧化亚氮', 'Nitrous oxide', 273.0000000000, 'IPCC AR6', 'N2O', 30, '0', '企业端温室气体下拉主数据')
ON DUPLICATE KEY UPDATE
    gas_name = VALUES(gas_name),
    gas_name_en = VALUES(gas_name_en),
    gwp_value = VALUES(gwp_value),
    gwp_version = VALUES(gwp_version),
    chemical_formula = VALUES(chemical_formula),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);
