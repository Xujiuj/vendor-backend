-- Vendor Source(A) master-data constraint redesign.
-- Database boundary: vendor only.
--
-- Purpose:
--   * Keep vendor cloud data limited to vendor-owned master data and templates.
--   * Align strict relationships with Source(A): 101, 103, 106, 202, 203,
--     205, 206 and legacy report-template-download.
--   * Do not move enterprise-local fact data into the vendor database.
--
-- Run carbon_vendor_source_a_dirty_data_diagnostics.sql first. This script
-- blocks on duplicate/orphan rows that would make the new keys unsafe.

DELIMITER //

DROP PROCEDURE IF EXISTS cv_add_column_if_missing//
CREATE PROCEDURE cv_add_column_if_missing(
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128),
    IN p_column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_name, ' ', p_column_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

DROP PROCEDURE IF EXISTS cv_add_index_if_missing//
CREATE PROCEDURE cv_add_index_if_missing(
    IN p_table_name VARCHAR(128),
    IN p_index_name VARCHAR(128),
    IN p_index_sql TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD ', p_index_sql);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

DROP PROCEDURE IF EXISTS cv_drop_column_if_exists//
CREATE PROCEDURE cv_drop_column_if_exists(
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP COLUMN ', p_column_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

DROP PROCEDURE IF EXISTS cv_drop_index_if_exists//
CREATE PROCEDURE cv_drop_index_if_exists(
    IN p_table_name VARCHAR(128),
    IN p_index_name VARCHAR(128)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP INDEX ', p_index_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

DROP PROCEDURE IF EXISTS cv_drop_fk_if_exists//
CREATE PROCEDURE cv_drop_fk_if_exists(
    IN p_table_name VARCHAR(128),
    IN p_constraint_name VARCHAR(128)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND constraint_name = p_constraint_name
          AND constraint_type = 'FOREIGN KEY'
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP FOREIGN KEY ', p_constraint_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

DROP PROCEDURE IF EXISTS cv_add_fk_if_missing//
CREATE PROCEDURE cv_add_fk_if_missing(
    IN p_table_name VARCHAR(128),
    IN p_constraint_name VARCHAR(128),
    IN p_constraint_sql TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND constraint_name = p_constraint_name
          AND constraint_type = 'FOREIGN KEY'
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD CONSTRAINT ', p_constraint_name, ' ', p_constraint_sql);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

DROP PROCEDURE IF EXISTS cv_add_check_if_missing//
CREATE PROCEDURE cv_add_check_if_missing(
    IN p_table_name VARCHAR(128),
    IN p_constraint_name VARCHAR(128),
    IN p_constraint_sql TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND constraint_name = p_constraint_name
          AND constraint_type = 'CHECK'
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD CONSTRAINT ', p_constraint_name, ' ', p_constraint_sql);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

DROP PROCEDURE IF EXISTS cv_assert_zero//
CREATE PROCEDURE cv_assert_zero(
    IN p_check_code VARCHAR(128),
    IN p_problem_count BIGINT
)
BEGIN
    IF p_problem_count > 0 THEN
        SET @message_text = CONCAT('Vendor Source(A) constraint redesign blocked by dirty data: ', p_check_code, '=', p_problem_count);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = @message_text;
    END IF;
END//

DROP FUNCTION IF EXISTS cv_table_exists//
CREATE FUNCTION cv_table_exists(p_table_name VARCHAR(128))
RETURNS TINYINT
READS SQL DATA
BEGIN
    RETURN EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
    );
END//

DROP PROCEDURE IF EXISTS cv_select_factor_record_orphans//
CREATE PROCEDURE cv_select_factor_record_orphans()
BEGIN
    IF cv_table_exists('cv_factor_record') AND cv_table_exists('cv_factor_version') THEN
        SELECT 'factor_record.version.orphan' AS check_code,
               COUNT(*) AS problem_count
        FROM cv_factor_record fr
        LEFT JOIN cv_factor_version fv ON fv.id = fr.version_id
        WHERE fr.version_id IS NOT NULL
          AND fv.id IS NULL;
    ELSE
        SELECT 'factor_record.version.orphan' AS check_code,
               0 AS problem_count;
    END IF;
END//

DROP PROCEDURE IF EXISTS cv_assert_factor_record_orphans//
CREATE PROCEDURE cv_assert_factor_record_orphans()
BEGIN
    IF cv_table_exists('cv_factor_record') AND cv_table_exists('cv_factor_version') THEN
        CALL cv_assert_zero('factor_record.version.orphan', (
            SELECT COUNT(*)
            FROM cv_factor_record fr
            LEFT JOIN cv_factor_version fv ON fv.id = fr.version_id
            WHERE fr.version_id IS NOT NULL
              AND fv.id IS NULL
        ));
    END IF;
END//

DELIMITER ;

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

CALL cv_add_column_if_missing('cv_emission_source_category', 'business_key',
    'VARCHAR(64) DEFAULT NULL AFTER category_code');
CALL cv_add_column_if_missing('cv_emission_source_category', 'effective_date',
    'DATE NOT NULL DEFAULT ''2024-01-01'' AFTER parent_code');
CALL cv_add_column_if_missing('cv_emission_source_category', 'expire_date',
    'DATE NOT NULL DEFAULT ''9999-12-31'' AFTER effective_date');
CALL cv_add_column_if_missing('cv_emission_source_category', 'current_flag',
    'CHAR(1) NOT NULL DEFAULT ''Y'' AFTER expire_date');
CALL cv_add_column_if_missing('cv_emission_source_category', 'version_no',
    'VARCHAR(64) NOT NULL DEFAULT ''1'' AFTER current_flag');
CALL cv_add_column_if_missing('cv_emission_source_category', 'standard_category',
    'VARCHAR(255) DEFAULT NULL AFTER version_no');
CALL cv_add_column_if_missing('cv_emission_source_category', 'category_current_key',
    'VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN current_flag = ''Y'' THEN category_code ELSE NULL END) STORED AFTER standard_category');

CALL cv_add_column_if_missing('cv_base_year', 'base_year_key',
    'VARCHAR(64) DEFAULT NULL AFTER id');
CALL cv_add_column_if_missing('cv_base_year', 'description',
    'VARCHAR(500) DEFAULT NULL AFTER is_current');

UPDATE cv_emission_source_category
   SET business_key = category_code
 WHERE business_key IS NULL
    OR business_key = '';

UPDATE cv_emission_source_category
   SET standard_category = COALESCE(NULLIF(standard_category, ''), NULLIF(gb_subcategory, ''), NULLIF(ghg_scope_category, ''), category_name)
 WHERE standard_category IS NULL
    OR standard_category = '';

UPDATE cv_base_year
   SET base_year_key = COALESCE(NULLIF(base_year_key, ''), CAST(base_year AS CHAR))
 WHERE base_year_key IS NULL
    OR base_year_key = '';

UPDATE cv_admin_division
   SET parent_code = NULL
 WHERE parent_code = '';

UPDATE cv_emission_source_category
   SET parent_code = NULL
 WHERE parent_code = '';

-- Diagnostic result sets. All counts below must be zero before strict keys
-- and foreign keys can be added safely.
SELECT 'admin_division.code.duplicate' AS check_code,
       COUNT(*) AS problem_count
FROM (
    SELECT division_code
    FROM cv_admin_division
    WHERE division_code IS NOT NULL
      AND division_code <> ''
    GROUP BY division_code
    HAVING COUNT(*) > 1
) d;

SELECT 'admin_division.parent.orphan' AS check_code,
       COUNT(*) AS problem_count
FROM cv_admin_division child
LEFT JOIN cv_admin_division parent ON parent.division_code = child.parent_code
WHERE child.parent_code IS NOT NULL
  AND child.parent_code <> ''
  AND parent.id IS NULL;

SELECT 'emission_source_category.current.duplicate' AS check_code,
       COUNT(*) AS problem_count
FROM (
    SELECT category_code
    FROM cv_emission_source_category
    WHERE category_code IS NOT NULL
      AND category_code <> ''
      AND current_flag = 'Y'
    GROUP BY category_code
    HAVING COUNT(*) > 1
) d;

SELECT 'emission_source_category.scd.duplicate' AS check_code,
       COUNT(*) AS problem_count
FROM (
    SELECT category_code, version_no, effective_date
    FROM cv_emission_source_category
    WHERE category_code IS NOT NULL
      AND category_code <> ''
    GROUP BY category_code, version_no, effective_date
    HAVING COUNT(*) > 1
) d;

SELECT 'emission_source_category.parent.orphan' AS check_code,
       COUNT(*) AS problem_count
FROM cv_emission_source_category child
LEFT JOIN cv_emission_source_category parent ON parent.category_current_key = child.parent_code
WHERE child.parent_code IS NOT NULL
  AND child.parent_code <> ''
  AND parent.id IS NULL;

SELECT 'base_year.key.duplicate' AS check_code,
       COUNT(*) AS problem_count
FROM (
    SELECT base_year_key
    FROM cv_base_year
    WHERE base_year_key IS NOT NULL
      AND base_year_key <> ''
    GROUP BY base_year_key
    HAVING COUNT(*) > 1
) d;

SELECT 'base_year.year.duplicate' AS check_code,
       COUNT(*) AS problem_count
FROM (
    SELECT base_year
    FROM cv_base_year
    WHERE base_year IS NOT NULL
    GROUP BY base_year
    HAVING COUNT(*) > 1
) d;

SELECT 'base_year.current.duplicate' AS check_code,
       COUNT(*) AS problem_count
FROM (
    SELECT is_current
    FROM cv_base_year
    WHERE is_current = 1
    GROUP BY is_current
    HAVING COUNT(*) > 1
) d;

SELECT 'electricity_factor.version_division.duplicate' AS check_code,
       COUNT(*) AS problem_count
FROM (
    SELECT factor_version, division_code
    FROM cv_electricity_factor
    WHERE factor_version IS NOT NULL
      AND factor_version <> ''
      AND division_code IS NOT NULL
      AND division_code <> ''
    GROUP BY factor_version, division_code
    HAVING COUNT(*) > 1
) d;

SELECT 'electricity_factor.division.orphan' AS check_code,
       COUNT(*) AS problem_count
FROM cv_electricity_factor ef
LEFT JOIN cv_admin_division ad ON ad.division_code = ef.division_code
WHERE ef.division_code IS NOT NULL
  AND ef.division_code <> ''
  AND ad.id IS NULL;

SELECT 'electricity_factor.version.orphan' AS check_code,
       COUNT(*) AS problem_count
FROM cv_electricity_factor ef
LEFT JOIN cv_electricity_factor_version ev ON ev.factor_version = ef.factor_version
WHERE ef.factor_version IS NOT NULL
  AND ef.factor_version <> ''
  AND ev.id IS NULL;

SELECT 'electricity_factor_version.effective_year.duplicate' AS check_code,
       COUNT(*) AS problem_count
FROM (
    SELECT effective_year
    FROM cv_electricity_factor_version
    WHERE effective_year IS NOT NULL
    GROUP BY effective_year
    HAVING COUNT(*) > 1
) d;

CALL cv_select_factor_record_orphans();

CALL cv_assert_zero('admin_division.code.duplicate', (
    SELECT COUNT(*)
    FROM (
        SELECT division_code
        FROM cv_admin_division
        WHERE division_code IS NOT NULL
          AND division_code <> ''
        GROUP BY division_code
        HAVING COUNT(*) > 1
    ) d
));
CALL cv_assert_zero('admin_division.parent.orphan', (
    SELECT COUNT(*)
    FROM cv_admin_division child
    LEFT JOIN cv_admin_division parent ON parent.division_code = child.parent_code
    WHERE child.parent_code IS NOT NULL
      AND child.parent_code <> ''
      AND parent.id IS NULL
));
CALL cv_assert_zero('emission_source_category.current.duplicate', (
    SELECT COUNT(*)
    FROM (
        SELECT category_code
        FROM cv_emission_source_category
        WHERE category_code IS NOT NULL
          AND category_code <> ''
          AND current_flag = 'Y'
        GROUP BY category_code
        HAVING COUNT(*) > 1
    ) d
));
CALL cv_assert_zero('emission_source_category.scd.duplicate', (
    SELECT COUNT(*)
    FROM (
        SELECT category_code, version_no, effective_date
        FROM cv_emission_source_category
        WHERE category_code IS NOT NULL
          AND category_code <> ''
        GROUP BY category_code, version_no, effective_date
        HAVING COUNT(*) > 1
    ) d
));
CALL cv_assert_zero('emission_source_category.parent.orphan', (
    SELECT COUNT(*)
    FROM cv_emission_source_category child
    LEFT JOIN cv_emission_source_category parent ON parent.category_current_key = child.parent_code
    WHERE child.parent_code IS NOT NULL
      AND child.parent_code <> ''
      AND parent.id IS NULL
));
CALL cv_assert_zero('base_year.key.duplicate', (
    SELECT COUNT(*)
    FROM (
        SELECT base_year_key
        FROM cv_base_year
        WHERE base_year_key IS NOT NULL
          AND base_year_key <> ''
        GROUP BY base_year_key
        HAVING COUNT(*) > 1
    ) d
));
CALL cv_assert_zero('base_year.year.duplicate', (
    SELECT COUNT(*)
    FROM (
        SELECT base_year
        FROM cv_base_year
        WHERE base_year IS NOT NULL
        GROUP BY base_year
        HAVING COUNT(*) > 1
    ) d
));
CALL cv_assert_zero('base_year.current.duplicate', (
    SELECT COUNT(*)
    FROM (
        SELECT is_current
        FROM cv_base_year
        WHERE is_current = 1
        GROUP BY is_current
        HAVING COUNT(*) > 1
    ) d
));
CALL cv_assert_zero('electricity_factor.version_division.duplicate', (
    SELECT COUNT(*)
    FROM (
        SELECT factor_version, division_code
        FROM cv_electricity_factor
        WHERE factor_version IS NOT NULL
          AND factor_version <> ''
          AND division_code IS NOT NULL
          AND division_code <> ''
        GROUP BY factor_version, division_code
        HAVING COUNT(*) > 1
    ) d
));
CALL cv_assert_zero('electricity_factor.division.orphan', (
    SELECT COUNT(*)
    FROM cv_electricity_factor ef
    LEFT JOIN cv_admin_division ad ON ad.division_code = ef.division_code
    WHERE ef.division_code IS NOT NULL
      AND ef.division_code <> ''
      AND ad.id IS NULL
));
CALL cv_assert_zero('electricity_factor.version.orphan', (
    SELECT COUNT(*)
    FROM cv_electricity_factor ef
    LEFT JOIN cv_electricity_factor_version ev ON ev.factor_version = ef.factor_version
    WHERE ef.factor_version IS NOT NULL
      AND ef.factor_version <> ''
      AND ev.id IS NULL
));
CALL cv_assert_zero('electricity_factor_version.effective_year.duplicate', (
    SELECT COUNT(*)
    FROM (
        SELECT effective_year
        FROM cv_electricity_factor_version
        WHERE effective_year IS NOT NULL
        GROUP BY effective_year
        HAVING COUNT(*) > 1
    ) d
));
CALL cv_assert_factor_record_orphans();

CALL cv_drop_fk_if_exists('cv_electricity_factor', 'fk_cv_electricity_factor_version');
CALL cv_drop_index_if_exists('cv_emission_source_category', 'uk_cv_emission_source_category_code');
CALL cv_drop_index_if_exists('cv_base_year', 'uk_cv_base_year');
CALL cv_drop_index_if_exists('cv_base_year', 'uk_cv_base_year_current');
CALL cv_drop_column_if_exists('cv_base_year', 'current_base_year_key');
CALL cv_drop_index_if_exists('cv_electricity_factor_version', 'uk_cv_electricity_factor_version_code');
CALL cv_drop_column_if_exists('cv_base_year', 'current_factory_key');
CALL cv_drop_column_if_exists('cv_base_year', 'factory_code');
CALL cv_drop_column_if_exists('cv_base_year', 'factory_name');
CALL cv_add_index_if_missing('cv_admin_division', 'idx_cv_admin_division_parent',
    'KEY idx_cv_admin_division_parent (parent_code)');
CALL cv_add_index_if_missing('cv_emission_source_category', 'idx_cv_emission_source_category_parent',
    'KEY idx_cv_emission_source_category_parent (parent_code)');
CALL cv_add_index_if_missing('cv_emission_source_category', 'uk_cv_emission_source_category_scd',
    'UNIQUE KEY uk_cv_emission_source_category_scd (category_code, version_no, effective_date)');
CALL cv_add_index_if_missing('cv_emission_source_category', 'uk_cv_emission_source_category_current',
    'UNIQUE KEY uk_cv_emission_source_category_current (category_current_key)');
CALL cv_add_index_if_missing('cv_base_year', 'uk_cv_base_year_key',
    'UNIQUE KEY uk_cv_base_year_key (base_year_key)');
CALL cv_add_index_if_missing('cv_base_year', 'uk_cv_base_year_year',
    'UNIQUE KEY uk_cv_base_year_year (base_year)');
CALL cv_add_index_if_missing('cv_base_year', 'uk_cv_base_year_current',
    'UNIQUE KEY uk_cv_base_year_current ((CASE WHEN is_current = 1 THEN 1 ELSE NULL END))');
CALL cv_add_index_if_missing('cv_base_year', 'idx_cv_base_year_current',
    'KEY idx_cv_base_year_current (is_current, base_year)');
CALL cv_add_index_if_missing('cv_electricity_factor_version', 'uk_cv_electricity_factor_effective_year',
    'UNIQUE KEY uk_cv_electricity_factor_effective_year (effective_year)');
CALL cv_add_index_if_missing('cv_electricity_factor_version', 'idx_cv_electricity_factor_version_code',
    'KEY idx_cv_electricity_factor_version_code (factor_version)');
CALL cv_add_index_if_missing('cv_electricity_factor', 'idx_cv_electricity_factor_division',
    'KEY idx_cv_electricity_factor_division (division_code)');
CALL cv_add_index_if_missing('cv_electricity_factor', 'idx_cv_electricity_factor_version',
    'KEY idx_cv_electricity_factor_version (factor_version)');

CALL cv_add_fk_if_missing('cv_admin_division', 'fk_cv_admin_division_parent',
    'FOREIGN KEY (parent_code) REFERENCES cv_admin_division (division_code)');
CALL cv_add_fk_if_missing('cv_emission_source_category', 'fk_cv_emission_source_category_parent',
    'FOREIGN KEY (parent_code) REFERENCES cv_emission_source_category (category_current_key)');
CALL cv_add_fk_if_missing('cv_electricity_factor', 'fk_cv_electricity_factor_division',
    'FOREIGN KEY (division_code) REFERENCES cv_admin_division (division_code)');
CALL cv_add_check_if_missing('cv_emission_source_category', 'chk_cv_emission_source_category_current',
    'CHECK (current_flag IN (''Y'', ''N''))');
CALL cv_add_check_if_missing('cv_emission_source_category', 'chk_cv_emission_source_category_dates',
    'CHECK (effective_date <= expire_date)');
CALL cv_add_check_if_missing('cv_base_year', 'chk_cv_base_year_current',
    'CHECK (is_current IN (0, 1))');
CALL cv_add_check_if_missing('cv_base_year', 'chk_cv_base_year_range',
    'CHECK (base_year BETWEEN 1900 AND 9999)');
CALL cv_add_check_if_missing('cv_electricity_factor', 'chk_cv_electricity_factor_non_negative',
    'CHECK ((province_factor IS NULL OR province_factor >= 0) AND (region_factor IS NULL OR region_factor >= 0) AND (national_factor IS NULL OR national_factor >= 0) AND (non_fossil_excluded_factor IS NULL OR non_fossil_excluded_factor >= 0) AND (national_fossil_power_factor IS NULL OR national_fossil_power_factor >= 0))');
CALL cv_add_check_if_missing('cv_greenhouse_gas', 'chk_cv_greenhouse_gas_gwp',
    'CHECK (gwp_value IS NULL OR gwp_value >= 0)');

DROP PROCEDURE IF EXISTS cv_add_column_if_missing;
DROP PROCEDURE IF EXISTS cv_drop_column_if_exists;
DROP PROCEDURE IF EXISTS cv_add_index_if_missing;
DROP PROCEDURE IF EXISTS cv_drop_index_if_exists;
DROP PROCEDURE IF EXISTS cv_drop_fk_if_exists;
DROP PROCEDURE IF EXISTS cv_add_fk_if_missing;
DROP PROCEDURE IF EXISTS cv_add_check_if_missing;
DROP PROCEDURE IF EXISTS cv_assert_zero;
DROP PROCEDURE IF EXISTS cv_select_factor_record_orphans;
DROP PROCEDURE IF EXISTS cv_assert_factor_record_orphans;
DROP FUNCTION IF EXISTS cv_table_exists;
