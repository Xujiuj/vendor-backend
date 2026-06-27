-- Ensure 203 electricity factor version numbers are unique.
-- Keeps the earliest row for each factor_version, preserves factor references,
-- then enforces a physical unique key on factor_version.

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_cv_electricity_factor_version_keep;
CREATE TEMPORARY TABLE tmp_cv_electricity_factor_version_keep AS
SELECT factor_version, MIN(id) AS keep_id, MIN(effective_year) AS effective_year
FROM cv_electricity_factor_version
GROUP BY factor_version;

DELETE v
FROM cv_electricity_factor_version v
JOIN tmp_cv_electricity_factor_version_keep k
  ON k.factor_version = v.factor_version
WHERE v.id <> k.keep_id;

UPDATE cv_electricity_factor_version v
JOIN tmp_cv_electricity_factor_version_keep k
  ON k.factor_version = v.factor_version
 AND k.keep_id = v.id
SET v.effective_year = k.effective_year;

COMMIT;

SET @drop_old_unique := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE cv_electricity_factor_version DROP INDEX uk_cv_electricity_factor_version',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'cv_electricity_factor_version'
      AND index_name = 'uk_cv_electricity_factor_version'
      AND seq_in_index = 2
);
PREPARE stmt FROM @drop_old_unique;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_code_index := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE cv_electricity_factor_version DROP INDEX idx_cv_electricity_factor_version_code',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'cv_electricity_factor_version'
      AND index_name = 'idx_cv_electricity_factor_version_code'
);
PREPARE stmt FROM @drop_code_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_unique := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE cv_electricity_factor_version ADD UNIQUE KEY uk_cv_electricity_factor_version (factor_version)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'cv_electricity_factor_version'
      AND index_name = 'uk_cv_electricity_factor_version'
);
PREPARE stmt FROM @add_unique;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
