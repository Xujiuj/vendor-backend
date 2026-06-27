-- Read-only diagnostics for vendor Source(A) master data dirty data.
-- Database boundary: vendor only. Run this before applying
-- carbon_vendor_source_a_constraint_redesign.sql.

DELIMITER //

DROP FUNCTION IF EXISTS cv_diag_table_exists//
CREATE FUNCTION cv_diag_table_exists(p_table_name VARCHAR(128))
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

DROP PROCEDURE IF EXISTS cv_diag_optional_dimension_record//
CREATE PROCEDURE cv_diag_optional_dimension_record()
BEGIN
    IF cv_diag_table_exists('cv_dimension_record') THEN
        SELECT 'dimension_record.dimension_code.duplicate.summary' AS check_code,
               COUNT(*) AS duplicate_groups,
               COALESCE(SUM(duplicate_count), 0) AS rows_in_duplicate_groups,
               COALESCE(SUM(duplicate_count - 1), 0) AS extra_rows
        FROM (
            SELECT dimension_code, record_code, COUNT(*) AS duplicate_count
            FROM cv_dimension_record
            WHERE dimension_code IS NOT NULL
              AND dimension_code <> ''
              AND record_code IS NOT NULL
              AND record_code <> ''
            GROUP BY dimension_code, record_code
            HAVING COUNT(*) > 1
        ) d;
    ELSE
        SELECT 'dimension_record.dimension_code.duplicate.summary' AS check_code,
               0 AS duplicate_groups,
               0 AS rows_in_duplicate_groups,
               0 AS extra_rows;
    END IF;
END//

DELIMITER ;

SELECT table_name,
       table_rows
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'cv_admin_division',
      'cv_emission_source_category',
      'cv_base_year',
      'cv_electricity_factor',
      'cv_electricity_factor_version',
      'cv_electricity_factor_scope',
      'cv_greenhouse_gas',
      'cv_factor_version',
      'cv_dimension_record'
  )
ORDER BY table_name;

SELECT 'admin_division.code.duplicate.summary' AS check_code,
       COUNT(*) AS duplicate_groups,
       COALESCE(SUM(duplicate_count), 0) AS rows_in_duplicate_groups,
       COALESCE(SUM(duplicate_count - 1), 0) AS extra_rows
FROM (
    SELECT division_code, COUNT(*) AS duplicate_count
    FROM cv_admin_division
    WHERE division_code IS NOT NULL
      AND division_code <> ''
    GROUP BY division_code
    HAVING COUNT(*) > 1
) d;

SELECT 'emission_source_category.code.duplicate.summary' AS check_code,
       COUNT(*) AS duplicate_groups,
       COALESCE(SUM(duplicate_count), 0) AS rows_in_duplicate_groups,
       COALESCE(SUM(duplicate_count - 1), 0) AS extra_rows
FROM (
    SELECT category_code, COUNT(*) AS duplicate_count
    FROM cv_emission_source_category
    WHERE category_code IS NOT NULL
      AND category_code <> ''
    GROUP BY category_code
    HAVING COUNT(*) > 1
) d;

SELECT 'base_year.current.duplicate.summary' AS check_code,
       COUNT(*) AS duplicate_groups,
       COALESCE(SUM(duplicate_count), 0) AS rows_in_duplicate_groups,
       COALESCE(SUM(duplicate_count - 1), 0) AS extra_rows
FROM (
    SELECT is_current, COUNT(*) AS duplicate_count
    FROM cv_base_year
    WHERE is_current = 1
    GROUP BY is_current
    HAVING COUNT(*) > 1
) d;

SELECT 'electricity_factor.version_division.duplicate.summary' AS check_code,
       COUNT(*) AS duplicate_groups,
       COALESCE(SUM(duplicate_count), 0) AS rows_in_duplicate_groups,
       COALESCE(SUM(duplicate_count - 1), 0) AS extra_rows
FROM (
    SELECT factor_version, division_code, COUNT(*) AS duplicate_count
    FROM cv_electricity_factor
    WHERE factor_version IS NOT NULL
      AND factor_version <> ''
      AND division_code IS NOT NULL
      AND division_code <> ''
    GROUP BY factor_version, division_code
    HAVING COUNT(*) > 1
) d;

SELECT 'electricity_factor_version.effective_year.duplicate.summary' AS check_code,
       COUNT(*) AS duplicate_groups,
       COALESCE(SUM(duplicate_count), 0) AS rows_in_duplicate_groups,
       COALESCE(SUM(duplicate_count - 1), 0) AS extra_rows
FROM (
    SELECT effective_year, COUNT(*) AS duplicate_count
    FROM cv_electricity_factor_version
    WHERE effective_year IS NOT NULL
    GROUP BY effective_year
    HAVING COUNT(*) > 1
) d;

CALL cv_diag_optional_dimension_record();

SELECT 'admin_division.parent.orphan' AS check_code,
       child.parent_code,
       COUNT(*) AS orphan_count
FROM cv_admin_division child
LEFT JOIN cv_admin_division parent ON parent.division_code = child.parent_code
WHERE child.parent_code IS NOT NULL
  AND child.parent_code <> ''
  AND parent.id IS NULL
GROUP BY child.parent_code
ORDER BY orphan_count DESC, child.parent_code
LIMIT 100;

SELECT 'emission_source_category.parent.orphan' AS check_code,
       child.parent_code,
       COUNT(*) AS orphan_count
FROM cv_emission_source_category child
LEFT JOIN cv_emission_source_category parent ON parent.category_current_key = child.parent_code
WHERE child.parent_code IS NOT NULL
  AND child.parent_code <> ''
  AND parent.id IS NULL
GROUP BY child.parent_code
ORDER BY orphan_count DESC, child.parent_code
LIMIT 100;

SELECT 'electricity_factor.division.orphan' AS check_code,
       ef.division_code,
       COUNT(*) AS orphan_count
FROM cv_electricity_factor ef
LEFT JOIN cv_admin_division ad ON ad.division_code = ef.division_code
WHERE ef.division_code IS NOT NULL
  AND ef.division_code <> ''
  AND ad.id IS NULL
GROUP BY ef.division_code
ORDER BY orphan_count DESC, ef.division_code
LIMIT 100;

SELECT 'electricity_factor.version.orphan' AS check_code,
       ef.factor_version,
       COUNT(*) AS orphan_count
FROM cv_electricity_factor ef
LEFT JOIN cv_electricity_factor_version ev ON ev.factor_version = ef.factor_version
WHERE ef.factor_version IS NOT NULL
  AND ef.factor_version <> ''
  AND ev.id IS NULL
GROUP BY ef.factor_version
ORDER BY orphan_count DESC, ef.factor_version
LIMIT 100;

SELECT 'admin_division.code.duplicate' AS check_code,
       division_code,
       COUNT(*) AS duplicate_count
FROM cv_admin_division
WHERE division_code IS NOT NULL
  AND division_code <> ''
GROUP BY division_code
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC, division_code
LIMIT 100;

SELECT 'emission_source_category.code.duplicate' AS check_code,
       category_code,
       COUNT(*) AS duplicate_count
FROM cv_emission_source_category
WHERE category_code IS NOT NULL
  AND category_code <> ''
GROUP BY category_code
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC, category_code
LIMIT 100;

SELECT 'base_year.current.duplicate' AS check_code,
       COUNT(*) AS duplicate_count
FROM cv_base_year
WHERE is_current = 1
GROUP BY is_current
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC
LIMIT 100;

DROP PROCEDURE IF EXISTS cv_diag_optional_dimension_record;
DROP FUNCTION IF EXISTS cv_diag_table_exists;
