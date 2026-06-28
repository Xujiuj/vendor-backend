-- Read-only constraint/index status for vendor Source(A) master-data tables.

SHOW CREATE TABLE cv_admin_division;
SHOW CREATE TABLE cv_emission_source_category;
SHOW CREATE TABLE cv_base_year;
SHOW CREATE TABLE cv_electricity_factor;
SHOW CREATE TABLE cv_electricity_factor_version;
SHOW CREATE TABLE cv_electricity_factor_scope;
SHOW CREATE TABLE cv_greenhouse_gas;

SELECT table_name,
       constraint_name,
       constraint_type
FROM information_schema.table_constraints
WHERE table_schema = DATABASE()
  AND table_name IN (
      'cv_admin_division',
      'cv_emission_source_category',
      'cv_base_year',
      'cv_electricity_factor',
      'cv_electricity_factor_version',
      'cv_electricity_factor_scope',
      'cv_greenhouse_gas'
  )
ORDER BY table_name, constraint_type, constraint_name;

SELECT table_name,
       index_name,
       non_unique,
       GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns_in_index
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name IN (
      'cv_admin_division',
      'cv_emission_source_category',
      'cv_base_year',
      'cv_electricity_factor',
      'cv_electricity_factor_version',
      'cv_electricity_factor_scope',
      'cv_greenhouse_gas'
  )
GROUP BY table_name, index_name, non_unique
ORDER BY table_name, index_name;

SELECT table_name,
       column_name,
       referenced_table_name,
       referenced_column_name,
       constraint_name
FROM information_schema.key_column_usage
WHERE table_schema = DATABASE()
  AND referenced_table_name IS NOT NULL
  AND table_name IN (
      'cv_admin_division',
      'cv_emission_source_category',
      'cv_base_year',
      'cv_electricity_factor',
      'cv_electricity_factor_version',
      'cv_electricity_factor_scope',
      'cv_greenhouse_gas'
  )
ORDER BY table_name, constraint_name, ordinal_position;
